(ns shelver.user
  (:require [datomic.api :as d :refer [db q]]
            [shelver.crypto :as crypto]
            [shelver.util :refer :all]))

(defn- ->entity [ns model]
  (into {} (map (fn [[k v]]
                  (let [new-key (keyword ns (name k))
                        new-val (if (keyword? v)
                                  (keyword (format "%s.%s/%s" ns k v))
                                  v)]
                    (vector new-key new-val)))
                model)))

(defn ->model [entity]
  (into {} (map (fn [[k v]]
                  (let [new-key (keyword (name k))
                        new-val (if (map? v)
                                  (->model v)
                                  v)]
                    (vector new-key new-val)))
                entity)))

(defn create-user [datomic-conn crypto-client {:keys [email password oauth-token]}]
  (let [[hash salt] (crypto/encrypt-password crypto-client password)
        user-entity {:db/id              #db/id[:db.part/user]
                     :user/email         email
                     :user/password-hash hash
                     :user/password-salt salt}]
    (d/transact
      datomic-conn
      (if oauth-token
        (let [token-entity (-> (->entity "oauth-token" oauth-token)
                               (assoc :db/id #db/id[:db.part/user -1]))]
          [token-entity
           (assoc user-entity :user/oauth-token #db/id[:db.part/user -1])])
        [user-entity]))))

(defn find-user [datomic-db email]
  (-> (q '[:find (pull ?u [* {:user/oauth-token [:db/id :oauth-token/oauth_token :oauth-token/oauth_token_secret]}])
           :in $ ?email
           :where
           [?u :user/email ?email]]
         datomic-db
         email)
      ffirst
      ->model))