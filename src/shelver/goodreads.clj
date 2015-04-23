(ns shelver.goodreads
  (:require [shelver.oauth :as oauth]
            [clj-http.client :as clj-http]))

;key: EV7wnkrFg211dYRJNf8bg
;secret: wNW0K3qUnjHlcbaO6ur0R7ia1TKddiRnoLrx8vLxY

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