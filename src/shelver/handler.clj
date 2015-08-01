(ns shelver.handler
  (:require [environ.core :refer [env]]
            [taoensso.timbre :as timbre]
            [taoensso.timbre.appenders.core :as appenders]
            [compojure.core :refer [defroutes GET POST rfn context routes wrap-routes]]
            [compojure.response :refer [render]]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults api-defaults]]
            [ring.util.response :refer [redirect]]
            [buddy.auth.backends.session :refer [session-backend]]
            [buddy.auth.middleware :refer [wrap-authentication wrap-authorization]]
            [buddy.auth :refer [authenticated? throw-unauthorized]]
            [shelver.html :as html]))

(defn wrap-log-request [handler]
  (fn [req]
    (timbre/with-merged-config
      {:appenders {:request (appenders/spit-appender {:fname "request.log"})
                   :println {:enabled? false}}}
      (timbre/info req))
    (handler req)))

(defn wrap-log-response [handler]
  (fn [req]
    (let [resp (handler req)]
      (timbre/with-merged-config
        {:appenders {:response (appenders/spit-appender {:fname "response.log"})
                     :println  {:enabled? false}}}
        (timbre/info (dissoc resp :body)))
      resp)))

(defn wrap-exception [handler]
  (fn [req]
    (try
      (handler req)
      (catch Exception e
        (timbre/error e)
        (throw e)))))

(defn wrap-check-auth [handler login-path]
  (fn [req]
    (if (authenticated? req)
      (handler req)
      (let [[path query] (-> (:uri req)
                             java.net.URI.
                             ((juxt #(.getPath %) #(.getQuery %))))
            redirect-target (-> (format "%s?%s" (or path "") (or query ""))
                                (java.net.URLEncoder/encode "UTF-8"))]
        (redirect (format "%s?next=%s" login-path redirect-target))))))

(defn authenticated-routes [{:keys [datomic crypto-client oauth-client] :as deps}]
  (-> (routes
        (GET "/confirm" [oauth_token authorize :as request] (html/confirm datomic oauth-client oauth_token authorize request)))
      (wrap-routes #(wrap-check-auth % "/sign-up"))))

(defn logout [request]
  (let [updated-session (dissoc (:session request) :identity)]
    (-> (redirect "/")
        (render request)
        (assoc :session updated-session))))

(defn public-routes [{:keys [datomic crypto-client oauth-client] :as deps}]
  (routes
    (GET "/" request (html/index request))
    (GET "/about" request (html/about request))
    (GET "/contact" request (html/contacts request))
    (GET "/sign-up" request (html/sign-up "register" request))
    (POST "/register" request (html/register datomic crypto-client oauth-client request))
    (POST "/logout" request (logout request))))

(defn base-routes []
  (routes
    (rfn request (html/not-found request))))

;(defn api-routes [{:keys [datomic] :as deps}]
;  (routes
;    (context "/api" []
;      (GET "/register" request (api/sign-up "email" "pass" "confirm")))))
;
;(defn app [deps]
;  (let [apis (api-routes deps)
;        pages (-> (page-routes deps)
;                  (wrap-defaults site-defaults))]
;    (-> (routes apis pages)
;        wrap-log-request
;        (wrap-defaults api-defaults)
;        wrap-log-response
;        wrap-exception)))

(defn app [deps]
  (-> (routes (public-routes deps) (authenticated-routes deps) (base-routes))
      wrap-log-request
      (wrap-authentication (session-backend))
      (wrap-defaults site-defaults)
      wrap-log-response
      wrap-exception))