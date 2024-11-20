(ns sanide-frontend.db)

(def default-db
  {:name "re-frame"
   :active-item :editor
   :project-path (js->clj (.getItem js/localStorage "project-path"))})
