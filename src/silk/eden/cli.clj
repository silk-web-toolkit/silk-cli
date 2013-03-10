(ns silk.eden.cli
  (:require [clojure.java.io :refer [delete-file file]]
            [me.raynes.laser :as l]
            [silk.input.env :as se]
            [silk.input.file :as sf])
  (:use [clojure.string :only [split]])
  (import java.io.File)
  (:gen-class))

;; =============================================================================
;; Helper functions
;; =============================================================================

(def c-state (atom nil))

(defn- delete-directory
  [d]
  (doseq [f (reverse (file-seq (File. d)))] (delete-file f)))

(defn- get-views [] (rest (file-seq (file se/views-path))))

(defn- build-component
  [i]
  (let [comp-str (str ((split i #":") 1) ".html")
        parsed-comp (l/parse (sf/component comp-str))]
    (l/select parsed-comp
              (l/child-of (l/element= :body) (l/any)))))

(defn- view-inject
  [v]
  (let [parsed-view (l/parse v)]
    {:file (.getName v)
     :content (l/document
                (l/parse (sf/template "default.html"))
                (l/id="silk-view")
                  (l/replace
                    (l/select parsed-view
                      (l/child-of (l/element= :body) (l/any)))))}))

(defn- component-inject
  [i]
   (let [injected (l/document
                  (l/parse @c-state)
                  (l/id= i)
                  (l/replace (build-component i)))]
    (reset! c-state injected)))

(defn- process-components
  [t]
  (let [comps (l/select (l/parse (:content t)) (l/re-id #"silk-component"))
        comp-ids (map #(:id (:attrs %)) comps)]
    (reset! c-state (:content t))
    (doseq [id comp-ids]
      (component-inject id))
    (assoc t :content @c-state)))


;; =============================================================================
;; Application entry point
;; =============================================================================

(defn -main [& args]
  (let [views (get-views)
        templated-views (map #(view-inject %) views)
        pages (map #(process-components %) templated-views)]
    (when (.exists (File. "site")) (delete-directory "site"))
    (.mkdir (new File "site"))
    (doseq [t pages]
      (spit (str se/site-path (:file t)) (:content t)))))
