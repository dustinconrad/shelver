(ns shelver.user
  (:require [datomic.api :as d :refer [db q]]
            [shelver.crypto :as crypto]))

(defn create-user [datomic-conn crypto-client {:keys [email password]}]
  (let [[hash salt] (crypto/encrypt-password crypto-client password)]
    (d/transact
      datomic-conn
      [{:db/id              #db/id[:db.part/user]
        :user/email         email
        :user/password-hash hash
        :user/password-salt salt}])))

(defn find-user [datomic-db email]
  (-> (q '[:find (pull ?u [*])
           :in $ ?email
           :where
           [?u :user/email ?email]]
         datomic-db
         email)
      ffirst))