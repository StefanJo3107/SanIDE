(ns sanide-backend.api-test
  (:require [clojure.test :as t]
            [ring.mock.request :as mock]
            [sanide-backend.handlers :as handlers]
            [sanide-backend.helpers :as helpers]
            [sanide-backend.config :as config]
            [clojure.java.io :as io]))

(def test-dir "test-project-dir")
(def examples-dir "test-examples")

(defn delete-directory-recursive
  "Recursively delete a directory."
  [^java.io.File file]
  (when (.isDirectory file)
    (run! delete-directory-recursive (.listFiles file)))
  (io/delete-file file))

(t/use-fixtures :each
  (fn [f]
    (.mkdir (io/file test-dir))
    (.mkdir (io/file examples-dir))
    (.mkdir (io/file (str examples-dir "/example1")))
    (f)
    (delete-directory-recursive (io/file test-dir))
    (delete-directory-recursive (io/file examples-dir))))

(t/deftest test-new-project
  (let [project-name "new-project"
        req (assoc (mock/request :post "/fs/new") :parameters {:query {:project_name project-name}})]
    (with-redefs [helpers/dir-picker (fn [] (io/file test-dir))]
      (let [resp (handlers/new-project req)]
        (t/is (= 200 (:status resp)))
        (t/is (.exists (io/file test-dir project-name "config.toml")))))))

(t/deftest test-pick-project
  (spit (io/file test-dir "test.san") "print \"Hello SanUSB\"")
  (spit (io/file test-dir "config.toml") "mode=\"auto\"")
  (let [req (mock/request :get "/fs/open-dialog")]
    (with-redefs [helpers/dir-picker (fn [] (io/file test-dir))]
      (let [resp (handlers/pick-project req)]
        (t/is (= 200 (:status resp)))
        (t/is (= "print \"Hello SanUSB\"" (:payload_content (:body resp))))))))

(t/deftest test-get-examples
  (spit (io/file (str examples-dir "/example1") "example1.san") "print \"Example 1\"")
  (spit (io/file (str examples-dir "/example1") "config.toml") "mode=\"auto\"")
  (with-redefs [config/examples-path examples-dir]
    (let [req (mock/request :get "/fs/get-examples")
          resp (handlers/get-examples req)]
      (t/is (= 200 (:status resp)))
      (t/is (= ["example1"] (:body resp))))))

(t/deftest test-open-example
  (spit (io/file (str examples-dir "/example1") "example1.san") "print \"Example 1\"")
  (spit (io/file (str examples-dir "/example1") "config.toml") "mode=\"auto\"")
  (with-redefs [config/examples-path examples-dir]
    (let [req (assoc (mock/request :post "/fs/open-example") :parameters {:query {:example_name "example1"}})
          resp (handlers/open-example req)]
      (t/is (= 200 (:status resp)))
      (t/is (= "print \"Example 1\"" (:payload_content (:body resp)))))))

(t/deftest test-open-at-path
  (spit (io/file test-dir "test.san") "print \"Hello SanUSB\"")
  (spit (io/file test-dir "config.toml") "mode=\"auto\"")
  (let [req (assoc (mock/request :post "/fs/open-path") :parameters {:query {:path test-dir}}) resp (handlers/open-at-path req)]
    (t/is (= 200 (:status resp)))
    (t/is (= "print \"Hello SanUSB\"" (:payload_content (:body resp))))))

(t/deftest test-save-file
  (let [file-path (str test-dir "/file.san")
        req (assoc (mock/request :post "/fs/save") :parameters {:body {:file_path file-path :content "Updated content"}})]
    (spit file-path "Initial content")
    (let [resp (handlers/save-file req)]
      (t/is (= 200 (:status resp)))
      (t/is (= "Updated content" (slurp file-path))))))

(t/deftest test-build-sanscript
  (let [path (str test-dir "/test.san")
        req (assoc (mock/request :post "/fs/build") :parameters {:query {:path path}})]
    (spit path "print \"Hello SanUSB\";")
    (let [resp (handlers/build-sanscript req)]
      (t/is (= 200 (:status resp)))
      (t/is (= "Build completed successfully" (:msg (:body resp)))))))

(t/run-tests)
