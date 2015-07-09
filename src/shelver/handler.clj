(ns shelver.handler
  (:require [taoensso.timbre :as timbre]
            [environ.core :refer [env]]
            [taoensso.timbre.appenders.core :as appenders]
            [compojure.core :refer [defroutes GET POST context routes]]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults api-defaults]]
            [shelver.html :as html]
            [shelver.api :as api]))

(defn wrap-log-request [handler]
  (fn [req]
    (timbre/with-merged-config
      {:appenders {:request (appenders/spit-appender {:fname "request.log"})
                   :println {:enabled? false}}}
      (timbre/info req))
    (handler req)))

(defn wrap-exception [handler]
  (fn [req]
    (try
      (handler req)
      (catch Exception e
        (timbre/error e)
        (throw e)))))

(defn page-routes [{:keys [datomic crypto-client oauth-client] :as deps}]
  (routes
    (GET "/" request (html/index request))
    (GET "/about" request (html/about request))
    (GET "/contact" request (html/contacts request))
    (GET "/sign-up" request (html/sign-up "register" request))
    (POST "/register" request (html/register "confirm" (env :base-uri) datomic crypto-client oauth-client request))
    (GET "/confirm" request (html/confirm request))))

(defn api-routes [{:keys [datomic] :as deps}]
  (routes
    (context "/api" []
      (GET "/register" request (api/sign-up "email" "pass" "confirm")))))

(defn app [deps]
  (let [apis (-> (api-routes deps)
                 wrap-log-request
                 wrap-exception)
        pages (-> (page-routes deps)
                  wrap-log-request
                  wrap-exception
                  (wrap-defaults site-defaults))]

    (-> (routes apis pages)
        (wrap-defaults api-defaults))))