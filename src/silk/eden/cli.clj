(ns silk.eden.cli
  (:require [clojure.java.io :refer [copy delete-file file]]
            [me.raynes.laser :as l]
            [pathetic.core :as path]
            [me.rossputin.diskops :as do]
            [silk.input.env :as se]
            [silk.input.file :as sf]
            [silk.transform.path :as sp]
            [silk.transform.element :as sel])
  (:use [clojure.string :only [split]]
        [watchtower.core]
        [silk.eden.io])
  (import java.io.File)
  (:gen-class))

;; =============================================================================
;; Helper functions
;; =============================================================================

(def c-state (atom nil))

(defn- build-component
  [i]
  (let [comp-str (str ((split i #":") 1) ".html")
        lcp (str se/pwd se/fs "components" se/fs comp-str)
        c-path (if (.exists (File. lcp)) (file lcp) (sf/component comp-str))
        parsed-comp (l/parse c-path)]
    (l/select parsed-comp
              (l/child-of (l/element= :body) (l/any)))))

(defn- view-inject
  [v]
  (let [parsed-view (l/parse v)
        meta-template (l/select parsed-view
                       (l/and (l/element= :meta) (l/attr= :name "template")))
        template (if-not (nil? (first meta-template))
                   (sf/template
                    (str (:content (:attrs (first meta-template))) ".html"))
                   (sf/template "default.html"))]
    {:path (sp/relativise-> se/views-path (.getPath v))
     :content (l/document
                (l/parse template)
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

(defn- spin
  [args]
  (let [views (sf/get-views)
        templated-views (map #(view-inject %) views)
        comp-parse-1 (map #(process-components %) templated-views)
        comp-parse-2 (map #(process-components %) comp-parse-1)
        link-rewritten (map #(sel/relativise-attrs :link :href % (first args)) comp-parse-2)
        img-rewritten (map #(sel/relativise-attrs :img :src % (first args)) link-rewritten)
        script-rewritten (map #(sel/relativise-attrs :script :src % (first args)) img-rewritten)
        a-rewritten (map #(sel/relativise-attrs :a :href % (first args)) script-rewritten)]
    (println "Spinning your site...")
    (side-effecting-spin-io)
    (doseq [t a-rewritten]
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
  (watcher ["view/" "template/" "components/" "resource/"]
    (rate 500) ;; poll every 500ms
    (file-filter ignore-dotfiles) ;; add a filter for the files we care about
    (file-filter (extensions :html :css :js)) ;; filter by extensions
    (on-change #(reload-report %))))


;; =============================================================================
;; Application entry point
;; =============================================================================

(defn -main [& args]
  (cli-app-banner-display)
  (if (= (first args) "reload")
    (reload)
    (spin args)))
