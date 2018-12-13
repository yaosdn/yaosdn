(ns user
  (:require [yaosdn.library.ignite :as ignite]
            [yaosdn.library.pcap :as pcap]
            [yaosdn.library.tun :as tun]))


(defn is-local [ignite-packet-processor]
  (-> (:connection ignite-packet-processor)
      .cluster
      .forOldest
      .node
      .isLocal))


(defn filter-function [data]
  (is-local))
