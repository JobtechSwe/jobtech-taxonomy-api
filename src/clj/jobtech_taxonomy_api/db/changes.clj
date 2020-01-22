(ns jobtech-taxonomy-api.db.changes
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

(def show-version-instance-ids
  '[:find ?inst ?version
    :in $
    :where
    [?t :taxonomy-version/id ?version ?inst]
    ]
  )

(def show-latest-version-id
  '[:find (max ?version)
    :in $
    :where
    [?t :taxonomy-version/id ?version]
    ]
  )

(def show-updated-concepts
  '[:find ?e ?aname ?v ?tx ?inst ?concept-id ?preferred-label ?type ?concept-id-tx ?preferred-label-tx ?type-tx
    :in $ ?one-version-before-from-version ?to-version
    :where
    [?a :db/ident :concept/preferred-label]
    [?e :concept/id ?concept-id ?concept-id-tx]
    [?e :concept/preferred-label ?preferred-label ?preferred-label-tx]
    [?e :concept/type ?type ?type-tx]
    [?e ?a ?v ?tx true]
    (not [(= ?v ?preferred-label)])
    [(= ?tx ?preferred-label-tx)]
    [?a :db/ident ?aname]
    [?tx :db/txInstant ?inst]

    [?fv :taxonomy-version/id ?one-version-before-from-version ?one-version-before-from-version-tx]
    [?one-version-before-from-version-tx :db/txInstant ?one-version-before-from-version-inst]
    [(< ?one-version-before-from-version-inst ?inst)]
    [?tv :taxonomy-version/id ?to-version ?to-version-tx]
    [?to-version-tx :db/txInstant ?to-version-inst]
    [(> ?to-version-inst ?inst)]

    ])

(def show-created-concepts
  '[:find ?e ?v ?tx ?inst ?concept-id ?preferred-label ?type ?concept-id-tx ?preferred-label-tx ?type-tx
    :in $ ?one-version-before-from-version ?to-version
    :where
    [?e :concept/id ?concept-id ?concept-id-tx]
    [?e :concept/preferred-label ?preferred-label ?preferred-label-tx]
    [?e :concept/type ?type ?type-tx]
    [?e :concept/preferred-label ?v ?tx true]
    [?tx :db/txInstant ?inst]

    [(= ?v ?preferred-label)]
    [(= ?tx ?preferred-label-tx)]
    [(= ?tx ?concept-id-tx)]
    [(= ?tx ?type-tx)]


    [?fv :taxonomy-version/id ?one-version-before-from-version ?one-version-before-from-version-tx]
    [?one-version-before-from-version-tx :db/txInstant ?one-version-before-from-version-inst]
    [(< ?one-version-before-from-version-inst ?inst)]
    [?tv :taxonomy-version/id ?to-version ?to-version-tx]
    [?to-version-tx :db/txInstant ?to-version-inst]
    [(> ?to-version-inst ?inst)]

    ]
  )


(def show-deprecated-concepts
  '[:find ?e ?tx ?inst ?concept-id ?preferred-label ?type ?concept-id-tx ?preferred-label-tx ?type-tx
    :in $ ?one-version-before-from-version ?to-version
    :where

    [?e :concept/id ?concept-id ?concept-id-tx]
    [?e :concept/preferred-label ?preferred-label ?preferred-label-tx]
    [?e :concept/type ?type ?type-tx]
    [?e :concept/deprecated true ?tx true]
    [?tx :db/txInstant ?inst]

  ;  [(= ?v ?preferred-label)]
  ;  [(= ?tx ?preferred-label-tx)]
  ;  [(= ?tx ?concept-id-tx)]
  ;  [(= ?tx ?type-tx)]


    [?fv :taxonomy-version/id ?one-version-before-from-version ?one-version-before-from-version-tx]
    [?one-version-before-from-version-tx :db/txInstant ?one-version-before-from-version-inst]
    [(< ?one-version-before-from-version-inst ?inst)]
    [?tv :taxonomy-version/id ?to-version ?to-version-tx]
    [?to-version-tx :db/txInstant ?to-version-inst]
    [(> ?to-version-inst ?inst)]

    ]
  )



