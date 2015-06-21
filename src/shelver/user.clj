(ns shelver.user
  (:require [datomic.api :as d :refer [db q]]
            [shelver.crypto :as crypto]))

(defn create-user [datomic-db crypto-client {:keys [email password]}]
  (let [[hash salt] (crypto/encrypt-password crypto-client password)]
    (d/transact
      (:conn datomic-db)
      [{:db/id              #db/id[:db.part/user]
        :user/email         email
        :user/password-hash hash
        :user/password-salt salt}])))

(defn find-user [datomic-db email]
  (-> (q '[:find (pull ?u [*])
           :in $ ?email
           :where
           [?u :user/email ?email]]
         (db (:conn datomic-db))
         email)
      ffirst))