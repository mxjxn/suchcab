(ns suchcab.db.users
  (:require
   [buddy.hashers :as h]
   [buddy.sign.jwt :as jwt]
   [suchcab.config :refer [secret]]
   [suchcab.db :refer [crux-node]]
   [crux.api :as crux]
   [clj-time.core :as time])
  (:import (java.util UUID)))


(defn- encrypt-password [pw] (h/encrypt pw))

(defn create-uuid [{:keys [email usertype]}]
  (let [user-bytes (.getBytes
                    (str (name usertype) "-" (.toString email)))]
    (UUID/nameUUIDFromBytes user-bytes)))

(defn get-user-by-email [email]
  (crux/q (crux/db crux-node)
          {:find '[e me p]
           :where '[[e :email me]
                    [e :password p]]
           :args [{'me email}]}))

(defn create-user [{email :email,username :username,password :password,:as opts}]
  (let [user-uuid (create-uuid (merge opts {:usertype :user}))
        user-pass (encrypt-password password)
        new-user (merge opts {:password user-pass :crux.db/id user-uuid})
        result (crux/submit-tx crux-node [[:crux.tx/put new-user]])]
    (merge result {:body {:status :success}})))

(defn login-user [{:keys [email password] :as opts}]
  (let [user-obj (get-user-by-email email)]
    (if (not-empty user-obj)
      (let [[_ _ encrypted-pw] (first user-obj)
            pw-valid? (h/check password encrypted-pw)]
        (if pw-valid?
          (let [claims {:user email
                        :exp (time/plus (time/now) (time/hours 24))}
                token (jwt/encrypt claims secret {:alg :a256kw :enc :a128gcm})]
            {:status :success, :token token})))
      {:status :wrong-email})))



; (create-user {:email "max@jackson.com" :password "password" :username "mxjxn"})
; (get-user-by-email "max@jackson.com")
; (login-user {:email "max@jackson.com" :password "password"})


