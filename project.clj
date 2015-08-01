(defproject
  shelver "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :repositories {"my.datomic.com" {:url "https://my.datomic.com/repo"
                                   :creds :gpg}}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [com.stuartsierra/component "0.2.3"]
                 [reloaded.repl "0.1.0"]
                 [environ "1.0.0"]
                 [ring "1.3.2"]
                 [enlive "1.1.5"]
                 [ring/ring-defaults "0.1.4"]
                 [com.taoensso/timbre "4.0.2"]
                 [compojure "1.3.3"]
                 [clj-oauth "1.5.2"]
                 [clj-http "1.1.0"]
                 [org.clojure/data.xml "0.0.8"]
                 [org.clojure/data.zip "0.1.1"]
                 [buddy/buddy-auth "0.6.0"]
                 [com.datomic/datomic-pro "0.9.5173"]
                 [io.rkn/conformity "0.3.4" :exclusions [com.datomic/datomic-free]]]
  :plugins [[lein-gorilla "0.3.4"]
            [lein-environ "1.0.0"]]
  :main ^:skip-aot shelver.core
  :target-path "target/%s"
  :profiles {:goodreads-api  {:env {:goodreads-api-key    "your-goodreads-key"
                                    :goodreads-api-secret "your-goodreads-secret"}}
             :google-api     {:env {:google-api-key "your-google-key"}}
             :goodreads-test {:env {:goodreads-access-token {:oauth_token        "oauth-test-token"
                                                             :oauth_token_secret "oauth-test-token-secret"}}}
             :crypto-client  {:env {:iterations  "number"
                                    :target-size "bytes"
                                    :salt-size   "bytes"}}

             :dev            [{:source-paths ["dev"]
                               :env          {:http-port   3000
                                              :test-user-id 45511156
                                              :datomic-uri "datomic:dev://datomic-db:4334/shelver"}}
                              :goodreads-api
                              :google-api
                              :crypto-client
                              :goodreads-test]
             :test           [{:test-user-id 45511156} :goodreads-api :google-api :goodreads-test]
             :prod           {:env          {:http-port 8000
                                             :repl-port 8001}
                              :dependencies [[org.clojure/tools.nrepl "0.2.5"]]}
             :uberjar        {:aot :all}})
