(ns yaosdn.library.proto)


(defprotocol PacketProcessor
  (connect
    [packet-processor]
    [packet-processor params])
  (close [packet-processor])
  (get-packets
    [packet-processor]
    [packet-processor filter-function])
  (send-packets [packet-processor router-function packets]))
