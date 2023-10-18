(ns aleph-stress.system
  (:require [aleph-stress.router :as router]
            [aleph.http :as http]
            [clojure.java.io :as io]
            [hikari-cp.core :as cp]
            [integrant.core :as ig]
            [reitit.ring :as ring]))

(def config (ig/read-string (slurp (io/resource "config.edn"))))

(defmethod ig/init-key :aleph-stress/datasource
  [_ datasource-options]
  (cp/make-datasource datasource-options))

(defmethod ig/halt-key! :aleph-stress/datasource
  [_ datasource]
  (cp/close-datasource datasource))

(defmethod ig/init-key :aleph-stress/app
  [_ ctx]
  (let [handler-fn (fn [] (ring/ring-handler
                           (router/router ctx)
                           (constantly {:status 404, :body "Not found"})))]

    (ring/reloading-ring-handler handler-fn)))

(defmethod ig/init-key :aleph-stress/server
  [_ {port :port app :app}]
  (http/start-server app {:port port :join? false}))

(defmethod ig/halt-key! :aleph-stress/server
  [_ server]
  (.close server))

(defn start
  ([] (start nil))
  ([mode]
   (ig/init (cond-> config
              mode
              (assoc-in  [:aleph-stress/app :mode] mode)))))

(defn stop! [sys]
  (ig/halt! sys))
