(ns suchcab.env
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[suchcab started successfully]=-"))
   :stop
   (fn []
     (log/info "\n-=[suchcab has shut down successfully]=-"))
   :middleware identity})
