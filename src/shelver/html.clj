(ns shelver.html
  (:require [net.cgrand.enlive-html :as html]
            [environ.core :refer [env]]
            [shelver.util :as util]
            [shelver.oauth :as oauth]
            [shelver.user :as user]
            [ring.util.anti-forgery :as csrf]
            [taoensso.timbre :as timbre]))

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

(html/deftemplate base "templates/base.html" [{:keys [uri] :as req} {:keys [title main] :as props}]
                  [:head :title] (html/content title)
                  [:body :#nav] (html/substitute (nav-snip uri))
                  [:body :#main] (maybe-substitute main))

(defn index [request]
  (apply str (base request {:title "shelver"})))

(defn about [request]
  (apply str (base request {:title "shelver - About"})))

(defn contacts [request]
  (apply str (base request {:title "shelver - Contacts"})))

(defn sign-up [register-endpoint request]
  (apply str (base request {:title "shelver - Sign Up"
                            :main (credentials-snip register-endpoint)})))

(defn register [confirm-endpoint base-uri datomic crypto-client oauth-client request]
  (let [callback-uri (str base-uri "/" confirm-endpoint)
        request-token (oauth/request-token oauth-client callback-uri)
        approval-uri  (oauth/user-approval-uri oauth-client request-token)]
    (user/create-user (:conn datomic) crypto-client (:params request))
    (apply str (base request {:title "shelver - Register"
                              :main (register-snip approval-uri)}))))

(defn confirm [request]
  "<html>test</html>")