(ns jobtech-taxonomy-api.db.api-util
  (:require
   [clojure.set :as set]
   )
  )

#_(defn transform-replaced-by [concept]
  (set/rename-keys concept {:concept/id :id
                        :concept/definition :definition
                        :concept/type :type
                        :concept/preferred-label :preferred-label
                        :concept/deprecated :deprecated })
 )


#_(defn rename-concept-keys-for-api [concept]
  (let [renamed-concept (set/rename-keys concept {:concept/preferred-label :preferred-label, :concept/id :id, :concept/definition :definition, :concept/type :type :concept/deprecated :deprecated :concept/replaced-by :replaced-by})]

    (if (:replaced-by renamed-concept)
      (update renamed-concept :replaced-by #(map transform-replaced-by %))
      renamed-concept
      )
    )
  )

(defn move-relations-into-concept [[concept broader narrower related affinity]]
  (merge {:relations {:broader broader
                      :narrower narrower
                              :related related
                              :affinity affinity
                      }}
         concept)
  )

(defn parse-find-concept-datomic-result [result]
  (->> result
       (map move-relations-into-concept)
       ;;(map rename-concept-keys-for-api)
       )
  )

(defn parse-seach-concept-datomic-result [result]
  (->> result
       (map first)
       ;;(map rename-concept-keys-for-api)
       )
  )

(defn pagination
  [coll offset limit]
  (cond
    (and coll offset limit) (take limit (drop offset coll))
    (and coll limit) (take limit coll)
    (and coll offset) (drop offset coll)
    :else coll
    )
  )
