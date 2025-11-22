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
    :unauthorized-handler (fn [req meta] (unauthorized {:error "Unauthorized"}))
    :options {:alg :a256kw :enc :a128gcm}}))

(defn wrap-http-cookie [handler]
  (wrap-cookies handler))

(defn token-authentication [handler]
  (wrap-authentication handler auth-backend))

(defn token-authorization [handler]
  (wrap-authorization handler auth-backend))

(defn login-user-handler
  [{{{:keys [email password] :as opts} :body :as params} :parameters}]
  (let [login-result (users/login-user opts)]
    (condp = (:status login-result)
      :success          (ok login-result)
      :wrong-email      (bad-request {:error "Email not found"})
      :wrong-password   (bad-request {:error "Invalid password"}))))

(defn create-user-handler
  [{{{:keys [email username password] :as opts} :body} :parameters}]
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
