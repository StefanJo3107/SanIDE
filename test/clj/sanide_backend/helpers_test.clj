(ns sanide-backend.helpers-test
  (:require [clojure.java.io :as io]
            [sanide-backend.helpers :as helpers]
            [clojure.test :as t]))

(t/deftest test-get-file-path
  (let [file (java.io.File. "/tmp/test-file")]
    (t/is (= "/tmp/test-file" (helpers/get-file-path file)))))

(t/deftest test-get-filenames
  (let [temp-dir (io/file "test-dir")]
    (.mkdir temp-dir)
    (spit (io/file temp-dir "file1.txt") "Content 1")
    (spit (io/file temp-dir "file2.san") "Content 2")
    (let [files (helpers/get-filenames temp-dir)]
      (t/is (= #{"file1.txt" "file2.san"} files)))
    (io/delete-file (io/file temp-dir "file1.txt"))
    (io/delete-file (io/file temp-dir "file2.san"))
    (.delete temp-dir)))

(t/deftest test-get-file-with-extension
  (let [temp-dir (io/file "test-dir")]
    (.mkdir temp-dir)
    (spit (io/file temp-dir "file1.txt") "Content 1")
    (spit (io/file temp-dir "file2.san") "Content 2")
    (t/is (= "file2.san" (helpers/get-file-with-extension temp-dir ".san")))
    (io/delete-file (io/file temp-dir "file1.txt"))
    (io/delete-file (io/file temp-dir "file2.san"))
    (.delete temp-dir)))

(t/deftest test-is-san-project?
  (let [temp-dir (io/file "test-dir")]
    (.mkdir temp-dir)
    (spit (io/file temp-dir "file1.san") "Content 1")
    (spit (io/file temp-dir "config.toml") "mode=\"auto\"")
    (t/is (helpers/is-san-project? temp-dir))
    (io/delete-file (io/file temp-dir "file1.san"))
    (io/delete-file (io/file temp-dir "config.toml"))
    (.delete temp-dir)))


(t/deftest test-read-file-content
  (let [temp-dir (io/file "test-dir")]
    (.mkdir temp-dir)
    (spit (io/file temp-dir "file1.txt") "Hello World")
    (t/is (= "Hello World" (helpers/read-file-content temp-dir "file1.txt")))
    (io/delete-file (io/file temp-dir "file1.txt"))
    (.delete temp-dir)))

(t/deftest test-create-dir
  (let [temp-dir-path "test-create-dir"]
    (helpers/create-dir temp-dir-path)
    (t/is (.exists (io/file temp-dir-path)))
    (.delete (io/file temp-dir-path))))

(t/deftest test-create-file
  (let [temp-file-path "test-create-file.txt"]
    (helpers/create-file temp-file-path "Test Content")
    (t/is (= "Test Content" (slurp temp-file-path)))
    (io/delete-file temp-file-path)))


(t/deftest test-create-project
  (let [temp-dir-path "helper-test-dir"
        project-name "test-project"]
    (.mkdir (io/file temp-dir-path))
    (helpers/create-project temp-dir-path project-name)
    (t/is (.exists (io/file temp-dir-path project-name)))
    (t/is (.exists (io/file temp-dir-path project-name (str project-name ".san"))))
    (t/is (.exists (io/file temp-dir-path project-name "config.toml")))
    (io/delete-file (io/file temp-dir-path project-name (str project-name ".san")))
    (io/delete-file (io/file temp-dir-path project-name "config.toml"))
    (.delete (io/file temp-dir-path project-name))
    (.delete (io/file temp-dir-path))))


(t/deftest test-file-exists?
  (let [temp-file-path "test-file-exists.txt"]
    (spit temp-file-path "Test Content")
    (t/is (helpers/file-exists? temp-file-path))
    (io/delete-file temp-file-path)))