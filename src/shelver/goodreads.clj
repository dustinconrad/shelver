(ns shelver.goodreads
  (:require [shelver.oauth :as oauth]
            [clj-http.client :as clj-http]
            [clojure.zip :as zip]
            [clojure.data.zip.xml :as zx]
            [clojure.data.xml :as xml]
            [shelver.util :refer :all]))

(defprotocol GoodreadsClient
  (get-auth-user [this])
  (get-shelves [this page])
  (get-shelf-books [this shelf]))

(defn- api-helper [request-method url params]
  (let [request-fn (case request-method
                     :GET clj-http/get)]
    (-> (request-fn url {:query-params params})
        (#(assoc % :parsed (->> (:body %) java.io.StringReader. xml/parse))))))

(defn- oauth-api-helper [oauth-client access-token request-method url params]
  (let [credentials (oauth/credentials oauth-client access-token request-method url params)
        request-fn (case request-method
                     :GET clj-http/get)]
    (api-helper request-method url credentials)
    #_(-> (request-fn url {:query-params credentials})
        (#(assoc % :parsed (->> (:body %) java.io.StringReader. xml/parse))))))

(defn shelf-name [shelf]
  (-> shelf
      zip/xml-zip
      (zx/xml1-> :name)
      zip/node
      :content
      first))

(defrecord DefaultGoodreadsClient [oauth-client access-token user-id]
  GoodreadsClient
  (get-auth-user [this]
    (let [url "https://www.goodreads.com/api/auth_user"
          request-method :GET
          params nil]
      (oauth-api-helper oauth-client access-token request-method url params)))
  (get-shelves [this page]
    (let [url "https://www.goodreads.com/shelf/list.xml"
          request-method :GET
          params {:key     (:api-key oauth-client)
                  :user_id user-id
                  :page    (or page 1)}]
      (api-helper request-method url params)))
  (get-shelf-books [this shelf-name]
    (let [url "https://www.goodreads.com/review/list.xml"
          request-method :GET
          params {:v     2
                  :id    user-id
                  :shelf shelf-name
                  :key   (:api-key oauth-client)}]
      (oauth-api-helper oauth-client access-token request-method url params))))

(defn get-shelf-by-name [goodreads-client shelf-name]
  (let [by-name #(-> %
                     :parsed
                     zip/xml-zip
                     (zx/xml1-> :shelves :user_shelf [:name shelf-name])
                     zip/node)
        info #(-> %
                  :parsed
                  zip/xml-zip
                  (zx/xml-> :shelves)
                  zip/node
                  :attrs)]
    (loop [page-num 1]
      (let [resp (get-shelves goodreads-client page-num)]
        (if-let [shelf (by-name resp)]
          shelf
          (let [{:keys [start end total]} (info resp)]
            (if (or (= end total) (= 0 start end))
              nil
              (recur (get-shelves goodreads-client (inc page-num))))))))))

(defn new-goodreads-client [oauth-client access-token user-id]
  (let [goodreads-client (map->DefaultGoodreadsClient {:oauth-client oauth-client :access-token access-token :user-id user-id})]
    (if user-id
      goodreads-client
      (let [uid (-> (get-auth-user goodreads-client)
                    :parsed
                    zip/xml-zip
                    (zx/xml1-> :user)
                    zip/node
                    (get-in [:attrs :id])
                    Long/parseLong)]
        (assoc goodreads-client :user-id uid)))))