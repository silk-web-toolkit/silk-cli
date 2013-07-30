(ns silk.eden.io
  (:require [silk.input.env :as se]
            [silk.input.file :as sf]
            [silk.transform.pipeline :as pipes]
            [clojure.java.io :refer [file]]
            [me.rossputin.diskops :as do])
  (import java.io.File))

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
        tpl (file (str se/pwd se/fs "template" se/fs "detail" se/fs name ".html"))]
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
  "Writes the current project directory to the central store."
  []
  (let [file se/spun-projects-file pwd se/pwd]
    (if (not (.exists file)) (.createNewFile file))
    (if (not (.contains (slurp (.getPath file)) pwd))
      (spit (.getPath file) (str pwd "\n") :append true))))
