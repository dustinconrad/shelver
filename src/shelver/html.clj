(ns shelver.html
  (:require [net.cgrand.enlive-html :as html]
            [environ.core :refer [env]]
            [datomic.api :as d :refer [db q]]
            [shelver.user :as user]
            [ring.util.anti-forgery :as csrf]
            [ring.util.response :refer [redirect]]
            [compojure.response :refer [render]]))

(def navigation-items
  [["Home" "/"]
   ["About" "/about"]
   ["Contact" "/contact"]])

(def sign-up-items
  [["Sign Up/In" "/sign-up"]])

(defmacro maybe-substitute
  ([expr] `(if-let [x# ~expr] (html/substitute x#) identity))
  ([expr & exprs] `(maybe-substitute (or ~expr ~@exprs))))

(defmacro maybe-content
  ([expr] `(if-let [x# ~expr] (html/content x#) identity))
  ([expr & exprs] `(maybe-content (or ~expr ~@exprs))))

(defmacro maybe-append
  ([expr] `(if-let [x# ~expr] (html/append x#) identity))
  ([expr & exprs] `(maybe-append (or ~expr ~@exprs))))

(defn- replace-nav-item [current-path nav-items]
  (html/clone-for [[caption url] nav-items]
                  [:li] (if (= current-path url)
                          (html/set-attr :class "active")
                          identity)
                  [:li :a] (html/content caption)
                  [:li :a] (html/set-attr :href url)))

(html/defsnippet logout-snip "templates/logout.html" [:#logout-form] [logout-path]
                 [:#logout-form] (html/set-attr :method "POST"
                                                :action logout-path)
                 [:#logout-csrf] (html/html-content (csrf/anti-forgery-field)))

(html/defsnippet nav-snip "templates/nav.html" [:body :.navbar] [logout-path current-path user-identity]
                 [[:ul.nav (html/but :.navbar-right)] [:li html/first-of-type]] (replace-nav-item current-path navigation-items)
                 [:ul.nav.navbar-right] (maybe-substitute
                                          (when user-identity
                                            (logout-snip logout-path)))
                 [:ul.nav.navbar-right [:li html/first-of-type]] (replace-nav-item current-path sign-up-items))

(html/defsnippet credentials-snip "templates/credentials.html" [:body :#credentials-box] [register-endpoint]
                 [:#signup-form] (html/set-attr :method "POST"
                                                :action register-endpoint)
                 [:#signup-csrf] (html/html-content (csrf/anti-forgery-field)))

(html/defsnippet register-snip "templates/register.html" [:body :#register] [approval-uri]
                 [:.btn] (html/set-attr :href approval-uri))

(html/defsnippet not-found-snip "templates/notfound.html" [:body :#notfound] [])

(html/defsnippet confirm-deny-snip "templates/confirm-deny.html" [:body :#confirm-deny] [])

(html/deftemplate base "templates/base.html" [{:keys [title main scripts] :as props} {:keys [uri identity] :as req}]
                  [:head :title] (html/content title)
                  [:body :#nav] (html/substitute (nav-snip "/logout" uri identity))
                  [:body :#main] (maybe-substitute main)
                  [:body] (maybe-append scripts))

(defn index [request]
  (apply str (base {:title "shelver"} request)))

(defn about [request]
  (apply str (base {:title "shelver - About"} request)))

(defn contacts [request]
  (apply str (base {:title "shelver - Contacts"} request)))

(defn sign-up [register-endpoint request]
  (apply str (base {:title "shelver - Sign Up"
                    :main  (credentials-snip register-endpoint)
                    :scripts [(html/html [:script {:src "/js/validator.js"}]) (html/html [:script {:src "/js/credentials.js"}])]}
                   request)))

(defn not-found [request]
  (apply str (base {:title "shelver - Not Found"
                    :main (not-found-snip)}
                   request)))

(defn register [datomic crypto-client oauth-client request]
  (let [approval-uri (user/register-user datomic crypto-client oauth-client (:params request))]
    (when approval-uri
      (let [updated-session (-> (:session request)
                                (assoc :identity (get-in request [:params :email])))]
        (-> (apply str (base {:title "shelver - Register"
                              :main  (register-snip approval-uri)}
                             request))
            (render request)
            (assoc :session updated-session))))))

(defn confirm [datomic oauth-client oauth_token authorize request]
  (if (= "1" authorize)
    (when (user/confirm-registration datomic oauth-client oauth_token (:identity request))
      (redirect "/"))
    (apply str (base {:title "shelver - Denied"
                      :main  (confirm-deny-snip)}
                     request))))