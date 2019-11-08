(ns jobtech-taxonomy-api.test.concepts-test
  (:require [clojure.test :as test]
            [jobtech-taxonomy-api.test.test-utils :as util]
            [jobtech-taxonomy-api.db.events :as events]
            [jobtech-taxonomy-api.db.core :as core]
            [jobtech-taxonomy-api.db.concepts :as concept]
            [jobtech-taxonomy-database.nano-id :as nano]
            ))

(test/use-fixtures :each util/fixture)

(test/deftest ^:integration-concepts-test-0 concepts-test-0
  (test/testing "test concepts "
    (concept/assert-concept "skill" "cyklade" "cykla")
    (let [[status body] (util/send-request-to-json-service
                          :get "/v1/taxonomy/main/concepts"
                          :headers [(util/header-auth-user)]
                          :query-params [{:key "type", :val "skill"}])
          found-concept (first (concept/find-concepts-including-unpublished {:preferred-label "cykla"}))]
      (test/is (= "cykla" (get found-concept :concept/preferred-label)))))

  #_(test/testing "test concept relation 'related'"
    (concept/assert-concept "skill" "cyklade" "cykla")
    (let [[status body] (util/send-request-to-json-service
                          :get "/v1/taxonomy/main/concepts"
                          :headers [(util/header-auth-user)]
                          :query-params [{:key "type", :val "skill"}])
          found-concept (first (concept/find-concepts-including-unpublished {:preferred-label "cykla"}))]
      (test/is (= "cykla" (get found-concept :preferredLabel)))))

  )
