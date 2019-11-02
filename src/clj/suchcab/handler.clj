(ns suchcab.handler
  (:require
    [buddy.auth.backends.token :refer [jwe-backend]]
    [buddy.auth.middleware :refer [wrap-authentication wrap-authorization]]
    [suchcab.middleware :as middleware]
    [suchcab.config :refer [secret]]
    [suchcab.routes.services :refer [service-routes]]
    [suchcab.routes.oauth :refer [oauth-routes]]
    [reitit.swagger-ui :as swagger-ui]
    [reitit.ring :as ring]
    [ring.middleware.content-type :refer [wrap-content-type]]
    [ring.middleware.webjars :refer [wrap-webjars]]
    [suchcab.env :refer [defaults]]
    [mount.core :as mount]))


(def auth-backend (jwe-backend {:secret secret

                                ; TODO implement this shit
                                ;:authfn (fn [req token] nil)
                                ;:unauthorized-handler (fn [] nil)

                                :options {:alg :a256kw :enc :a128gcm}}))

(mount/defstate init-app
  :start ((or (:init defaults) (fn [])))
  :stop  ((or (:stop defaults) (fn []))))

(mount/defstate app-routes
  :start
  (ring/ring-handler
    (ring/router
      [["/" {:get
             {:handler (constantly {:status 301 :headers {"Location" "/api/api-docs/index.html"}})}}]
       (service-routes)
       ;(oauth-routes)
       ])
    (ring/routes
      (ring/create-resource-handler
        {:path "/"})
      (wrap-content-type (wrap-webjars (constantly nil)))
      (ring/create-default-handler))))


(defn app []
  (middleware/wrap-base #'app-routes))
