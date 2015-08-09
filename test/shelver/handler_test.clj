(ns shelver.handler-test
  (:require [clojure.test :refer :all]
            [shelver.handler :refer :all]
            [shelver.util :refer :all]))

(defn- base64-encode [s]
  (-> (java.util.Base64/getUrlEncoder)
      (.withoutPadding)
      (.encodeToString (.getBytes s "UTF-8"))))

(deftest test-add-redirect
  (testing "Test adding a relative redirect parameter"
    (are [base target]
      (let [target-encoded (base64-encode target)]
        (= (format "%s?next=%s" base target-encoded) (add-redirect-next base target)))

      "http://host.com/path" "/target"
      "http://host.com/path" "/target?query"
      "http://host.com/path" "/target?query#frag"

      "/path" "/target"
      "/path" "/target?query"
      "/path" "/target?query#frag"))

  (testing "Test adding an absolute redirect parameter"
    (are [base target]
      (let [target-with-base (str "http://targethost.edu" target)
            target-encoded (base64-encode target)]
        (= (format "%s?next=%s" base target-encoded) (add-redirect-next base target-with-base)))

      "http://host.com/path" "/target"
      "http://host.com/path" "/target?query"
      "http://host.com/path" "/target?query#frag"

      "/path" "/target"
      "/path" "/target?query"
      "/path" "/target?query#frag"))

  (testing "Test adding to path with an existing query parameter"
    (are [base target]
      (let [target-with-base (str "http://targethost.edu" target)
            target-encoded (base64-encode target)]
        (= (format "%s&next=%s" base target-encoded) (add-redirect-next base target-with-base)))

      "http://host.com/path?q1" "/target"
      "http://host.com/path?q1=v1" "/target"

      "/path?q1" "/target"
      "/path?q1=v1" "/target")))

(deftest resolve-roundtrip
  (testing "Test redirect roundtrip, different redirects"
    (are [tc]
      (= tc (-> (add-redirect-next "http://host.com/path" tc) resolve-redirect-next))
      "/target"
      "/target?query"
      "/target?query#frag"))

  (testing "Test redirect roundtrip, different bases"
    (are [tc]
      (= "/target" (-> (add-redirect-next tc "/target") resolve-redirect-next))
      "http://host.com/path?q1"
      "http://host.com/path?q1=v1"

      "/path?q1"
      "/path?q1=v1")))
