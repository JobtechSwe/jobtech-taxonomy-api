(ns jobtech-taxonomy-api.routes.services
  (:refer-clojure :exclude [type])
  (:require
   [ring.util.http-response :as response]
   [ring.middleware.json :refer [wrap-json-response]]
   [compojure.api.sweet :refer :all]
   [schema.core :as s]
   [compojure.api.meta :refer [restructure-param]]
   [compojure.api.exception :as ex]
   [buddy.auth.accessrules :refer [restrict]]
   [buddy.auth :refer [authenticated?]]
   [buddy.auth.http :as http]
   [clojure.data.json :as json]
   [clj-time [format :as f]]
   [clj-time.coerce :as c]
   [jobtech-taxonomy-api.db.core :refer :all]
   [jobtech-taxonomy-api.middleware :as middleware]
   [jobtech-taxonomy-api.db.concepts :as concepts]
   [jobtech-taxonomy-api.db.search :as search]
   [jobtech-taxonomy-api.db.events :as events]
   [jobtech-taxonomy-api.db.versions :as v]
   [clojure.tools.logging :as log]
   [clojure.pprint :as pp]))

(import java.util.Date)

(def date? (partial instance? Date))

(def date-validator
  {:message "must be a Date"
   :optional true
   :validate date?})

(defn access-error [_ _]
  (response/unauthorized {:error "unauthorized"}))

(defn wrap-restricted [handler rule]
  (restrict handler {:handler  rule
                     :on-error access-error}))

(defmethod restructure-param :auth-rules
  [_ rule acc]
  (update-in acc [:middleware] conj [wrap-restricted rule]))

