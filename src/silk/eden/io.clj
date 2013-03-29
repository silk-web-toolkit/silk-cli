(ns silk.eden.io
  (:require [me.rossputin.diskops :as do])
  (import java.io.File))

;; =============================================================================
;; Ugly side effecting IO
;; =============================================================================

(defn ugly-side-effecting-io
  []
  (when (.exists (File. "site")) (do/delete-directory "site"))
  (.mkdir (new File "site"))
  (do/copy-recursive "resource" "site")
  (do/copy-recursive "meta" "site")
  (println "Spinning your site..."))
