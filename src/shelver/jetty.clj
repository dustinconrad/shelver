(ns shelver.jetty
  (:require [com.stuartsierra.component :as component]
            [ring.adapter.jetty :refer [run-jetty]]))

(defrecord WebServer [port server handler]
  component/Lifecycle
  (start [component]
    (let [deps (dissoc component port server handler)
          server (run-jetty (handler deps) {:port port :join? false})]
      (assoc component :server server)))
  (stop [component]
    (when server
      (.stop server)
      component)))

(defn new-web-server [port handler]
  (map->WebServer {:port port :handler handler}))