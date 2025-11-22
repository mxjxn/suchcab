(ns suchcab.config
  (:require
    [cprop.core :refer [load-config]]
    [cprop.source :as source]
    [buddy.core.hash :refer [sha256]]
    [mount.core :refer [args defstate]]))


(defstate env
  :start
  (load-config
    :merge
    [(args)
     (source/from-system-props)
     (source/from-env)]))

(def secret
  (sha256 (or (:secret env) "default-dev-secret")))
