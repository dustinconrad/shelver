(ns shelver.goodreads
  (:require [shelver.oauth :as oauth]
            [clj-http.client :as clj-http]
            [clojure.xml :as xml]))

(defprotocol GoodreadsClient
  (auth-user [this]))

(defn- api-helper [oauth-client access-token request-method url params]
  (let [credentials (oauth/credentials oauth-client access-token request-method url params)
        request-fn (case request-method
                     :GET clj-http/get)]
    (-> (request-fn url {:query-params credentials})
        (update-in [:body] #(-> (.getBytes %) java.io.ByteArrayInputStream. xml/parse)))))

(defrecord DefaultGoodreadsClient [oauth-client access-token]
  GoodreadsClient
  (auth-user [this]
    (let [url "https://www.goodreads.com/api/auth_user"
          request-method :GET
          params nil]
      (api-helper oauth-client access-token request-method url params))))

(defn new-goodreads-client [oauth-client access-token]
  (map->DefaultGoodreadsClient {:oauth-client oauth-client :access-token access-token}))