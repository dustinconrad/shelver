(ns shelver.user-dao-test
  (:require [clojure.test :refer :all]
            [shelver.util :refer :all]
            [shelver.user-dao :as ud]
            [shelver.crypto :as crypto]
            [shelver.datomic :refer [new-datomic]]
            [datomic.api :as d :refer [db q]]
            [com.stuartsierra.component :as component]))

(def datomic nil)

;"datomic:dev://datomic-db:4334/shelver"
(defn datomic-test-fixture [test-fn]
  (let [datomic-component (-> (new-datomic (str "datomic:mem://" (java.util.UUID/randomUUID)) "migrations/schema.edn")
                              component/start)]
    (with-redefs [datomic datomic-component]
      (test-fn))
    (component/stop datomic-component)))

(def crypto-client nil)

(defn crypto-client-test-fixture [test-fn]
  (with-redefs [crypto-client (crypto/->DefaultCryptoClient 4321 64 32)]
    (test-fn)))

(use-fixtures :once datomic-test-fixture crypto-client-test-fixture)

(deftest test-create-user
  (testing "testing create and find a user"
    (let [email (-> (java.util.UUID/randomUUID)
                    str
                    (concat "@gmail.com")
                    clojure.string/join)
          password (.toString (java.util.UUID/randomUUID))
          db-after (-> (ud/create-user (:conn datomic) crypto-client {:email email :password password})
                       deref
                       :db-after)
          found (ud/find-user db-after email)]
      (is (= email (:email found)))
      (is (true? (crypto/verify-password crypto-client (:password-hash found) password (:password-salt found))))))
  (testing "testing create and find with associated oauth token"
    (let [email (-> (java.util.UUID/randomUUID)
                    str
                    (concat "@gmail.com")
                    clojure.string/join)
          token {:oauth_token (str (java.util.UUID/randomUUID)) :oauth_token_secret (str (java.util.UUID/randomUUID)) :type :request}
          db-after (-> (ud/create-user (:conn datomic) crypto-client {:email email :password "password" :oauth-token token})
                       deref
                       :db-after)
          found (ud/find-user db-after email)]
      (is (= token (dissoc (:oauth-token found) :id)))))
  (testing "testing no user found"
    (is (nil? (ud/find-user (db (:conn datomic)) (str (java.util.UUID/randomUUID)))))))

(deftest test-oauth-token
  (testing "testing find an oauth token attached to a user"
    (let [email (-> (java.util.UUID/randomUUID)
                    str
                    (concat "@gmail.com")
                    clojure.string/join)
          token-string (str (java.util.UUID/randomUUID))
          token {:oauth_token token-string :oauth_token_secret (str (java.util.UUID/randomUUID)) :type :request}
          db-after (-> (ud/create-user (:conn datomic) crypto-client {:email email :password "password" :oauth-token token})
                       deref
                       :db-after)
          found-token (ud/find-oauth-token db-after email token-string :request)]
      (is (= token (dissoc found-token :id)))))
  (testing "testing find non-existing oauth token"
    (is (nil? (ud/find-oauth-token (db (:conn datomic)) (str (java.util.UUID/randomUUID)) (str (java.util.UUID/randomUUID)) :request))))
  (testing "testing update oauth token"
    (let [email (-> (java.util.UUID/randomUUID)
                    str
                    (concat "@gmail.com")
                    clojure.string/join)
          token-string (str (java.util.UUID/randomUUID))
          token {:oauth_token token-string :oauth_token_secret (str (java.util.UUID/randomUUID)) :type :request}
          db-after (-> (ud/create-user (:conn datomic) crypto-client {:email email :password "password" :oauth-token token})
                       deref
                       :db-after)
          found-token (ud/find-oauth-token db-after email token-string :request)
          updated-token (assoc found-token :type :access)
          found-updated (-> (ud/update-oauth-token (:conn datomic) updated-token)
                            deref
                            :db-after
                            (ud/find-oauth-token email token-string :access))]
      (is (= updated-token found-updated)))))