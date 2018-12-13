(ns yaosdn.library.tun
  (:import (java.nio ByteBuffer)
           (org.it4y.net.tuntap TunDevice)
           (org.pcap4j.packet AbstractPacket))
  (:require [yaosdn.library.proto :as proto]
            [yaosdn.library.pcap]))


(defmacro with-tun-interface [if-name & body]
  `(with-open [~'tun (get-tun-interface ~if-name)]
     ~@body))


(defn get-tun-interface [& [^String if-name]]
  (doto (TunDevice. if-name)
    (.open)))


(def ^:dynamic *tun-mtu* 1500)


(defmacro with-tun-mtu [mtu & body]
  `(binding [*tun-mtu* ~mtu]
     ~@body))


(defn ^AbstractPacket poll-tun-packet [^TunDevice tun]
  (let [buffer-size *tun-mtu*
        buffer (ByteBuffer/allocateDirect buffer-size)
        length (.readByteBuffer tun buffer false)
        arr (byte-array length)
        _ (.get buffer arr 0 length)
        packet (yaosdn.library.pcap/packet-of arr length)]
    packet))


(defn ^Integer push-tun-packet [^TunDevice tun ^AbstractPacket packet]
  (let [buffer-size *tun-mtu*
        buffer* (ByteBuffer/allocateDirect buffer-size)
        raw-data (.getRawData packet)
        buffer (.put buffer* raw-data)
        length (.writeByteBuffer tun buffer (count raw-data))]
    length))


(def ^:dynamic *tun-timeout* 100)


(defmacro with-tun-timeout [timeout & body]
  `(binding [*tun-timeout* ~timeout]
     ~@body))


(defn ^Boolean tun-has-data? [^TunDevice tun]
  (.isDataReady tun *tun-timeout*))


(def ^:dynamic *default-tun-interface* "tun0")


(defn- get-tun-connection [packet-processor params]
  (let [{interface-name :interface-name} params
        tun (get-tun-interface interface-name)]
    (assoc packet-processor
           :interface tun)))


(defn- read-tun-packets [interface filter-function]
  (loop [result ()]
    (if-not (tun-has-data? interface)
      result
      (recur (concat result
                     (let [data (poll-tun-packet interface)]
                       (when (filter-function data)
                         (list data))))))))


(defn- write-tun-packets [interface packets]
  (count (for [packet packets]
           (push-tun-packet interface packet))))


(defrecord TunPacketProcessor [interface]
  proto/PacketProcessor
  (connect [packet-processor] (.connect packet-processor nil))
  (connect [packet-processor params] (get-tun-connection packet-processor
                                                         params))
  (close [packet-processor] (assoc packet-processor
                                   :connection (.close interface)))
  (get-packets [packet-processor] (.get-packets packet-processor identity))
  (get-packets [packet-processor filter-function] (read-tun-packets interface filter-function))
  (send-packets [packet-processor router-function packets] (write-tun-packets interface packets)))


(defn new-tun-packet-processor [& [params]]
  (-> (->TunPacketProcessor nil)
      (.connect params)))
