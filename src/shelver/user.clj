(ns shelver.user
  (:require [datomic.api :as d :refer [db q]]
            [shelver.crypto :as crypto]
            [shelver.util :refer :all]))

(defn- ->entity [ns model]
  (into {} (map (fn [[k v]]
                  (let [new-key (if (= "id" (name k))
                                  (keyword "db" (name k))
                                  (keyword ns (name k)))
                        new-val (if (keyword? v)
                                  (keyword (format "%s.%s/%s" ns (name k) (name v)))
                                  v)]
                    (vector new-key new-val)))
                model)))

(defn ->model [entity]
  (into {} (map (fn [[k v]]
                  (let [new-key (keyword (name k))
                        new-val (if (map? v)
                                  (if (keyword? (:db/ident v))
                                    (keyword (name (:db/ident v)))
                                    (->model v))
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
  (some-> (q '[:find (pull ?u [* {:user/oauth-token [*]}])
           :in $ ?email
           :where
           [?u :user/email ?email]]
         datomic-db
         email)
      ffirst
      ->model))

(defn find-oauth-token [datomic-db email oauth-token-string]
  (some-> (q '[:find (pull ?o [*])
           :in $ ?e ?t
           :where
           [?u :user/email ?e]
           [?u :user/oauth-token ?o]
           [?o :oauth-token/oauth_token ?t]]
         datomic-db
         email
         oauth-token-string)
      ffirst
      ->model))

(defn update-oauth-token [datomic-conn oauth-token]
  (let [token-entity (->entity "oauth-token" oauth-token)]
    (d/transact
      datomic-conn
      [token-entity])))