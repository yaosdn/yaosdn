(ns yaosdn.core
  (:require [yaosdn.library.ignite :as ignite]
            [yaosdn.library.pcap :as pcap]
            [yaosdn.library.tun :as tun]
            [yaosdn.helpers :as helpers]
            [yaosdn.state :as state]
            [yaosdn.process :as process])
  (:gen-class))


(defn -main [interface-name]
  (state/set-logging! true)
  (state/set-current-interface! interface-name)
  (process/process-loop interface-name))


(defn repl-test-local-node [interface-name o3-function]
  (future (let [local-fn (fn [_ octets]
                           (if (and (= (:o1 octets) 10)
                                    (= (:o2 octets) 0))
                             (update octets :o3 o3-function)
                             octets))
                tx-map-function (helpers/build-local-tx-map-function local-fn
                                                                     local-fn)]
            (with-redefs [process/tx-map-function tx-map-function]
              (-main interface-name)))))
