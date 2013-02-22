(ns silk.eden.cli
  (:require [clojure.java.io :refer [file]]
            [me.raynes.laser :as l]
            [silk.input.env :as se]
            [silk.input.file :as sf])
  (import java.io.File)
  (:gen-class))

;; =============================================================================
;; Helper functions
;; =============================================================================

(defn- get-views [] (rest (file-seq (file se/views-path))))

(defn- template-wrap
  [v]
  (let [parsed-view (l/parse v)]
    {:file (.getName v)
     :content (l/document 
                (l/parse (sf/template "default.html"))
                (l/id="silk-view") 
                  (l/replace 
                    (l/select parsed-view 
                      (l/child-of (l/element= :body) (l/any)))))}))


;; =============================================================================
;; Application entry point
;; =============================================================================

(defn -main [& args]
  (let [views (get-views)
        templated (map #(template-wrap %) views)]
    (.mkdir (new File "site"))
    (doseq [t templated]
      (spit (str se/site-path (:file t)) (:content t)))))
