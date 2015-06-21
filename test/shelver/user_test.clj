(ns shelver.user-test
  (:require [clojure.test :refer :all]
            [shelver.util :refer :all]
            [shelver.user :as user]
            [shelver.crypto :as crypto]
            [shelver.datomic :refer [new-datomic-db]]
            [com.stuartsierra.component :as component]))

(deftest test-create-user
  (testing "testing create and find a user"
    (let [datomic-db (-> (new-datomic-db "datomic:mem://shelver" "migrations/schema.edn")
                         component/start)
          crypto-client (crypto/->DefaultCryptoClient 4321 64 32)
          email (-> (java.util.UUID/randomUUID)
                    .toString
                    (concat "@gmail.com")
                    clojure.string/join)
          password "password"]
      (user/create-user datomic-db crypto-client {:email email :password password})
      (->> (user/find-user datomic-db email)
           (#(is (= email (:user/email %)))))
      (testing "check user password"
        (user/create-user datomic-db crypto-client {:email email :password password})
        (let [found (user/find-user datomic-db email)]
          (is (true? (crypto/verify-password crypto-client (:user/password-hash found) password (:user/password-salt found)))))))))