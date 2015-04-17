(ns shelver.html
  (:require [net.cgrand.enlive-html :as html]))

(def navigation-items
  {"Home" "/"
   "About" "/about"
   "Contact" "/contact"})

;(html/defsnippet header "templates/header.html"
;                 [:body :div.navbar]
;                 [current-path]
;                 [:a.brand] (html/content "Enlive starter kit")
;                 [:ul.nav [:li html/first-of-type]] (html/clone-for [[caption url] navigation-items]
;                                                                    [:li] (if (= current-path url)
;                                                                            (html/set-attr :class "active")
;                                                                            identity)
;                                                                    [:li :a] (html/content caption)
;                                                                    [:li :a] (html/set-attr :href url)))

;(html/defsnippet content "templates/content.html"
;                 [:#content]
;                 [replacements]
;                 [:#content html/any-node] (html/replace-vars replacements))

;(html/deftemplate base "templates/base.html" [{:keys [path]}]
;                  [:head :title] (html/content "Enlive starter kit")
;                  [:body] (html/do-> (html/append (header path))
;                                     (html/append (content {:header "This is an interpolated header"
;                                                            :content_part_1 "Use this document as a way to quick start any new project"
;                                                            :content_part_2 "All you get is this message and a barebones HTML document"}))))

(html/deftemplate base "templates/base.html" [{:keys [title]}]
                  [:head :title] (html/content title))

(defn index [request]
  (base {:title "shelver - home"}))