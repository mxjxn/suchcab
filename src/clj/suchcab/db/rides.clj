(ns suchcab.db.rides
  (:require
   [suchcab.db :refer [crux-node]]
   [suchcab.db.drivers :as drivers]
   [crux.api :as crux]
   [java-time :as time])
  (:import (java.util UUID)))

(defn create-ride
  "Create a new ride request (on-demand or scheduled)"
  [{:keys [passenger-id driver-id pickup-location dropoff-location
           scheduled-time ride-type notes] :as opts}]
  (let [ride-uuid (UUID/randomUUID)
        now (time/instant)
        new-ride {:crux.db/id ride-uuid
                  :ride/passenger-id passenger-id
                  :ride/driver-id driver-id
                  :ride/pickup-location pickup-location
                  :ride/dropoff-location dropoff-location
                  :ride/scheduled-time (or scheduled-time now)
                  :ride/ride-type (or ride-type :on-demand) ; :on-demand or :scheduled
                  :ride/status :requested ; :requested, :accepted, :in-progress, :completed, :cancelled
                  :ride/notes (or notes "")
                  :ride/created-at now
                  :ride/updated-at now}
        result (crux/submit-tx crux-node [[:crux.tx/put new-ride]])]
    {:status :success
     :ride-id ride-uuid
     :tx-id (:crux.tx/tx-id result)}))

(defn get-ride-by-id [ride-id]
  (crux/entity (crux/db crux-node) ride-id))

(defn update-ride-status
  "Update the status of a ride"
  [ride-id new-status]
  (let [ride (get-ride-by-id ride-id)]
    (when ride
      (let [updated-ride (merge ride
                                {:ride/status new-status
                                 :ride/updated-at (time/instant)})
            result (crux/submit-tx crux-node [[:crux.tx/put updated-ride]])]
        {:status :success
         :ride-id ride-id
         :ride-status new-status
         :tx-id (:crux.tx/tx-id result)}))))

(defn accept-ride
  "Driver accepts a ride request"
  [ride-id driver-id]
  (let [ride (get-ride-by-id ride-id)]
    (when (and ride (= :requested (:ride/status ride)))
      (let [updated-ride (merge ride
                                {:ride/driver-id driver-id
                                 :ride/status :accepted
                                 :ride/accepted-at (time/instant)
                                 :ride/updated-at (time/instant)})
            result (crux/submit-tx crux-node [[:crux.tx/put updated-ride]])]
        {:status :success
         :ride-id ride-id
         :tx-id (:crux.tx/tx-id result)}))))

(defn complete-ride
  "Mark a ride as completed and optionally add rating"
  [ride-id {:keys [rating tip] :as completion-data}]
  (let [ride (get-ride-by-id ride-id)]
    (when ride
      (let [updated-ride (merge ride
                                {:ride/status :completed
                                 :ride/completed-at (time/instant)
                                 :ride/rating rating
                                 :ride/tip tip
                                 :ride/updated-at (time/instant)})
            result (crux/submit-tx crux-node [[:crux.tx/put updated-ride]])]
        ;; Update driver rating if provided
        (when (and rating (:ride/driver-id ride))
          (drivers/update-driver-rating (:ride/driver-id ride) rating))
        {:status :success
         :ride-id ride-id
         :tx-id (:crux.tx/tx-id result)}))))

(defn get-rides-by-passenger
  "Get all rides for a specific passenger"
  [passenger-id]
  (crux/q (crux/db crux-node)
          {:find '[(pull e [*])]
           :where '[[e :ride/passenger-id pid]]
           :args [{'pid passenger-id}]}))

(defn get-rides-by-driver
  "Get all rides for a specific driver"
  [driver-id]
  (crux/q (crux/db crux-node)
          {:find '[(pull e [*])]
           :where '[[e :ride/driver-id did]]
           :args [{'did driver-id}]}))

(defn get-active-rides
  "Get all active (requested or accepted) rides"
  []
  (crux/q (crux/db crux-node)
          {:find '[(pull e [*])]
           :where '[[e :ride/status status]
                    [(contains? #{:requested :accepted :in-progress} status)]]}))

(defn get-scheduled-rides
  "Get upcoming scheduled rides"
  []
  (crux/q (crux/db crux-node)
          {:find '[(pull e [*])]
           :where '[[e :ride/ride-type :scheduled]
                    [e :ride/status status]
                    [(contains? #{:requested :accepted} status)]]}))
