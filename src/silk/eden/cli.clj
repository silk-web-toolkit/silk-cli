(ns silk.eden.cli
  (:require [clojure.java.io :refer [file]]
            [silk.input.env :as se])
  (:gen-class))

(defn- get-templates [] (file-seq (file se/templates-path)))

(defn- get-views [] (file-seq (file se/views-path)))

(defn -main [& args]
  (println args)
  (println (get-templates))
  (println (get-views)))
