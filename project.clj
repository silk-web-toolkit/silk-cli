(defproject silk "0.5.0-SNAPSHOT"
  :description "Silk compile time command line interface."
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [me.raynes/laser "1.1.1"]
                 [me.rossputin/diskops "0.2.0"]
                 [pathetic "0.4.0"]
                 [org.clojars.zcaudate/watchtower "0.1.2"]
                 [silk-core "0.5.0-SNAPSHOT"]]

  :aot :all
  :main silk.cli)
