(ns jobtech-taxonomy-api.db.daynotes
  (:refer-clojure :exclude [type])
  (:require
   [datomic.client.api :as d]
   [schema.core :as s]
   [jobtech-taxonomy-api.db.api-util :as u]
   [jobtech-taxonomy-api.db.database-connection :refer :all]
   [jobtech-taxonomy-api.config :refer [env]]
   [mount.core :refer [defstate]]
   [clojure.set :refer :all]
   )
  )

;; TODO Utred om vi ska ha egna attribut för created updated at

(def show-concept-entity-id
  '[:find ?e ?concept-id
    :in $ ?concept-id
    :where
    [?e :concept/id ?concept-id ]
    ]
  )

(def show-data-made-by-user-id
  '[:find ?user-id ?attr ?v
    :in $
    :where
    [?tx :taxonomy-user/id ?user-id]
    [?e ?a ?v ?tx ]
    [?a :db/ident ?attr]
    ]
  )

;; (d/q show-data-made-by-user-id (get-db))


(def show-concept-relations-entity-ids
  '[:find ?e ?concept-id
    :in $ ?concept-id
    :where
    [?e :concept/id ?concept-id ]
    ]
  )


(def fetch-all-relations-entity-ids-for-concept-query
  '[:find ?tx ?inst  ?user-id ?input-concept-id ?pl-1 ?added-1
    ?concept-id-2 ?pl-2  ?added-2 ?rt ?input-concept-is-source
    :in $ ?input-concept-id
    :where
    [?r  :relation/type ?rt]
    (or-join [?r ?c1 ?c2 ?tx ?added-1 ?added-2
              ?input-concept-id ?pl-1 ?concept-id-2 ?pl-2 ?input-concept-is-source]

             (and      [?r :relation/concept-1 ?c1 ?tx ?added-1]
                       [?r :relation/concept-2 ?c2 ?tx ?added-2]

                       [?c1 :concept/id ?input-concept-id]
                       [?c1 :concept/preferred-label ?pl-1]

                       [?c2 :concept/id ?concept-id-2]
                       [?c2 :concept/preferred-label ?pl-2]

                       [(ground true) ?input-concept-is-source]
                       )
             (and
              [?r :relation/concept-1 ?c2 ?tx ?added-2]
              [?r :relation/concept-2 ?c1 ?tx ?added-1]

              [?c2 :concept/id ?concept-id-2]
              [?c2 :concept/preferred-label ?pl-2]

              [?c1 :concept/id ?input-concept-id]
              [?c1 :concept/preferred-label ?pl-1]
              [(ground false) ?input-concept-is-source]
              )
             )
    [?tx :taxonomy-user/id ?user-id]
    [?tx :db/txInstant ?inst]
    ]
  )


(defn convert-relation-datoms-to-events [{:keys [transaction-id timestamp user-id
                                                 concept-id-1 preferred-label-1
                                                 concept-id-2 preferred-label-2
                                                 added-1 relation-type
                                                 concept-1-is-source
                                                 ]}]
  (let [concept-1 {:concept/id concept-id-1
                   :concept/preferred-label preferred-label-1
                   }
        concept-2 {:concept/id concept-id-2
                   :concept/preferred-label preferred-label-2
                   }
        ]
    {:event-type (if added-1 "CREATED" "DEPRECATED")
     :timestamp timestamp
     :transaction-id transaction-id
     :user-id user-id
     :relation {
                :relation-type relation-type
                :source (if concept-1-is-source concept-1 concept-2)
                :target (if concept-1-is-source concept-2 concept-1)
                }
     })
  )


(defn get-relation-history [concept-id]
  (let [result (d/q fetch-all-relations-entity-ids-for-concept-query (get-db-hist (get-db)) concept-id)]
    (map #(->> % (map vector [:transaction-id
                              :timestamp
                              :user-id
                              :concept-id-1
                              :preferred-label-1
                              :added-1
                              :concept-id-2
                              :preferred-label-2
                              :added-2
                              :relation-type
                              :concept-1-is-source
                              ]) (into {})) result)
    )
  )

(defn get-automatic-day-notes-for-relation [concept-id]
  (map convert-relation-datoms-to-events (get-relation-history concept-id))
  )

