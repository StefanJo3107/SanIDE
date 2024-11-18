(ns sanide-backend.handlers
  (:require [ring.util.http-response :as response]))

(defn pick-project [_]

  (response/ok))

(defn open-example [_]

  (response/ok))

(defn save-file [_]
  (response/ok))

(defn get-examples [_]
  (response/ok))