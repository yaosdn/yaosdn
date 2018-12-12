(ns yaosdn.library.pcap
  (:import (java.util ArrayList)
           (org.pcap4j.core Pcaps
                            PcapNetworkInterface)
           (org.pcap4j.packet AbstractPacket
                              IpV4Packet)))


(defn ^PcapNetworkInterface get-interface-by-name [^String if-name]
  (Pcaps/getDevByName if-name))


(defn ^ArrayList get-addresses [^PcapNetworkInterface interface]
  (map #(.getAddress %)
       (.getAddresses interface)))


(def ^:dynamic *new-packet-factory* (fn [arr offset length]
                                      (IpV4Packet/newPacket arr
                                                            offset
                                                            length)))


(defn ^AbstractPacket packet-of
  ([^AbstractPacket packet] (let [raw-data (.getRawData packet)]
                              (apply packet-of
                                     ((juxt identity count) raw-data))))
  ([^bytes arr ^Integer length] (*new-packet-factory* arr 0 length)))
