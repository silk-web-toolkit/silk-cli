(ns silk.eden.cli
  (:require [silk.input.env :as se]
            [silk.transform.pipeline :as pipes])
  (:use [clojure.string :only [split]]
        [watchtower.core]
        [silk.eden.io])
  (import java.io.File)
  (:gen-class))

;; =============================================================================
;; Helper functions
;; =============================================================================

(defn- spin
  [args]
  (let [vdp (pipes/view-driven-pipeline-> (first args))]
    (println "Spinning your site...")
    (side-effecting-spin-io)
    (doseq [t vdp]
      (let [parent (.getParent (new File (:path t)))]
        (when-not (nil? parent) (.mkdirs (File. "site" parent)))
        (spit (str se/site-path (:path t)) (:content t))))
    (println "Site spinning is complete, we hope you like it.")))

(defn- reload-report
  [payload]
  (println "files changed : " payload)
  (spin ["spin"]))

(defn- reload
  []
  (def fut (future (watcher ["view/" "template/" "components/" "resource/"]
    (rate 500) ;; poll every 500ms
    (file-filter ignore-dotfiles) ;; add a filter for the files we care about
    (file-filter (extensions :html :css :js)) ;; filter by extensions
    (on-change #(reload-report %)))))

  (println "Press enter to exit")
  (loop [input (read-line)]
    (when-not (= "\n" input)
      (System/exit 0)
      (recur (read-line)))))


;; =============================================================================
;; Application entry point
;; =============================================================================

(defn -main
  [& args]
  {:pre [(is-silk-project?)]}
  (cli-app-banner-display)
  (if (= (first args) "reload")
    (reload)
    (spin args)))
