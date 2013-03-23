(defproject silk-eden "0.2.0-pre.2"
  :description "Silk compile time, habitat for templates, components, inputs and transformations."
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [me.raynes/laser "0.1.22"]
                 [pathetic "0.4.0"]
                 [watchtower/watchtower "0.1.1"]
                 [silk "0.2.0-pre.1"]]

  :aot :all
  :main silk.eden.cli)
