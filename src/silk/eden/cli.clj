(ns silk.eden.cli
  (:require [clojure.java.io :refer [copy delete-file file]]
            [me.raynes.laser :as l]
            [pathetic.core :as path]
            [silk.input.env :as se]
            [silk.input.file :as sf])
  (:use [clojure.string :only [split]]
        [watchtower.core])
  (import java.io.File)
  (:gen-class))

;; =============================================================================
;; Helper functions
;; =============================================================================

(defn- ascii-art
  []
  (println "    _ _ _")
  (println " __(_) | |__")
  (println "(_-< | | / /")
  (println "/__/_|_|_\\_\\"))

(def c-state (atom nil))

(def m-state (atom "test"))

(defn- delete-directory
  [d]
  (doseq [f (reverse (file-seq (File. d)))] (delete-file f)))

(defn- copy-recursive
  [src dest]
  (doseq [f (remove #(.isDirectory %) (file-seq (file src)))]
    (let [dest-file (file dest f)]
      (.mkdirs (.getParentFile dest-file))
      (copy f dest-file))))

(defn- get-views [] (remove #(.isDirectory %) (file-seq (file se/views-path))))

(defn- build-component
  [i]
  (let [comp-str (str ((split i #":") 1) ".html")
        lcp (str se/pwd se/fs "components" se/fs comp-str)
        c-path (if (.exists (File. lcp)) (file lcp) (sf/component comp-str))
        parsed-comp (l/parse c-path)]
    (l/select parsed-comp
              (l/child-of (l/element= :body) (l/any)))))

(defn- relativise
  "Relativise two paths, prepping Windows paths."
  [a b]
  (let [doze? (if (re-find #"indow" (System/getProperty "os.name")) true false)
        pa (if doze? (.replaceAll ((split a #":") 1) "\\\\" "/") a)
        pb (if doze? (.replaceAll ((split b #":") 1) "\\\\" "/") b)]
    (path/relativize pa pb)))

(defn- view-inject
  [v]
  (let [parsed-view (l/parse v)
        meta-template (l/select parsed-view
                       (l/and (l/element= :meta) (l/attr= :name "template")))
        template (if-not (nil? (first meta-template))
                   (sf/template
                    (str (:content (:attrs (first meta-template))) ".html"))
                   (sf/template "default.html"))]
    {:file (relativise se/views-path (.getPath v))
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

(defn- relativise-attr
  [v p]
  (let [vp (.getParent (File. p))]
    (if-not (nil? vp)
      (let [pv (path/parse-path v)
            parsed-uri (some #{"http:" "https:" "mailto:"} pv)]
        (if-not (nil? parsed-uri)
          v
          (let [rel (relativise
                     (.getParent (File. se/views-path p))
                     se/views-path)]
            (str rel "/" v))))
      (if (= @m-state "live") (str "/" v) v))))

(defn- attrib-rewrite
  [e a p]
  (let [page (l/parse (:content p))
        uri-tx (l/document
                 page
                 (l/and (l/element= e) (l/attr? a))
                   (l/update-attr a relativise-attr (:file p)))]
    (assoc p :content uri-tx)))

(defn- spin
  [args]
  (let [views (get-views)
        templated-views (map #(view-inject %) views)
        pages (map #(process-components %) templated-views)
        link-rewritten (map #(attrib-rewrite :link :href %) pages)
        img-rewritten (map #(attrib-rewrite :img :src %) link-rewritten)
        script-rewritten (map #(attrib-rewrite :script :src %) img-rewritten)
        a-rewritten (map #(attrib-rewrite :a :href %) script-rewritten)]
    (if-not (nil? (first args)) (reset! m-state (first args)))
    (ascii-art)
    (println "")
    (println "v0.2.0-pre.2")
    (when (.exists (File. "site")) (delete-directory "site"))
    (.mkdir (new File "site"))
    (copy-recursive "resource" "site")
    (copy-recursive "meta" "site")
    (println "Spinning your site...")
    (doseq [t a-rewritten]
      (let [parent (.getParent (new File (:file t)))]
        (when-not (nil? parent) (.mkdirs (File. "site" parent)))
        (spit (str se/site-path (:file t)) (:content t))))
    (println "Site spinning is complete, we hope you like it.")))

(defn- reload-report
  [payload]
  (println "files changed : " payload)
  (spin ["spin"]))

(defn- reload
  []
  (watcher ["view/" "template/" "resource/"]
    (rate 500) ;; poll every 500ms
    (file-filter ignore-dotfiles) ;; add a filter for the files we care about
    (file-filter (extensions :html :css :js)) ;; filter by extensions
    (on-change #(reload-report %))))


;; =============================================================================
;; Application entry point
;; =============================================================================

(defn -main [& args]
  (if (= (first args) "reload")
    (reload)
    (spin args)))
