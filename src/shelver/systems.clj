(ns shelver.systems
  (:require [environ.core :refer [env]]
            [shelver.handler :refer [app]]
            [shelver.oauth :refer [new-goodreads-oauth-client]]
            [shelver.jetty :refer [new-web-server]]
            [shelver.crypto :refer [new-crypto-client]]
            [com.stuartsierra.component :as component]
            [shelver.datomic :refer [new-datomic]]))

;(defsystem dev-system
;           [:web (new-web-server (Integer. (env :http-port)) app)])
(defn dev-system []
  (component/system-map
    :web (component/using
           (new-web-server (Integer. (env :http-port)) app)
           [:datomic :crypto-client :oauth-client])
    :datomic (new-datomic (env :datomic-uri) "migrations/schema.edn")
    :crypto-client (new-crypto-client (env :iterations) (env :target-size) (env :salt-size))
    :oauth-client (new-goodreads-oauth-client (env :goodreads-api-key) (env :goodreads-api-secret))
    ))

