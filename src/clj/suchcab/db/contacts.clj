(ns suchcab.db.contacts
  (:require
   [suchcab.db :refer [crux-node]]
   [crux.api :as crux]
   [java-time :as time])
  (:import (java.util UUID)))

(defn add-contact
  "Add a driver to a passenger's contacts/favorites"
  [{:keys [passenger-id driver-id notes] :as opts}]
  (let [contact-uuid (UUID/randomUUID)
        new-contact {:crux.db/id contact-uuid
                     :contact/passenger-id passenger-id
                     :contact/driver-id driver-id
                     :contact/notes (or notes "")
                     :contact/favorite true
                     :contact/created-at (time/instant)}
        result (crux/submit-tx crux-node [[:crux.tx/put new-contact]])]
    {:status :success
     :contact-id contact-uuid
     :tx-id (:crux.tx/tx-id result)}))

(defn remove-contact
  "Remove a driver from passenger's contacts"
  [passenger-id driver-id]
  (let [contacts (get-contact passenger-id driver-id)]
    (when (seq contacts)
      (let [contact-id (:crux.db/id (first (first contacts)))
            result (crux/submit-tx crux-node [[:crux.tx/delete contact-id]])]
        {:status :success
         :tx-id (:crux.tx/tx-id result)}))))

(defn get-contact
  "Check if a specific driver is in passenger's contacts"
  [passenger-id driver-id]
  (crux/q (crux/db crux-node)
          {:find '[(pull e [*])]
           :where '[[e :contact/passenger-id pid]
                    [e :contact/driver-id did]]
           :args [{'pid passenger-id 'did driver-id}]}))

(defn get-passenger-contacts
  "Get all contacts/favorite drivers for a passenger"
  [passenger-id]
  (crux/q (crux/db crux-node)
          {:find '[(pull e [*])]
           :where '[[e :contact/passenger-id pid]
                    [e :contact/favorite true]]
           :args [{'pid passenger-id}]}))

(defn is-favorite?
  "Check if a driver is marked as favorite"
  [passenger-id driver-id]
  (not-empty (get-contact passenger-id driver-id)))
