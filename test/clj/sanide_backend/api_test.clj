(ns sanide-backend.api-test
  (:require [ring.mock.request :as mock]
            [sanide-backend.handlers :as handlers]
            [test-with-files.tools :as twf]
            [clojure.java.io :as io]
            [clojure.test :as t]))

(t/deftest api-tests
  (t/is (= (handlers/get-examples (mock/request :get "/fs/get-examples")) {:body ["sanscript_example1" "sanscript_example2"]
                                                                           :headers {} :status 200}))
  (twf/with-files tmp-proj ["tmp.san" "print \"Hello\""
                            "config.toml" "mode=\"auto\""]
    (t/is (= {:status 200
              :body {:config_content "mode=\"auto\""
                     :payload_content "print \"Hello\""
                     :payload_name "tmp.san"
                     :project_path tmp-proj}
              :headers {}}
             (handlers/open-at-path (assoc (mock/request :get (str "/fs/open-path?path=" tmp-proj)) :parameters
                                           {:query {:path tmp-proj}})))))
  (twf/with-files tmp-proj ["tmp.san" "print \"Hello\""
                            "config.toml" "mode=\"auto\""]
    (t/is (= {:status 200
              :body {:file_path (str tmp-proj "/tmp.san")
                     :content "print \"Hello World\""}
              :headers {}}
             (handlers/save-file (assoc (-> (mock/request :post "/fs/save")
                                            (mock/json-body {:file_path (str tmp-proj "/tmp.san")
                                                             :content "print \"Hello World\""}))
                                        :parameters {:body {:file_path (str tmp-proj "/tmp.san")
                                                            :content "print \"Hello World\""}}))))))