(ns shelver.goodreads-test
  (:require [shelver.oauth :as oauth]
            [com.stuartsierra.component :as component]
            [shelver.goodreads :as gr]
            [clojure.test :refer :all]
            [environ.core :refer [env]]
            [shelver.util :refer :all]
            [clojure.zip :as zip]
            [clojure.data.zip.xml :as zx]))

(defn default-oauth-client []
  (->>
    (oauth/new-goodreads-oauth-client (env :goodreads-api-key) (env :goodreads-api-secret))
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
        (let [resp (gr/get-auth-user goodreads-client)
              name (-> resp
                       zip/xml-zip
                       (zx/xml1-> :user :name)
                       zx/text)]
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
          (let [resp (gr/get-shelves goodreads-client 1)
                id "138241012"
                to-read (-> resp
                            zip/xml-zip
                            (zx/xml1-> :shelves :user_shelf [:id id]))
                name (gr/shelf-name (zip/node to-read))]
            (is (= name "to-read"))))
        ;<?xml version="1.0" encoding="UTF-8"?>
        ;<GoodreadsResponse>
        ;  <Request>
        ;    <authentication>true</authentication>
        ;    <key><![CDATA[EV7wnkrFg211dYRJNf8bg]]></key>
        ;    <method><![CDATA[shelf_list]]></method>
        ;  </Request>
        ;  <shelves start="0" end="0" total="3">
        ;  </shelves>
        ;</GoodreadsResponse>
        (testing "page 2"
          (let [resp (gr/get-shelves goodreads-client 2)
                shelves (-> resp
                            zip/xml-zip
                            (zx/xml-> :shelves :user_shelf))]
            (is (empty? shelves))))
        (testing "find by shelf name"
          (let [name "to-read"
                expected-id "138241012"
                shelf (gr/get-shelf-by-name goodreads-client name)]
            (is (= expected-id (-> shelf zip/xml-zip (zx/xml1-> :id) zx/text))))
          (let [name "currently-reading"
                expected-id "138241013"
                shelf (gr/get-shelf-by-name goodreads-client name)]
            (is (= expected-id (-> shelf zip/xml-zip (zx/xml1-> :id) zx/text)))))))))

(deftest test-get-shelf-books
  ;<?xml version="1.0" encoding="UTF-8"?>
  ;<GoodreadsResponse>
  ;   <Request>
  ;      <authentication>true</authentication>
  ;      <key><![CDATA[EV7wnkrFg211dYRJNf8bg]]></key>
  ;      <method><![CDATA[review_list]]></method>
  ;   </Request>
  ;   <reviews start="1" end="1" total="1">
  ;      <review>
  ;         <id>1263549541</id>
  ;         <book>
  ;            <id type="integer">10281630</id>
  ;            <isbn nil="true" />
  ;            <isbn13 nil="true" />
  ;            <text_reviews_count type="integer">0</text_reviews_count>
  ;            <title>Clojure in Small Pieces</title>
  ;            <image_url>https://s.gr-assets.com/assets/nophoto/book/111x148-bcc042a9c91a29c1d680899eff700a03.png</image_url>
  ;            <small_image_url>https://s.gr-assets.com/assets/nophoto/book/50x75-a91bf249278a81aabab721ef782c4a74.png</small_image_url>
  ;            <large_image_url />
  ;            <link>https://www.goodreads.com/book/show/10281630-clojure-in-small-pieces</link>
  ;            <num_pages />
  ;            <format />
  ;            <edition_information />
  ;            <publisher />
  ;            <publication_day />
  ;            <publication_year />
  ;            <publication_month />
  ;            <average_rating>5.00</average_rating>
  ;            <ratings_count>1</ratings_count>
  ;            <description>A literate programming port of the source for Clojure.</description>
  ;            <authors>
  ;               <author>
  ;                  <id>7145075</id>
  ;                  <name>Rich Hickey</name>
  ;                  <role />
  ;                  <image_url><![CDATA[https://s.gr-assets.com/assets/nophoto/user/u_200x266-e183445fd1a1b5cc7075bb1cf7043306.png]]></image_url>
  ;                  <small_image_url><![CDATA[https://s.gr-assets.com/assets/nophoto/user/u_50x66-632230dc9882b4352d753eedf9396530.png]]></small_image_url>
  ;                  <link><![CDATA[https://www.goodreads.com/author/show/7145075.Rich_Hickey]]></link>
  ;                  <average_rating>5.00</average_rating>
  ;                  <ratings_count>1</ratings_count>
  ;                  <text_reviews_count>0</text_reviews_count>
  ;               </author>
  ;            </authors>
  ;            <published />
  ;         </book>
  ;         <rating>0</rating>
  ;         <votes>0</votes>
  ;         <spoiler_flag>false</spoiler_flag>
  ;         <spoilers_state>none</spoilers_state>
  ;         <shelves>
  ;            <shelf name="to-read" exclusive="true" review_shelf_id="969361495" sortable="true" />
  ;         </shelves>
  ;         <recommended_for />
  ;         <recommended_by />
  ;         <started_at />
  ;         <read_at />
  ;         <date_added>Fri Apr 24 19:24:43 -0700 2015</date_added>
  ;         <date_updated>Fri Apr 24 19:24:44 -0700 2015</date_updated>
  ;         <read_count />
  ;         <body />
  ;         <comments_count>0</comments_count>
  ;         <url><![CDATA[https://www.goodreads.com/review/show/1263549541]]></url>
  ;         <link><![CDATA[https://www.goodreads.com/review/show/1263549541]]></link>
  ;         <owned>0</owned>
  ;      </review>
  ;   </reviews>
  ;</GoodreadsResponse>
  (testing "testing get shelf books"
    (let [goodreads-client (gr/new-goodreads-client (default-oauth-client) (env :goodreads-access-token) nil)]
      (testing "simple request"
        (let [resp (gr/get-shelf-books goodreads-client "to-read")
              expected "Clojure in Small Pieces"
              expected-id "10281630"
              book (-> resp
                       zip/xml-zip
                       (zx/xml1-> :reviews :review :book [:title expected]))]
          (is (= expected-id (-> book (zx/xml1-> :id) zx/text))))))))
