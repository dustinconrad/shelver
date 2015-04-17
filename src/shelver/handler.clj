(ns shelver.handler
  (:require [compojure.core :refer [defroutes GET]]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [shelver.html :as html]))

(defroutes routes
           (GET "/" request (html/index request))
           (GET "/about" request (html/about request))
           (GET "/contact" request (html/contacts request)))

(def app
  (-> routes
      (wrap-defaults site-defaults)))