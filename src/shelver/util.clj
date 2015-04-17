(ns shelver.util)

(defmacro dbg [x]
  `(let [x# ~x]
     (println "dbg:" '~x "=" x#)
     x#))

(defmacro dbg-v [x]
  `(let [x# ~x]
     (println "dbg:" x#)
     x#))

(defmacro trace [s x]
  `(let [x# ~x]
     (println "trace: " ~s)
     x#))