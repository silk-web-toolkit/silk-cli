(defproject silk-eden "0.2.0-pre.3"
  :description "Silk compile time command line interface."
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [me.raynes/laser "0.1.22"]
                 [me.rossputin/diskops "0.1.0"]
                 [pathetic "0.4.0"]
                 [watchtower/watchtower "0.1.1"]
                 [silk "0.2.0-pre.1"]
                 [seesaw "1.4.3"]]

  :aot :all
  :main silk.eden.cli)
