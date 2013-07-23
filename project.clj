(defproject silk "0.2.4"
  :description "Silk compile time command line interface."
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [me.raynes/laser "1.1.1"]
                 [me.rossputin/diskops "0.1.0"]
                 [pathetic "0.4.0"]
                 [watchtower/watchtower "0.1.1"]
                 [silk-core "0.2.4"]]

  :aot :all
  :main silk.eden.cli)
