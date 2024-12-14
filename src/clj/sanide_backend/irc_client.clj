(ns sanide-backend.irc-client
  (:require [clojure.core :as c]
            [org.httpkit.server :as hk]
            [clojure.data.json :as json])
  (:import [java.net Socket]
           [java.lang Thread]
           [java.io PrintWriter InputStreamReader BufferedReader]))

(declare conn-handler)

(def irc-conn (c/ref {}))

(defn connect [ws-channel server port]
  (let [socket (Socket. server port)
        in (BufferedReader. (InputStreamReader. (.getInputStream socket)))
        out (PrintWriter. (.getOutputStream socket))]
    (c/dosync
     (c/alter irc-conn merge {:socket socket :in in :out out}))
    (doto (Thread. #(conn-handler ws-channel)) (.start))))

(defn write [msg]
  (doto (:out @irc-conn)
    (.println (str msg "\r"))
    (.flush)))

(defn pong [msg]
  (write (str "PONG "  (re-find #":.*" msg))))

(defn conn-handler [ws-channel]
  (try (while (= false (:exit @irc-conn))
         (let [msg (.readLine (:in @irc-conn))]
           (println msg)
           (cond
             (re-find #"^ERROR :Closing Link:" msg) (c/dosync (c/alter irc-conn merge {:exit true}))
             (re-find #"^PING" msg) (pong msg)
             :else (hk/send! ws-channel msg))))
       (catch Exception _ (println "Closing socket connection"))
       (finally (println "Closing irc connection"))))

(defn login [user]
  (write (str "NICK " user))
  (write (str "USER " user " 0 * :" user)))

(defn join-channel [channel]
  (write (str "JOIN " channel)))

(defn participants [channel]
  (write (str "NAMES " channel)))

(defn privmsg [channel msg]
  (write (str "PRIVMSG " channel " :" msg)))

(defn init [ws-channel server port user channel]
  (connect ws-channel server port)
  (login user)
  (join-channel channel))
