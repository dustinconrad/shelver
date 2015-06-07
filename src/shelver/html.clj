(ns shelver.html
  (:require [net.cgrand.enlive-html :as html]
            [environ.core :refer [env]]
            [shelver.util :as util]
            [ring.util.anti-forgery :as csrf]))

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

(html/defsnippet nav "templates/nav.html" [:body :.navbar] [current-path]
                 [[:ul.nav (html/but :.navbar-right)] [:li html/first-of-type]] (replace-nav-item current-path navigation-items)
                 [:ul.nav.navbar-right [:li html/first-of-type]] (replace-nav-item current-path sign-up-items))

(html/defsnippet credentials "templates/credentials.html" [:body :#credentials-box] [register-endpoint]
                 [:#signup-form] (html/set-attr :method "POST"
                                                :action register-endpoint)
                 [:#csrf] (html/html-content (csrf/anti-forgery-field)))

(html/deftemplate base "templates/base.html" [{:keys [uri] :as req} {:keys [title main] :as props}]
                  [:head :title] (html/content title)
                  [:body :#nav] (html/substitute (nav uri))
                  [:body :#main] (maybe-substitute main))

(defn index [request]
  (apply str (base request {:title "shelver"})))

(defn about [request]
  (apply str (base request {:title "shelver - About"})))

(defn contacts [request]
  (apply str (base request {:title "shelver - Contacts"})))

(defn sign-up [register-endpoint request]
  (apply str (base request {:title "shelver - Sign Up"
                            :main (credentials register-endpoint)})))

(defn register [request]
  "<html>test</html>")