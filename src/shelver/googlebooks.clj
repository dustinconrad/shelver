(ns shelver.googlebooks
  (:require [shelver.price :refer [PriceClient]]
            [clj-http.client :as clj-http]
            [shelver.util :refer :all]
            [cheshire.core :as json]
            [shelver.price :as price]))

(defrecord GoogleBooksPriceClient [api-key]
  PriceClient
  (get-price [this book-info]
    (let [intitle (str "intitle:" "\"" (:title book-info) "\"")
          inauthor (str "inauthor:" "\"" (first (:authors book-info)) "\"")
          q (clojure.string/join [intitle inauthor])
          query-params {:q      q
                        :filter "ebooks"
                        :key    api-key
                        :fields "items(etag,id,saleInfo(isEbook,listPrice,retailPrice),selfLink,volumeInfo(title,industryIdentifiers,authors)),kind,totalItems"}
          url "https://www.googleapis.com/books/v1/volumes"
          response (-> (clj-http/get url {:query-params query-params})
                       :body
                       (json/parse-string true))]
      (when (= 1 (:totalItems response))
        (let [[item & _] (get response :items)
              {amount :amount currency :currencyCode} (get-in item [:saleInfo :retailPrice])
              gid (get item :id)
              isbn13 (->> (get-in item [:volumeInfo :industryIdentifiers])
                          (filter (comp (partial = "ISBN_13") :type))
                          first
                          :identifier)
              buy-link (str "https://play.google.com/store/books/details?id=" gid)]
          (price/map->PriceInfo {:amount amount
                                 :currency currency
                                 :isbn13 isbn13
                                 :buy-link buy-link}))))))
