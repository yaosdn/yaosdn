(ns user
  (:require [yaosdn.library.queue :as queue]
            [yaosdn.library.pcap :as pcap]
            [yaosdn.library.tun :as tun]
            [yaosdn.server]))


(def q (yaosdn.library.queue/ignite-queue yaosdn.server/ignite))


(defn is-local []
  (-> yaosdn.server/ignite
      .cluster
      .forOldest
      .node
      .isLocal))


(defn filter-function [data]
  (is-local))


(def locking-object (Object.))


(defn read-and-write []
  (locking locking-object
    (when (> (.size q) 0)
      (when-let [data (.peek q)]
        (when (filter-function data)
          (println "!!!" (.poll q)))))))
