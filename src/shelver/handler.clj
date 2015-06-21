(ns shelver.handler
  (:require [compojure.core :refer [defroutes GET POST context routes]]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults api-defaults]]
            [shelver.html :as html]
            [shelver.api :as api]))

(defn page-routes [{:keys [datomic-db] :as deps}]
  (routes
    (GET "/" request (html/index request))
    (GET "/about" request (html/about request))
    (GET "/contact" request (html/contacts request))
    (GET "/sign-up" request (html/sign-up "register" request))
    (POST "/register" request (html/register request))))

(defn api-routes [{:keys [datomic-db] :as deps}]
  (routes
    (context "/api" []
     (GET "/register" request (api/sign-up "email" "pass" "confirm")))))

(defn app [deps]
  (-> (routes (api-routes deps) (wrap-defaults (page-routes deps) site-defaults))
      (wrap-defaults api-defaults)))