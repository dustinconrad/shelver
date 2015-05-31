(ns shelver.html
  (:require [net.cgrand.enlive-html :as html]
            [environ.core :refer [env]]
            [net.cgrand.reload :as reload]
            [shelver.util :as util]))

(when (env :enlive-reload)
  (net.cgrand.reload/auto-reload *ns*))

(def navigation-items
  [["Home" "/"]
   ["About" "/about"]
   ["Contact" "/contact"]])

(html/defsnippet nav "templates/nav.html" [:body :div#nav] [current-path]
                 [:ul.nav [:li html/first-of-type]] (html/clone-for [[caption url] navigation-items]
                                                                    [:li] (if (= current-path url)
                                                                            (html/set-attr :class "active")
                                                                            identity)
                                                                    [:li :a] (html/content caption)
                                                                    [:li :a] (html/set-attr :href url)))

(html/deftemplate base "templates/base.html" [{:keys [uri] :as req} {:keys [title] :as props}]
                  [:head :title] (html/content title)
                  [:body :div.navbar] (html/substitute (nav uri)))

(defn index [request]
  (apply str (base request {:title "shelver"})))

(defn about [request]
  (apply str (base request {:title "shelver - about"})))

(defn contacts [request]
  (apply str (base request {:title "shelver - contacts"})))