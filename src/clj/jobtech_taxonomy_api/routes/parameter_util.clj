(ns jobtech-taxonomy-api.routes.parameter-util
  (:refer-clojure :exclude [type])
  (:require
   [spec-tools.data-spec :as ds]
   [jobtech-taxonomy-api.authentication-service :as keymanager]
   ))


(def common-parameters

  {
   :user-id [(ds/opt :user-id) (taxonomy/par string? "User id")]
   :id [(ds/opt :id) (taxonomy/par string? "Concept id")]
   :type [(ds/opt :type) (taxonomy/par string? "Concept type")]
   :definition [(ds/opt :definition) (taxonomy/par string? "Definition")]
   :preferred-label [(ds/opt :preferred-label) (taxonomy/par string? "Preferred label")]
   :deprecated [(ds/opt :deprecated) (taxonomy/par boolean? "Restrict to deprecation state")]

   ;; Warning the relation is not the same for all endpoints. use relation for concepts, use relation type for relation endpoints that doens care about direction

   :relation [(ds/opt :relation) (taxonomy/par #{"broader" "narrower" "related" "substitutability-to" "substitutability-from" } "Relation type")]
   :relation-type [ (ds/opt :relation-type) (taxonomy/par #{"broader" "related" "substitutability" } "Relation type")]

   :related-ids [(ds/opt :related-ids) (taxonomy/par string? "OR-restrict to these relation IDs (white space separated list)")]
   :offset [(ds/opt :offset) (taxonomy/par int? "Return list offset (from 0)")]
   :limit [(ds/opt :limit) (taxonomy/par int? "Return list limit")]
   :version [(ds/opt :version) (taxonomy/par int? "Version to use")]
   :concept-1 [(ds/opt :concept-1) (taxonomy/par string? "ID of source concept")]
   :concept-2 [(ds/opt :concept-2) (taxonomy/par string? "ID of target concept")]
   :substitutability-percentage [(ds/opt :substitutability-percentage) (taxonomy/par int? "You only need this one if the relation is substitutability")]

   })


(defn build-parameter-map [params]
  (reduce (fn [acc param]
            (let [[k v] (get common-parameters param)]
              (assoc acc k v ))) {} params )
  )


(defn get-user-id-from-request [request]
  (keymanager/get-user-id-from-api-key (get (:headers request) "api-key"))
  )

(defn get-query-from-request [request]
  (:query (:parameters request) )
  )
