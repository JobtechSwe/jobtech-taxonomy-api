(ns jobtech-taxonomy-api.test.auth-test
  (:require [clojure.test :as test]
            [jobtech-taxonomy-api.test.test-utils :as util]))


(test/use-fixtures :each util/fixture)

(test/deftest ^:integration-unauthorized-access-public unauthorized-access-public
  (test/testing "unauthorized access to /v0/taxonomy/public/concept/types"
    (let [[status body] (util/send-request-to-json-service :get "/v0/taxonomy/public/concept/types")]
      (test/is (and (= "unauthorized" (:error body))
               (= status 401))))))

(test/deftest ^:integration-authorized-access-public authorized-access-public
  (test/testing "unauthorized access to /v0/taxonomy/public/concept/types"

    (let [[status body] (util/send-request-to-json-service
                         :get "/v0/taxonomy/public/concept/types"
                         :headers [(util/header-auth-user)])]
      (test/is (= status 200)))))

(test/deftest ^:integration-unauthorized-access-private unauthorized-access-private
  (test/testing "unauthorized access to /v0/taxonomy/private/relation/types"
    (let [[status body] (util/send-request-to-json-service :get "/v0/taxonomy/private/relation/types")]
      (test/is (and (= "unauthorized" (:error body))
               (= status 401))))))

(test/deftest ^:integration-authorized-access-private authorized-access-private
  (test/testing "authorized access to /v0/taxonomy/private/relation/types"
    (let [[status body] (util/send-request-to-json-service
                         :get "/v0/taxonomy/private/relation/types"
                         :headers [(util/header-auth-admin)])]
      (test/is (= status 200)))))

(test/deftest ^:integration-authenticated-and-unauthorized-access-private authenticated-and-unauthorized-access-private
  (test/testing "authenticated and unauthorized access to /v0/taxonomy/private/relation/types"
    (let [[status body] (util/send-request-to-json-service
                         :get "/v0/taxonomy/private/relation/types"
                         :headers [(util/header-auth-user)])]
      (test/is (= status 401)))))
