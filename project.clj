(defproject silk-eden "0.2.0-beta.1"
  :description "Silk compile time command line interface."
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [me.raynes/laser "0.1.22"]
                 [me.rossputin/diskops "0.1.0"]
                 [pathetic "0.4.0"]
                 [watchtower/watchtower "0.1.1"]
                 [silk "0.2.0-beta.1"]]

  :aot :all
  :main silk.eden.cli)
