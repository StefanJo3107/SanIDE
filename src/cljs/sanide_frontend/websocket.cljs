(ns sanide-frontend.websocket
  (:require [re-frame.core :as re-frame]
            [websocket-fx.core :as wfx]
            [sanide-frontend.config :as config]))

(defn connect []
  (re-frame/dispatch [::wfx/connect config/socket-id
                      {:url config/ws-url
                       :format :transit-json}]))

(defn disconnect []
  (re-frame/dispatch [::wfx/disconnect :default]))

(defn connect-to-irc [server port username channel]
  (re-frame/dispatch [::wfx/push config/socket-id
                      {:type "connect"
                       :server server
                       :port port
                       :username username
                       :channel channel}]))

(defn get-participants []
  (re-frame/dispatch [::wfx/push config/socket-id {:type "get-participants"}]))

(defn message-callback [m]
  (println m))

(defn listen-for-messages []
  (re-frame/dispatch [::wfx/subscribe config/socket-id :message-subscriber
                      {:message {:type "message-subscribe"}
                       :on-message [message-callback]}]))