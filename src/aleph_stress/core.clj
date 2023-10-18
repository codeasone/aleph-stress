(ns aleph-stress.core
  (:require [aleph-stress.system :as system])
  (:gen-class))

(def ^:dynamic *system* nil)

(defn init! [mode]
  (let [sys (system/start mode)]
    (alter-var-root #'*system* (fn [_] sys))))

(defn halt! []
  (when *system*
    (system/stop! *system*)))

(defn -main
  [& args]
  (let [mode (or (first args) "api")]
    (prn (format "Starting in [%s] mode" mode))
    (when-not (= "driver" mode)
      (.addShutdownHook (Runtime/getRuntime) (Thread. halt!))
      (init! mode))))
