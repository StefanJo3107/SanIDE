(ns sanide-frontend.events
  (:require
   [re-frame.core :as re-frame]
   [sanide-frontend.db :as db]
   [sanide-frontend.config :as config]
   [day8.re-frame.http-fx]
   [ajax.core :as ajax]
   [clojure.string :as str]
   [wscljs.client :as ws]
   [wscljs.format :as fmt]
   [goog.string :as gstring]
   [goog.string.format]
   [sanide-frontend.common :as common]))

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
 ::set-latest-payload
 (fn [db [_ val]]
   (assoc db :latest-payload val)))

(re-frame/reg-event-db
 ::set-latest-config
 (fn [db [_ val]]
   (assoc db :latest-config val)))

(re-frame/reg-event-db
 ::set-server-address
 (fn [db [_ val]]
   (assoc db :server-address val)))

(re-frame/reg-event-db
 ::set-server-port
 (fn [db [_ val]]
   (assoc db :server-port val)))

(re-frame/reg-event-db
 ::set-username
 (fn [db [_ val]]
   (assoc db :username val)))

(re-frame/reg-event-db
 ::set-channel
 (fn [db [_ val]]
   (assoc db :channel val)))

(re-frame/reg-event-db
 ::add-message
 (fn [db [_ val]]
   (let [last-msg (last (db :messages))]
     (if (and (> (count (db :messages)) 0) (= (:from last-msg) (:from val)) (= (:time last-msg) (:time val)))
       (update db :messages (fn [v] (update v (dec (count v)) assoc :msg (str (:msg (last v)) "\n" (:msg val)))))
       (update db :messages conj val)))))

(re-frame/reg-event-db
 ::reset-messages
 (fn [db _]
   (assoc db :messages [])))

(re-frame/reg-event-db
 ::add-participants
 (fn [db [_ val]]
   (assoc db :participants (into (:participants db) val))))

(re-frame/reg-event-db
 ::add-participant
 (fn [db [_ val]]
   (update db :participants conj val)))

