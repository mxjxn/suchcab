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
  [{{{:keys [email username password user-type avatar-url] :as opts} :body} :parameters}]
  (let [result (users/create-user opts)]
    (ok result)))

(defn update-avatar-handler
  [{{{:keys [user-id avatar-url] :as opts} :body} :parameters}]
  (let [result (users/update-user-avatar user-id avatar-url)]
    (if result
      (ok result)
      (not-found {:error "User not found"}))))

(defn get-user-handler
  [{{{:keys [user-id]} :path} :parameters}]
  (if-let [user (users/get-user-by-id user-id)]
    (ok (dissoc user :password :user/password))
    (not-found {:error "User not found"})))

(defn user-routes []
  ["/user"
   {:swagger {:tags ["users"]}}
   ["/create"
    {:post {:summary "create user account"
            :parameters {:body {:email string? :username string? :password string?}}
            :responses {200 {:body {:status keyword?}}}
            :handler create-user-handler}}]
   ["/login"
    {:post {:summary "authenticate user account"
            :middleware [[wrap-http-cookie]]
            :parameters {:body {:email string? :password string?}}
            :responses {200 {:body {:status keyword?}}}
            :handler login-user-handler}}]
   ["/avatar"
    {:put {:summary "update user avatar"
           :parameters {:body {:user-id uuid? :avatar-url string?}}
           :responses {200 {:body {:status keyword?}}}
           :handler update-avatar-handler}}]
   ["/:user-id"
    {:get {:summary "get user by ID"
           :parameters {:path {:user-id uuid?}}
           :responses {200 {:body map?}}
           :handler get-user-handler}}]])