(defmethod restructure-param :current-user
  [_ binding acc]
  (update-in acc [:letks] into [binding `(:identity ~'+compojure-api-request+)]))

(defn custom-handler [f type]
  (fn [^Exception e data request]
    (log/log type (.getMessage e))
    (f {:message (.getMessage e), :type type})))

(defn authorized-private? [request]
  (= (http/-get-header request "api-key") (middleware/get-token :admin)))

(s/defschema VersionResponse  {:timestamp java.util.Date
                               :version s/Int
                               })

(def service-routes
  (api
   {:exceptions
    {:handlers
     {::ex/default (custom-handler response/internal-server-error :fatal)}}
    :swagger {:ui "/v0/taxonomy/swagger-ui"
              :spec "/v0/taxonomy/swagger.json"
              :data {:info {:version "0.9.0"
                            :title "Jobtech Taxonomy"
                            :description "Jobtech taxonomy services"}
                     ;; API header config found here: https://gist.github.com/Deraen/ef7f65d7ec26f048e2bb
                     :securityDefinitions {:api_key {:type "apiKey" :name "api-key" :in "header"}}}}}

   (context "/v0/taxonomy/public" []
     :tags ["public"]
     :auth-rules authenticated?

     (GET "/versions" []
       :query-params []
       ;;:body [versions VersionResponse]
       :return {:result VersionResponse}
       :responses {200 {:schema [    {:timestamp java.util.Date
                                      :version s/Int
                                      }]}
                   500 {:schema {:type s/Str, :message s/Str}}}
       :summary "Return a list of all Taxonomy versions."
       (log/info "GET /versions")
       (response/ok (v/get-all-versions))
       )

     (GET "/changes" []
       :query-params [fromVersion :- Long
                      {toVersion :- Long nil}
                      {offset       :- Long nil}
                      {limit        :- Long nil}]
       :responses {200 {:schema events/show-changes-schema}
                   500 {:schema {:type s/Str, :message s/Str}}}
       :summary      "Show the history from a given version."
       (log/info (str "GET /changes fromVersion:" fromVersion " toVersion " toVersion  " offset: " offset " limit: " limit))
       (response/ok (events/get-all-events-from-version-with-pagination fromVersion toVersion offset limit)))

     (GET "/concepts"    []
       :query-params [{id :- String nil}
                      {preferredLabel :- String nil}
                      {type :- String nil}
                      {deprecated :- Boolean false}
                      {relationType :-  (s/enum "broader" "related" "occupation_name_affinity" )  nil}
                      {relatedIds :- (describe [String] "Used together with relationType" ) nil}
                      {offset :- Long nil}
                      {limit :- Long nil}
                      {version :- Long nil}
                      ]

       :responses {200 {:schema concepts/find-concepts-schema}
                   500 {:schema {:type s/Str, :message s/Str}}}
       :summary      "Get concepts."
       (log/info (str "GET /concepts " "id:" id " preferredLabel:" preferredLabel " type:" type " deprecated:" deprecated " offset:" offset " limit:" limit))
       (response/ok (concepts/find-concepts id preferredLabel type deprecated relationType relatedIds  offset limit version)))

     (GET "/search" []
       :query-params  [q       :- (describe String "String to autocomplete on")
                      {type   :- String nil}
                       {relationType :-  (s/enum "broader" "related" "occupation_name_affinity" ) nil}
                       {relatedIds :- (describe [String] "Used together with relationType" ) nil}
                      {offset :- Long nil}
                      {limit  :- Long nil}
                      {version :- Long nil}
                      ]
       :responses {200 {:schema search/get-concepts-by-search-schema}
                   500 {:schema {:type s/Str, :message s/Str}}}
       :summary      "Autocomplete from query string"
       (log/info (str "GET /search q:" q " type:" type "relationType:" relationType "relatedIds:" relatedIds  " offset:" offset " limit:" limit  " version: " version))
       (response/ok (search/get-concepts-by-search q type relationType relatedIds offset limit version)))

     ;; "this is the replaced by endpoint"
     (GET "/replaced-by-changes" []
       :query-params [fromVersion :- Long
                      {toVersion :- Long nil}
                      ]
       :responses {200 {:schema s/Any} ;; show-concept-events-schema} TODO FIXME
                   500 {:schema {:type s/Str, :message s/Str}}}
       :summary      "Show the history of concepts being replaced from a given version."
       (log/info (str "GET /replaced-by-changes from-version: " fromVersion " toVersion: " toVersion))
       (response/ok (events/get-deprecated-concepts-replaced-by-from-version fromVersion toVersion)))

     (GET "/concept/types"    []
       :query-params [{version :- Long nil}]
       :responses {200 {:schema [ s/Str ]}
                   500 {:schema {:type s/Str, :message s/Str}}}
       :summary "Return a list of all taxonomy types."
       (log/info (str "GET /concept/types version: " version ))
       (response/ok (get-all-taxonomy-types version)))

     (GET "/relation/types" []
       :responses {200 {:schema s/Any}
                   500 {:schema {:type s/Str, :message s/Str}}}
       :summary "Relation graphs."
       (response/ok (get-relation-types)))

                 )


   (context "/v0/taxonomy/private" []
     :tags ["private"]
     ;;:auth-rules {:or [swagger-ui-user? (fn [req] (and (authenticated? req) (authorized-private? req)))]}
     :auth-rules {:and [authenticated? authorized-private?]}

     (DELETE "/concept"    []
       :query-params [id :- String]
       :summary      "Retract the concept with the given ID."
       :responses {200 {:schema {:message s/Str}}
                   404 {:schema {:message s/Str}}
                   500 {:schema {:type s/Str, :message s/Str}}}
       (log/info "GET /concept/types")
       (if (retract-concept id)
         (response/ok { :message "OK" })
         (response/not-found! { :message "Not found" } )))

     ;; alternativeTerms (optional - kolla om/hur det görs)
     (POST "/concept"    []
       :query-params [type :- String
                      definition :- String
                      preferredLabel :- String]
       :summary      "Assert a new concept."
       :responses {200 {:schema {:message s/Str :timestamp Date :concept concepts/concept-schema }}
                   409 {:schema {:message s/Str}}
                   500 {:schema {:type s/Str, :message s/Str}}}
       (log/info "POST /concept")
       (let [[result timestamp new-concept] (concepts/assert-concept type definition preferredLabel)]
         (if result
           (response/ok {:timestamp timestamp :message "OK" :concept new-concept})
           (response/conflict { :message "Can't create new concept since it is in conflict with existing concept." } ))))

     (POST "/replace-concept"    []
       :query-params [old-concept-id :- String
                      new-concept-id :- String]
       :summary      "Replace old concept with a new concept."
       {:body (replace-deprecated-concept old-concept-id new-concept-id)})

     (POST "/versions" []
       :query-params [new-version-id :- Long]
       :responses {200 {:schema  {:timestamp java.util.Date
                                  :version s/Int
                                  :message s/Str
                                  }}
                   500 {:schema {:type s/Str, :message s/Str}}}
       :summary "Creates a new version tag in the database."
       (log/info (str "POST /versions" new-version-id))

       (let [result (v/create-new-version new-version-id)]
         (if result
           (response/ok (merge result {:message "A new version of the Taxonomy was created."}))
           (response/unprocessable-entity! {:message (str new-version-id " is not the next valid version id!")})
           )))

     (GET "/relation/graph/:relation-type" []
       :path-params [relation-type :- String]
       :responses {200 {:schema s/Any}
                   500 {:schema {:type s/Str, :message s/Str}}}
       :summary "Relation graphs."
       (response/ok (get-relation-graph (keyword relation-type))))

     (GET "/relation/graph/:relation-type/:id" []
       :path-params [relation-type :- String
                     id :- String]
       :responses {200 {:schema s/Any}
                   500 {:schema {:type s/Str, :message s/Str}}}
       :summary "Relation graphs."
       (response/ok (get-relation-graph-from-concept (keyword relation-type) id)))

     )))
