(ns sanide-frontend.events
  (:require
   [re-frame.core :as re-frame]
   [sanide-frontend.db :as db]
   ))

(re-frame/reg-event-db
 ::initialize-db
 (fn [_ _]
   db/default-db))
