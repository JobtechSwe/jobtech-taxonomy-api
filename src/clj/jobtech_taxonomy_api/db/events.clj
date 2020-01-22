(ns jobtech-taxonomy-api.db.events
  (:refer-clojure :exclude [type])
  (:require
   [datomic.client.api :as d]
   [schema.core :as s]
   [jobtech-taxonomy-api.db.api-util :as u]
   [jobtech-taxonomy-api.db.database-connection :refer :all]
   [jobtech-taxonomy-api.config :refer [env]]
   [mount.core :refer [defstate]]
   [clojure.set :refer :all]
   ))

;; TODO bryt upp i tre olika frågor en för CREATE, DEPRECATE, UPDATE

(def show-history-2
  '[:find ?entity ?aname ?v ?tx ?added  ?concept-id ?preferred-label ?type ?idtx ?pltx ?ttx
    :keys  entity aname v tx added concept-id preferred-label type idtx pltx ttx
    :where
    [?entity :concept/id ?concept-id ?idtx]
    [?entity :concept/preferred-label ?preferred-label ?pltx]
    [?entity :concept/type ?type ?ttx]
    [?entity ?a ?v ?tx ?added]
    [?a :db/ident ?aname]
                                        ;  [?tx :db/txInstant ?inst]
    ]
  )

(def show-concept-history
  '[:find ?e ?aname ?v ?tx ?added ?inst ?concept-id ?preferred-label ?type
    :where
    [?e :concept/id ?concept-id]
    [?e :concept/preferred-label ?preferred-label]
    [?e :concept/type ?type]
    [?e ?a ?v ?tx ?added]
    [?a :db/ident ?aname]
    [?tx :db/txInstant ?inst]])

(def show-concept-history-since-query
  '[:find ?e ?aname ?v ?tx ?added ?inst ?concept-id ?preferred-label ?type ?deprecated
    :in $ ?since
    :where

    [?e :concept/preferred-label ?preferred-label]
    [?e :concept/id ?concept-id]
    [?e :concept/type ?type]
    [(get-else $ ?e :concept/deprecated false) ?deprecated]
    [?e ?a ?v ?tx ?added]
    [?tx :db/txInstant ?inst]
    [(< ?since ?inst)]
    [?a :db/ident ?aname]
    ]
  )

;; TODO skriv om fråga, går inte att lita på vilken den väljer??
;; om fel sak kommer sist så används den...
;; add tx for every attribute, take the largest tx for preferred-label

#_(def show-concept-history-since-version-query
  '[:find ?e ?aname ?v ?tx ?added ?inst ?concept-id ?preferred-label ?type ?deprecated ?concept-id-tx ?preferred-label-tx  ?type-tx
    :in $ ?one-version-before-from-version ?to-version
    :where
    [?e :concept/preferred-label ?preferred-label ?preferred-label-tx]
    [?e :concept/id ?concept-id ?concept-id-tx]
    [?e :concept/type ?type ?type-tx]
    [(get-else $ ?e :concept/deprecated false) ?deprecated]
    [?e ?a ?v ?tx ?added]
    [?tx :db/txInstant ?inst]
    [?fv :taxonomy-version/id ?one-version-before-from-version ?one-version-before-from-version-tx]
    [?one-version-before-from-version-tx :db/txInstant ?one-version-before-from-version-inst]
    [(< ?one-version-before-from-version-inst ?inst)]
    [?tv :taxonomy-version/id ?to-version ?to-version-tx]
    [?to-version-tx :db/txInstant ?to-version-inst]
    [(> ?to-version-inst ?inst)]
    [?a :db/ident ?aname]
    ]
  )


