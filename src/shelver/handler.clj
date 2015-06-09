(ns shelver.handler
  (:require [compojure.core :refer [defroutes GET POST context routes]]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults api-defaults]]
            [shelver.html :as html]
            [shelver.api :as api]))

(defroutes page-routes
           (GET "/" request (html/index request))
           (GET "/about" request (html/about request))
           (GET "/contact" request (html/contacts request))
           (GET "/sign-up" request (html/sign-up "register" request))
           (POST "/register" request (html/register request)))

(defroutes api-routes
           (context "/api" []
             (GET "/register" request (api/sign-up "email" "pass" "confirm"))))

(def app
  (-> (routes api-routes (wrap-defaults page-routes site-defaults))
      (wrap-defaults api-defaults)))