(defn datom-to-updated-event [[e aname v tx inst concept-id preferred-label type concept-id-tx preferred-label-tx type-tx]]
  {:event-type "UPDATED"
   :transaction-id tx
   :type type
   :timestamp inst
   :concept-id concept-id
   :preferred-label v}
  )

(defn datom-to-created-event [[e v tx inst concept-id preferred-label type concept-id-tx preferred-label-tx type-tx]]
  {:event-type "CREATED"
   :transaction-id tx
   :type type
   :timestamp inst
   :concept-id concept-id
   :preferred-label preferred-label}
  )

(defn datom-to-deprecated-event [[e tx inst concept-id preferred-label type concept-id-tx preferred-label-tx type-tx]]
  {:event-type "DEPRECATED"
   :transaction-id tx
   :type type
   :timestamp inst
   :concept-id concept-id
   :preferred-label preferred-label
   :deprecated true}
  )

(defn convert-transaction-id-to-version-id [versions-with-transatcion-ids transaction-id]
  (let [
        sorted-versions (sort-by first (conj versions-with-transatcion-ids [transaction-id]))
        index-of-next-element-to-transaction-id (.indexOf  sorted-versions [transaction-id])
        version-id (second (nth sorted-versions (inc index-of-next-element-to-transaction-id)))
        ]
    version-id
    )
  )

(defn convert-events-transaction-ids-to-version-ids [events]
  (let [versions (d/q show-version-instance-ids (get-db))]
    (map (fn [event]
           (let [version-id (convert-transaction-id-to-version-id  versions  (:transaction-id event))
                 event-with-version-id (merge event {:version version-id})
                 ]
             event-with-version-id
             ))
         events)
    )
  )

(defn transform-event-result [{:keys [type version preferred-label concept-id event-type deprecated] }]
  {:event-type event-type
   :version version
   :concept (merge (if (true? deprecated) {:deprecated true} {}) ; deprecated optional
                   {:id concept-id,
                    :type type,
                    :preferred-label preferred-label})})


(defn get-updated-concepts [db from-version to-version]
  (d/q show-updated-concepts (get-db-hist db) from-version to-version)
  )

(defn get-created-concepts [db from-version to-version]
  (d/q show-created-concepts (get-db-hist db) from-version to-version)
  )


(defn get-deprecated-concepts [db from-version to-version]
  (d/q show-deprecated-concepts (get-db-hist db) from-version to-version)
  )

(defn transform-event-result [{:keys [type version preferred-label concept-id event-type deprecated] }]
  {:event-type event-type
   :version version
   :concept (merge (if (true? deprecated) {:deprecated true} {}) ; deprecated optional
                   {:id concept-id,
                    :type type,
                    :preferred-label preferred-label})})


(defn get-all-events-between-versions "inclusive" [db from-version to-version]
  (let [updated-events (map datom-to-updated-event
                            (get-updated-concepts db from-version to-version))
        created-events (map datom-to-created-event
                            (get-created-concepts db from-version to-version)
                            )
        deprecated-events (map datom-to-deprecated-event
                            (get-deprecated-concepts db from-version to-version)
                            )

        events (concat updated-events created-events deprecated-events)
        ]

    (convert-events-transaction-ids-to-version-ids
     (sort-by :transaction-id events))
    )
  )

(defn get-all-events-from-version "inclusive" [db from-version]
  (let [latest-version (ffirst (d/q show-latest-version-id db))]
    (get-all-events-between-versions db from-version latest-version)
    )
  )

(defn get-all-events-from-version-with-pagination " v1.0" [from-version to-version offset limit]
  (let [events (if to-version
                 (get-all-events-between-versions (get-db) from-version to-version)
                 (get-all-events-from-version (get-db) from-version)
                 )]
    (u/pagination  (map transform-event-result  events)  offset limit))
  )
