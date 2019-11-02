(ns suchcab.env
  (:require
    [selmer.parser :as parser]
    [clojure.tools.logging :as log]
    [suchcab.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init
   (fn []
     (parser/cache-off!)
     (log/info "\n-=[suchcab started successfully using the development profile]=-"))
   :stop
   (fn []
     (log/info "\n-=[suchcab has shut down successfully]=-"))
   :middleware wrap-dev})
