(ns silk.eden.io
  (:require [silk.input.env :as se]
            [clojure.java.io :refer [file]]
            [me.rossputin.diskops :as do])
  (import java.io.File))

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
