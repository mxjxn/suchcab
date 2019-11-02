(ns suchcab.db
  (:require
   [mount.core :refer [defstate]]
   [crux.api :as crux]))

(defn start-node []
  (crux/start-standalone-node
   {:kv-backend "crux.kv.memdb.MemKv"
    :db-dir "data/db-dir"
    :event-log-dir "data/event-log-1"}))

(defstate crux-node
  :start
  (start-node)
  :stop
  (.close crux-node))
