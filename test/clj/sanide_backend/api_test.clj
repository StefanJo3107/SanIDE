(ns sanide-backend.api-test
  (:use clojure.test)
  (:require [ring.mock.request :as mock]
            [sanide-backend.handlers :as handlers]
            [test-with-files.tools :as twf]
            [clojure.java.io :as io]))

(deftest api-tests
  (is (= (handlers/get-examples (mock/request :get "/fs/get-examples")) {:body ["sanscript_example1" "sanscript_example2"]
                                                                         :headers {} :status 200}))
  (twf/with-files tmp-proj ["tmp.san" "print \"Hello\""
                            "config.toml" "mode=\"auto\""]
    (is (= {:status 200
            :body {:config_content "mode=\"auto\""
                   :payload_content "print \"Hello\""
                   :payload_name "tmp.san"
                   :project_path tmp-proj}
            :headers {}}
           (handlers/open-at-path (assoc (mock/request :get (str "/fs/open-path?path=" tmp-proj)) :parameters
                                         {:query {:path tmp-proj}}))))))
