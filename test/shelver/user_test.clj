(ns shelver.user-test
  (:require [clojure.test :refer :all]
            [shelver.util :refer :all]
            [shelver.user :as user]
            [shelver.crypto :as crypto]
            [shelver.datomic :refer [new-datomic]]
            [datomic.api :as d :refer [db q]]
            [com.stuartsierra.component :as component]))

(deftest test-create-user
  (let [datomic-uri (or "datomic:dev://datomic-db:4334/shelver" "datomic:mem://shelver")
        datomic (-> (new-datomic datomic-uri "migrations/schema.edn")
                    component/start)
        crypto-client (crypto/->DefaultCryptoClient 4321 64 32)]
    (testing "testing create and find a user"
      (let [email (-> (java.util.UUID/randomUUID)
                      str
                      (concat "@gmail.com")
                      clojure.string/join)
            password (.toString (java.util.UUID/randomUUID))
            db-after (-> (user/create-user (:conn datomic) crypto-client {:email email :password password})
                         deref
                         :db-after)
            found (user/find-user db-after email)]
        (is (= email (:email found)))
        (is (true? (crypto/verify-password crypto-client (:password-hash found) password (:password-salt found))))))
    (testing "testing create and find with associated oauth token"
      (let [email (-> (java.util.UUID/randomUUID)
                      str
                      (concat "@gmail.com")
                      clojure.string/join)
            token {:oauth_token (str (java.util.UUID/randomUUID)) :oauth_token_secret (str (java.util.UUID/randomUUID)) :type :request}
            db-after (-> (user/create-user (:conn datomic) crypto-client {:email email :password "password" :oauth-token token})
                         deref
                         :db-after)
            found (user/find-user db-after email)]
        (is (= token (dissoc (:oauth-token found) :id)))))
    (testing "testing no user found"
      (is (nil? (user/find-user (db (:conn datomic)) (str (java.util.UUID/randomUUID))))))))

(deftest test-oauth-token
  (let [datomic-uri (or "datomic:dev://datomic-db:4334/shelver" "datomic:mem://shelver")
        datomic (-> (new-datomic datomic-uri "migrations/schema.edn")
                    component/start)
        crypto-client (crypto/->DefaultCryptoClient 4321 64 32)]
    (testing "testing find an oauth token attached to a user"
      (let [email (-> (java.util.UUID/randomUUID)
                      str
                      (concat "@gmail.com")
                      clojure.string/join)
            token-string (str (java.util.UUID/randomUUID))
            token {:oauth_token token-string :oauth_token_secret (str (java.util.UUID/randomUUID)) :type :request}
            db-after (-> (user/create-user (:conn datomic) crypto-client {:email email :password "password" :oauth-token token})
                         deref
                         :db-after)
            found-token (user/find-oauth-token db-after email token-string)]
        (is (= token (dissoc found-token :id)))))
    (testing "testing find non-existing oauth token"
      (is (nil? (user/find-oauth-token (db (:conn datomic)) (str (java.util.UUID/randomUUID)) (str (java.util.UUID/randomUUID))))))
    (testing "testing update oauth token"
      (let [email (-> (java.util.UUID/randomUUID)
                      str
                      (concat "@gmail.com")
                      clojure.string/join)
            token-string (str (java.util.UUID/randomUUID))
            token {:oauth_token token-string :oauth_token_secret (str (java.util.UUID/randomUUID)) :type :request}
            db-after (-> (user/create-user (:conn datomic) crypto-client {:email email :password "password" :oauth-token token})
                         deref
                         :db-after)
            found-token (user/find-oauth-token db-after email token-string)
            updated-token (assoc found-token :type :access)
            found-updated (-> (user/update-oauth-token (:conn datomic) updated-token)
                              deref
                              :db-after
                              (user/find-oauth-token email token-string))]
        (is (= updated-token found-updated))))))