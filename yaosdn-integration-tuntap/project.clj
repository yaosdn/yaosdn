(defproject yaosdn/yaosdn-integration-tuntap "0.1.0-SNAPSHOT"
  :description "YAOSDN TunTap integration"
  :url "http://github.com/yaosdn/yaosdn"
  :scm {:dir ".."}
  :license {:name "MIT License"
            :url "https://opensource.org/licenses/MIT"}
  :plugins [[jonase/eastwood "0.3.4"]
            [lein-cljfmt "0.6.2"]
            [lein-bump-version "0.1.6"]]
  :cljfmt {:remove-consecutive-blank-lines? false}
  :aliases {"lint" ["do" ["cljfmt" "check"] ["eastwood"]]
            "test-all" ["with-profile" "default" "test"]
            "lint-and-test-all" ["do" ["lint"] ["test-all"]]}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [clj-linux-net "0.2.0-SNAPSHOT"]
                 [yaosdn/yaosdn-integration-pcap "0.1.0-SNAPSHOT"]])
