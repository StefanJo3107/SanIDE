(ns sanide-frontend.events
  (:require
   [re-frame.core :as re-frame]
   [sanide-frontend.db :as db]
   [sanide-frontend.config :as config]
   [day8.re-frame.http-fx]
   [ajax.core :as ajax]
   [clojure.string :as str]
   [wscljs.client :as ws]
   [wscljs.format :as fmt]))

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
   (update db :messages conj val)))

(re-frame/reg-event-db
 ::add-participant
 (fn [db [_ val]]
   (update db :participants conj val)))

(re-frame/reg-event-db
 ::set-irc-connected
 (fn [db [_ val]]
   (assoc db :irc-connected val)))

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

(re-frame/reg-event-db
 ::save-project-failure
 (fn [db [_ fail]]
   (assoc db :save-project-failure fail)))

(re-frame/reg-event-db
 ::get-examples-result
 (fn [db [_ result]]
   (assoc db :examples result)))

(re-frame/reg-event-db
 ::get-examples-failure
 (fn [db [_ fail]]
   (assoc db :get-examples-failure fail)))

(re-frame/reg-event-db
 ::open-example-failure
 (fn [db [_ fail]]
   (assoc db :open-example-failure fail)))

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
    :fx [[:dispatch [::cache-loaded-project result]]]}))

(re-frame/reg-event-fx
 ::save-project-fx
 (fn [cofx [_ result]]
   (let [file_name (last (str/split (:file_path result) #"/"))]
     {:db (update-in (:db cofx) [:project] assoc
                     (if (= file_name "config.toml") :config_content :payload_content) (:content result))})))

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

(re-frame/reg-event-fx
 ::save-file
 (fn [_ [_ file]]
   {:http-xhrio {:method :post
                 :uri (str config/api-url "/fs/save")
                 :params file
                 :format (ajax/json-request-format)
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success [::save-project-fx]
                 :on-failure [::save-project-failure]}}))

(re-frame/reg-event-fx
 ::get-examples
 (fn [_ [_ _]]
   {:http-xhrio {:method :get
                 :uri (str config/api-url "/fs/get-examples")
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success [::get-examples-result]
                 :on-failure [::get-examples-failure]}}))

(re-frame/reg-event-fx
 ::open-example
 (fn [_ [_ example_name]]
   {:http-xhrio {:method :get
                 :uri (str config/api-url "/fs/open-example")
                 :params {:example_name example_name}
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success [::open-project-fx]
                 :on-failure [::open-example-failure]}}))

;cofx
(re-frame/reg-cofx
 ::remove-cached-project
 (fn [_ _]
   (.removeItem js/localStorage "project")))

;; websockets
(defn irc-msg-handler
  [msg]
  (println (.-data msg))
  (let [msgsplit (str/split (.-data msg) #" ") msglen (count msgsplit)]
    (case (second msgsplit)
      "001" (re-frame/dispatch [::set-irc-connected true])
      "372" (re-frame/dispatch [::add-message {:from "#channel" :type "MOTD"
                                               :msg (if (> msglen 4) (str/join " " (subvec msgsplit 4 msglen)) "\n")}])
      "JOIN" (do
               (re-frame/dispatch [::add-participant (subs (first (str/split (first msgsplit) #"!")) 1)])
               (re-frame/dispatch [::add-message {:from "#channel" :type "JOIN" :msg (str "JOIN " (last msgsplit))}]))
      "PRIVMSG" (re-frame/dispatch [::add-message {:from (subs (str/split (first msgsplit) #"!") 1)
                                                   :type "PRIVMSG" :msg (str/join " " (subvec msgsplit 3))}])
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
                      :channel (:channel vals)}}))