(ns sanide-backend.handlers
  (:require [ring.util.http-response :as response]
            [taoensso.timbre :as log]
            [clojure.string :as str])
  (:import [javax.swing JFileChooser]))

(defn dir-picker []
  (let [chooser (JFileChooser.)]
    (.setFileSelectionMode chooser JFileChooser/DIRECTORIES_ONLY)
    (if (= (.showOpenDialog chooser nil) JFileChooser/APPROVE_OPTION)
      (.getSelectedFile chooser)
      nil)))

(defn get-file-path [file]
  (.getAbsolutePath file))

(defn get-filenames [dir]
  (if (.isDirectory dir)
    (set (map #(.getName %) (.listFiles dir)))
    nil))

(defn get-file-with-extension [dir ext]
  (first (filter #(str/includes? % ext) (get-filenames dir))))

(defn is-san-project? [dir]
  (if (and (some #(str/includes? % ".san") (get-filenames dir)) (some #(= "config.toml" %) (get-filenames dir)))
    true
    false))

(defn read-file-content [dir filename]
  (slurp (str (get-file-path dir) "/" filename)))

;; ------------ HANDLERS ------------ 

(defn new-project [_])

(defn pick-project [_]
  (let [dir (dir-picker)]
    (if (is-san-project? dir)
      (response/ok {:project_path (get-file-path dir)
                    :payload_content (read-file-content dir (get-file-with-extension dir ".san"))
                    :payload_name (get-file-with-extension dir ".san")
                    :config_content (read-file-content dir "config.toml")})
      (response/bad-request))))

(defn open-example [_]

  (response/ok))

(defn save-file [_]
  (response/ok))

(defn get-examples [_]
  (response/ok))