(ns sanide-backend.routes
  (:require [reitit.ring :as ring]
            [reitit.swagger :as swagger]
            [reitit.swagger-ui :as swagger-ui]
            [sanide-backend.handlers :as handler]
            [muuntaja.core :as m]
            [reitit.coercion.schema :as rcs]
            [reitit.ring.coercion :as rrc]
            [reitit.ring.middleware.muuntaja :as rrmm]
            [ring.middleware.cors :refer [wrap-cors]]
            [schema.core :as s]))

(defn app
  []
  (ring/ring-handler
   (ring/router
    [["/fs"
      ["/new" {:post {:summary "Creates new sanscript project"
                      :parameters {:body {:project_name s/Str}}
                      :handler handler/new-project
                      :responses {200 {:body {:project_path s/Str :payload_content s/Str :payload_name s/Str :config_content s/Str}}}}}]
      ["/open-dialog" {:get {:summary "Opens file dialog for project selection"
                             :handler handler/pick-project
                             :responses {200 {:body {:project_path s/Str :payload_content s/Str :payload_name s/Str :config_content s/Str}}}}}]
      ["/open-example" {:get {:summary "Opens desired example project"
                              :handler handler/open-example
                              :parameters {:body {:project_path s/Str}}
                              :responses {200 {:body {:project_path s/Str :payload_content s/Str :payload_name s/Str :config_content s/Str}}}}}]
      ["/get-examples" {:get {:summary "Returns list of paths for example projects"
                              :handler handler/get-examples
                              :responses {200 {:body [{:example_name s/Str :example_path s/Str}]}}}}]
      ["/save" {:post {:summary "Saves content to the desired file"
                       :parameters {:body {:file_path s/Str :content s/Str}}
                       :handler handler/save-file
                       :responses {200 {:body nil}}}}]]
     ["" {:no-doc true}
      ["/swagger.json" {:get {:no-doc  true
                              :swagger
                              {:info     {:title       "SanIDE API"
                                          :description "This is an implementation of the SanIDE API, using Clojure and Ring/Reitit."
                                          :version     "0.1.0"}}
                              :handler (swagger/create-swagger-handler)}}]
      ["/api-docs/*" {:get (swagger-ui/create-swagger-ui-handler)}]]]
    {:data {:muuntaja   m/instance
            :coercion   rcs/coercion
            :middleware [rrmm/format-middleware
                         rrc/coerce-exceptions-middleware
                         rrc/coerce-response-middleware
                         rrc/coerce-request-middleware
                         [wrap-cors :access-control-allow-origin  #".*"
                          :access-control-allow-methods [:get :put :post :patch :delete]]]}})
   (ring/routes
    (swagger-ui/create-swagger-ui-handler {:path "/"}))
   (ring/create-default-handler
    {:not-found (constantly {:status 404 :body "Not found"})})))