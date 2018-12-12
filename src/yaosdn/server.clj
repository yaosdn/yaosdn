(ns yaosdn.server
  (:import [org.apache.ignite Ignition])
  (:require [clojure.tools.namespace.repl :refer [disable-unload!]]))


(disable-unload!)


(defn start-ignite []
  (Ignition/start))


(defonce ignite (start-ignite))
