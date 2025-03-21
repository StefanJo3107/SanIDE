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

(defn new-project [{{{:keys [project_name]} :query} :parameters}]
  (try
    (let [parent-dir-path (helpers/get-file-path (helpers/dir-picker))]
      (helpers/create-project parent-dir-path project_name)
      (let [project-path (str parent-dir-path "/" project_name) project-dir (java.io.File. project-path)]
        (response/ok {:project_path project-path
                      :payload_content (helpers/read-file-content project-dir (str project_name ".san"))
                      :payload_name (str project_name ".san")
                      :config_content (helpers/read-file-content project-dir "config.toml")})))
    (catch Exception e
      (str "caught exception:" (.getMessage e))
      (response/bad-request {:err "An error occurred while trying to create new SanScript project"
                             :exc (.getMessage e)}))))

(defn pick-project [_]
  (try
    (let [dir (helpers/dir-picker)]
      (if (helpers/is-san-project? dir)
        (response/ok {:project_path (helpers/get-file-path dir)
                      :payload_content (helpers/read-file-content dir (helpers/get-file-with-extension dir ".san"))
                      :payload_name (helpers/get-file-with-extension dir ".san")
                      :config_content (helpers/read-file-content dir "config.toml")})
        (response/bad-request {:err "Path doesn't contain SanScript project"})))
    (catch Exception e
      (str "caught exception:" (.getMessage e))
      (response/bad-request {:err "An error occurred while trying to open SanScript project"
                             :exc (.getMessage e)}))))

(defn get-examples [_]
  (try
    (let [examples (reduce #(if (helpers/is-san-project? (java.io.File. (str config/examples-path "/" %2)))
                              (conj %1 %2)
                              %1)
                           []
                           (helpers/get-filenames (java.io.File. config/examples-path)))]
      (response/ok examples))
    (catch Exception e
      (str "caught exception:" (.getMessage e))
      (response/bad-request {:err "An error occurred while trying to get example projects"
                             :exc (.getMessage e)}))))

(defn open-example [{{{:keys [example_name]} :query} :parameters}]
  (try
    (let [example-dir-path (str config/examples-path "/" example_name) example-dir (java.io.File. example-dir-path)]
      (if (helpers/is-san-project? example-dir)
        (response/ok {:project_path example-dir-path
                      :payload_content (helpers/read-file-content example-dir (helpers/get-file-with-extension example-dir ".san"))
                      :payload_name (helpers/get-file-with-extension example-dir ".san")
                      :config_content (helpers/read-file-content example-dir "config.toml")})
        (response/bad-request {:err "Path doesn't contain SanScript project"})))
    (catch Exception e
      (str "caught exception:" (.getMessage e))
      (response/bad-request {:err "An error occurred while trying to open example project"
                             :exc (.getMessage e)}))))

(defn open-at-path [{{{:keys [path]} :query} :parameters}]
  (try
    (let [dir (java.io.File. path)]
      (if (helpers/is-san-project? dir)
        (response/ok {:project_path path
                      :payload_content (helpers/read-file-content dir (helpers/get-file-with-extension dir ".san"))
                      :payload_name (helpers/get-file-with-extension dir ".san")
                      :config_content (helpers/read-file-content dir "config.toml")})
        (response/bad-request {:err "Path doesn't contain SanScript project"})))
    (catch Exception e
      (str "caught exception:" (.getMessage e))
      (response/bad-request {:err "An error occurred while trying to open SanScript project"
                             :exc e}))))

(defn save-file [{{{:keys [file_path content]} :body} :parameters}]
  (try
    (if (helpers/file-exists? file_path)
      (do
        (helpers/create-file file_path content)
        (response/ok {:file_path file_path :content content}))
      (response/bad-request {:err "Path doesn't contain SanScript project"}))
    (catch Exception e
      (str "caught exception:" (.getMessage e))
      (response/bad-request {:err "An error occurred while trying to save file"
                             :exc (.getMessage e)}))))


(defn init-sanscript [_]
  (let [santool-name (last (.split config/santool-url "/"))]
    (if-not (.exists  (io/as-file santool-name))
      (try
        (with-open [in (io/input-stream config/santool-url)
                    out (io/output-stream santool-name)]
          (io/copy in out)
          (log/info  santool-name " has been downloaded."))
        (bb/shell "chmod" "+x" "santool")
        (bb/shell "./santool" "download")
        (bb/shell "chmod" "+x" "san_vm")
        (bb/shell "chmod" "+x" "san_compiler")
        (response/ok {:msg "Successfully initialized SanScript"})
        (catch Exception e
          (str "caught exception:" (.getMessage e))
          (bb/shell "rm" santool-name)
          (response/bad-request {:err "An error occurred while trying to initialize SanScript"
                                 :exc (.getMessage e)})))
      (response/ok {:msg "SanScript is already initialized"}))))

(defn build-sanscript [{{{:keys [path]} :query} :parameters}]
  (try
    (let [compile-result (bb/sh "./santool" "compile" "--source-path" path)]
      (helpers/create-file "build.log" (:out compile-result))
      (if (string/includes? (:out compile-result) "success")
        (response/ok {:msg "Build completed successfully"
                      :res (:out compile-result)})
        (response/bad-request {:err "Build failed"
                               :exc (:out compile-result)})))
    (catch Exception e (str "caught exception:" (.getMessage e))
           (helpers/create-file "build.log" e)
           (response/bad-request {:err "An error occurred while trying to build SanScript project"
                                  :exc (.getMessage e)}))))

(defn flash-sanscript [{{{:keys [san_path config_path]} :query} :parameters}]
  (try
    (let [flash-result (bb/sh "./santool" "flash" "-s" san_path "-c" config_path)]
      (helpers/create-file "flash.log" (str (:out flash-result) (:err flash-result)))
      (cond
        (string/includes? (:err flash-result) "Error: espflash::no_serial")
        (response/bad-request {:err "Flash failed" :exc (str "Error: " (last (.split (:err flash-result) "Error:")))})
        (string/includes? (:out flash-result) "failed to compile") (response/bad-request {:err "Build failed" :exc (:out flash-result)})
        (string/includes? (:err flash-result) "Flashing has completed") (response/ok {:msg "Flash completed successfully"
                                                                                      :res (str (:out flash-result) (:err flash-result))})
        :else (response/bad-request {:err "Flash failed"
                                     :exc "Unknown error occurred, check flash.log for more info..."})))
    (catch Exception e (str "caught exception:" (.getMessage e))
           (helpers/create-file "flash.log" e)
           (response/bad-request {:err "An error occurred while trying to flash SanScript project"
                                  :exc (.getMessage e)}))))

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
      "disconnect" (irc/disconnect)
      "get-participants" (irc/participants (:channel message-json))
      "send-message" (irc/privmsg (:channel message-json) (:msg message-json))
      (log/info "No matching clause"))))

(defn websocket-handler [req]
  (hk/as-channel req
                 {:on-open websocket-on-open
                  :on-close websocket-on-close
                  :on-receive websocket-on-receive}))