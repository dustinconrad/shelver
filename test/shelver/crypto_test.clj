(ns shelver.crypto-test
  (:require [clojure.test :refer :all]
            [environ.core :refer [env]]
            [shelver.crypto :as crypto]))

(deftest test-equals
  (testing "testing eq?"
    (are [tf a b]
      (tf (crypto/eq? (byte-array a) (byte-array b)))

      true? (range 10) (range 10)
      false? (range 10) (range 1 11)
      false? (range 5) (range 6))))

(deftest test-crypto-client
  (testing "testing encrypt and verify"
    (let [crypto-client (crypto/new-crypto-client 12345 64 32)]
      (are [password]
        (let [[hash salt] (crypto/encrypt-password crypto-client password)]
          (true? (crypto/verify-password crypto-client hash password salt)))

        "password"
        "asdfasdfasdfs")
      (are [expected base check]
        (let [[hash salt] (crypto/encrypt-password crypto-client base)]
          (expected (crypto/verify-password crypto-client hash check salt)))

        true? "pass" "pass"
        false? "not" "equal"
        false? "password" "pass"
        false? "pass" "password"))))