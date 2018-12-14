(ns yaosdn.state
  (:require [clojure.tools.logging :as logging]))


(def global-state (atom {}))


(defn log [& args]
  (when (:logging @global-state)
    (logging/infof ((partial clojure.string/join #" ") args))))


(defn set-logging! [enabled]
  (swap! global-state assoc :logging enabled))


(defn set-current-interface! [interface-name]
  (swap! global-state assoc :current-interface interface-name))


(defn get-current-interface []
  (get @global-state :current-interface))


(defmacro build-info-getter [data-type]
  (let [function-name (symbol (str "get-" data-type))]
    `(defn ~function-name [& [interface-name#]]
       (get-in @global-state [(or interface-name#
                                  (get-current-interface))
                              (keyword '~data-type)]))))


(build-info-getter rx)
(build-info-getter tx)
(build-info-getter last-packet)
(build-info-getter ip-addresses)
