(ns shelver.goodreads-test
  (:require [shelver.oauth :as oauth]
            [com.stuartsierra.component :as component]
            [shelver.goodreads :as gr]
            [clojure.test :refer :all]
            [environ.core :refer [env]]
            [clj-xpath.core :refer :all]
            [shelver.util :refer :all]))

(defn default-oauth-client []
  (->> (oauth/map->DefaultOAuthClient {:api-key           (env :goodreads-api-key)
                                       :api-secret        (env :goodreads-api-secret)
                                       :request-token-url "https://www.goodreads.com/oauth/request_token"
                                       :access-token-url  "https://www.goodreads.com/oauth/access_token"
                                       :authorize-url     "https://www.goodreads.com/oauth/authorize"
                                       :signature-method  :hmac-sha1})
       component/start))

(deftest test-goodreads-client
  (testing "Testing"
    (let [goodreads-client (gr/new-goodreads-client (default-oauth-client) (env :goodreads-access-token) nil)]
      (testing "auth-user"
        ;<GoodreadsResponse>
        ;  <Request>
        ;    <authentication>true</authentication>
        ;    <key><![CDATA[EV7wnkrFg211dYRJNf8bg]]></key>
        ;    <method><![CDATA[api_auth_user]]></method>
        ;  </Request>
        ;  <user id="42456052">
        ;    <name>Conrad</name>
        ;    <link><![CDATA[https://www.goodreads.com/user/show/42456052-conrad?utm_medium=api]]></link>
        ;  </user>
        ;</GoodreadsResponse>
        (let [resp (gr/auth-user goodreads-client)
              name ($x:text "/GoodreadsResponse/user/name" (:body resp))]
          (is (= name "Conrad"))))
      (testing "list shelves"
        ;<GoodreadsResponse>
        ;  <Request>
        ;    <authentication>true</authentication>
        ;    <key><![CDATA[EV7wnkrFg211dYRJNf8bg]]></key>
        ;    <method><![CDATA[shelf_list]]></method>
        ;  </Request>
        ;  <shelves start="1" end="3" total="3">
        ;    <user_shelf>
        ;      <id type="integer">138241014</id>
        ;      <name>read</name>
        ;      <book_count type="integer">0</book_count>
        ;      <exclusive_flag type="boolean">true</exclusive_flag>
        ;      <description nil="true"/>
        ;      <sort nil="true"/>
        ;      <order nil="true"/>
        ;      <per_page type="integer" nil="true"/>
        ;      <display_fields></display_fields>
        ;      <featured type="boolean">true</featured>
        ;      <recommend_for type="boolean">false</recommend_for>
        ;      <sticky type="boolean" nil="true"/>
        ;    </user_shelf>
        ;
        ;    <user_shelf>
        ;      <id type="integer">138241013</id>
        ;      <name>currently-reading</name>
        ;      <book_count type="integer">0</book_count>
        ;      <exclusive_flag type="boolean">true</exclusive_flag>
        ;      <description nil="true"/>
        ;      <sort nil="true"/>
        ;      <order nil="true"/>
        ;      <per_page type="integer" nil="true"/>
        ;      <display_fields></display_fields>
        ;      <featured type="boolean">false</featured>
        ;      <recommend_for type="boolean">false</recommend_for>
        ;      <sticky type="boolean" nil="true"/>
        ;    </user_shelf>
        ;
        ;    <user_shelf>
        ;      <id type="integer">138241012</id>
        ;      <name>to-read</name>
        ;      <book_count type="integer">1</book_count>
        ;      <exclusive_flag type="boolean">true</exclusive_flag>
        ;      <description nil="true"/>
        ;      <sort>position</sort>
        ;      <order>a</order>
        ;      <per_page type="integer" nil="true"/>
        ;      <display_fields></display_fields>
        ;      <featured type="boolean">false</featured>
        ;      <recommend_for type="boolean">true</recommend_for>
        ;      <sticky type="boolean" nil="true"/>
        ;    </user_shelf>
        ;
        ;  </shelves>
        ;</GoodreadsResponse>
        (testing "page 1"
          (let [resp (gr/shelves goodreads-client 1)
                id "138241012"
                to-read ($x:text (str "//user_shelf/id[text() = " id "]/../name") (:body resp))]
            (is (= to-read "to-read"))))
        (testing "page 2"
          (let [resp (gr/shelves goodreads-client 2)]
            (is (nil? (some #(= [:id ["138241012"]] ((juxt :tag :content) %)) (xml-seq (:body resp)))))))))))
