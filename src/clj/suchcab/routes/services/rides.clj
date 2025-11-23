(ns suchcab.routes.services.rides
  (:require
   [ring.util.http-response :refer :all]
   [suchcab.db.rides :as rides]))

(defn create-ride-handler
  [{{{:keys [passenger-id driver-id pickup-location dropoff-location
             scheduled-time ride-type notes] :as opts} :body} :parameters}]
  (let [result (rides/create-ride opts)]
    (ok result)))

(defn get-ride-handler
  [{{{:keys [id]} :path} :parameters}]
  (if-let [ride (rides/get-ride-by-id id)]
    (ok ride)
    (not-found {:error "Ride not found"})))

(defn update-ride-status-handler
  [{{{:keys [ride-id status] :as opts} :body} :parameters}]
  (let [result (rides/update-ride-status ride-id status)]
    (if result
      (ok result)
      (not-found {:error "Ride not found"}))))

(defn accept-ride-handler
  [{{{:keys [ride-id driver-id] :as opts} :body} :parameters}]
  (let [result (rides/accept-ride ride-id driver-id)]
    (if result
      (ok result)
      (bad-request {:error "Cannot accept ride"}))))

(defn complete-ride-handler
  [{{{:keys [ride-id rating tip] :as opts} :body} :parameters}]
  (let [completion-data {:rating rating :tip tip}
        result (rides/complete-ride ride-id completion-data)]
    (if result
      (ok result)
      (not-found {:error "Ride not found"}))))

(defn get-passenger-rides-handler
  [{{{:keys [passenger-id]} :path} :parameters}]
  (let [rides-list (rides/get-rides-by-passenger passenger-id)]
    (ok {:rides rides-list})))

(defn get-driver-rides-handler
  [{{{:keys [driver-id]} :path} :parameters}]
  (let [rides-list (rides/get-rides-by-driver driver-id)]
    (ok {:rides rides-list})))

(defn get-active-rides-handler
  [_]
  (ok {:rides (rides/get-active-rides)}))

(defn get-scheduled-rides-handler
  [_]
  (ok {:rides (rides/get-scheduled-rides)}))

(defn ride-routes []
  ["/rides"
   {:swagger {:tags ["rides"]}}
   ["/create"
    {:post {:summary "create a new ride request"
            :parameters {:body {:passenger-id uuid?
                                :pickup-location map?
                                :dropoff-location map?}}
            :responses {200 {:body {:status keyword?}}}
            :handler create-ride-handler}}]
   ["/:id"
    {:get {:summary "get ride by ID"
           :parameters {:path {:id uuid?}}
           :responses {200 {:body map?}}
           :handler get-ride-handler}}]
   ["/status"
    {:put {:summary "update ride status"
           :parameters {:body {:ride-id uuid?
                               :status keyword?}}
           :responses {200 {:body {:status keyword?}}}
           :handler update-ride-status-handler}}]
   ["/accept"
    {:post {:summary "driver accepts a ride"
            :parameters {:body {:ride-id uuid?
                                :driver-id uuid?}}
            :responses {200 {:body {:status keyword?}}}
            :handler accept-ride-handler}}]
   ["/complete"
    {:post {:summary "complete a ride"
            :parameters {:body {:ride-id uuid?}}
            :responses {200 {:body {:status keyword?}}}
            :handler complete-ride-handler}}]
   ["/passenger/:passenger-id"
    {:get {:summary "get rides for passenger"
           :parameters {:path {:passenger-id uuid?}}
           :responses {200 {:body {:rides vector?}}}
           :handler get-passenger-rides-handler}}]
   ["/driver/:driver-id"
    {:get {:summary "get rides for driver"
           :parameters {:path {:driver-id uuid?}}
           :responses {200 {:body {:rides vector?}}}
           :handler get-driver-rides-handler}}]
   ["/active"
    {:get {:summary "get all active rides"
           :responses {200 {:body {:rides vector?}}}
           :handler get-active-rides-handler}}]
   ["/scheduled"
    {:get {:summary "get upcoming scheduled rides"
           :responses {200 {:body {:rides vector?}}}
           :handler get-scheduled-rides-handler}}]])
