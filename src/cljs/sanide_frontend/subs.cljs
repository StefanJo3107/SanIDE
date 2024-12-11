(ns sanide-frontend.subs
  (:require
   [re-frame.core :as re-frame]))

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

(re-frame/reg-sub
 ::active-file
 (fn [db]
   (:active-file db)))

(re-frame/reg-sub
 ::latest-payload
 (fn [db]
   (:latest-payload db)))

(re-frame/reg-sub
 ::latest-config
 (fn [db]
   (:latest-config db)))

(re-frame/reg-sub
 ::examples
 (fn [db]
   (:examples db)))

(re-frame/reg-sub
 ::irc-connected
 (fn [db]
   (:irc-connected db)))

(re-frame/reg-sub
 ::messages
 (fn [db]
   (:messages db)))