(re-frame/reg-event-db
 ::remove-participant
 (fn [db [_ val]]
   (assoc db :participants (remove #(= (:name %) val) (:participants db)))))

(re-frame/reg-event-db
 ::reset-participants
 (fn [db _]
   (assoc db :participants [])))

(re-frame/reg-event-db
 ::set-irc-connected
 (fn [db [_ val]]
   (assoc db :irc-connected val)))

(re-frame/reg-event-db
 ::set-irc-loading
 (fn [db [_ val]]
   (assoc db :irc-loading val)))

(re-frame/reg-event-fx
 ::response-failure-notify
 (fn [_ [_ fail]]
   {::error-notify (:err (:response fail))}))

(re-frame/reg-event-db
 ::get-examples-result
 (fn [db [_ result]]
   (assoc db :examples result)))

(re-frame/reg-event-db
 ::flash-loading
 (fn [db _]
   (assoc db :flash-loading true)))

(re-frame/reg-event-db
 ::update-loading-char
 (fn [db _]
   (let [loading-chars ["" "" "" "" "" ""]]
     (assoc db :loading-char (get loading-chars (mod (+ (.indexOf loading-chars (:loading-char db)) 1) (count loading-chars)))))))

;; reg-event-fx
(re-frame/reg-event-fx
 ::close-project
 [(re-frame/inject-cofx ::remove-cached-project)]
 (fn [cofx [_ _]]
   {:db (assoc (:db cofx) :show-project false :project nil)}))

(re-frame/reg-event-fx
 ::open-project-fx
 (fn [cofx [_ result]]
   {:db (assoc (:db cofx) :project result :show-project true :active-file (:payload_name result)
               :latest-payload (:payload_content result) :latest-config (:config_content result))
    ::cache-loaded-project result}))

(re-frame/reg-event-fx
 ::save-project-fx
 (fn [cofx [_ result]]
   (let [file_name (last (str/split (:file_path result) #"/"))]
     {:db (update-in (:db cofx) [:project] assoc
                     (if (= file_name "config.toml") :config_content :payload_content) (:content result))})))

(re-frame/reg-event-fx
 ::init-fx
 (fn [_ [_ result]]
   {::success-notify (:msg result)}))

(re-frame/reg-event-fx
 ::build-fx
 (fn [cofx [_ result]]
   {:db (assoc (:db cofx) :build-result (:res result) :flash-loading false)
    ::success-notify (:msg result)}))

(re-frame/reg-event-fx
 ::build-failure
 (fn [cofx [_ result]]
   {:db (assoc (:db cofx) :build-result (:exc (:response result)) :flash-loading false)
    ::error-notify (:err (:response result))}))

(re-frame/reg-event-fx
 ::get-new-project
 (fn [_ [_ name]]
   {:http-xhrio {:method :get
                 :uri (str config/api-url "/fs/new")
                 :params {:project_name name}
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success [::open-project-fx]
                 :on-failure [::response-failure-notify]}}))

(re-frame/reg-event-fx
 ::open-at-path
 (fn [_ [_ path]]
   {:http-xhrio {:method :get
                 :uri (str config/api-url "/fs/open-path")
                 :params {:path path}
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success [::open-project-fx]
                 :on-failure [::response-failure-notify]}}))

(re-frame/reg-event-fx
 ::open-dialog
 (fn [_ [_ _]]
   {:http-xhrio {:method :get
                 :uri (str config/api-url "/fs/open-dialog")
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success [::open-project-fx]
                 :on-failure [::response-failure-notify]}}))

(re-frame/reg-event-fx
 ::save-file
 (fn [_ [_ file]]
   {:http-xhrio {:method :post
                 :uri (str config/api-url "/fs/save")
                 :params file
                 :format (ajax/json-request-format)
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success [::save-project-fx]
                 :on-failure [::response-failure-notify]}}))

(re-frame/reg-event-fx
 ::get-examples
 (fn [_ [_ _]]
   {:http-xhrio {:method :get
                 :uri (str config/api-url "/fs/get-examples")
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success [::get-examples-result]
                 :on-failure [::response-failure-notify]}}))

(re-frame/reg-event-fx
 ::open-example
 (fn [_ [_ example_name]]
   {:http-xhrio {:method :get
                 :uri (str config/api-url "/fs/open-example")
                 :params {:example_name example_name}
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success [::open-project-fx]
                 :on-failure [::response-failure-notify]}}))

(re-frame/reg-event-fx
 ::init-sanscript
 (fn [_ _]
   {:http-xhrio {:method :get
                 :uri (str config/api-url "/fs/init")
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success [::init-fx]
                 :on-failure [::response-failure-notify]}}))

(re-frame/reg-event-fx
 ::build
 (fn [_ [_ path]]
   {:http-xhrio {:method :get
                 :uri (str config/api-url "/fs/build")
                 :params {:path path}
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success [::build-fx]
                 :on-failure [::build-failure]}}))

(re-frame/reg-event-fx
 ::flash
 (fn [_ [_ path]]
   {:http-xhrio {:method :get
                 :uri (str config/api-url "/fs/flash")
                 :params {:san_path (:san_path path)
                          :config_path (:config_path path)}
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success [::build-fx]
                 :on-request [::flash-loading]
                 :on-failure [::build-failure]}}))

;cofx
(re-frame/reg-cofx
 ::remove-cached-project
 (fn [_ _]
   (.removeItem js/localStorage "project")))


(defn current-time []
  (let [now (js/Date.)
        hour (.getHours now)
        minute (.getMinutes now)]
    (str (gstring/format "%02d" hour) ":" (gstring/format "%02d" minute))))

;; websockets
(defn irc-msg-handler
  [msg]
  (let [msgsplit (str/split (.-data msg) #" ") msglen (count msgsplit)]
    (case (second msgsplit)
      "001" (do (re-frame/dispatch [::set-irc-connected true])
                (re-frame/dispatch [::set-irc-loading false]))
      "372" (re-frame/dispatch [::add-message {:from "" :type "MOTD" :time (current-time)
                                               :msg (if (> msglen 4) (str/join " " (subvec msgsplit 4 msglen)) " ")}])
      "353" (when (> msglen 6) (re-frame/dispatch [::add-participants (map #(hash-map
                                                                             :name (str/replace % #"[@:]" "")
                                                                             :color [(rand-int 360)
                                                                                     (rand-int 101)
                                                                                     (+ 50 (rand-int 51))])
                                                                           (subvec msgsplit 6 msglen))]))
      "JOIN" (do
               (re-frame/dispatch [::add-participant {:name (subs (first (str/split (first msgsplit) #"!")) 1)
                                                      :color [(rand-int 360) (rand-int 101) (+ 50 (rand-int 51))]}])
               (re-frame/dispatch [::add-message {:from "" :type "JOIN" :time (current-time)
                                                  :msg (str "↳ " (subs (first (str/split (first msgsplit) #"!")) 1) " joined " (last msgsplit))}]))
      "QUIT" (do
               (re-frame/dispatch [::remove-participant (subs (first (str/split (first msgsplit) #"!")) 1)])
               (re-frame/dispatch [::add-message {:from "" :type "QUIT" :time (current-time)
                                                  :msg (str (str "↲ " (subs (first (str/split (first msgsplit) #"!")) 1) " left"))}]))
      "PRIVMSG" (re-frame/dispatch [::add-message {:from (subs (first (str/split (first msgsplit) #"!")) 1)
                                                   :time (current-time) :type "PRIVMSG"
                                                   :msg (subs (str/join " " (subvec msgsplit 3)) 1)}])
      "ERR" (do
              (common/error-toast (str/join " " (subvec msgsplit 2)))
              (re-frame/dispatch [::set-irc-loading false]))
      ())))

(re-frame/reg-cofx
 ::ws-connect-cofx
 (fn [cfx {socket-id :socket-id url :url}]
   (assoc cfx socket-id (ws/create url
                                   {:on-message irc-msg-handler
                                    :on-open #(println "Opening new ws connection")
                                    :on-close #(println "Closing ws connection")}))))

(re-frame/reg-event-fx
 ::ws-connect
 [(re-frame/inject-cofx ::ws-connect-cofx {:socket-id :irc-socket :url config/ws-url})]
 (fn [cofx _]
   {:db (assoc (:db cofx) :ws-socket (:irc-socket cofx))}))

(re-frame/reg-fx
 ::irc-connect-fx
 (fn [options]
   (ws/send (:socket options)
            {:type "connect"
             :server (:server options)
             :port (:port options)
             :username (:username options)
             :channel (:channel options)}
            fmt/json)))

(re-frame/reg-event-fx
 ::irc-connect
 (fn [cofx [_ vals]]
   {::irc-connect-fx {:socket (get-in cofx [:db :ws-socket])
                      :server (:server vals)
                      :port (:port vals)
                      :username (:username vals)
                      :channel (:channel vals)}
    :fx [[:dispatch [::set-server-address (:server vals)]]
         [:dispatch [::set-server-port (:port vals)]]
         [:dispatch [::set-username (:username vals)]]
         [:dispatch [::set-channel (:channel vals)]]
         [:dispatch [::set-irc-loading true]]]}))

(re-frame/reg-fx
 ::irc-disconnect-fx
 (fn [options]
   (ws/send (:socket options)
            {:type "disconnect"}
            fmt/json)))

(re-frame/reg-event-fx
 ::irc-disconnect
 (fn [cofx _]
   {::irc-disconnect-fx {:socket (get-in cofx [:db :ws-socket])}
    :fx [[:dispatch [::set-irc-connected false]]
         [:dispatch [::reset-participants]]
         [:dispatch [::set-server-address ""]]
         [:dispatch [::set-username ""]]
         [:dispatch [::set-channel ""]]
         [:dispatch [::reset-messages]]]}))

(re-frame/reg-fx
 ::irc-send-msg-fx
 (fn [vals]
   (ws/send (:socket vals)
            {:type "send-message"
             :msg (:msg vals)
             :channel (:channel vals)}
            fmt/json)))

(re-frame/reg-fx
 ::cache-loaded-project
 (fn [val]
   (.setItem js/localStorage "project" (.stringify js/JSON (clj->js val)))))

(re-frame/reg-fx
 ::error-notify
 (fn [val]
   (common/error-toast val)))

(re-frame/reg-fx
 ::success-notify
 (fn [val]
   (common/success-toast val)))

(re-frame/reg-event-fx
 ::irc-send-msg
 (fn [cofx [_ msg]]
   {::irc-send-msg-fx {:socket (get-in cofx [:db :ws-socket]) :msg msg
                       :channel (get-in cofx [:db :channel])}
    :fx [[:dispatch [::add-message {:from (get-in cofx [:db :username])
                                    :time (current-time) :type "PRIVMSG"
                                    :msg msg}]]]}))