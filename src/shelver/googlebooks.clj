(ns shelver.googlebooks
  (:require [shelver.price :refer [PriceClient]]
            [shelver.util :refer :all]))

(defrecord GoogleBooksPriceClient [api-key]
  PriceClient
  (get-price [this book-info]
    nil))
