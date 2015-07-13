(ns shelver.user-test
  (:require [clojure.test :refer :all]
            [shelver.util :refer :all]
            [shelver.user :as user]
            [shelver.crypto :as crypto]
            [shelver.datomic :refer [new-datomic]]
            [datomic.api :as d :refer [db q]]
            [com.stuartsierra.component :as component]))

(deftest test-create-user
  (let [datomic (-> (new-datomic "datomic:mem://shelver" "migrations/schema.edn")
                    component/start)
        crypto-client (crypto/->DefaultCryptoClient 4321 64 32)]
    (testing "testing create and find a user"
      (let [email (-> (java.util.UUID/randomUUID)
                      .toString
                      (concat "@gmail.com")
                      clojure.string/join)
            password "password"
            db-after (-> (user/create-user (:conn datomic) crypto-client {:email email :password password})
                         deref
                         :db-after)]
        (->> (user/find-user db-after email)
             (#(is (= email (:email %)))))
        (testing "check user password"
          (let [found (user/find-user db-after email)]
            (is (true? (crypto/verify-password crypto-client (:password-hash found) password (:password-salt found))))))))
    (testing "testing create and find with associated oauth token"
      (let [email (-> (java.util.UUID/randomUUID)
                      .toString
                      (concat "@gmail.com")
                      clojure.string/join)
            password "password"
            token {:oauth_token "token" :oauth_token_secret "token_secret"}
            db-after (-> (user/create-user (:conn datomic) crypto-client {:email email :password password :oauth-token token})
                         deref
                         :db-after)]
        (->> (user/find-user db-after email)
             (#(is (= email (:email %)))))))))