(ns shelver.googlebooks
  (:require [shelver.price :refer [PriceClient]]
            [clj-http.client :as clj-http]
            [shelver.util :refer :all]
            [cheshire.core :as json]))

(defrecord GoogleBooksPriceClient [api-key]
  PriceClient
  (get-price [this book-info]
    (let [intitle (str "intitle:" "\"" (:title book-info) "\"")
          inauthor (str "inauthor:" "\"" (first (:authors book-info)) "\"")
          q (clojure.string/join [intitle inauthor])
          query-params {:q q :filter "ebooks" :key api-key}
          url "https://www.googleapis.com/books/v1/volumes"]
      (-> (clj-http/get url {:query-params query-params})
          :body
          (json/parse-string true)))))
