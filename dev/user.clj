(ns user
  (:require [aleph-stress.system :as system]
            [integrant.repl :as ig-repl]))

(ig-repl/set-prep! (fn [] system/config))

(def go ig-repl/go)
(def halt ig-repl/halt)
(def reset ig-repl/reset)
(def reset-all ig-repl/reset-all)

(comment
  (go)
  (halt)
  (reset)
  (reset-all)
  ;;
  )
