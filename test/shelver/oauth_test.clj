(ns shelver.oauth-test
  (:require [shelver.oauth :as oauth]
            [com.stuartsierra.component :as component]
            [clojure.test :refer :all]
            [environ.core :refer [env]]))

(defn default-oauth-client []
  (->>
    (oauth/new-oauth-client (env :goodreads-api-key) (env :goodreads-api-secret))
    component/start))

(deftest test-oauth-wrapper
  (testing "Testing oauth client"
    (let [oauth-client (default-oauth-client)]
      (testing "request token"
        (let [request-token (oauth/request-token oauth-client nil)]
          (is (some? {:oauth_token request-token}))
          (is (some? {:oauth_token_secret request-token}))))
      (testing "user approval uri"
        (let [request-token (oauth/request-token oauth-client nil)]
          (is (= (str "https://www.goodreads.com/oauth/authorize?oauth_token=" (:oauth_token request-token))
                 (oauth/user-approval-uri oauth-client request-token))))))))