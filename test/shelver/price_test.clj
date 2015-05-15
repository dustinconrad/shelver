(ns shelver.price-test
  (:require [shelver.price :as p]
            [clojure.test :refer :all]
            [clojure.zip :as zip]
            [clojure.data.xml :as xml]))

(deftest test-xml->book-info
  (testing "testing various xml strings"
    (let [xml-string "<book>\r\n  <id type=\"integer\">23019295</id>\r\n  <isbn>0062385321</isbn>\r\n  <isbn13>9780062385321</isbn13>\r\n  <text_reviews_count type=\"integer\">10</text_reviews_count>\r\n  <title>When to Rob a Bank</title>\r\n  <image_url>https://d.gr-assets.com/books/1428535442m/23019295.jpg</image_url>\r\n  <small_image_url>https://d.gr-assets.com/books/1428535442s/23019295.jpg</small_image_url>\r\n  <large_image_url/>\r\n  <link>https://www.goodreads.com/book/show/23019295-when-to-rob-a-bank</link>\r\n  <num_pages>400</num_pages>\r\n  <format>Hardcover</format>\r\n  <edition_information/>\r\n  <publisher>William Morrow </publisher>\r\n  <publication_day>5</publication_day>\r\n  <publication_year>2015</publication_year>\r\n  <publication_month>5</publication_month>\r\n  <average_rating>3.60</average_rating>\r\n  <ratings_count>99</ratings_count>\r\n  <description>In celebration of the 10th anniversary of the landmark book Freakonomics comes this curated collection from the most readable economics blog in the universe. It\u2019s the perfect solution for the millions of readers who love all things Freakonomics. Surprising and erudite, eloquent and witty, When to Rob a Bank demonstrates the brilliance that has made the Freakonomics guys an international sensation, with more than 7 million books sold in 40 languages, and 150 million downloads of their Freakonomics Radio podcast.&lt;br&gt;&lt;br&gt;When Freakonomics was first published, the authors started a blog\u2014and they\u2019ve kept it up. The writing is more casual, more personal, even more outlandish than in their books. In When to Rob a Bank, they ask a host of typically off-center questions: Why don\u2019t flight attendants get tipped? If you were a terrorist, how would you attack? And why does KFC always run out of fried chicken?&lt;br&gt;&lt;br&gt;Over the past decade, Steven D. Levitt and Stephen J. Dubner have published more than 8,000 blog posts on Freakonomics.com. Many of them, they freely admit, were rubbish. But now they\u2019ve gone through and picked the best of the best. You\u2019ll discover what people lie about, and why; the best way to cut gun deaths; why it might be time for a sex tax; and, yes, when to rob a bank. (Short answer: never; the ROI is terrible.) You\u2019ll also learn a great deal about Levitt and Dubner\u2019s own quirks and passions, from gambling and golf to backgammon and the abolition of the penny. &lt;br&gt;&lt;br&gt;Steven D. Levitt, a professor of economics at the University of Chicago, was awarded the John Bates Clark medal, given to the most influential American economist under forty. He is also a founder of The Greatest Good, which applies Freakonomics-style thinking to business and philanthropy.&lt;br&gt;&lt;br&gt;Stephen J. Dubner is an award-winning author, journalist, and radio and TV personality. He quit his first career\u2014as an almost-rock-star\u2014to become a writer. He has since taught English at Columbia, worked for The New York Times and published three non-Freakonomics books.</description>\r\n<authors>\r\n<author>\r\n<id>798</id>\r\n<name>Steven D. Levitt</name>\r\n<role></role>\r\n<image_url>\r\n<![CDATA[https://d.gr-assets.com/authors/1215370905p5/798.jpg]]>\r\n</image_url>\r\n<small_image_url>\r\n<![CDATA[https://d.gr-assets.com/authors/1215370905p2/798.jpg]]>\r\n</small_image_url>\r\n<link><![CDATA[https://www.goodreads.com/author/show/798.Steven_D_Levitt]]></link>\r\n<average_rating>3.88</average_rating>\r\n<ratings_count>502635</ratings_count>\r\n<text_reviews_count>16232</text_reviews_count>\r\n</author>\r\n</authors>\r\n  <published>2015</published>\r\n</book>"
          book-info (-> xml-string
                        java.io.StringReader.
                        xml/parse
                        p/xml->book-info)]
      (is (= ((juxt :isbn :isbn13 :title :authors) book-info)
             ["0062385321" "9780062385321" "When to Rob a Bank" #{"Steven D. Levitt"}])))))
