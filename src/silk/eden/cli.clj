(ns silk.eden.cli
  (:require [silk.input.env :as se]
            [silk.input.file :as sf]
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
  (println "Spinning your site...")
  (check-silk-configuration)
  (check-silk-project-structure)
  (side-effecting-spin-io)
  (create-view-driven-pages (pipes/view-driven-pipeline-> (first args)))
  (create-data-driven-pages (first args))
  (sf/store-project-dir)
  (println "Site spinning is complete, we hope you like it."))

(def spin-handled (handler spin handle-silk-project-exception))

(defn- reload-report
  [payload]
  (println "files changed : " payload)
  (spin-handled ["spin"]))

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

(defn sites
  []
  (let [site-list (slurp se/spun-projects-file)]
    (println "Your Silk sites are : ")
    (println site-list)))

(defn launch
  [args]
  (cli-app-banner-display)
  (cond
   (= (first args) "reload") (reload)
   (= (first args) "sites")  (do (check-silk-configuration) (sites))
   :else (spin-handled args)))

(def launch-handled (handler launch handle-silk-project-exception))

;; =============================================================================
;; Application entry point
;; =============================================================================

(defn -main
  [& args]
  (launch-handled args))
