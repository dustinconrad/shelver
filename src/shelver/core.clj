(ns shelver.core
  (:gen-class)
  (:require
    [reloaded.repl :refer [system init start stop go reset]]
    [shelver.systems :refer [dev-system]]))

(defn -main
  "Start a dev system."
  [& args]
  (reloaded.repl/set-init! dev-system)
  (go))
