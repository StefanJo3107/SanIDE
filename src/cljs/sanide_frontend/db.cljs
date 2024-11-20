(ns sanide-frontend.db)

(def default-db
  {:name "re-frame"
   :active-item :editor
   :project (js->clj (.getItem js/localStorage "project"))
   :examples (js->clj (.getItem))})
