(ns aleph-stress.api
  (:require [aleph.http :as http]
            [clojure.core.async :as async]
            [hugsql.core :as hugsql]
            [manifold.deferred :as d]
            [manifold.stream :as s]
            [next.jdbc :as jdbc]))

(hugsql/def-sqlvec-fns "sql/router.sql")

(defn post-to-heavylifter! [id]
  (prn "Posting to heavylifter")
  (d/chain
   (http/post "http://heavylifter:9000/process"
              {:body (str id)
               :headers {"Content-Type" "text/plain; charset=utf-8"}})
   :body
   s/consume
   str))

(defn submission-handler
  [{:keys [database path-params]}]
  (let [{:keys [id]} path-params]
    (prn "Received submission: " id)
    (jdbc/execute-one! database (insert-submission-sqlvec
                                 {:id (read-string id)}) {:return-keys true})
    (post-to-heavylifter! id)
    {:status 200}))

(defn complete-handler
  [{:keys [database body]}]
  (let [id (read-string (slurp body))]
    (jdbc/execute-one! database (complete-submission-sqlvec
                                 {:id id}))
    (prn "Completed submission!")
    {:status 200}))

(defn websocket-handler
  [{:keys [database path-params] :as request}]
  (d/chain
   (http/websocket-connection request)
   (fn [socket]
     (let [{:keys [id]} path-params]
       #_(s/consume identity socket)
       (async/go-loop []
         (if (s/closed? socket)
           (prn (format "Client [%d] disconnected" id))
           (let [_ (async/<! (async/timeout 1000))
                 {:keys [:submission/completed]}
                 (try
                   (jdbc/execute-one! database (submission-complete?-sqlvec
                                                {:id (read-string id)}))
                   (catch Exception _
                     (prn "Error polling database")))]
             (if completed
               (s/put! socket (str {:completed id}))
               (do
                 (prn "Waiting...")
                 (recur))))))))))

(defn routes
  []
  [["/submission/:id" {:get submission-handler :post submission-handler}]
   ["/webhook"
    ["/complete" {:post complete-handler}]]
   ["/websocket/:id" websocket-handler]])
