(ns shelver.oauth
  (:require [oauth.client :as oauth]
            [com.stuartsierra.component :as component]))

;key: EV7wnkrFg211dYRJNf8bg
;secret: wNW0K3qUnjHlcbaO6ur0R7ia1TKddiRnoLrx8vLxY

(defprotocol OAuthClient
  (request-token [this callback-url])
  (user-approval-uri [this request-token])
  (access-token [this request-token verifier])
  (credentials [this access-token request-method request-uri params]))

(defrecord DefaultOAuthClient [consumer-key consumer-secret
                               request-token-url access-token-url authorize-url
                               signature-method]
  component/Lifecycle
  (start [component]
    (if (:consumer component)
      component
      (let [consumer (oauth/make-consumer consumer-key
                                          consumer-secret
                                          request-token-url
                                          access-token-url
                                          authorize-url
                                          signature-method)]
        (assoc component :consumer consumer))))
  (stop [component]
    (assoc component :consumer nil))

  OAuthClient
  (request-token [this callback-url]
    (oauth/request-token (:consumer this) callback-url))
  (user-approval-uri [this request-token]
    (oauth/user-approval-uri (:consumer this) (:oauth_token request-token)))
  (access-token [this request-token verifier]
    (oauth/access-token (:consumer this) request-token verifier))
  (credentials [this access-token request-method request-uri params]
    (oauth/credentials (:consumer this) (:oauth_token access-token) (:oauth_token_secret access-token) request-method request-uri params)))