;; list transactions made by user "0"
;; (d/q '[:find ?tx  :in $ ?user-id :where [?tx :taxonomy-user/id ?user-id]] (get-db-hist (get-db))  "0")

;; (d/q '[:find ?tx ?user-id :in $ :where [?tx :taxonomy-user/id ?user-id]] (get-db-hist (get-db)) )



(defn get-concept-entity-id [concept-id]
  (ffirst (d/q show-concept-entity-id (get-db) concept-id)))

(defn get-entity-history
  "Takes an entity and shows all the transactions that touched this entity.
  Pairs well with clojure.pprint/print-table"
  [eid]
  (->> eid
       (d/q
        '[:find ?e ?attr ?v ?tx ?added ?inst  ?user-id
          :in $ ?e
          :where
          [?e ?a ?v ?tx ?added]
          [?tx :db/txInstant ?inst]
          [?tx :taxonomy-user/id ?user-id]
          [?a :db/ident ?attr]]
        (get-db-hist (get-db)))
       (map #(->> %
                  (map vector [:e :a :v :tx :added :inst :user-id])
                  (into {})))
       (sort-by :tx)))


(defn group-by-transaction-and-entity-id [entity-history]
  (group-by  (juxt #(:tx %) #(:e %)) entity-history)
  )

(defn is-deprecated-event? [entity-transaction-datoms]
  (some #(and  (=  :concept/deprecated (:a %) )  (:v %)) entity-transaction-datoms )
  )

(defn is-created-event? [entity-transaction-datoms]
  (some #(and (:added %) (= :concept/id (:a %))) entity-transaction-datoms )
  )

(defn is-update-event?  [entity-transaction-datoms]
  (not (apply = (map #(:added %) entity-transaction-datoms)))
  )


(defn created-event-reducer [acc element]
  (-> acc
      (assoc (:a element) (:v element))
      (assoc :timestamp (:inst element))
      (assoc :transaction-id (:tx element ))
      (assoc :user-id (:user-id element))
      )
  )

(defn reduce-created-event [entity-transaction-datoms]
  (let [concept (reduce created-event-reducer {} entity-transaction-datoms)
        timestamp (:timestamp concept)
        transaction-id (:transaction-id concept)
        user-id (:user-id concept)
        concept-no-timestamp (-> concept
                                 (dissoc  :timestamp)
                                 (dissoc :transaction-id)
                                 (dissoc :user-id)
                                 )
        ]
    {:event-type "CREATED"
     :timestamp timestamp
     :transaction-id transaction-id
     :user-id user-id
     :concept concept-no-timestamp
     }
    )
  )

(defn handle-updated-attribute [grouped]
  (let [attribute (first grouped)
        changes (second grouped)
        added (first (map :v (filter :added changes)))
        removed (first (map :v (filter #(not (:added %)) changes)))
        ]
    {:attribute attribute
     :new-value added
     :old-value removed
     }
    )
  )

(defn reduce-updated-event [entity-transaction-datoms]
  (let [grouped (group-by :a entity-transaction-datoms)
        changes (map handle-updated-attribute grouped)
        timestamp (:inst (first entity-transaction-datoms))
        transaction-id (:tx (first entity-transaction-datoms))
        user-id (:user-id (first entity-transaction-datoms))
        ]

    {:event-type "UPDATED"
     :timestamp timestamp
     :transaction-id transaction-id
     :user-id user-id
     :changes changes
     }
    )
  )

(defn reduce-deprecated-event [entity-transaction-datoms]
  (let [first-datom (first entity-transaction-datoms)]
    {:event-type "DEPRECATED"
     :timestamp (:inst first-datom)
     :transaction-id (:tx first-datom)
     :user-id (:user-id first-datom)
     })

  )

(defn map-entity-transaction-datoms-to-event [group-of-datoms]
  (let [datoms (second group-of-datoms)]
    (cond
      (is-deprecated-event? datoms) (reduce-deprecated-event datoms)
      (is-created-event? datoms) (reduce-created-event datoms)
      (is-update-event? datoms) (reduce-updated-event datoms)
      )
   )
  )

(defn map-entity-transaction-datoms-to-events [entity-transaction-datoms]
  (map map-entity-transaction-datoms-to-event entity-transaction-datoms)
  )

(defn get-automatic-day-notes-for-concept [concept-id]
  (-> concept-id
      get-concept-entity-id
      get-entity-history
      group-by-transaction-and-entity-id
      map-entity-transaction-datoms-to-events
      )
  )
