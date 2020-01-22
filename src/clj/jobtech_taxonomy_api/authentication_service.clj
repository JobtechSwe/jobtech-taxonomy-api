(ns jobtech-taxonomy-api.authentication-service
  (:require
   [jobtech-taxonomy-api.config :refer [env]]
   [clj-http.conn-mgr :as conn]
   [clj-http.client :as client]
   [muuntaja.core :as m]
   [clojure.set :as sets]
   ))


(defn keymanager-url []
  (:keymanager-url env)
  )

(def state (atom { }))


;; https://github.com/dakrone/clj-http#caching
(defn setup-connection []
  (let [cm (conn/make-reusable-conn-manager {})
        caching-client (:http-client (client/get (keymanager-url)
                                                 {:connection-manager cm :cache true}))
        ]
    {:cm cm
     :client caching-client
     }
    )
  )


(defn get-state []
  (let [current-state @state]
    (if (empty? current-state)
      (reset! state (setup-connection))
      current-state
      )
    )
  )


(defn call-api []
  (let [current-state (get-state)]
    (client/get (keymanager-url) {:connection-manager (:cm current-state) :http-client (:client current-state) :cache true} ))
  )

(defn parse-response [response]
  (m/decode "application/json" (:body response))
  )

(defn valid-keys-from-external-key-service []
  (set (keys (get-in  (parse-response (call-api)) [:hits :hits 0 :_source])))
)

(defn get-tokens-from-env []
  (get-in env [:jobtech-taxonomy-api :auth-tokens])
  )

(defn valid-keys-from-env []
  (set (keys (get-tokens-from-env)))
  )

(defn valid-keys  "If keymanager-url is not set, use only keys from env" []
  (cond-> (valid-keys-from-env)
    (keymanager-url)
      (sets/union (valid-keys-from-external-key-service))
      )
  )

(defn is-valid-key? [a-key]
  (contains? (valid-keys) (keyword a-key))
  )

(defn get-user [token]
  (cond
    (= :admin (get (get-tokens-from-env) token)) :admin
    (is-valid-key? token) :user
    :else nil
    )
  )

(defn get-token "i e (get-token :admin)" [token]
  (let [tokens (get-tokens-from-env)]
    (str (clojure.string/replace (first (filter #(= (% tokens) token) (keys tokens))) #":" ""))))


(defn get-user-ids-from-env []
  (get-in env [:jobtech-taxonomy-api :user-ids])
  )

(defn get-user-id-from-api-key [api-key]
  (get (get-user-ids-from-env) (keyword api-key))
  )
