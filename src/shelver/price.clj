(ns shelver.price
  (:require [clojure.zip :as zip]
            [clojure.data.zip.xml :as zx]
            [clojure.data.xml :as xml]
            [shelver.util :refer :all]))

(defrecord BookInfo [isbn isbn13 title authors])

(defn xml->book-info [xml-book]
  (let [create-field-fn (fn [part]
                     #(vector part (-> % (zx/xml1-> part) zx/text)))
        field-fns (map create-field-fn [:isbn :isbn13 :title])
        authors-fn #(->> (zx/xml-> % :authors :author :name)
                        (map zx/text)
                         set
                         (vector :authors))]
    (->> (zip/xml-zip xml-book)
         ((apply juxt authors-fn field-fns))
         (into {}))))

(defprotocol PriceClient
  (get-price [this book-info]))
