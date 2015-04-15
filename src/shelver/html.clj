(ns shelver.html
  (:require
    (hiccup [page :refer [html5 include-js include-css]])))

(def bootstrap-version "3.3.4")
(def jquery-version "1.11.2")

(defn index []
  (html5
    [:head
     [:meta {:charset "utf-8"}]
     [:meta {:http-equiv "X-UA-Compatible" :content "IE=edge"}]
     [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
     [:meta {:name "description" :content "shelver"}]
     [:meta {:name "author" :content "Dustin Conrad"}]
     [:title "shelver"]

     (include-css
       "//maxcdn.bootstrapcdn.com/font-awesome/4.1.0/css/font-awesome.min.css"
       (str "//maxcdn.bootstrapcdn.com/bootstrap/" bootstrap-version "/css/bootstrap.min.css")
       (str "//maxcdn.bootstrapcdn.com/bootstrap/" bootstrap-version "/css/bootstrap-theme.min.css"))]

    [:body
     [:div#main-area.container
      [:p "This is an example project for systems."]]


     (include-js
       (str "https://ajax.googleapis.com/ajax/libs/jquery/" jquery-version "/jquery.min.js")
       (str "//maxcdn.bootstrapcdn.com/bootstrap/" bootstrap-version "/js/bootstrap.min.js"))]))