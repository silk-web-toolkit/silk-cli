(ns silk.io
  (:require [silk.core.input.env :as se]
            [silk.core.input.file :as sf]
            [silk.core.transform.pipeline :as pipes]
            [clojure.java.io :refer [file]]
            [me.rossputin.diskops :as do])
  (import java.io.File java.io.FileNotFoundException))

;; =============================================================================
;; Helper functions
;; =============================================================================

(defmacro get-version []
  (System/getProperty "silk.version"))

(defn- filter-file
  [r]
  (reify java.io.FilenameFilter
    (accept [_ d name] (not (nil? (re-find r name))))))

(defn- is-detail?
  [d r]
  (let [files (.list (file d) (filter-file r))]
    (if (seq files) true false)))

(defn- do-detail-pages
  [path mode]
  (let [f (file path)
        name (.getName f)
        tpl (file
              (str
                (do/pwd) (do/fs) "template" (do/fs) "detail" (do/fs) name ".html"))]
    (when (.exists (file tpl))
      (let [details (pipes/data-detail-pipeline-> (.listFiles f) tpl mode)]
        (doseq [d details]
          (let [parent (.getParent (new File (:path d)))
                raw (str se/site-path (:path d))
                save-path (str (subs raw 0 (.lastIndexOf raw ".")) ".html")]
            (when-not (nil? parent) (.mkdirs (File. "site" parent)))
            (spit save-path (:content d))))))))

(defn- do-index-pages
  [d]
  (println (str "processing data driven index pages"))
  (println (str "d is : " d)))


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
  (println (str "v" (get-version))))

(defn side-effecting-spin-io
  []
  (when (do/exists-dir? "site") (do/delete-directory "site"))
  (.mkdir (new File "site"))
  (when (do/exists-dir? "resource") (do/copy-recursive "resource" "site"))
  (when (do/exists-dir? "meta") (do/copy-file-children "meta" "site")))

(defn is-silk-project?
  []
  (and
   (do/exists-dir? "view") (do/exists-dir? "template")))

;; last spun time and silk projects both live in silk home
(defn is-silk-configured?
  []
  (do/exists-dir? se/silk-home))

(defn check-silk-configuration
  []
  (if (not (is-silk-configured?))
    (do
      (throw (IllegalArgumentException. "Silk is not configured, please ensure your SILK_PATH is setup and contains a components and data directory.")))))

(defn check-silk-project-structure
  []
  (if (not (is-silk-project?))
    (do
      (throw (IllegalArgumentException. "Not a Silk project, a directory may be missing - template or view ?")))))

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
      (println "ERROR: Sorry, there was a problem, either a component or datasource is missing or this is not a silk project ?")
      (println (str "Cause of error: " (.getMessage ex))))))

(defn create-view-driven-pages
  [vdp]
  (doseq [t vdp]
    (let [parent (.getParent (new File (:path t)))]
      (when-not (nil? parent) (.mkdirs (File. "site" parent)))
      (spit (str se/site-path (:path t)) (:content t)))))

(defn create-data-driven-pages
  [mode]
  (let [data-dirs (sf/get-data-directories)]
    (doseq [d data-dirs]
      (if (is-detail? d #".edn") (do-detail-pages d mode) (do-index-pages d)))))

(defn store-project-dir
  "Writes the current project path and time to the central store."
  []
  (let [f se/spun-projects-file]
    (if (not (.exists f)) (.createNewFile f))
    (let [path (.getPath f)
          millis (.getTime (new java.util.Date))
          old (with-open [rdr (clojure.java.io/reader path)] (doall (line-seq rdr)))
          removed (remove #(.contains % (str (do/pwd) ",")) old)
          formatted (apply str (map #(str % "\n") removed))
          updated (conj [(str (do/pwd) "," millis "\n")]  formatted)]
      (spit path (apply str updated)))))