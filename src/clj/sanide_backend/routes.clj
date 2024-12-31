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
            [ring.middleware.params :as params]
            [schema.core :as s]))

(defn app
  []
  (ring/ring-handler
   (ring/router
    [["/ws" {:get {:summary "Initiates websocket connection for IRC client"
                   :handler handler/websocket-handler}}]
     ["/fs"
      ["/new" {:get {:summary "Creates new sanscript project"
                     :parameters {:query {:project_name s/Str}}
                     :handler handler/new-project
                     :responses {200 {:body {:project_path s/Str
                                             :payload_content s/Str
                                             :payload_name s/Str
                                             :config_content s/Str}}}}}]
      ["/open-dialog" {:get {:summary "Opens file dialog for project selection"
                             :handler handler/pick-project
                             :responses {200 {:body {:project_path s/Str
                                                     :payload_content s/Str
                                                     :payload_name s/Str
                                                     :config_content s/Str}}}}}]
      ["/open-path" {:get {:summary "Opens project at desired path"
                           :handler handler/open-at-path
                           :parameters {:query {:path s/Str}}
                           :responses {200 {:body {:project_path s/Str
                                                   :payload_content s/Str
                                                   :payload_name s/Str
                                                   :config_content s/Str}}}}}]
      ["/open-example" {:get {:summary "Opens desired example project"
                              :handler handler/open-example
                              :parameters {:query {:example_name s/Str}}
                              :responses {200 {:body {:project_path s/Str
                                                      :payload_content s/Str
                                                      :payload_name s/Str :config_content s/Str}}}}}]
      ["/get-examples" {:get {:summary "Returns list of paths for example projects"
                              :handler handler/get-examples
                              :responses {200 {:body [s/Str]}}}}]
      ["/save" {:post {:summary "Saves content to the desired file"
                       :parameters {:body {:file_path s/Str :content s/Str}}
                       :handler handler/save-file
                       :responses {200 {:body {:file_path s/Str :content s/Str}}}}}]
      ["/init" {:get {:summary "Initializes SanScript"
                      :handler handler/init-sanscript
                      :responses {200 {}}}}]
      ["/build" {:get {:summary "Compiles provided SanScript project to bytecode"
                       :parameters {:query {:path s/Str}}
                       :handler handler/build-sanscript
                       :responses {200 {}}}}]
      ["/flash" {:get {:summary "Flashes provided SanScript project to SanUSB"
                       :parameters {:query {:path s/Str}}
                       :handler handler/flash-sanscript
                       :responses {200 {}}}}]]
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
            :middleware [params/wrap-params
                         rrmm/format-middleware
                         rrc/coerce-exceptions-middleware
                         rrc/coerce-response-middleware
                         rrc/coerce-request-middleware
                         [wrap-cors :access-control-allow-origin  #".*"
                          :access-control-allow-methods [:get :put :post :patch :delete]]]}})
   (ring/create-default-handler
    {:not-found (constantly {:status 404 :body "Not found"})})))