(ns sanide-frontend.events
  (:require
   [re-frame.core :as re-frame]
   [sanide-frontend.db :as db]
   [sanide-frontend.config :as config]
   [day8.re-frame.http-fx]
   [ajax.core :as ajax]))

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
 ::set-active-file
 (fn [db [_ val]]
   (assoc db :active-file val)))

(re-frame/reg-event-db
 ::cache-loaded-project
 (fn [_ [_ val]]
   (.setItem js/localStorage "project" (.stringify js/JSON (clj->js val)))))

(re-frame/reg-event-db
 ::new-project-failure
 (fn [db [_ fail]]
   (assoc db :new-project-failure fail)))

(re-frame/reg-event-db
 ::open-project-failure
 (fn [db [_ fail]]
   (assoc db :open-project-failure fail)))


;; reg-event-fx
(re-frame/reg-event-fx
 ::open-project-fx
 (fn [cofx [_ result]]
   {:db (assoc (:db cofx) :project result :show-project true)
    :fx [[:dispatch [::cache-loaded-project result]]
         [:dispatch [::set-active-file (:payload_name result)]]]}))

(re-frame/reg-event-fx
 ::get-new-project
 (fn [_ [_ name]]
   {:http-xhrio {:method :get
                 :uri (str config/api-url "/fs/new")
                 :params {:project_name name}
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success [::open-project-fx]
                 :on-failure [::new-project-failure]}}))

(re-frame/reg-event-fx
 ::open-at-path
 (fn [_ [_ path]]
   {:http-xhrio {:method :get
                 :uri (str config/api-url "/fs/open-path")
                 :params {:path path}
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success [::open-project-fx]
                 :on-failure [::open-project-failure]}}))

(re-frame/reg-event-fx
 ::open-dialog
 (fn [_ [_ _]]
   {:http-xhrio {:method :get
                 :uri (str config/api-url "/fs/open-dialog")
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success [::open-project-fx]
                 :on-failure [::open-project-failure]}}))