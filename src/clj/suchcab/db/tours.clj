(ns suchcab.db.tours
  (:require
   [suchcab.db :refer [crux-node]]
   [crux.api :as crux]
   [java-time :as time])
  (:import (java.util UUID)))

(defn- driver-exists? [driver-uuid]
  (not-empty
   (crux/q (crux/db crux-node)
           {:find '[e]
            :where '[[e :crux.db/id driver-id]]
            :args [{'driver-id driver-uuid}]})))

(defn get-tour-by-id [tour-uuid]
  (crux/entity (crux/db crux-node) tour-uuid))

(defn list-tours
  "List all tours, optionally filtered by driver"
  ([]
   (crux/q (crux/db crux-node)
           {:find '[(pull e [*])]
            :where '[[e :tour/title]]}))
  ([driver-uuid]
   (crux/q (crux/db crux-node)
           {:find '[(pull e [*])]
            :where '[[e :tour/title]
                     [e :tour/driver driver-id]]
            :args [{'driver-id driver-uuid}]})))

(defn create-tour [{:keys [title description driver price duration location] :as opts}]
  (if (driver-exists? driver)
    (let [tour-uuid (UUID/randomUUID)
          new-tour {:crux.db/id tour-uuid
                    :tour/title title
                    :tour/description description
                    :tour/driver driver
                    :tour/price (or price 0.0)
                    :tour/duration (or duration 60)
                    :tour/location (or location "")
                    :tour/created-at (time/instant)
                    :tour/active true}
          result (crux/submit-tx crux-node [[:crux.tx/put new-tour]])]
      {:status :success
       :tour-id tour-uuid
       :tx-id (:crux.tx/tx-id result)})
    {:status :driver-not-found}))
