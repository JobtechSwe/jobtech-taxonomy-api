(ns jobtech-taxonomy-api.middleware
  (:require [jobtech-taxonomy-api.env :refer [defaults]]
            [jobtech-taxonomy-api.config :refer [env]]
            [ring.middleware.flash :refer [wrap-flash]]
            [immutant.web.middleware :refer [wrap-session]]
            [ring.middleware.defaults :refer [site-defaults wrap-defaults]]
            [buddy.auth.middleware :refer [wrap-authentication wrap-authorization]]
            [buddy.auth.accessrules :refer [restrict]]
            [buddy.auth :refer [authenticated?]]
            [buddy.auth.backends.session :refer [session-backend]]
            [jobtech-taxonomy-api.apikey :refer [apikey-backend]]))

(defn on-error [request response]
  {:status 403
   :headers {}
   :body (str "Access to " (:uri request) " is not authorized")})

(defn wrap-restricted [handler]
  (restrict handler {:handler authenticated?
                     :on-error on-error}))

;; Define a in-memory relation between tokens and users:
;; TODO use the api-key service


(defn get-tokens-from-system-env []
  (get-in env [:jobtech-taxonomy-api :auth-tokens])
  )

(defn get-all-tokens []
   (get-tokens-from-system-env)
  )

(defn authenticate-user [api-key]
  (contains? (set (keys (get-all-tokens)))   (keyword api-key) )
  )

(defn authenticate-admin [api-key]
  (contains? (set (map first (filter (fn [[token role]] (= role :admin)  ) (get-all-tokens))))
             (keyword api-key)
             )
  )

;; Define an authfn, function with the responsibility
;; to authenticate the incoming token and return an
;; identity instance


(defn my-authfn
  [request token]
  (let [token (keyword token)]
    (get (get-all-tokens) token nil)))

;; Create an instance
(def api-backend-instance (apikey-backend {:authfn my-authfn}))

(defn wrap-auth [handler]
  (let [backend api-backend-instance]
    (-> handler
        (wrap-authentication backend)
        (wrap-authorization backend))))

(defn wrap-base [handler]
  (-> ((:middleware defaults) handler)
      wrap-auth
      wrap-flash
      (wrap-session {:cookie-attrs {:http-only true}})
      (wrap-defaults
       (-> site-defaults
           (assoc-in [:security :anti-forgery] false)
           (dissoc :session)))))
