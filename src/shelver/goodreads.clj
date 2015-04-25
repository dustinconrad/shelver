(ns shelver.goodreads
  (:require [shelver.oauth :as oauth]
            [clj-http.client :as clj-http]
            [clojure.xml :as xml]
            [shelver.util :refer :all]))

(defprotocol GoodreadsClient
  (auth-user [this]))

(defn- api-helper [oauth-client access-token request-method url params]
  (let [credentials (oauth/credentials oauth-client access-token request-method url params)
        request-fn (case request-method
                     :GET clj-http/get)]
    (-> (request-fn url {:query-params credentials})
        (#(assoc % :raw-body (:body %)))
        (update-in [:body] #(-> (.getBytes %) java.io.ByteArrayInputStream. xml/parse)))))

(defrecord DefaultGoodreadsClient [oauth-client access-token user-id]
  GoodreadsClient
  (auth-user [this]
    (let [url "https://www.goodreads.com/api/auth_user"
          request-method :GET
          params nil]
      (api-helper oauth-client access-token request-method url params))))

(defn new-goodreads-client [oauth-client access-token user-id]
  (let [goodreads-client (map->DefaultGoodreadsClient {:oauth-client oauth-client :access-token access-token :user-id user-id})]
    (if user-id
      goodreads-client
      (let [uid (->> (auth-user goodreads-client)
                     :body
                     xml-seq
                     (filter #(= :user (:tag %)))
                     first
                     :attrs
                     :id
                     Long/parseLong)]
        (assoc goodreads-client :user-id uid)))))