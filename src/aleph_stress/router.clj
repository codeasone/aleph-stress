(ns aleph-stress.router
  (:require [aleph-stress.api :as api]
            [aleph-stress.heavylifter :as heavylifter]
            [aleph-stress.middleware :as middleware]
            [reitit.ring :as r]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [ring.middleware.params :refer [wrap-params]]))

(defn router
  [{:keys [mode] :as ctx}]
  (r/router
   (case mode
     "heavylifter" (heavylifter/routes)
     "api" (api/routes)
     ;; Otherise everything
     (concat (api/routes)
             (heavylifter/routes)))

   {:data {:middleware [[wrap-defaults api-defaults]
                        wrap-params
                        [middleware/wrap-context ctx]]}}))
