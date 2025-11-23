(ns suchcab.db.drivers
  (:require
   [suchcab.db :refer [crux-node]]
   [crux.api :as crux]
   [java-time :as time])
  (:import (java.util UUID)))

(defn create-driver-profile
  "Create or update a driver profile with card link"
  [{:keys [user-id name bio vehicle-info card-slug avatar-url] :as opts}]
  (let [driver-uuid (or user-id (UUID/randomUUID))
        card-link (or card-slug (str (UUID/randomUUID)))
        driver-profile {:crux.db/id driver-uuid
                        :driver/user-id driver-uuid
                        :driver/name name
                        :driver/bio (or bio "")
                        :driver/vehicle-info (or vehicle-info {})
                        :driver/card-link card-link
                        :driver/avatar-url avatar-url
                        :driver/rating 5.0
                        :driver/total-rides 0
                        :driver/available false
                        :driver/location nil
                        :driver/status :pending  ; :pending, :approved, :rejected, :banned
                        :driver/created-at (time/instant)
                        :driver/updated-at (time/instant)}
        result (crux/submit-tx crux-node [[:crux.tx/put driver-profile]])]
    {:status :success
     :driver-id driver-uuid
     :card-link card-link
     :tx-id (:crux.tx/tx-id result)}))

(defn get-driver-by-id [driver-id]
  (crux/entity (crux/db crux-node) driver-id))

(defn get-driver-by-card-link [card-link]
  (let [result (crux/q (crux/db crux-node)
                       {:find '[(pull e [*])]
                        :where '[[e :driver/card-link card]]
                        :args [{'card card-link}]})]
    (when (seq result)
      (first (first result)))))

(defn update-driver-availability
  "Update driver's availability status and location"
  [driver-id available? location]
  (let [driver (get-driver-by-id driver-id)]
    (when driver
      (let [updated-driver (merge driver
                                  {:driver/available available?
                                   :driver/location location
                                   :driver/updated-at (time/instant)})
            result (crux/submit-tx crux-node [[:crux.tx/put updated-driver]])]
        {:status :success
         :driver-id driver-id
         :available available?
         :tx-id (:crux.tx/tx-id result)}))))

(defn get-available-drivers
  "Get all currently available and approved drivers"
  ([]
   (crux/q (crux/db crux-node)
           {:find '[(pull e [*])]
            :where '[[e :driver/available true]
                     [e :driver/status :approved]]}))
  ([lat lon radius-km]
   ;; For now, return all available drivers
   ;; TODO: Implement geospatial filtering
   (get-available-drivers)))

(defn update-driver-rating
  "Update driver rating after a ride"
  [driver-id new-rating]
  (let [driver (get-driver-by-id driver-id)]
    (when driver
      (let [total-rides (:driver/total-rides driver)
            current-rating (:driver/rating driver)
            new-total (inc total-rides)
            updated-rating (/ (+ (* current-rating total-rides) new-rating) new-total)
            updated-driver (merge driver
                                  {:driver/rating updated-rating
                                   :driver/total-rides new-total
                                   :driver/updated-at (time/instant)})
            result (crux/submit-tx crux-node [[:crux.tx/put updated-driver]])]
        {:status :success
         :driver-id driver-id
         :rating updated-rating
         :tx-id (:crux.tx/tx-id result)}))))

(defn list-all-drivers []
  (crux/q (crux/db crux-node)
          {:find '[(pull e [*])]
           :where '[[e :driver/user-id]]}))

(defn get-drivers-by-status
  "Get drivers by approval status"
  [status]
  (crux/q (crux/db crux-node)
          {:find '[(pull e [*])]
           :where '[[e :driver/status s]]
           :args [{'s status}]}))

(defn update-driver-status
  "Update driver's approval status"
  [driver-id new-status notes]
  (let [driver (get-driver-by-id driver-id)]
    (when driver
      (let [updated-driver (merge driver
                                  {:driver/status new-status
                                   :driver/status-notes notes
                                   :driver/status-updated-at (time/instant)
                                   :driver/updated-at (time/instant)})
            result (crux/submit-tx crux-node [[:crux.tx/put updated-driver]])]
        {:status :success
         :driver-id driver-id
         :driver-status new-status
         :tx-id (:crux.tx/tx-id result)}))))
