(ns aleph-stress.middleware)

(defn wrap-context
  [handler ctx]
  (fn [request]
    (handler (merge request ctx))))
