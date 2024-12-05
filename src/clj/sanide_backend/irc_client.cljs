(ns sanide-backend.irc-client
  (:require [clojure.core :as c])
  (:import [java.net Socket]
           [java.lang Thread]
           [java.io PrintWriter InputStreamReader BufferedReader]))

(declare conn-handler)

(defn connect [server]
  (let [socket (Socket. (:name server) (:port server))
        in (BufferedReader. (InputStreamReader. (.getInputStream socket)))
        out (PrintWriter. (.getOutputStream socket))
        conn (c/ref {:in in :out out})]
    (doto (Thread. #(conn-handler conn)) (.start))
    conn))

(defn write [conn msg]
  (doto (:out @conn)
    (.println (str msg "\r"))
    (.flush)))

(defn pong [conn msg]
  (write conn (str "PONG "  (re-find #":.*" msg))))

(defn conn-handler [conn]
  (while (nil? (:exit @conn))
    (let [msg (.readLine (:in @conn))]
      (println msg)
      (cond
        (re-find #"^ERROR :Closing Link:" msg)
        (c/dosync (c/alter conn merge {:exit true}))
        (re-find #"^PING" msg)
        (pong conn msg)))))

(defn login [conn user]
  (write conn (str "NICK " (:nick user)))
  (write conn (str "USER " (:nick user) " 0 * :" (:name user))))

(defn join-channel [conn channel]
  (write conn (str "JOIN " channel)))

(defn participants [conn channel]
  (write conn (str "NAMES " channel)))

(defn privmsg [conn channel msg]
  (write conn (str "PRIVMSG " channel " :" msg)))

(defn init [server user channel]
  (let [irc (connect server)]
    (login irc user)
    (join-channel irc channel)))
