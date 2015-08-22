(ns shelver.user
  (:require [datomic.api :as d :refer [db q]]
            [shelver.util :refer :all]
            [shelver.crypto :as crypto]
            [shelver.oauth :as oauth]
            [shelver.user-dao :as ud]))

(defn validate-user [{:keys [email password]}]
  (and email (re-matches #"^.+@.+$" email) password))

(defn normalize-user [user]
  (-> (select-keys user [:email :password])
      (update-in [:email] clojure.string/lower-case)))

(defn register-user [datomic crypto-client oauth-client dirty-user]
  (when (validate-user dirty-user)
    (let [request-token (-> (oauth/request-token oauth-client nil)
                            (assoc :type :request))
          user (-> (normalize-user dirty-user)
                   (assoc :oauth-token request-token))]
      (when @(ud/create-user (:conn datomic) crypto-client user)
        (->> request-token
             (oauth/user-approval-uri oauth-client)
             (vector user))))))

;(defn connect-user [datomic oauth-client user-email]
;  (when-let [existing-user (ud/find-user (db (:conn datomic)) (clojure.string/lower-case user-email))]
;    (when-not (:oauth-token existing-user)
;      (let [request-token (-> (oauth/request-token oauth-client nil)
;                              (assoc :type :request))]
;        (when @(ud/create-user (:conn datomic) crypto-client user)
;          (->> request-token
;               (oauth/user-approval-uri oauth-client)
;               (vector user)))))))

(defn confirm-registration [datomic oauth-client oauth_token dirty-user-email]
  (let [user-email (clojure.string/lower-case dirty-user-email)
        datomic-db (db (:conn datomic))]
    (when-let [found-token (ud/find-oauth-token datomic-db user-email oauth_token :request)]
      (let [access-token (oauth/access-token oauth-client found-token nil)]
        @(ud/update-oauth-token (:conn datomic) (merge found-token {:type :access} access-token))))))

(defn login [datomic crypto-client dirty-user]
  (when (validate-user dirty-user)
    (let [user (normalize-user dirty-user)
          datomic-db (db (:conn datomic))
          existing-user (ud/find-user datomic-db (:email user))]
      (when (crypto/verify-password crypto-client (:password-hash existing-user) (:password user) (:password-salt existing-user))
        existing-user))))