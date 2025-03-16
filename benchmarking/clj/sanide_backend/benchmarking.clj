(ns sanide-backend.benchmarking
  (:require [clojure.java.io :as io]
            [sanide-backend.helpers :as helpers]
            [criterium.core :as crit]
            [taoensso.timbre :as log]))

(defn bench-create-project []
  (log/info "Running create project benchmark")
  (let [temp-dir-path "helper-test-dir"
        project-name "test-project"]
    (.mkdir (io/file temp-dir-path))
    (log/with-min-level :error (let [bench-out (with-out-str (crit/quick-bench (helpers/create-project temp-dir-path project-name)))]
                                 (spit "./benchmarking/clj/sanide_backend/bench-results/bench-create-project.txt" bench-out)))
    (io/delete-file (io/file temp-dir-path project-name (str project-name ".san")))
    (io/delete-file (io/file temp-dir-path project-name "config.toml"))
    (.delete (io/file temp-dir-path project-name))
    (.delete (io/file temp-dir-path))))

(defn bench-create-file []
  (log/info "Running create file benchmark")
  (let [temp-file-path "test-create-file.txt"]
    (log/with-min-level :error (let [bench-out (with-out-str (crit/quick-bench (helpers/create-file temp-file-path "Test Content")))]
                                 (spit "./benchmarking/clj/sanide_backend/bench-results/bench-create-file.txt" bench-out)))
    (io/delete-file temp-file-path)))

(defn bench-create-files []
  (log/info "Running create files benchmark")
  (let [file-prefix "test-create-file-"
        file-count 100
        file-paths (map #(str file-prefix % ".txt") (range file-count))]
    (log/with-min-level :error (let [bench-out (with-out-str (crit/quick-bench (doseq [path file-paths]
                                                                                 (helpers/create-file path "Test Content"))))]
                                 (spit "./benchmarking/clj/sanide_backend/bench-results/bench-create-files.txt" bench-out)))
    (doseq [path file-paths]
      (io/delete-file path))))

(defn bench-create-dir []
  (log/info "Running create dir benchmark")
  (let [temp-dir-path "test-create-dir"]
    (log/with-min-level :error (let [bench-out (with-out-str (crit/quick-bench (helpers/create-dir temp-dir-path)))]
                                 (spit "./benchmarking/clj/sanide_backend/bench-results/bench-create-dir.txt" bench-out)))
    (.delete (io/file temp-dir-path))))

(defn bench-create-dirs []
  (log/info "Running create dirs benchmark")
  (let [dir-prefix "test-create-dir-"
        dir-count 100
        dir-paths (map #(str dir-prefix %) (range dir-count))]
    (log/with-min-level :error (let [bench-out (with-out-str (crit/quick-bench (doseq [path dir-paths]
                                                                                 (helpers/create-dir path))))]
                                 (spit "./benchmarking/clj/sanide_backend/bench-results/bench-create-dirs.txt" bench-out)))
    (doseq [path dir-paths]
      (.delete (io/file path)))))

(defn bench-read-file-content []
  (log/info "Running read file content benchmark")
  (let [temp-dir (io/file "test-dir")]
    (.mkdir temp-dir)
    (spit (io/file temp-dir "file1.txt") "Hello World")
    (log/with-min-level :error (let [bench-out (with-out-str (crit/quick-bench (helpers/read-file-content temp-dir "file1.txt")))]
                                 (spit "./benchmarking/clj/sanide_backend/bench-results/bench-read-file-content.txt" bench-out)))
    (io/delete-file (io/file temp-dir "file1.txt"))
    (.delete temp-dir)))

(defn bench-read-large-file-content []
  (log/info "Running read large file content benchmark")
  (let [temp-dir (io/file "test-dir")
        temp-file (io/file temp-dir "large-file.txt")
        large-content (apply str (repeat 1000000 "A"))]
    (.mkdir temp-dir)
    (spit temp-file large-content)
    (log/with-min-level :error (let [bench-out (with-out-str (crit/quick-bench (helpers/read-file-content temp-dir "large-file.txt")))]
                                 (spit "./benchmarking/clj/sanide_backend/bench-results/bench-read-large-file-content.txt" bench-out)))
    (io/delete-file temp-file)
    (.delete temp-dir)))

(defn bench-is-san-project? []
  (log/info "Running is san project benchmark")
  (let [temp-dir (io/file "test-dir")]
    (.mkdir temp-dir)
    (spit (io/file temp-dir "file1.san") "Content 1")
    (spit (io/file temp-dir "config.toml") "mode=\"auto\"")
    (log/with-min-level :error (let [bench-out (with-out-str (crit/quick-bench (helpers/is-san-project? temp-dir)))]
                                 (spit "./benchmarking/clj/sanide_backend/bench-results/bench-is-san-project.txt" bench-out)))
    (io/delete-file (io/file temp-dir "file1.san"))
    (io/delete-file (io/file temp-dir "config.toml"))
    (.delete temp-dir)))

(defn bench-get-file-path []
  (log/info "Running get file path benchmark")
  (let [file (java.io.File. "/tmp/test-file")]
    (log/with-min-level :error (let [bench-out (with-out-str (crit/quick-bench (helpers/get-file-path file)))]
                                 (spit "./benchmarking/clj/sanide_backend/bench-results/bench-get-file-path.txt" bench-out)))))

(defn bench-get-filenames []
  (log/info "Running get filenames benchmark")
  (let [temp-dir (io/file "test-dir")]
    (.mkdir temp-dir)
    (spit (io/file temp-dir "file1.txt") "Content 1")
    (spit (io/file temp-dir "file2.san") "Content 2")
    (log/with-min-level :error (let [bench-out (with-out-str (crit/quick-bench (helpers/get-filenames temp-dir)))]
                                 (spit "./benchmarking/clj/sanide_backend/bench-results/bench-get-filenames.txt" bench-out)))
    (io/delete-file (io/file temp-dir "file1.txt"))
    (io/delete-file (io/file temp-dir "file2.san"))
    (.delete temp-dir)))

(defn bench-get-file-with-extension []
  (log/info "Running get file with extension benchmark")
  (let [temp-dir (io/file "test-dir")]
    (.mkdir temp-dir)
    (spit (io/file temp-dir "file1.txt") "Content 1")
    (spit (io/file temp-dir "file2.san") "Content 2")
    (log/with-min-level :error (let [bench-out (with-out-str (crit/quick-bench (helpers/get-file-with-extension temp-dir ".san")))]
                                 (spit "./benchmarking/clj/sanide_backend/bench-results/bench-get-file-with-extension.txt" bench-out)))
    (io/delete-file (io/file temp-dir "file1.txt"))
    (io/delete-file (io/file temp-dir "file2.san"))
    (.delete temp-dir)))

(defn -main [& _]
  (bench-create-dir)
  (bench-create-dirs)
  (bench-create-file)
  (bench-create-files)
  (bench-create-project)
  (bench-get-file-path)
  (bench-get-file-with-extension)
  (bench-get-filenames)
  (bench-is-san-project?)
  (bench-read-file-content)
  (bench-read-large-file-content))