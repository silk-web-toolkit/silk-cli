(ns silk.eden.io
  (:require [silk.input.env :as se]
            [clojure.java.io :refer [file]]
            [me.rossputin.diskops :as do])
  (import java.io.File java.io.FileNotFoundException))

;; =============================================================================
;; Ugly side effecting IO
;; =============================================================================

(defmacro get-version []
  (System/getProperty "silk.version"))

(def version (get-version))

(defn cli-app-banner-display
  []
  (println "    _ _ _")
  (println " __(_) | |__")
  (println "(_-< | | / /")
  (println "/__/_|_|_\\_\\")
  (println "")
  (println (str "v" version)))

(defn is-dir?
  [d]
  (.exists (file d)))

(defn side-effecting-spin-io
  []
  (when (is-dir? "site") (do/delete-directory "site"))
  (.mkdir (new File "site"))
  (do/copy-recursive "resource" "site")
  (do/copy-recursive "meta" "site"))

(defn is-silk-project?
  []
  (and
   (is-dir? "view") (is-dir? "template") (is-dir? "resource") (is-dir? "meta")
   (is-dir? "components") (is-dir? "data")))

(defn is-silk-configured?
  []
  (and 
    (is-dir? se/components-path) (is-dir? se/data-path)))
        
(defn check-silk-configuration
  []
  (if (not (is-silk-configured?))
    (do
      (throw (IllegalArgumentException. "Silk is not configured, please ensure your SILK_PATH is setup and contains a components and data directory.")))))

(defn check-silk-project-structure
  []
  (if (not (is-silk-project?))
    (do
      (throw (IllegalArgumentException. "Not a Silk project, a directory may be missing - template, view, components, data, resource or meta ?")))))

(defn handler
  [f & handlers]
  (reduce (fn [handled h] (partial h handled)) f (reverse handlers)))

(defn handle-silk-project-exception
  [f & args]
  (try
    (apply f args)
    (catch IllegalArgumentException iex
      (println "ERROR: Sorry, either Silk is not configured properly or there is a problem with this Silk project.")
      (println (str "Cause of error: " (.getMessage iex))))
    (catch FileNotFoundException ex
      (println "ERROR: Sorry, there was a problem, either a component is missing or this is not a silk project ?")
      (println (str "Cause of error: " (.getMessage ex))))))
