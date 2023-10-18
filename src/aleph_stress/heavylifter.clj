(ns aleph-stress.heavylifter
  (:require [aleph.http :as http]
            [manifold.time :as t]))

(defn invoke-webhook!
  [{:keys [body]}]
  (let [id (slurp body)]
    (prn "Invoking webhook")
    (http/post "http://api-lb:9000/webhook/complete"
               {:body id
                :headers {"Content-Type" "application/json"}})))

(defn process-handler
  [request]
  ;; Simulating work without consuming system resources
  (t/in 1000 #(invoke-webhook! request))
  {:status 200})

(defn routes
  []
  [["/process" {:post process-handler}]])
