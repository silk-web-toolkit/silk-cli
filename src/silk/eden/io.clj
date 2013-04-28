(ns silk.eden.io
  (:require [me.rossputin.diskops :as do])
  (import java.io.File))

;; =============================================================================
;; Ugly side effecting IO
;; =============================================================================

(defn cli-app-banner-display
  []
  (println "    _ _ _")
  (println " __(_) | |__")
  (println "(_-< | | / /")
  (println "/__/_|_|_\\_\\")
  (println "")
  (println "v0.2.0-alpha.1"))

(defn side-effecting-spin-io
  []
  (when (.exists (File. "site")) (do/delete-directory "site"))
  (.mkdir (new File "site"))
  (do/copy-recursive "resource" "site")
  (do/copy-recursive "meta" "site"))
