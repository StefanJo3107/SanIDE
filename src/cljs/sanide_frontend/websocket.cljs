(ns sanide-frontend.websocket
  (:require [re-frame.core :as re-frame]
            [websocket-fx.core :as wfx]
            [sanide-frontend.config :as config]
            [sanide-frontend.events :as events]))

(defn connect []
  (re-frame/dispatch [::wfx/connect config/socket-id
                      {:url config/ws-url
                       :format :json}]))

(defn disconnect []
  (re-frame/dispatch [::wfx/disconnect :default]))


(defn listen-for-messages []
  (re-frame/dispatch [::wfx/subscribe config/socket-id :message-subscriber
                      {:message {:type "message-subscription"}
                       :on-message [::events/add-message]}]))

(defn connect-to-irc [server port username channel]
  (connect)
  (re-frame/dispatch [::wfx/push config/socket-id
                      {:type "connect"
                       :server server
                       :port (int port)
                       :username username
                       :channel channel}])
  (listen-for-messages))

(defn get-participants []
  (re-frame/dispatch [::wfx/push config/socket-id {:type "get-participants"}]))