(def show-concept-history-since-version-query
  '[:find ?e ?aname ?v ?tx ?added ?inst ?concept-id ?preferred-label ?type ?deprecated ?concept-id-tx ?preferred-label-tx ?type-tx
    :in $ ?one-version-before-from-version ?to-version
    :where
    [?e :concept/preferred-label ?preferred-label ?preferred-label-tx]
    [?e :concept/id ?concept-id ?concept-id-tx]
    [?e :concept/type ?type ?type-tx]
    [(get-else $ ?e :concept/deprecated false) ?deprecated]
    [?e ?a ?v ?tx ?added]
    [?tx :db/txInstant ?inst]
    [?fv :taxonomy-version/id ?one-version-before-from-version ?one-version-before-from-version-tx]
    [?one-version-before-from-version-tx :db/txInstant ?one-version-before-from-version-inst]
    [(< ?one-version-before-from-version-inst ?inst)]
    [?tv :taxonomy-version/id ?to-version ?to-version-tx]
    [?to-version-tx :db/txInstant ?to-version-inst]
    [(> ?to-version-inst ?inst)]
    [?a :db/ident ?aname]
    ]
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


#_(def show-version-hist
  '[:find ?v ?version-inst
    :in $ ?version
    :where [?v :taxonomy-version/id ?version ?version-inst]
    ]
  )


#_(def show-concept-history-since-transaction-query
  '[:find ?e ?aname ?v ?tx ?added ?concept-id ?term ?pft ?cat
    :in $ ?fromtx
    :where
    [?e :concept/preferred-label ?pft]
    [?e :concept/id ?concept-id]
    [?e :concept/type ?cat]
    [?e ?a ?v ?tx ?added]
    [?tx :db/txInstant]
    [(< ?fromtx ?tx)]
    [?a :db/ident ?aname]
    ]
  )


