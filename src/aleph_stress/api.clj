(ns aleph-stress.api
  (:require [aleph.http :as http]
            [clojure.core.async :as async]
            [hugsql.core :as hugsql]
            [manifold.deferred :as d]
            [manifold.stream :as s]
            [next.jdbc :as jdbc]))

(hugsql/def-sqlvec-fns "sql/aleph_stress/api.sql")

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
                                 {:id (read-string id)}))
    (post-to-heavylifter! id)
    {:status 200}))

(defn complete-handler
  [{:keys [database body]}]
  (let [id (read-string (slurp body))]
    (jdbc/execute-one! database (complete-submission-sqlvec
                                 {:id id}))
    (prn "Completed submission!")
    {:status 200}))

(def clients (atom {}))

(defn websocket-handler [{:keys [database path-params] :as request}]
  (let [ws @(http/websocket-connection request)
        {:keys [id]} path-params]
    (prn (format "Client [%s] connected" id))
    (s/on-closed ws (fn []
                      (swap! clients dissoc ws)
                      (prn (format "Client [%s] disconnected" id))))
    (async/go-loop []
      (if (s/closed? ws)
        (prn (format "Client [%s] disconnected" id))
        (let [_ (async/<! (async/timeout 1000))
              {:keys [:submission/completed]}
              (try
                (jdbc/execute-one! database
                                   (submission-complete?-sqlvec {:id (read-string id)}))
                (catch Exception e
                  (prn (format "Error polling submission status for client [%s]: %s"
                               id (.getMessage e)))))]
          (if completed
            (do
              (s/put! ws (str {:completed id}))
              (prn (format "Submission completed for client [%s]" id)))
            (do
              (prn (format "Polling submission status for client [%s]..." id))
              (recur))))))
    (swap! clients assoc ws id)))

(defn routes
  []
  [["/submission/:id" {:get submission-handler :post submission-handler}]
   ["/webhook"
    ["/complete" {:post complete-handler}]]
   ["/websocket/:id" websocket-handler]])
