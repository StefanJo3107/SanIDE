(ns sanide-backend.handlers
  (:require [ring.util.http-response :as response]
            [sanide-backend.helpers :as helpers]
            [sanide-backend.config :as config]
            [sanide-backend.irc-client :as irc]
            [org.httpkit.server :as hk]
            [taoensso.timbre :as log]
            [babashka.process :as bb]
            [clojure.java.io :as io]
            [clojure.data.json :as json]
            [clojure.string :as string]))

;; TODO convert to clojure.java.io.file
(defn new-project [{{{:keys [project_name]} :query} :parameters}]
  (let [parent-dir-path (helpers/get-file-path (helpers/dir-picker))]
    (helpers/create-project parent-dir-path project_name)
    (let [project-path (str parent-dir-path "/" project_name) project-dir (java.io.File. project-path)]
      (response/ok {:project_path project-path
                    :payload_content (helpers/read-file-content project-dir (str project_name ".san"))
                    :payload_name (str project_name ".san")
                    :config_content (helpers/read-file-content project-dir "config.toml")}))))

(defn pick-project [_]
  (let [dir (helpers/dir-picker)]
    (if (helpers/is-san-project? dir)
      (response/ok {:project_path (helpers/get-file-path dir)
                    :payload_content (helpers/read-file-content dir (helpers/get-file-with-extension dir ".san"))
                    :payload_name (helpers/get-file-with-extension dir ".san")
                    :config_content (helpers/read-file-content dir "config.toml")})
      (response/bad-request))))

;; TODO convert to clojure.java.io.file
(defn get-examples [_]
  (let [examples (reduce #(if (helpers/is-san-project? (java.io.File. (str config/examples-path "/" %2)))
                            (conj %1 %2)
                            %1)
                         []
                         (helpers/get-filenames (java.io.File. config/examples-path)))]
    (response/ok examples)))

;; TODO convert to clojure.java.io.file
(defn open-example [{{{:keys [example_name]} :query} :parameters}]
  (let [example-dir-path (str config/examples-path "/" example_name) example-dir (java.io.File. example-dir-path)]
    (if (helpers/is-san-project? example-dir)
      (response/ok {:project_path example-dir-path
                    :payload_content (helpers/read-file-content example-dir (helpers/get-file-with-extension example-dir ".san"))
                    :payload_name (helpers/get-file-with-extension example-dir ".san")
                    :config_content (helpers/read-file-content example-dir "config.toml")})
      (response/bad-request))))

;; TODO convert to clojure.java.io.file
(defn open-at-path [{{{:keys [path]} :query} :parameters}]
  (let [dir (java.io.File. path)]
    (if (helpers/is-san-project? dir)
      (response/ok {:project_path path
                    :payload_content (helpers/read-file-content dir (helpers/get-file-with-extension dir ".san"))
                    :payload_name (helpers/get-file-with-extension dir ".san")
                    :config_content (helpers/read-file-content dir "config.toml")})
      (response/bad-request))))

(defn save-file [{{{:keys [file_path content]} :body} :parameters}]
  (if (helpers/file-exists? file_path)
    (do
      (helpers/create-file file_path content)
      (response/ok {:file_path file_path :content content}))
    (response/bad-request)))


(defn init-sanscript []
  (let [santool-name (last (.split config/santool-url "/"))]
    (if-not (.exists  (io/as-file santool-name))
      (try
        (with-open [in (io/input-stream config/santool-url)
                    out (io/output-stream santool-name)]
          (io/copy in out)
          (log/info  santool-name " has been downloaded."))
        (bb/shell "chmod" "+x" "santool")
        (bb/shell "./santool" "download")
        (response/ok)
        (catch Exception e
          (str "caught exception:" (.getMessage e))
          (bb/shell "rm" santool-name)
          (response/bad-request {:err e})))
      (do
        (print santool-name "is already initialized")
        (response/bad-request {:err "Already initialized"})))))

(defn build-sanscript [{{{:keys [path]} :query} :parameters}]
  (try
    (let [compile-result (bb/sh "./santool" "compile" "--source-path" path)]
      (if (string/includes? (:out compile-result) "success")
        (response/ok {:res (:out compile-result)})
        (response/bad-request {:err (:out compile-result)})))
    (catch Exception e (str "caught exception:" (.getMessage e))
           (response/bad-request {:err e}))))

(defn flash-sanscript [{{{:keys [path]} :query} :parameters}]
  (try
    (let [flash-result (bb/sh "./santool" "flash" "--config-path" path)]
      ;TODO check what is the error message for flashing process
      (if (string/includes? (:out flash-result) "success")
        (response/ok {:res (:out flash-result)})
        (response/bad-request {:err (:out flash-result)})))
    (catch Exception e (str "caught exception:" (.getMessage e))
           (response/bad-request {:err e}))))

(def channels (atom #{}))

(defn websocket-on-open [ch]
  (swap! channels conj ch)
  (dosync (alter irc/irc-conn merge {:exit false})))

(defn websocket-on-close [ch _]
  (swap! channels disj ch)
  (when (not (nil? (:socket @irc/irc-conn))) (.close (:socket @irc/irc-conn)))
  (dosync (alter irc/irc-conn merge {:exit true})))

(defn websocket-on-receive [ch message]
  (let [message-json (json/read-str message :key-fn keyword)]
    (log/info message-json)
    (case (:type message-json)
      "connect" (irc/init ch (:server message-json) (:port message-json) (:username message-json) (:channel message-json))
      "get-participants" (irc/participants (:channel message-json))
      "send-message" (irc/privmsg (:channel message-json) (:msg message-json))
      (log/info "No matching clause"))))

(defn websocket-handler [req]
  (hk/as-channel req
                 {:on-open websocket-on-open
                  :on-close websocket-on-close
                  :on-receive websocket-on-receive}))