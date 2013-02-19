(ns silk.eden.cli
  (:require [silk.input.env :as se])
  (:gen-class))

(defn -main [& args]
  (println args)
  (println se/silk-home))
