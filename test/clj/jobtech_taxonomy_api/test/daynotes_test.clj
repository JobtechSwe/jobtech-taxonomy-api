(ns jobtech-taxonomy-api.test.daynotes-test
  (:require
   [clojure.test :as test]
   [jobtech-taxonomy-api.db.daynotes :refer :all])
  )

(def example-event-history {[13194139533325 22390454788030565]
                            [{:e 22390454788030565,
                              :a :concept/id,
                              :v "t1G1_sg9_Pkk",
                              :tx 13194139533325,
                              :added true,
                              :inst #inst "2019-11-20T15:40:47.394-00:00"}
                             {:e 22390454788030565,
                              :a :concept/type,
                              :v "skill",
                              :tx 13194139533325,
                              :added true,
                              :inst #inst "2019-11-20T15:40:47.394-00:00"}
                             {:e 22390454788030565,
                              :a :concept/definition,
                              :v "Hoppa som en groda",
                              :tx 13194139533325,
                              :added true,
                              :inst #inst "2019-11-20T15:40:47.394-00:00"}
                             {:e 22390454788030565,
                              :a :concept/preferred-label,
                              :v "Hoppning",
                              :tx 13194139533325,
                              :added true,
                              :inst #inst "2019-11-20T15:40:47.394-00:00"}],
                            [13194139533326 22390454788030565]
                            [{:e 22390454788030565,
                              :a :concept/preferred-label,
                              :v "dansa",
                              :tx 13194139533326,
                              :added true,
                              :inst #inst "2019-11-20T16:14:05.364-00:00"}
                             {:e 22390454788030565,
                              :a :concept/preferred-label,
                              :v "Hoppning",
                              :tx 13194139533326,
                              :added false,
                              :inst #inst "2019-11-20T16:14:05.364-00:00"}],
                            [13194139533327 22390454788030565]
                            [{:e 22390454788030565,
                              :a :concept/preferred-label,
                              :v "dansa",
                              :tx 13194139533327,
                              :added false,
                              :inst #inst "2019-11-20T16:14:59.807-00:00"}
                             {:e 22390454788030565,
                              :a :concept/preferred-label,
                              :v "dansa3",
                              :tx 13194139533327,
                              :added true,
                              :inst #inst "2019-11-20T16:14:59.807-00:00"}],
                            [13194139533458 22390454788030565]
                            [{:e 22390454788030565,
                              :a :concept/deprecated,
                              :v true,
                              :tx 13194139533458,
                              :added true,
                              :inst #inst "2019-12-16T15:11:34.692-00:00"}]})


(def deprecated-entity-transaction-datoms
  [{:e 22390454788030565,
    :a :concept/deprecated,
    :v true,
    :tx 13194139533458,
    :added true,
    :inst #inst "2019-12-16T15:11:34.692-00:00"}])


(def created-entity-transaction-datoms
  [{:e 22390454788030565,
    :a :concept/id,
    :v "t1G1_sg9_Pkk",
    :tx 13194139533325,
    :added true,
    :inst #inst "2019-11-20T15:40:47.394-00:00"}
   {:e 22390454788030565,
    :a :concept/type,
    :v "skill",
    :tx 13194139533325,
    :added true,
    :inst #inst "2019-11-20T15:40:47.394-00:00"}
   {:e 22390454788030565,
    :a :concept/definition,
    :v "Hoppa som en groda",
    :tx 13194139533325,
    :added true,
    :inst #inst "2019-11-20T15:40:47.394-00:00"}
   {:e 22390454788030565,
    :a :concept/preferred-label,
    :v "Hoppning",
    :tx 13194139533325,
    :added true,
    :inst #inst "2019-11-20T15:40:47.394-00:00"}]
  )

(def updated-entity-transaction-datoms
  [{:e 22390454788030565,
    :a :concept/preferred-label,
    :v "dansa",
    :tx 13194139533326,
    :added true,
    :inst #inst "2019-11-20T16:14:05.364-00:00"}
   {:e 22390454788030565,
    :a :concept/preferred-label,
    :v "Hoppning",
    :tx 13194139533326,
    :added false,
    :inst #inst "2019-11-20T16:14:05.364-00:00"}]
  )

(def two-updated-entity-transaction-datoms
  {[13194139533326 22390454788030565]
   [{:e 22390454788030565,
     :a :concept/preferred-label,
     :v "dansa",
     :tx 13194139533326,
     :added true,
     :inst #inst "2019-11-20T16:14:05.364-00:00"}
    {:e 22390454788030565,
     :a :concept/preferred-label,
     :v "Hoppning",
     :tx 13194139533326,
     :added false,
     :inst #inst "2019-11-20T16:14:05.364-00:00"}],
   [13194139533327 22390454788030565]
   [{:e 22390454788030565,
     :a :concept/preferred-label,
     :v "dansa",
     :tx 13194139533327,
     :added false,
     :inst #inst "2019-11-20T16:14:59.807-00:00"}
    {:e 22390454788030565,
     :a :concept/preferred-label,
     :v "dansa3",
     :tx 13194139533327,
     :added true,
     :inst #inst "2019-11-20T16:14:59.807-00:00"}]},
  )

(test/deftest test-convert-history-to-events
  (test/is (= (map-entity-transaction-datoms-to-events example-event-history)

               '({:event-type "CREATED",
                                                                                 :timestamp #inst "2019-11-20T15:40:47.394-00:00",
                                                                                 :transaction-id 13194139533325,
                                                                                 :concept
                                                                                 #:concept{:id "t1G1_sg9_Pkk",
                                                                                           :type "skill",
                                                                                           :definition "Hoppa som en groda",
                                                                                           :preferred-label "Hoppning"}}
                                                                                {:event-type "UPDATED",
                                                                                 :timestamp #inst "2019-11-20T16:14:05.364-00:00",
                                                                                 :transaction-id 13194139533326,
                                                                                 :changes
                                                                                 ({:attribute :concept/preferred-label,
                                                                                   :new-value "dansa",
                                                                                   :old-value "Hoppning"})}
                                                                                {:event-type "UPDATED",
                                                                                 :timestamp #inst "2019-11-20T16:14:59.807-00:00",
                                                                                 :transaction-id 13194139533327,
                                                                                 :changes
                                                                                 ({:attribute :concept/preferred-label,
                                                                                   :new-value "dansa3",
                                                                                   :old-value "dansa"})}
                                                                                {:event-type "DEPRECATED",
                                                                                 :timestamp #inst "2019-12-16T15:11:34.692-00:00",
                                                                                 :transaction-id 13194139533458})


              )))
