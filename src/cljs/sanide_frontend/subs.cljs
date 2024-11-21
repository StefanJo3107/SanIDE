(ns sanide-frontend.subs
  (:require
   [re-frame.core :as re-frame]))

(re-frame/reg-sub
 ::name
 (fn [db]
   (:name db)))

(re-frame/reg-sub
 ::active-item
 (fn [db]
   (:active-item db)))

(re-frame/reg-sub
 ::project
 (fn [db]
   (:project db)))

(re-frame/reg-sub
 ::show-project
 (fn [db]
   (:show-project db)))
