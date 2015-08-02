(ns shelver.user-test
  (:require [clojure.test :refer :all]
            [shelver.util :refer :all]
            [shelver.user-dao :as ud]
            [shelver.user :as user]
            [shelver.crypto :as crypto]
            [shelver.datomic :refer [new-datomic]]
            [datomic.api :as d :refer [db q]]
            [com.stuartsierra.component :as component]))

(defn mock-oauth-client []
  (reify shelver.oauth/OAuthClient
    (request-token [this callback-url]
      {:oauth_token "requesttoken" :oauth_token_secret "requesttokensecret"})
    (user-approval-uri [this request-token]
      (str "http://approval.com?oauth_token=" (:oauth_token request-token)))
    (access-token [this request-token verifier]
      {:oauth_token "accesstoken" :oauth_token_secret "accesstokensecrt"})
    (credentials [this access-token request-method request-uri params]
      (assoc params :oauth_signature (-> (java.util.Base64/getEncoder)
                                         (.encode (.getBytes "credentials")))))))

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

(def oauth-client nil)

(defn oauth-client-test-fixture [test-fn]
  (with-redefs [oauth-client (mock-oauth-client)]
    (test-fn)))

(use-fixtures :once datomic-test-fixture crypto-client-test-fixture oauth-client-test-fixture)

(deftest test-login
  (testing "test register and login"
    (let [dirty-user {:email    (str (java.util.UUID/randomUUID) "@shelver.com")
                      :password "password"}]
      (user/register-user datomic crypto-client oauth-client dirty-user)
      (is (true? (user/login datomic crypto-client dirty-user))))))
