(ns suchcab.db.admin
  (:require
   [suchcab.db :refer [crux-node]]
   [suchcab.db.drivers :as drivers]
   [suchcab.db.rides :as rides]
   [suchcab.db.users :as users]
   [crux.api :as crux]
   [java-time :as time]))

(defn get-pending-drivers
  "Get all drivers pending approval"
  []
  (drivers/get-drivers-by-status :pending))

(defn approve-driver
  "Approve a driver application"
  [driver-id admin-id notes]
  (drivers/update-driver-status driver-id :approved notes))

(defn reject-driver
  "Reject a driver application"
  [driver-id admin-id notes]
  (drivers/update-driver-status driver-id :rejected notes))

(defn ban-driver
  "Ban a driver from the platform"
  [driver-id admin-id reason]
  (let [result (drivers/update-driver-status driver-id :banned reason)]
    ;; Also set driver as unavailable
    (when result
      (drivers/update-driver-availability driver-id false nil))
    result))

(defn unban-driver
  "Unban a driver and return them to approved status"
  [driver-id admin-id notes]
  (drivers/update-driver-status driver-id :approved notes))

(defn get-banned-drivers
  "Get all banned drivers"
  []
  (drivers/get-drivers-by-status :banned))

(defn get-rejected-drivers
  "Get all rejected driver applications"
  []
  (drivers/get-drivers-by-status :rejected))

(defn get-approved-drivers
  "Get all approved drivers"
  []
  (drivers/get-drivers-by-status :approved))

(defn get-platform-stats
  "Get dashboard statistics for admin"
  []
  (let [all-drivers (drivers/list-all-drivers)
        all-rides (rides/get-active-rides)
        pending (drivers/get-drivers-by-status :pending)
        approved (drivers/get-drivers-by-status :approved)
        banned (drivers/get-drivers-by-status :banned)
        rejected (drivers/get-drivers-by-status :rejected)]
    {:total-drivers (count all-drivers)
     :pending-drivers (count pending)
     :approved-drivers (count approved)
     :banned-drivers (count banned)
     :rejected-drivers (count rejected)
     :active-rides (count all-rides)
     :timestamp (time/instant)}))

(defn search-drivers
  "Search drivers by name or email"
  [search-term]
  (let [drivers (drivers/list-all-drivers)]
    (filter #(or (clojure.string/includes?
                  (clojure.string/lower-case (or (:driver/name (first %)) ""))
                  (clojure.string/lower-case search-term))
                 (clojure.string/includes?
                  (clojure.string/lower-case (or (:driver/bio (first %)) ""))
                  (clojure.string/lower-case search-term)))
            drivers)))

(defn get-driver-details
  "Get comprehensive driver details for admin review"
  [driver-id]
  (let [driver (drivers/get-driver-by-id driver-id)
        driver-rides (rides/get-rides-by-driver driver-id)]
    {:driver driver
     :total-rides (count driver-rides)
     :ride-history (take 10 driver-rides)}))
