(ns shelver.html
  (:require [net.cgrand.enlive-html :as html]
            [environ.core :refer [env]]
            [datomic.api :as d :refer [db q]]
            [shelver.oauth :as oauth]
            [shelver.user :as user]
            [ring.util.anti-forgery :as csrf]
            [compojure.response :refer [render]]))

(def navigation-items
  [["Home" "/"]
   ["About" "/about"]
   ["Contact" "/contact"]])

(def sign-up-items
  [["Sign Up" "/sign-up"]])

(defmacro maybe-substitute
  ([expr] `(if-let [x# ~expr] (html/substitute x#) identity))
  ([expr & exprs] `(maybe-substitute (or ~expr ~@exprs))))

(defmacro maybe-content
  ([expr] `(if-let [x# ~expr] (html/content x#) identity))
  ([expr & exprs] `(maybe-content (or ~expr ~@exprs))))

(defn- replace-nav-item [current-path nav-items]
  (html/clone-for [[caption url] nav-items]
                  [:li] (if (= current-path url)
                          (html/set-attr :class "active")
                          identity)
                  [:li :a] (html/content caption)
                  [:li :a] (html/set-attr :href url)))

(html/defsnippet nav-snip "templates/nav.html" [:body :.navbar] [current-path]
                 [[:ul.nav (html/but :.navbar-right)] [:li html/first-of-type]] (replace-nav-item current-path navigation-items)
                 [:ul.nav.navbar-right [:li html/first-of-type]] (replace-nav-item current-path sign-up-items))

(html/defsnippet credentials-snip "templates/credentials.html" [:body :#credentials-box] [register-endpoint]
                 [:#signup-form] (html/set-attr :method "POST"
                                                :action register-endpoint)
                 [:#csrf] (html/html-content (csrf/anti-forgery-field)))

(html/defsnippet register-snip "templates/register.html" [:body :#register] [approval-uri]
                 [:.btn] (html/set-attr :href approval-uri))

(html/defsnippet not-found-snip "templates/notfound.html" [:body :#notfound] [])

(html/deftemplate base "templates/base.html" [{:keys [title main] :as props} {:keys [uri] :as req}]
                  [:head :title] (html/content title)
                  [:body :#nav] (html/substitute (nav-snip uri))
                  [:body :#main] (maybe-substitute main))

(defn index [request]
  (apply str (base {:title "shelver"} request)))

(defn about [request]
  (apply str (base {:title "shelver - About"} request)))

(defn contacts [request]
  (apply str (base {:title "shelver - Contacts"} request)))

(defn sign-up [register-endpoint request]
  (apply str (base {:title "shelver - Sign Up"
                    :main  (credentials-snip register-endpoint)} request)))

(defn not-found [request]
  (apply str (base {:title "shelver - not found"
                    :main (not-found-snip)
                    } request)))

(defn register [datomic crypto-client oauth-client request]
  (let [request-token (-> (oauth/request-token oauth-client nil)
                          (assoc :type :request))
        approval-uri (->> request-token
                          (oauth/user-approval-uri oauth-client))
        result (user/create-user (:conn datomic) crypto-client (assoc (:params request) :oauth-token request-token))]
    (when @result
      (let [updated-session (-> (:session request)
                                (assoc :identity (get-in request [:params :email])))]
        (-> (apply str (base {:title "shelver - Register"
                              :main  (register-snip approval-uri)}
                             request))
            (render request)
            (assoc :session updated-session))))))

(defn confirm [datomic oauth_token authorize request]
  (let [datomic-db (db (:conn datomic))
        email (:identity request)]
    (when-let [found-token (user/find-oauth-token datomic-db email oauth_token)]
      (if (= "1" authorize)
        (let [updated-token (assoc found-token :type :access)]
          (when @(user/update-oauth-token (:conn datomic) updated-token)
            "<html>yep</html>"))
        "<html>nope</html>"))))