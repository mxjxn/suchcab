(ns suchcab.routes.services.drivers
  (:require
   [ring.util.http-response :refer :all]
   [suchcab.db.drivers :as drivers]))

(defn create-driver-profile-handler
  [{{{:keys [user-id name bio vehicle-info card-slug avatar-url] :as opts} :body} :parameters}]
  (let [result (drivers/create-driver-profile opts)]
    (ok result)))

(defn get-driver-handler
  [{{{:keys [id]} :path} :parameters}]
  (if-let [driver (drivers/get-driver-by-id id)]
    (ok driver)
    (not-found {:error "Driver not found"})))

(defn get-driver-by-card-handler
  [{{{:keys [card-link]} :path} :parameters}]
  (if-let [driver (drivers/get-driver-by-card-link card-link)]
    (ok driver)
    (not-found {:error "Driver not found"})))

(defn update-availability-handler
  [{{{:keys [driver-id available location] :as opts} :body} :parameters}]
  (let [result (drivers/update-driver-availability driver-id available location)]
    (if result
      (ok result)
      (not-found {:error "Driver not found"}))))

(defn get-available-drivers-handler
  [{{:keys [query]} :parameters}]
  (let [lat (:lat query)
        lon (:lon query)
        radius (:radius query)
        drivers-list (if (and lat lon)
                       (drivers/get-available-drivers lat lon (or radius 10))
                       (drivers/get-available-drivers))]
    (ok {:drivers drivers-list})))

(defn list-all-drivers-handler
  [_]
  (ok {:drivers (drivers/list-all-drivers)}))

(defn driver-routes []
  ["/drivers"
   {:swagger {:tags ["drivers"]}}
   ["/create"
    {:post {:summary "create or update driver profile"
            :parameters {:body {:user-id uuid?
                                :name string?}}
            :responses {200 {:body {:status keyword?}}}
            :handler create-driver-profile-handler}}]
   ["/:id"
    {:get {:summary "get driver by ID"
           :parameters {:path {:id uuid?}}
           :responses {200 {:body map?}}
           :handler get-driver-handler}}]
   ["/card/:card-link"
    {:get {:summary "get driver by card link"
           :parameters {:path {:card-link string?}}
           :responses {200 {:body map?}}
           :handler get-driver-by-card-handler}}]
   ["/availability"
    {:put {:summary "update driver availability and location"
           :parameters {:body {:driver-id uuid?
                               :available boolean?}}
           :responses {200 {:body {:status keyword?}}}
           :handler update-availability-handler}}]
   ["/available"
    {:get {:summary "get available drivers"
           :responses {200 {:body {:drivers vector?}}}
           :handler get-available-drivers-handler}}]
   ["/list"
    {:get {:summary "list all drivers"
           :responses {200 {:body {:drivers vector?}}}
           :handler list-all-drivers-handler}}]])
