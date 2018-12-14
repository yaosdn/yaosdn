(ns yaosdn.library.pcap
  (:import (java.util ArrayList)
           (org.pcap4j.core Pcaps
                            PcapNetworkInterface)
           (org.pcap4j.packet AbstractPacket
                              IpV4Packet
                              IpV6Packet
                              TcpPacket
                              UdpPacket
                              IcmpV4CommonPacket
                              IllegalRawDataException)))


(defn ^PcapNetworkInterface get-interface-by-name [^String if-name]
  (Pcaps/getDevByName if-name))


(defn ^ArrayList get-addresses [^PcapNetworkInterface interface]
  (map #(.getAddress %)
       (.getAddresses interface)))


(defn ipv4-packet-factory [arr offset length]
  (IpV4Packet/newPacket arr offset length))


(defn ipv6-packet-factory [arr offset length]
  (IpV6Packet/newPacket arr offset length))


(defn tcp-packet-factory [arr offset length]
  (TcpPacket/newPacket arr offset length))


(defn udp-packet-factory [arr offset length]
  (UdpPacket/newPacket arr offset length))


(defn icmpv4-packet-factory [arr offset length]
  (IcmpV4CommonPacket/newPacket arr offset length))


(def ^:dynamic *new-packet-factory* ipv4-packet-factory)


(defmacro with-packet-factory [packet-factory & body]
  `(binding [*new-packet-factory* ~packet-factory]
     ~@body))


(defn ^AbstractPacket packet-of
  ([^AbstractPacket packet] (let [raw-data (.getRawData packet)]
                              (apply packet-of
                                     ((juxt identity count) raw-data))))
  ([^bytes arr ^Integer length] (try
                                  (*new-packet-factory* arr 0 length)
                                  (catch IllegalRawDataException _
                                    nil))))
