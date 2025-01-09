(ns sanide-backend.api-test
  (:require [clojure.test :refer :all]
            [ring.mock.request :as mock]
            [sanide-backend.handlers :as handlers]
            [sanide-backend.helpers :as helpers]
            [sanide-backend.config :as config]
            [clojure.java.io :as io]))

(def test-dir "test-project-dir")
(def examples-dir "test-examples")

(use-fixtures :each
  (fn [f]
    (.mkdir (io/file test-dir))
    (.mkdir (io/file examples-dir))
    (f)
    (doseq [file (.listFiles (io/file test-dir))] (.delete file))
    (.delete (io/file test-dir))
    (doseq [file (.listFiles (io/file examples-dir))] (.delete file))
    (.delete (io/file examples-dir))))

(deftest test-new-project
  (let [project-name "new-project"
        req (mock/request :post "/fs/new" {:query {:project_name project-name}})]
    (with-redefs [helpers/dir-picker (fn [] (io/file test-dir))]
      (let [resp (handlers/new-project req)]
        (is (= 200 (:status resp)))
        (is (.exists (io/file test-dir project-name "config.toml")))))))

(deftest test-pick-project
  (spit (io/file test-dir "test.san") "print \"Hello SanUSB\"")
  (spit (io/file test-dir "config.toml") "mode=\"auto\"")
  (let [req (mock/request :get "/fs/open-dialog")]
    (with-redefs [helpers/dir-picker (fn [] (io/file test-dir))]
      (let [resp (handlers/pick-project req)]
        (is (= 200 (:status resp)))
        (is (= "print \"Hello SanUSB\"" (:payload_content (:body resp))))))))

(deftest test-get-examples
  (spit (io/file examples-dir "example1.san") "print \"Example 1\"")
  (spit (io/file examples-dir "config.toml") "mode=\"auto\"")
  (with-redefs [config/examples-path examples-dir]
    (let [req (mock/request :get "/fs/get-examples")
          resp (handlers/get-examples req)]
      (is (= 200 (:status resp)))
      (is (= ["example1.san"] (:body resp))))))

(deftest test-open-example
  (spit (io/file examples-dir "example1.san") "print \"Example 1\"")
  (spit (io/file examples-dir "config.toml") "mode=\"auto\"")
  (with-redefs [config/examples-path examples-dir]
    (let [req (mock/request :post "/fs/open-example" {:query {:example_name "example1.san"}})
          resp (handlers/open-example req)]
      (is (= 200 (:status resp)))
      (is (= "print \"Example 1\"" (:payload_content (:body resp)))))))

(deftest test-open-at-path
  (spit (io/file test-dir "test.san") "print \"Hello SanUSB\"")
  (spit (io/file test-dir "config.toml") "mode=\"auto\"")
  (let [req (mock/request :post "/fs/open-path" {:query {:path test-dir}}) resp (handlers/open-at-path req)]
    (is (= 200 (:status resp)))
    (is (= "print \"Hello SanUSB\"" (:payload_content (:body resp))))))

(deftest test-save-file
  (let [file-path (str test-dir "/file.san")
        req (mock/request :post "/fs/save" {:body {:file_path file-path :content "Updated content"}})]
    (spit file-path "Initial content")
    (let [resp (handlers/save-file req)]
      (is (= 200 (:status resp)))
      (is (= "Updated content" (slurp file-path))))))

(deftest test-build-sanscript
  (let [path (str test-dir "/test.san")
        req (mock/request :post "/fs/build" {:query {:path path}})]
    (spit path "print \"Hello SanUSB\";")
    (let [resp (handlers/build-sanscript req)]
      (is (= 200 (:status resp)))
      (is (= "Build completed successfully" (:msg (:body resp)))))))

(run-tests)
