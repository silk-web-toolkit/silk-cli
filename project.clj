(defproject silk "0.4.0-beta.1"
  :description "Silk compile time command line interface."
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [me.raynes/laser "1.1.1"]
                 [me.rossputin/diskops "0.1.1"]
                 [pathetic "0.4.0"]
                 [org.clojars.zcaudate/watchtower "0.1.2"]
                 [silk-core "0.4.0-beta.1"]]

  :aot :all
  :main silk.eden.cli)
