(ns shelver.googlebooks-test
  (:require [clojure.test :refer :all]
            [environ.core :refer [env]]
            [shelver.util :refer :all]
            [clojure.zip :as zip]
            [clojure.data.zip.xml :as zx]
            [shelver.googlebooks :as gb]
            [shelver.price :as price]))

(deftest test-googlebooks-price-client
  (testing "Testing googlebooks price client"
    (let [gb-price-client (gb/->GoogleBooksPriceClient (env :google-api-key))]
      (testing "get price"
        (let [book (price/->BookInfo nil nil "Judas Unchained" #{"Peter F. Hamilton"})
              response (price/get-price gb-price-client book)]
          (prn response))))))
