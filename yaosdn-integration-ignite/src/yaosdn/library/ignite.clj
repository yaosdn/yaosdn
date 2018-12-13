(ns yaosdn.library.ignite
  (:require [yaosdn.library.proto :as proto])
  (:import (org.apache.ignite Ignition)
           (org.apache.ignite.lang IgniteBiPredicate)))


(def ^:dynamic *ignite-default-topic-name* "yaosdn-topic")


(defn- get-ignite-connection [packet-processor params]
  (let [{ignite-params :ignite-params
         topic-name :topic-name
         messaging-configuration :messaging-configuration} params
        connection (if ignite-params
                     (Ignition/start ignite-params)
                     (Ignition/start))]
    (assoc packet-processor
           :connection connection
           :messaging (let [messaging-connection (if messaging-configuration
                                                   (.message connection messaging-configuration)
                                                   (.message connection))
                            queue-atom (atom [])]
                        {:queue queue-atom
                         :connection (doto messaging-connection
                                       (.localListen (or topic-name
                                                         *ignite-default-topic-name*)
                                                     (reify IgniteBiPredicate
                                                       (apply [this node-id msg]
                                                         (swap! queue-atom conj msg)
                                                         true))))}))))


(defn- read-messages [messaging filter-function]
  (filter filter-function
          (first (swap-vals! (-> messaging :queue)
                             (fn [_] [])))))


(defn- write-messages [messaging router-function packets]
  (count (for [packet packets]
           (.send (:connection messaging)
                  (router-function packet)
                  packet))))


(defrecord IgnitePacketProcessor [connection messaging]
  proto/PacketProcessor
  (connect [packet-processor] (.connect packet-processor nil))
  (connect [packet-processor params] (get-ignite-connection packet-processor
                                                            params))
  (close [packet-processor] (assoc packet-processor
                                   :connection (.close connection)
                                   :messaging nil))
  (get-packets [packet-processor] (.get-packets packet-processor identity))
  (get-packets [packet-processor filter-function] (read-messages messaging filter-function))
  (send-packets [packet-processor router-function packets] (write-messages messaging
                                                                           router-function
                                                                           packets)))


(defn new-ignite-packet-processor [& [params]]
  (-> (->IgnitePacketProcessor nil nil)
      (.connect params)))
