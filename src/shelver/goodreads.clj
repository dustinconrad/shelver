(ns shelver.goodreads
  (:require [shelver.oauth :as oauth]
            [clj-http.client :as clj-http]))

(defprotocol GoodreadsClient
  (auth-user [this]))

(defrecord DefaultGoodreadsClient [oauth-client access-token]
  GoodreadsClient
  (auth-user [this]
    (let [url "https://www.goodreads.com/api/auth_user"
          credentials (oauth/credentials oauth-client access-token :GET url nil)]
      (clj-http/post url {:query-params credentials}))))

(defn new-goodreads-client [oauth-client access-token]
  (map->DefaultGoodreadsClient {:oauth-client oauth-client :access-token access-token}))