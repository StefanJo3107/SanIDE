(ns sanide-frontend.events
  (:require
   [re-frame.core :as re-frame]
   [sanide-frontend.db :as db]
   [sanide-frontend.config :as config]
   [day8.re-frame.http-fx]
   [ajax.core :as ajax]
   [taoensso.timbre :as log]))

;; reg-event-db
(re-frame/reg-event-db
 ::initialize-db
 (fn [_ _]
   db/default-db))

(re-frame/reg-event-db
 ::set-active-item
 (fn [db [_ val]]
   (assoc db :active-item val)))

(re-frame/reg-event-db
 ::cache-loaded-project
 (fn [db [_ val]]
   (js->clj (.setItem js/localStorage "project" val))))

(re-frame/reg-event-db
 ::new-project-result
 (fn [db [_ result]]
   (assoc db :project result)))

(re-frame/reg-event-db
 ::new-project-failure
 (fn [db [_ fail]]
   (log/error fail)))

;; reg-event-fx
(re-frame/reg-event-fx
 ::get-new-project
 (fn [_ [_ name]]
   {:http-xhrio {:method :get
                 :uri (str config/api-url "/fs/new")
                 :params {:project_name name}
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success [::new-project-result]
                 :on-failure [::new-project-failure]}}))