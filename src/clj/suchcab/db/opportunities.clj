(ns suchcab.db.opportunities
  (:require
   [suchcab.db :refer [crux-node]]
   [suchcab.db.drivers :as drivers]
   [crux.api :as crux]
   [java-time :as time])
  (:import (java.util UUID)))

(defn create-opportunity
  "Create a business opportunity for drivers"
  [{:keys [title description type value valid-from valid-to terms] :as opts}]
  (let [opportunity-uuid (UUID/randomUUID)
        new-opportunity {:crux.db/id opportunity-uuid
                         :opportunity/title title
                         :opportunity/description description
                         :opportunity/type type  ; :bonus, :promotion, :special-event, :incentive
                         :opportunity/value (or value 0.0)
                         :opportunity/valid-from (or valid-from (time/instant))
                         :opportunity/valid-to valid-to
                         :opportunity/terms (or terms "")
                         :opportunity/active true
                         :opportunity/created-at (time/instant)}
        result (crux/submit-tx crux-node [[:crux.tx/put new-opportunity]])]
    {:status :success
     :opportunity-id opportunity-uuid
     :tx-id (:crux.tx/tx-id result)}))

(defn get-opportunity-by-id [opportunity-id]
  (crux/entity (crux/db crux-node) opportunity-id))

(defn list-opportunities
  "List all opportunities, optionally filtered by active status"
  ([]
   (crux/q (crux/db crux-node)
           {:find '[(pull e [*])]
            :where '[[e :opportunity/title]]}))
  ([active-only?]
   (if active-only?
     (crux/q (crux/db crux-node)
             {:find '[(pull e [*])]
              :where '[[e :opportunity/title]
                       [e :opportunity/active true]]})
     (list-opportunities))))

(defn assign-opportunity-to-driver
  "Assign an opportunity to a specific driver"
  [opportunity-id driver-id assigned-by]
  (let [assignment-uuid (UUID/randomUUID)
        new-assignment {:crux.db/id assignment-uuid
                        :assignment/opportunity-id opportunity-id
                        :assignment/driver-id driver-id
                        :assignment/assigned-by assigned-by
                        :assignment/assigned-at (time/instant)
                        :assignment/status :offered  ; :offered, :accepted, :declined, :completed
                        :assignment/notified false}
        result (crux/submit-tx crux-node [[:crux.tx/put new-assignment]])]
    {:status :success
     :assignment-id assignment-uuid
     :tx-id (:crux.tx/tx-id result)}))

(defn assign-opportunity-random
  "Assign opportunity to N random approved drivers"
  [opportunity-id count assigned-by]
  (let [approved-drivers (drivers/get-drivers-by-status :approved)
        selected-drivers (take count (shuffle (map first approved-drivers)))
        assignments (mapv #(assign-opportunity-to-driver opportunity-id % assigned-by)
                          selected-drivers)]
    {:status :success
     :opportunity-id opportunity-id
     :assigned-count (count assignments)
     :assignments assignments}))

(defn get-driver-opportunities
  "Get all opportunities assigned to a driver"
  [driver-id]
  (crux/q (crux/db crux-node)
          {:find '[(pull e [*])]
           :where '[[e :assignment/driver-id did]]
           :args [{'did driver-id}]}))

(defn get-opportunity-assignments
  "Get all assignments for a specific opportunity"
  [opportunity-id]
  (crux/q (crux/db crux-node)
          {:find '[(pull e [*])]
           :where '[[e :assignment/opportunity-id oid]]
           :args [{'oid opportunity-id}]}))

(defn update-assignment-status
  "Update the status of an opportunity assignment"
  [assignment-id new-status]
  (let [assignment (crux/entity (crux/db crux-node) assignment-id)]
    (when assignment
      (let [updated-assignment (merge assignment
                                      {:assignment/status new-status
                                       :assignment/updated-at (time/instant)})
            result (crux/submit-tx crux-node [[:crux.tx/put updated-assignment]])]
        {:status :success
         :assignment-id assignment-id
         :assignment-status new-status
         :tx-id (:crux.tx/tx-id result)}))))

(defn deactivate-opportunity
  "Deactivate an opportunity"
  [opportunity-id]
  (let [opportunity (get-opportunity-by-id opportunity-id)]
    (when opportunity
      (let [updated-opportunity (merge opportunity {:opportunity/active false})
            result (crux/submit-tx crux-node [[:crux.tx/put updated-opportunity]])]
        {:status :success
         :opportunity-id opportunity-id
         :tx-id (:crux.tx/tx-id result)}))))
