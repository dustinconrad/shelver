(ns shelver.datomic
  (:require [com.stuartsierra.component :as component]
            [datomic.api :as d :refer [db q]]
            [io.rkn.conformity :as c]))

(defrecord Datomic [uri conn schema]
  component/Lifecycle
  (start [component]
    (let [_ (d/create-database uri)
          conn (d/connect uri)]
      (->> (c/load-schema-rsc schema)
           (c/ensure-conforms conn))
      (assoc component :conn conn)))
  (stop [component]
    (assoc component :conn nil)))

(defn new-datomic-db [uri schema]
  (map->Datomic {:uri uri :schema schema}))