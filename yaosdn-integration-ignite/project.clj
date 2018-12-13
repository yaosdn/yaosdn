(defproject yaosdn/yaosdn-integration-ignite "0.1.0-SNAPSHOT"
  :description "YAOSDN Apache Ignite integration"
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
                 [org.apache.ignite/ignite-core "2.7.0"]
                 [yaosdn/yaosdn-proto "0.1.0-SNAPSHOT"]])
