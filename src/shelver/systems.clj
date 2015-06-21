(ns shelver.systems
  (:require [environ.core :refer [env]]
            [shelver.handler :refer [app]]
            [shelver.jetty :refer [new-web-server]]
            [shelver.crypto :refer [new-crypto-client]]
            [com.stuartsierra.component :as component]
            [shelver.datomic :refer [new-datomic-db]]))

;(defsystem dev-system
;           [:web (new-web-server (Integer. (env :http-port)) app)])

(defn dev-system []
  (component/system-map
    :web (component/using
           (new-web-server (Integer. (env :http-port)) app)
           [:datomic-db :crypto-client])
    :datomic-db (new-datomic-db (env :datomic-uri) "migrations/schema.edn")
    :crypto-client (new-crypto-client (env :iterations) (env :target-size) (env :salt-size))
    ))

