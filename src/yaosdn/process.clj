(ns yaosdn.process
  (:require [yaosdn.library.ignite :as ignite]
            [yaosdn.library.pcap :as pcap]
            [yaosdn.library.tun :as tun]
            [yaosdn.state :as state]))


(defn process-packets [in-packet-processor
                       filter-function
                       map-function
                       out-packet-processor
                       router-function]
  (let [in-packets (.get-packets in-packet-processor
                                 filter-function)
        processed-packets (into () (map map-function in-packets))
        count-out-packets (.send-packets out-packet-processor
                                         router-function
                                         processed-packets)]
    count-out-packets))


(defn handle-tx-and-rx [interface-name tx rx]
  (swap! state/global-state update-in [interface-name :tx] + tx)
  (swap! state/global-state update-in [interface-name :rx] + rx))


(defn tx-map-function [interface-name packet]
  packet)


(defn rx-map-function [interface-name packet]
  packet)


(defn router-function [interface-name packet]
  (let [packet-header (.getHeader packet)
        src-ip-address (-> packet-header
                           .getSrcAddr
                           .getHostAddress)
        dst-ip-address (-> packet-header
                           .getDstAddr
                           .getHostAddress)]
    (swap! state/global-state assoc-in [interface-name :last-packet] packet)
    (state/log "OUT:" src-ip-address "->" dst-ip-address)
    dst-ip-address))


(defn filter-function [interface-name packet]
  (let [packet-header (.getHeader packet)
        src-ip-address (-> packet-header
                           .getSrcAddr
                           .getHostAddress)
        dst-ip-address (-> packet-header
                           .getDstAddr
                           .getHostAddress)]
    (state/log "IN:" dst-ip-address "<-" src-ip-address)
    (.contains (state/get-ip-addresses interface-name)
               dst-ip-address)))


(defn process-loop [interface-name]
  (let [ip-addresses (->> interface-name
                          pcap/get-interface-by-name
                          pcap/get-addresses
                          (map #(.getHostAddress %)))
        params {:interface-name interface-name
                :topic-names ip-addresses}]
    (with-open [tun-packet-processor (tun/new-tun-packet-processor params)]
      (with-open [ignite-packet-processor (ignite/new-ignite-packet-processor params)]
        (swap! state/global-state assoc interface-name {:active true
                                                  :tx 0
                                                  :rx 0
                                                  :ip-addresses ip-addresses})
        (while (get-in @state/global-state [interface-name :active])
          (let [tx (process-packets tun-packet-processor
                                    identity
                                    (partial tx-map-function interface-name)
                                    ignite-packet-processor
                                    (partial router-function interface-name))
                rx (process-packets ignite-packet-processor
                                    (partial filter-function interface-name)
                                    (partial rx-map-function interface-name)
                                    tun-packet-processor
                                    identity)]
            (handle-tx-and-rx interface-name tx rx)))))))


(defn stop-packet-processing! [& [interface-name]]
  (swap! state/global-state assoc-in [(or interface-name
                                          (state/get-current-interface))
                                      :active]
         false))


(defn start-packet-processing! [& [interface-name]]
  (let [interface-name (or interface-name
                           (state/get-current-interface))]
    (when-not (get-in @state/global-state [interface-name
                                           :active])
      (future (process-loop interface-name)))))
