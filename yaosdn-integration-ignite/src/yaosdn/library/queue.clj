(ns yaosdn.library.queue
  (:import [org.apache.ignite IgniteQueue]
           [org.apache.ignite.configuration CollectionConfiguration]))


(def ^:dynamic *queue-name* "yaosdn-networking-queue")


(defn ignite-queue [ignite]
  (.queue ignite *queue-name* 0 (CollectionConfiguration.)))
