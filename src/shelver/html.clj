(ns shelver.html
  (:require [net.cgrand.enlive-html :as html]
            [environ.core :refer [env]]
            [shelver.util :as util]))

(def navigation-items
  [["Home" "/"]
   ["About" "/about"]
   ["Contact" "/contact"]])

(def sign-up-items
  [["Sign Up" "/sign-up"]])

(defn- replace-nav-item [current-path nav-items]
  (html/clone-for [[caption url] nav-items]
                  [:li] (if (= current-path url)
                          (html/set-attr :class "active")
                          identity)
                  [:li :a] (html/content caption)
                  [:li :a] (html/set-attr :href url)))

(html/defsnippet nav "templates/nav.html" [:body :div#nav] [current-path]
                 [[:ul.nav (html/but :.navbar-right)] [:li html/first-of-type]] (replace-nav-item current-path navigation-items)
                 [:ul.nav.navbar-right [:li html/first-of-type]] (replace-nav-item current-path sign-up-items))

(html/deftemplate base "templates/base.html" [{:keys [uri] :as req} {:keys [title main] :as props}]
                  [:head :title] (html/content title)
                  [:body :div.navbar] (html/substitute (nav uri))
                  )


(defn index [request]
  (apply str (base request {:title "shelver"})))

(defn about [request]
  (apply str (base request {:title "shelver - About"})))

(defn contacts [request]
  (apply str (base request {:title "shelver - Contacts"})))

(defn sign-up [request]
  (apply str (base request {:title "shelver - Sign Up"})))