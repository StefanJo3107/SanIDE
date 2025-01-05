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
 ::irc-loading
 (fn [db]
   (:irc-loading db)))

(re-frame/reg-sub
 ::server-address
 (fn [db]
   (:server-address db)))

(re-frame/reg-sub
 ::server-port
 (fn [db]
   (:server-port db)))

(re-frame/reg-sub
 ::username
 (fn [db]
   (:username db)))

(re-frame/reg-sub
 ::channel
 (fn [db]
   (:channel db)))

(re-frame/reg-sub
 ::loading-char
 (fn [db]
   (:loading-char db)))

(re-frame/reg-sub
 ::messages
 (fn [db]
   (:messages db)))

(re-frame/reg-sub
 ::participants
 (fn [db]
   (:participants db)))

(re-frame/reg-sub
 ::build-result
 (fn [db]
   (:build-result db)))

(re-frame/reg-sub
 ::flash-loading
 (fn [db]
   (:flash-loading db)))