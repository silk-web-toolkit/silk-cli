(ns silk.eden.cli
  (:require [silk.input.env :as se]
            [silk.input.file :as sf] 
            [silk.transform.pipeline :as pipes])
  (:use [clojure.string :only [split]]
        [watchtower.core]
        [silk.eden.io])
  (import java.io.File java.io.FileNotFoundException)
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
    (sf/store-project-dir)
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

(defn launch
  [args]
  (cli-app-banner-display)
  (if (not (is-silk-project?))
    (do
      (throw (IllegalArgumentException. "Not a Silk project, one of template, view, components or resource directory are missing."))))
  (if (= (first args) "reload")
    (reload)
    (spin args)))

(defn handler
  [f & handlers]
  (reduce (fn [handled h] (partial h handled)) f (reverse handlers)))

(defn handle-silk-project-exception
  [f & args]
  (try
    (apply f args)
    (catch IllegalArgumentException iex
      (println "ERROR: Sorry, this is not a Silk project.")
      (println (str "Cause of error: " (.getMessage iex))))
    (catch FileNotFoundException ex
      (println "ERROR: Sorry, there was a problem, either a component is missing or this is not a silk project ?")
      (println (str "Cause of error: " (.getMessage ex))))))

(def launch-handled (handler launch handle-silk-project-exception))


;; =============================================================================
;; Application entry point
;; =============================================================================

(defn -main
  [& args]
  (launch-handled args))
