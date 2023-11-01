(ns aleph-stress.metrics
  (:require [iapetos.collector :as collector]
            [iapetos.collector.jvm :as jvm]
            [iapetos.collector.ring :as ring]
            [iapetos.core :as prometheus]
            [iapetos.export :as export])
  (:import io.prometheus.client.hotspot.BufferPoolsExports))

(def jvm-buffer-pools-collector
  (collector/named {:namespace "iapetos_internal" :name "jvm_buffer_pools"} (BufferPoolsExports.)))

(defonce registry
  (-> (prometheus/collector-registry)
      (jvm/initialize)
      (ring/initialize)
      (prometheus/register jvm-buffer-pools-collector)))

(comment
  (print (export/text-format registry))
  ;;
  )
