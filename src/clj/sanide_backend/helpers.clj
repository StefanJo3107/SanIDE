(ns sanide-backend.helpers
  (:require [clojure.string :as str]
            [taoensso.timbre :as log])
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
  (first (filter #(str/ends-with? % ext) (get-filenames dir))))

(defn is-san-project? [dir]
  (if (and (some #(str/includes? % ".san") (get-filenames dir)) (some #(= "config.toml" %) (get-filenames dir)))
    true
    false))

(defn read-file-content [dir filename]
  (slurp (str (get-file-path dir) "/" filename)))

(defn create-dir [dir-path]
  (.mkdir (java.io.File. dir-path))
  (log/info (str "Create directory at path: " dir-path)))

(defn create-file [file-path content]
  (spit file-path content)
  (log/info (str "Created file at path: " file-path)))

(defn create-project [parent-dir-path project_name]
  (let [dir (str parent-dir-path "/" project_name)
        payload (str dir "/" project_name ".san")
        config (str dir "/" "config.toml")]
    (create-dir dir)
    (create-file payload "print \"Hello SanUSB\"")
    (create-file config "mode=\"auto\"")))

(defn file-exists? [file-path]
  (.exists (java.io.File. file-path)))