(ns shelver.user-test
  (:require [clojure.test :refer :all]
            [shelver.util :refer :all]
            [shelver.user :as user]
            [shelver.crypto :as crypto]
            [shelver.datomic :refer [new-datomic]]
            [datomic.api :as d :refer [db q]]
            [com.stuartsierra.component :as component]))

(deftest test-create-user
  (testing "testing create and find a user"
    (let [datomic (-> (new-datomic (or "datomic:dev://datomic-db:4334/shelver" "datomic:mem://shelver") "migrations/schema.edn")
                         component/start)
          crypto-client (crypto/->DefaultCryptoClient 4321 64 32)
          email (-> (java.util.UUID/randomUUID)
                    .toString
                    (concat "@gmail.com")
                    clojure.string/join)
          password "password"
          db-after (-> (user/create-user (:conn datomic) crypto-client {:email email :password password})
                       deref
                       :db-after)]
      (->> (user/find-user db-after email)
           (#(is (= email (:user/email %)))))
      (testing "check user password"
        (let [found (user/find-user db-after email)]
          (is (true? (crypto/verify-password crypto-client (:user/password-hash found) password (:user/password-salt found)))))))))