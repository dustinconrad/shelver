(ns shelver.util)

(defmacro dbg [x]
  `(let [x# ~x]
     (prn "dbg:" '~x "=" x#)
     x#))

(defmacro dbg-v [x]
  `(let [x# ~x]
     (prn "dbg:" x#)
     x#))

(defmacro trace [s x]
  `(let [x# ~x]
     (prn "trace: " ~s)
     x#))