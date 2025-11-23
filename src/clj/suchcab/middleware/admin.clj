(ns suchcab.middleware.admin
  (:require
   [ring.util.http-response :refer :all]
   [buddy.auth :refer [authenticated?]]))

(defn admin? [request]
  "Check if the authenticated user is an admin"
  (let [identity (:identity request)]
    (and identity
         (= :admin (:user-type identity)))))

(defn wrap-admin
  "Middleware to restrict access to admin users only"
  [handler]
  (fn [request]
    (if (authenticated? request)
      (if (admin? request)
        (handler request)
        (forbidden {:error "Admin access required"}))
      (unauthorized {:error "Authentication required"}))))