(defn group-by-transaction-and-entity [datoms]
  (group-by (juxt #(nth % 3) #(nth % 0)) datoms))

(defn group-by-attribute [grouped-datoms]
  (map #(group-by second %) grouped-datoms))

(defn keep-after-update [[_ _ _ _ operation]]
  operation)

#_(defn is-datom-updated [[_ _ attribute-value tx _ _ _ preferred-label _ _ id-tx preferred-label-tx type-tx]]
  (println attribute-value preferred-label tx id-tx preferred-label-tx)
  )


#_(defn is-event-updated? [datoms-grouped-by-attribute]
  (if-let [datoms (:concept/preferred-label datoms-grouped-by-attribute)]

    false)
  )

;; TODO skriv om till updated rör alla fält utom id.  type preferred-label definition
(defn is-event-update-preferred-label? "checks if op is not all true or false"
  [datoms-grouped-by-attribute]
  (if-let [datoms (:concept/preferred-label datoms-grouped-by-attribute)]
    (not (apply = (map #(nth % 4) datoms)))
    false))

(defn is-event-create-concept? [datoms-grouped-by-attribute]
  (if-let [datoms (:concept/id datoms-grouped-by-attribute)]
    (every? true? (map #(nth % 4) datoms))
    false))

(defn is-event-deprecated-concept? [datoms-grouped-by-attribute]
  (if-let [datoms (:concept/deprecated datoms-grouped-by-attribute)]
    (every? true? (map #(nth % 4) datoms))
    false))

(defn create-event-create-concept-from-datom [datoms-grouped-by-attribute]

  "TODO fix potential bugfest, first is a bit sketchy"
  (let [[_ _ _ transaction-id _ timestamp concept-id preferred-label type]  (first (:concept/preferred-label datoms-grouped-by-attribute))]
    {:event-type "CREATED"
     :transaction-id transaction-id
     :type type
     :timestamp timestamp
     :concept-id concept-id
     :preferred-label preferred-label}))

(defn create-event-deprecated-concept-from-datom [datoms-grouped-by-attribute]
  (let [[_ _ _ transaction-id _ timestamp concept-id preferred-label type]  (first (:concept/deprecated datoms-grouped-by-attribute))]
    {:event-type "DEPRECATED"
     :transaction-id transaction-id
     :type type
     :timestamp timestamp
     :concept-id concept-id
     :preferred-label preferred-label
     :deprecated true}))

(defn create-event-updated-preferred-label [datoms-grouped-by-attribute]
  (let [datoms  (:concept/preferred-label datoms-grouped-by-attribute)
        datom-after  (filter keep-after-update datoms)
        [[_ _ _ transaction-id _ timestamp concept-id preferred-label type]] datom-after]
    {:event-type "UPDATED"
     :transaction-id transaction-id
     :type type
     :timestamp timestamp
     :concept-id concept-id
     :preferred-label preferred-label}))

(defn determine-event-type [datoms-by-attibute]
  "This function will return nil events when the event is not CREATED, DEPRECATED or UPDATED.
Like replaced-by will return nil."
  (let [is-event-create-concept (is-event-create-concept? datoms-by-attibute)
        is-event-deprecated-concept (is-event-deprecated-concept? datoms-by-attibute)
        is-event-update-preferred-label (is-event-update-preferred-label? datoms-by-attibute)]
    (cond
      is-event-create-concept (create-event-create-concept-from-datom datoms-by-attibute)
      is-event-deprecated-concept (create-event-deprecated-concept-from-datom datoms-by-attibute)
      is-event-update-preferred-label (create-event-updated-preferred-label datoms-by-attibute))))

(defn convert-history-to-events [datoms]
  (let [grouped-datoms (map second (group-by-transaction-and-entity datoms))
        datoms-by-attribute (group-by-attribute grouped-datoms)
        events (filter some? (map determine-event-type datoms-by-attribute))]
    events))


;; (d/q show-version-instance-ids (get-db))
(comment
  (d/q show-version-instance-ids (get-db))
  [[13194139533328 68] [13194139533330 69] [13194139533326 67]]
  stoppa in ditt värde i listan ovan
  sortera listan på transactions id:n
  ta index för ditt värde ut listan
  stega upp ett index för att få nästföljande transaktionsid med tillhörande taxonomy-versionsid

  ...
  Blås databasen.

  Skapa 3 test transactioner
  1 spara version 66 i tom databas !!
  2 Spara conceptet gammel-java
  3 spara version 67
  4 updatera gammel java, sätt den till deprecated
  5. spara version 68
  6.

  1. databas tom
  2. skapa första versions tagg i tomma databasen.
  3. redaktionen lägger in saker i databasen.
  4. Redaktionen skapar en version av all som tidigare funnits i databasen, dvs allt innan versions-transaktionen fram till versionen innan.
  5. redaktionen lägger in mer data.
  6. skapar ny version.

  Hämta transaktioner från (från-version - 1) till transaktioner tidigare än  (till-version)

  )

(defn convert-transaction-id-to-version-id [versions-with-transatcion-ids transaction-id]
  (let [;; versions (d/q show-version-instance-ids (get-db))
        sorted-versions (sort-by first (conj versions-with-transatcion-ids [transaction-id]))
        index-of-next-element-to-transaction-id (.indexOf  sorted-versions [transaction-id])
        version-id (second (nth sorted-versions (inc index-of-next-element-to-transaction-id)))
        ]
    version-id
    )
  )

(defn get-all-events [db]
  (sort-by :transaction-id
           (convert-history-to-events
            (d/q show-concept-history (get-db-hist db)))))

(defn get-all-events-since [db date-time]
  (sort-by :transaction-id
           (convert-history-to-events
            (d/q show-concept-history-since-query (get-db-hist db) date-time))))



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

(defn get-all-events-between-versions "inclusive" [db from-version to-version]
  (convert-events-transaction-ids-to-version-ids
   (sort-by :transaction-id
            (convert-history-to-events
             (d/q show-concept-history-since-version-query (get-db-hist db)  from-version to-version))))
  )


(defn get-all-events-from-version "inclusive" [db from-version]
  (let [latest-version (ffirst (d/q show-latest-version-id db))]
    (get-all-events-between-versions db from-version latest-version)
    )
  )

(defn transform-event-result [{:keys [type version preferred-label concept-id event-type deprecated] }]
  {:event-type event-type
   :version version
   :concept (merge (if (true? deprecated) {:deprecated true} {}) ; deprecated optional
                   {:id concept-id,
                    :type type,
                    :preferred-label preferred-label})})

(defn get-all-events-since-v0-9 "Beta for v0.9." [db date-time offset limit]
  (u/pagination  (map transform-event-result  (get-all-events-since db date-time))  offset limit))

(defn get-all-events-from-version-with-pagination " v1.0" [from-version to-version offset limit]
  (let [events (if to-version
                 (get-all-events-between-versions (get-db) from-version to-version)
                 (get-all-events-from-version (get-db) from-version)
                 )]
    (u/pagination  (map transform-event-result  events)  offset limit))
  )


(def show-changes-schema
  "The response schema for /changes. Beta for v0.9."
  [{:event-type s/Str
    :version s/Int
    :concept { :id s/Str
              :type s/Str
              (s/optional-key :deprecated) s/Bool
              (s/optional-key :preferred-label) s/Str }}])



(def show-deprecated-replaced-by-query
  '[:find (pull ?c
                    [:concept/id
                     :concept/definition
                     :concept/type
                     :concept/preferred-label
                     :concept/deprecated
                     {:concept/replaced-by [:concept/id
                                            :concept/definition
                                            :concept/type
                                            :concept/preferred-label
                                            :concept/deprecated
                                            ]}])
    ?tx
    :in $ ?one-version-before-from-version ?to-version
    :where
    [?c :concept/deprecated true]
    [?c :concept/replaced-by ?rc ?tx]
    [?tx :db/txInstant ?inst]

    [?fv :taxonomy-version/id ?one-version-before-from-version ?one-version-before-from-version-tx]
    [?one-version-before-from-version-tx :db/txInstant ?one-version-before-from-version-inst]
    [(< ?one-version-before-from-version-inst ?inst)]

    [?tv :taxonomy-version/id ?to-version ?to-version-tx]
    [?to-version-tx :db/txInstant ?to-version-inst]
    [(> ?to-version-inst ?inst)]
    ])

#_(defn transform-replaced-by [concept]
  (rename-keys concept {:concept/id :id
                        :concept/definition :definition
                        :concept/type :type
                        :concept/preferred-label :preferred-label
                        :concept/deprecated :deprecated })
 )

(defn transform-deprecated-concept-replaced-by-result [[deprecated-concept transaction-id]]
  (let [{:keys [:concept/id :concept/preferred-label :concept/definition :concept/deprecated :concept/replaced-by :concept/type]}  deprecated-concept
        ]
    {:transaction-id transaction-id
     :concept {:id id
               :type type
               :definition definition
               :preferred-label preferred-label
               :deprecated deprecated
               :replaced-by replaced-by
               }
     }
    )
  )


(defn get-deprecated-concepts-replaced-by-from-version [from-version to-version]
  (let [db (get-db)
        deprecated-concepts (if to-version
                              (d/q show-deprecated-replaced-by-query db  from-version to-version)
                              (d/q show-deprecated-replaced-by-query db  from-version (ffirst (d/q show-latest-version-id db)))
                              )

        ]
    (sort-by :transaction-id (map #(dissoc % :transaction-id ) (convert-events-transaction-ids-to-version-ids (map transform-deprecated-concept-replaced-by-result deprecated-concepts))))
    )
  )


(comment


  ;; (d/transact (get-conn) {:tx-data [{:taxonomy-version/id 67}]})

  ;; (d/q '[:find (pull ?v [*])  :in $ :where [?v :taxonomy-version/id]] (get-db) )

  (def get-version
    '[:find ?e
      :in $
      :where [?e :taxonomy-version/id 67]
      ]
    )

  (defn get-verion-67-entity []
    (ffirst (d/q get-version (get-db))))

  (defn convert []
    (let [version-db-id (get-verion-67-entity)]
      [{:db/id version-db-id
        :taxonomy-version/id 68}]
      )

    )

  ;; (d/transact (get-conn) {:tx-data [{:taxonomy-version/id 67}]})

  )
