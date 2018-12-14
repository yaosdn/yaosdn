(ns yaosdn.helpers
  (:require [yaosdn.library.pcap :as pcap]))


(defn change-packet-address [address-type change-function interface-name packet]
  (let [packet-header (.getHeader packet)
        address (.getHostAddress (case address-type
                                   :dst (.getDstAddr packet-header)
                                   :src (.getSrcAddr packet-header)))
        [o1 o2 o3 o4] (map #(Integer/parseInt %) (clojure.string/split address #"\."))
        changed-address (->> (change-function interface-name
                                              {:o1 o1 :o2 o2 :o3 o3 :o4 o4})
                             vals
                             (clojure.string/join ".")
                             (java.net.InetAddress/getByName))
        packet-builder (.getBuilder packet)
        pre-builder (case address-type
                      :dst (.dstAddr packet-builder changed-address)
                      :src (.srcAddr packet-builder changed-address))]
    (.build pre-builder)))


(defn build-local-tx-map-function [& [src-function dst-function]]
  (fn [interface-name packet]
    (let [new-packet ((comp (partial change-packet-address
                                     :dst
                                     (or dst-function
                                         (fn [_ octets] octets))
                                     interface-name)
                            (partial change-packet-address
                                     :src
                                     (or src-function
                                         (fn [_ octets] octets))
                                     interface-name)) packet)]
      (if (= (-> packet .getHeader .getProtocol .value) 1)
        (let [icmp4-common-header (-> (pcap/with-packet-factory pcap/icmpv4-packet-factory
                                        (pcap/packet-of (.getPayload new-packet)))
                                      .getBuilder
                                      (.correctChecksumAtBuild true))]
          (-> (.getBuilder new-packet)
              (.payloadBuilder icmp4-common-header)
              (.correctChecksumAtBuild true)
              (.correctLengthAtBuild true)
              .build))
        new-packet))))
