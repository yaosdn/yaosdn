(defproject yaosdn "0.1.0-SNAPSHOT"
  :description "Yet Another Overlay Software Defined Network"
  :url "http://github.com/yaosdn/yaosdn"
  :license {:name "MIT License"
            :url "https://opensource.org/licenses/MIT"}
  :plugins [[lein-sub "0.3.0"]
            [lein-ancient "0.6.15"]
            [lein-bump-version "0.1.6"]]
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [yaosdn/yaosdn-proto "0.1.0-SNAPSHOT"]
                 [yaosdn/yaosdn-integration-ignite "0.1.0-SNAPSHOT"]
                 [yaosdn/yaosdn-integration-pcap "0.1.0-SNAPSHOT"]
                 [yaosdn/yaosdn-integration-tuntap "0.1.0-SNAPSHOT"]]
  :main ^:skip-aot yaosdn.core
  :target-path "target/%s"
  :repl-options {:timeout 120000}
  :sub ["yaosdn-proto"
        "yaosdn-integration-ignite"
        "yaosdn-integration-pcap"
        "yaosdn-integration-tuntap"]
  :aliases {"test" ["do" ["ancient-all"] ["sub" "lint-and-test-all"]]
            "bump-all" ["do" ["bump-version"] ["sub" "bump-version"]]
            "ancient-all" ["do" ["ancient"] ["sub" "ancient"]]
            "deploy-all" ["do" ["sub" "deploy" "clojars"] ["deploy" "clojars"]]}
  :profiles {:uberjar {:aot :all}
             :dev {:dependencies [[org.clojure/tools.namespace "0.2.11"]]}})
