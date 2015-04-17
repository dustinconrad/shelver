(ns shelver.goodreads
  (:require [oauth.client :as oauth]))

;key: EV7wnkrFg211dYRJNf8bg
;secret: wNW0K3qUnjHlcbaO6ur0R7ia1TKddiRnoLrx8vLxY

(def consumer-key "EV7wnkrFg211dYRJNf8bg")
(def consumer-secret "wNW0K3qUnjHlcbaO6ur0R7ia1TKddiRnoLrx8vLxY")

(def consumer (oauth/make-consumer consumer-key
                                   consumer-secret
                                   "https://www.goodreads.com/oauth/request_token"
                                   "https://www.goodreads.com/oauth/access_token"
                                   "https://www.goodreads.com/oauth/authorize"
                                   :hmac-sha1))