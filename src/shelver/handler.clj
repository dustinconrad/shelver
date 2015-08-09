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
            [shelver.html :as html]
            [shelver.user :as user]
            [shelver.util :refer :all]))

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

(defn add-redirect [redirect-key base redirect-target]
  (let [[scheme userInfo host port path query fragment] (-> (java.net.URI. base)
                                                            ((juxt #(.getScheme %) #(.getUserInfo %) #(.getHost %)
                                                                   #(.getPort %) #(.getPath %) #(.getQuery %) #(.getFragment %))))
        relative-target (->> (java.net.URI. redirect-target)
                             ((juxt #(.getPath %) #(.getQuery %) #(.getFragment %)))
                             (apply #(java.net.URI. nil nil nil -1 %1 %2 %3))
                             str)
        redirect-encoded (-> (java.util.Base64/getUrlEncoder)
                             (.withoutPadding)
                             (.encodeToString (.getBytes relative-target "UTF-8")))
        new-query (format "%s%s=%s" (if query (str query "&") "") redirect-key redirect-encoded)]
    (-> (java.net.URI. scheme userInfo host port path new-query fragment)
        str)))

(def add-redirect-next (partial add-redirect "next"))

(defn resolve-redirect [redirect-key uri]
  (when-let [query-pairs (some-> (java.net.URI. uri)
                                 (.getQuery)
                                 (clojure.string/split #"&"))]
    (when-let [redirect-param (some->> query-pairs
                                       (map #(clojure.string/split % #"="))
                                       (some (fn [[k v]] (when (= redirect-key k) v))))]
      (-> (java.util.Base64/getUrlDecoder)
          (.decode redirect-param)
          (String. "UTF-8")))))

(def resolve-redirect-next (partial resolve-redirect "next"))

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

(defn login [datomic crypto-client request]
  (when (user/login datomic crypto-client (:params request))
    (let [updated-session (-> (:session request)
                              (assoc :identity (get-in request [:params :email])))]
      (-> (redirect "/")
          (render request)
          (assoc :session updated-session)))))

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