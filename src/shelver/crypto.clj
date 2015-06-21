(ns shelver.crypto
  (:require [shelver.util :refer :all])
  (:import [java.security SecureRandom]
           [javax.crypto.spec PBEKeySpec]
           [javax.crypto SecretKeyFactory]
           [java.util Base64]))

(defn random-bytes-raw [size]
  (doto (byte-array size)
    (#(-> (SecureRandom.) (.nextBytes %)))))

(defn ->b64url [byte-array]
  (-> (Base64/getUrlEncoder)
      (.encodeToString byte-array)))

(defn ->bytes [^String b64url]
  (-> (Base64/getUrlDecoder)
      (.decode b64url)))

(defn random-bytes [size]
  ((comp ->b64url random-bytes-raw) size))

(defn hash-password-raw [iterations target-size password salt-bytes]
  (let [spec (PBEKeySpec. (char-array password) salt-bytes iterations (* 8 target-size))]
    (-> (SecretKeyFactory/getInstance "PBKDF2WithHmacSHA256")
        (.generateSecret spec)
        (.getEncoded))))

(defn eq? [a b]
  (let [initial (bit-xor (count a) (count b))]
    (->> (map vector a b)
         (reduce
           #(bit-or %1 (apply bit-xor %2))
           initial)
         zero?)))

(defn hash-password [iterations target-size password salt]
  ((comp ->b64url hash-password-raw) iterations target-size password (->bytes salt)))

(defn check-password-raw [iterations target-size expected-bytes candidate salt-bytes]
  (let [candidate-bytes (hash-password-raw iterations target-size candidate salt-bytes)]
    (eq? expected-bytes candidate-bytes)))

(defn check-password [iterations target-size expected candidate salt]
  (check-password-raw iterations target-size (->bytes expected) candidate (->bytes salt)))

(defprotocol CryptoClient
  (encrypt-password [this password])
  (verify-password [this expected candidate salt]))

(defrecord DefaultCryptoClient [iterations target-size salt-size]
  CryptoClient
  (encrypt-password [this password]
    (let [salt (random-bytes salt-size)]
      [(hash-password iterations target-size password salt) salt]))
  (verify-password [this expected candidate salt]
    (check-password iterations target-size expected candidate salt)))

(defn new-crypto-client [iterations target-size salt-size]
  (map->DefaultCryptoClient {:iterations iterations :target-size target-size :salt-size salt-size}))