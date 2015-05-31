(ns shelver.systems
  (:require [system.core :refer [defsystem]]
            (system.components
              [jetty :refer [new-web-server]]
              [repl-server :refer [new-repl-server]])
            [environ.core :refer [env]]
            [shelver.handler :refer [app]]
            [shelver.oauth :refer [new-oauth-client]]
            [com.stuartsierra.component :as component]))

;(defsystem dev-system
;           [:web (new-web-server (Integer. (env :http-port)) app)])

(defn dev-system []
  (component/system-map
    :web (new-web-server (Integer. (env :http-port)) app)
    :oauth-client (new-oauth-client (env :goodreads-api-key) (env :goodreads-api-secret))))

