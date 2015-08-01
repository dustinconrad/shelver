(ns shelver.user
  (:require [datomic.api :as d :refer [db q]]
            [shelver.oauth :as oauth]
            [shelver.user-dao :as user]))

(defn validate-user [{:keys [email password]}]
  (and email (re-matches #"^.+@.+$" email) password))

(defn register-user [datomic crypto-client oauth-client user]
  (when (validate-user user)
    (let [request-token (-> (oauth/request-token oauth-client nil)
                            (assoc :type :request))]
      (when @(user/create-user (:conn datomic) crypto-client (assoc user :oauth-token request-token))
        (->> request-token
             (oauth/user-approval-uri oauth-client))))))

