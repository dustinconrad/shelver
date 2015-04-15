(defproject
  shelver "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.danielsz/system "0.1.6"]
                 [environ "1.0.0"]
                 [ring "1.3.2"]
                 [hiccup "1.0.5"]
                 [ring/ring-defaults "0.1.4"]
                 [compojure "1.3.3"]]
  :main ^:skip-aot shelver.core
  :target-path "target/%s"
  :profiles {:dev     {:source-paths ["dev"]
                       :env          {:http-port 3000}}
             :prod    {:env          {:http-port 8000
                                      :repl-port 8001}
                       :dependencies [[org.clojure/tools.nrepl "0.2.5"]]}
             :uberjar {:aot :all}})
