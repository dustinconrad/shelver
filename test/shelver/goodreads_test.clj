(ns shelver.goodreads-test
  (:require [shelver.oauth :as oauth]
            [com.stuartsierra.component :as component]
            [shelver.goodreads :as gr]
            [clojure.test :refer :all]
            [environ.core :refer [env]]))

(defn default-oauth-client []
  (->> (oauth/map->DefaultOAuthClient {:api-key           (env :goodreads-api-key)
                                       :api-secret        (env :goodreads-api-secret)
                                       :request-token-url "https://www.goodreads.com/oauth/request_token"
                                       :access-token-url  "https://www.goodreads.com/oauth/access_token"
                                       :authorize-url     "https://www.goodreads.com/oauth/authorize"
                                       :signature-method  :hmac-sha1})
       component/start))

(deftest test-goodreads-client
  (testing "Testing"
    (let [goodreads-client (gr/new-goodreads-client (default-oauth-client) (env :goodreads-access-token) nil)]
      (testing "auth-user"
        (let [resp (gr/auth-user goodreads-client)]
          (is (some #(= [:name ["Conrad"]] ((juxt :tag :content) %)) (xml-seq (:body resp))))))
      (testing "list shelves"
        (let [resp (gr/shelves goodreads-client 1)]
          (is (some #(= [:id ["138241012"]] ((juxt :tag :content) %)) (xml-seq (:body resp)))))))))
