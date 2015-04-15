(ns shelver.systems
  (:require [system.core :refer [defsystem]]
            (system.components
              [jetty :refer [new-web-server]]
              [repl-server :refer [new-repl-server]])
            [environ.core :refer [env]]
            [shelver.handler :refer [app]]))

(defsystem dev-system
           [:web (new-web-server (Integer. (env :http-port)) app)])

