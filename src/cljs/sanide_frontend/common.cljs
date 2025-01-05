(ns sanide-frontend.common
  (:require ["react-hot-toast$default" :as toast]))

(defn error-toast [msg]
  (toast msg (clj->js {:icon "❎"
                       :className "error-toast"})))

(defn success-toast [msg]
  (toast msg (clj->js {:icon "✅"
                       :className "success-toast"})))