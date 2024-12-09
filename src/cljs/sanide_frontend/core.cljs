(ns sanide-frontend.core
  (:require
   [reagent.dom :as rdom]
   [re-frame.core :as re-frame]
   [sanide-frontend.events :as events]
   [sanide-frontend.views :as views]
   [sanide-frontend.config :as config]
   [re-pressed.core :as rp]))


(defn dev-setup []
  (when config/debug?
    (println "dev mode")))

(defn ^:dev/after-load mount-root []
  (re-frame/clear-subscription-cache!)
  (let [root-el (.getElementById js/document "app")]
    (rdom/unmount-component-at-node root-el)
    (rdom/render [views/main-panel] root-el)))

(defn init []
  (re-frame/dispatch-sync [::events/initialize-db])
  (re-frame/dispatch-sync [::rp/add-keyboard-event-listener "keydown"])
  (re-frame/dispatch-sync [::events/get-examples])
  (re-frame/dispatch-sync [::events/ws-connect])
  ;; (ws/connect)
  (let [project  (js->clj (.parse js/JSON (.getItem js/localStorage "project")) :keywordize-keys true)]
    (when (some? project)
      (re-frame/dispatch [::events/open-at-path (:project_path project)])))
  (dev-setup)
  (mount-root))
