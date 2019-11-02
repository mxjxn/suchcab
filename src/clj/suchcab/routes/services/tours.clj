(ns suchcab.routes.services.tours
  )

(defn create-tour-handler
  [{{{:keys [title description driver] :as opts} :body} :parameters}]
  nil
  )

(defn tour-routes []
  ["/tour"
   ["/create"
    {:post {:summary "create a new tour offering"
            :parameters {:body {:title string? :description string? :driver uuid?}}
            :responses {200 {:body {:status keyword?}}}
            :handler create-tour-handler}}]])
