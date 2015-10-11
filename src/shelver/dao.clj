(ns shelver.dao)

(defn resolve-pattern [entity-ns pattern model]
  (if (some (partial = *) pattern)
    (let [sub-patterns (->> (filter map? pattern)
                            (apply merge)
                            (into {}))]
      (->> (keys model)
           (map #(let [kn (name %)]
                  (if (= kn "id")
                    :db/id
                    (keyword entity-ns kn))))
           (remove sub-patterns)
           (#(if (seq sub-patterns)
              (cons sub-patterns %)
              %))
           (concat (remove (partial = *) pattern))
           vec))
    pattern))

;(defn ->entities [ns model]
;  (loop [entity {}
;         sub-entities '()
;         [[k v] & kvs] (seq model)]
;    (if k
;      (let [new-key (if (= "id" (name k))
;                      (keyword "db" (name k))
;                      (keyword ns (name k)))
;            [sube & _ :as subes] (when (map? v)
;                                   (-> (->entities (name k) v)
;                                       (update-in [0 :db/id] #(or % (d/tempid :db.part/user)))))
;            new-val (cond
;                      (keyword? v) (->enum ns k v)
;                      (some? sube) (get sube :db/id)
;                      :default v)]
;        (recur (assoc entity new-key new-val)
;               (concat sub-entities subes)
;               kvs))
;      (vec (conj sub-entities entity)))))

;(defn entities [ns model]
;  (loop [entity {}
;         sub-entities '()
;         [[k v] & kvs] (seq model)]
;    (if k
;      (let [new-key (if (= "id" (name k))
;                      (keyword "db" (name k))
;                      (keyword ns (name k)))
;            [sube & _ :as subes] (when (map? v)
;                                   (-> (entities (name k) v)
;                                       (update-in [0 :db/id] #(or % (d/tempid :db.part/user)))))
;            new-val (cond
;                      (keyword? v) (->enum ns k v)
;                      (some? sube) (get sube :db/id)
;                      :default v)]
;        (recur (assoc entity new-key new-val)
;               (concat sub-entities subes)
;               kvs))
;      (vec (conj sub-entities entity)))))

;(let [model-key (name p)
;      model-value (get model model-key)
;      entity-val (cond
;                   (keyword? model-value) (->enum ns model-key model-value)
;                   (map? model-value) (-> (select-keys model-value id-kw)
;                                          (update-in [id-kw] #(or % (d/tempid :db.part/user))))
;                   :default model-value)]
;  )

;(defn ->entities [id-kw ns pattern model]
;  (let [resolved-pattern (resolve-pattern id-kw pattern ns model)
;        ->entity-ref (fn [full-entity]
;                       (-> (select-keys full-entity [id-kw])
;                           (update-in [id-kw] #(or % (d/tempid :db.part/user)))))]
;    (reduce
;      (fn [[entity sub-entities :as acc] p]
;        (if (map? p)
;          (reduce
;            (fn [[entity sub-entities] [field field-pattern]]
;              (let [[e se] (->entities id-kw field field-pattern (get model (name field)))
;                    entity-ref (->entity-ref e)]
;                [(assoc entity field entity-ref) (-> (into sub-entities se)
;                                                     (conj e))]))
;            acc
;            p)
;          ))
;      [{} []]
;      resolved-pattern)))

;(defn resolve-pattern [id-kw ns pattern model]
;  (if (some (partial = *) pattern)
;    (let [sub-patterns (->> (filter map? pattern)
;                            (apply merge)
;                            (into {}))]
;      (->> (keys model)
;           (map #(if (= "id" (name %))
;                  id-kw
;                  %))
;           (map #(keyword (or (namespace %) ns) (name %)))
;           (remove sub-patterns)
;           (cons sub-patterns)
;           vec))
;    pattern))