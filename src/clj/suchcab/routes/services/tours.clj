(ns suchcab.routes.services.tours
  (:require
   [ring.util.http-response :refer :all]
   [suchcab.db.tours :as tours]))

(defn create-tour-handler
  [{{{:keys [title description driver price duration location] :as opts} :body} :parameters}]
  (let [result (tours/create-tour opts)]
    (condp = (:status result)
      :success           (ok result)
      :driver-not-found  (bad-request {:error "Driver not found"}))))

(defn list-tours-handler
  [{{:keys [query]} :parameters}]
  (let [driver-uuid (:driver query)
        tours-list (if driver-uuid
                     (tours/list-tours driver-uuid)
                     (tours/list-tours))]
    (ok {:tours tours-list})))

(defn get-tour-handler
  [{{{:keys [id]} :path} :parameters}]
  (if-let [tour (tours/get-tour-by-id id)]
    (ok tour)
    (not-found {:error "Tour not found"})))

(defn tour-routes []
  ["/tours"
   {:swagger {:tags ["tours"]}}
   ["/create"
    {:post {:summary "create a new tour offering"
            :parameters {:body {:title string?
                                :description string?
                                :driver uuid?}}
            :responses {200 {:body {:status keyword?}}}
            :handler create-tour-handler}}]
   ["/list"
    {:get {:summary "list all tours"
           :responses {200 {:body {:tours vector?}}}
           :handler list-tours-handler}}]
   ["/:id"
    {:get {:summary "get tour by ID"
           :parameters {:path {:id uuid?}}
           :responses {200 {:body map?}}
           :handler get-tour-handler}}]])
