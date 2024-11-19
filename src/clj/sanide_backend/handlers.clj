(ns sanide-backend.handlers
  (:require [ring.util.http-response :as response]
            [sanide-backend.helpers :as helpers]
            [sanide-backend.config :as config]))

(defn new-project [{{{:keys [project_name]} :body} :parameters}]
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

(defn get-examples [_]
  (let [examples (reduce #(if (helpers/is-san-project? (java.io.File. (str config/examples_path "/" %2)))
                            (conj %1 %2)
                            %1)
                         []
                         (helpers/get-filenames (java.io.File. config/examples_path)))]
    (response/ok examples)))

(defn open-example [_]
  (response/ok))

(defn save-file [_]
  (response/ok))