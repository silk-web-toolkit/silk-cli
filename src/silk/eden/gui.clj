(ns silk.eden.gui
  (:use [seesaw.core]
        [seesaw.widgets.log-window]
        [silk.eden.ops]))

(defn- content
  []
  ;Button to manually spin Silk
  (def btn-spin (button :text "Spin"))

  (def spin-text (with-out-str(spin "")))

  (def reload-on-text (with-out-str(reload-on)))

  (def reload-off-text (with-out-str(reload-off)))

  (def logger (log-window :id :log-window :limit nil))

  ;Vertical apply spin options and multi-line text
  (vertical-panel :items [
    ;Radio group options to auto spin Silk
    (let [group (button-group) 
          panel (horizontal-panel :items [
                    "Auto Spin"
                    (radio :id "on" :text "On" :group group)
                    (radio :id "off" :text "Off" :selected? true :group group)
                    btn-spin])]
      ;Listener for spin button   
      (listen btn-spin :action (fn [e] 
        (log logger spin-text)))
      ;Listener for auto spin
      (listen group :selection
        (fn [e]
          (when-let [s (selection group)]
            (def id (.toString (id-of s)))
            (if (.equals id ":on") 
              (log logger reload-on-text)
              (log logger reload-off-text)))))
      panel)
    (scrollable logger)]))

;; =============================================================================
;; Application entry point
;; =============================================================================

(defn -main [& args]
  (invoke-later
    (-> (frame
      :title "Silk"
      :size [640 :by 480] 
      :content (content)
      :on-close
      :exit)
    show!)))
