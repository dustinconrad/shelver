(ns shelver.handler
  (:require [compojure.core :refer [defroutes GET]]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [shelver.html :as html]))

(defroutes routes
           (GET "/" request (html/index request)))

(def app
  (-> routes
      (wrap-defaults site-defaults)))