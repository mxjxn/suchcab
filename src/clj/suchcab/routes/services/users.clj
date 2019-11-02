(ns suchcab.routes.services.users
  (:require
   [ring.util.http-response :refer :all]
   [ring.middleware.cookies :refer [wrap-cookies]]
   [buddy.sign.jwe :as jwe]
   [buddy.core.keys :as keys]
   [suchcab.config :refer [secret]]
   [buddy.auth.backends.token :refer [jwe-backend]]
   [buddy.auth.middleware :refer [wrap-authentication wrap-authorization]]
   [suchcab.db.users :as users]))

(def auth-backend
  (jwe-backend
   {:secret secret
    :unauthorized-handler (fn [req meta] (println "\n\n\nwhoops not authorized...\n\n\n" req))
    :options {:alg :a256kw :enc :a128gcm}}))

(defn wrap-http-cookie [handler]
  (println "\n\n\n\ninside wrap-http-cookie\n\n\n\n")
  (wrap-cookies handler))

(defn token-authentication [handler]
  (println "in token-authentication")
  (wrap-authentication handler auth-backend))

(defn token-authorization [handler]
  (println "in token-authorization")
  (wrap-authorization handler auth-backend))

(defn login-user-handler
  [{{{:keys [email password] :as opts} :body :as params} :parameters}]
  (println "\n\nall login request parameters\n\n" params)
  (let [login-result (users/login-user opts)
        token-value (:token login-result)]
    (condp = (:status login-result)
      :success     (-> login-result
                       ok)
      :wrong-email (bad-request login-result))))

(defn create-user-handler
  [{{{:keys [email username password] :as opts} :body} :parameters}]
  (println "creating user... " opts)
  (users/create-user opts))

(defn user-routes []
  ["/user"
   {:swagger {:tags ["users"]}}
   ["/create"
    {:post {:summary "create user account"
            :parameters {:body {:email string? :username string? :password string?}}
            :responses {200 {:body {:status keyword? }}}
            :handler create-user-handler}}]
   ["/login"
    {:post {:summary "authenticate user account"
            :middleware [[wrap-http-cookie]]
            :parameters {:body {:email string? :password string?}}
            :responses {200 {:body {:status keyword?}}}
            :handler login-user-handler}}]])
