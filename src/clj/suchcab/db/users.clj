(ns suchcab.db.users
  (:require
   [buddy.hashers :as h]
   [buddy.sign.jwt :as jwt]
   [suchcab.config :refer [secret]]
   [suchcab.db :refer [crux-node]]
   [crux.api :as crux]
   [clj-time.core :as time]
   [java-time :as jtime])
  (:import (java.util UUID)))


(defn- encrypt-password [pw] (h/encrypt pw))

(defn create-uuid [{:keys [email usertype]}]
  (let [user-bytes (.getBytes
                    (str (name usertype) "-" (.toString email)))]
    (UUID/nameUUIDFromBytes user-bytes)))

(defn get-user-by-id [user-id]
  (crux/entity (crux/db crux-node) user-id))

(defn get-user-by-email [email]
  (crux/q (crux/db crux-node)
          {:find '[e me p]
           :where '[[e :email me]
                    [e :password p]]
           :args [{'me email}]}))

(defn create-user
  "Create a new user account (passenger or driver)"
  [{:keys [email username password user-type avatar-url] :as opts}]
  (let [user-type (or user-type :passenger)
        user-uuid (create-uuid (merge opts {:usertype user-type}))
        user-pass (encrypt-password password)
        new-user {:crux.db/id user-uuid
                  :user/email email
                  :user/username username
                  :user/password user-pass
                  :user/type user-type
                  :user/avatar-url avatar-url
                  :user/created-at (jtime/instant)
                  ;; Legacy fields for backward compatibility
                  :email email
                  :username username
                  :password user-pass}
        result (crux/submit-tx crux-node [[:crux.tx/put new-user]])]
    {:status :success
     :user-id user-uuid
     :user-type user-type
     :tx-id (:crux.tx/tx-id result)}))

(defn update-user-avatar
  "Update user's avatar URL"
  [user-id avatar-url]
  (let [user (get-user-by-id user-id)]
    (when user
      (let [updated-user (merge user {:user/avatar-url avatar-url})
            result (crux/submit-tx crux-node [[:crux.tx/put updated-user]])]
        {:status :success
         :user-id user-id
         :avatar-url avatar-url
         :tx-id (:crux.tx/tx-id result)}))))

(defn login-user [{:keys [email password] :as opts}]
  (let [user-obj (get-user-by-email email)]
    (if (not-empty user-obj)
      (let [[user-id _ encrypted-pw] (first user-obj)
            pw-valid? (h/check password encrypted-pw)]
        (if pw-valid?
          (let [user (get-user-by-id user-id)
                claims {:user email
                        :user-id user-id
                        :user-type (or (:user/type user) :passenger)
                        :exp (time/plus (time/now) (time/hours 24))}
                token (jwt/encrypt claims secret {:alg :a256kw :enc :a128gcm})]
            {:status :success
             :token token
             :user-id user-id
             :user-type (:user/type user)
             :avatar-url (:user/avatar-url user)})
          {:status :wrong-password}))
      {:status :wrong-email})))



; (create-user {:email "max@jackson.com" :password "password" :username "mxjxn"})
; (get-user-by-email "max@jackson.com")
; (login-user {:email "max@jackson.com" :password "password"})


