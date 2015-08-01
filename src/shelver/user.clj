(ns shelver.user
  (:require [datomic.api :as d :refer [db q]]
            [shelver.oauth :as oauth]
            [shelver.user-dao :as ud]))

(defn validate-user [{:keys [email password]}]
  (and email (re-matches #"^.+@.+$" email) password))

(defn register-user [datomic crypto-client oauth-client user]
  (when (validate-user user)
    (let [request-token (-> (oauth/request-token oauth-client nil)
                            (assoc :type :request))]
      (when @(ud/create-user (:conn datomic) crypto-client (assoc user :oauth-token request-token))
        (->> request-token
             (oauth/user-approval-uri oauth-client))))))

(defn confirm-registration [datomic oauth-client oauth_token user-email]
  (let [datomic-db (db (:conn datomic))]
    (when-let [found-token (ud/find-oauth-token datomic-db user-email oauth_token :request)]
      (let [access-token (oauth/access-token oauth-client found-token nil)]
        @(ud/update-oauth-token (:conn datomic) (merge found-token {:type :access} access-token))))))
