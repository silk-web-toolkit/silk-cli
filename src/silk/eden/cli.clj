(ns silk.eden.cli
  (:use [silk.eden.io]
        [silk.eden.ops]))


;; =============================================================================
;; Application entry point
;; =============================================================================

(defn -main [& args]
  (cli-app-banner-display)
  (if (= (first args) "reload")
    (reload-on)
    (spin args)))

