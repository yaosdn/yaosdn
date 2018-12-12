(ns yaosdn.library.tun
  (:import (java.nio ByteBuffer)
           (org.it4y.net.tuntap TunDevice)
           (org.pcap4j.packet AbstractPacket))
  (:require [yaosdn.library.pcap]))


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
