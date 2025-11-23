(ns suchcab.routes.services.admin
  (:require
   [ring.util.http-response :refer :all]
   [suchcab.db.admin :as admin]
   [suchcab.db.opportunities :as opportunities]
   [suchcab.middleware.admin :refer [wrap-admin]]))

;; Driver Management Handlers

(defn get-pending-drivers-handler [_]
  (ok {:drivers (admin/get-pending-drivers)}))

(defn approve-driver-handler
  [{{{:keys [driver-id admin-id notes] :as opts} :body} :parameters}]
  (let [result (admin/approve-driver driver-id admin-id notes)]
    (if result
      (ok result)
      (not-found {:error "Driver not found"}))))

(defn reject-driver-handler
  [{{{:keys [driver-id admin-id notes] :as opts} :body} :parameters}]
  (let [result (admin/reject-driver driver-id admin-id notes)]
    (if result
      (ok result)
      (not-found {:error "Driver not found"}))))

(defn ban-driver-handler
  [{{{:keys [driver-id admin-id reason] :as opts} :body} :parameters}]
  (let [result (admin/ban-driver driver-id admin-id reason)]
    (if result
      (ok result)
      (not-found {:error "Driver not found"}))))

(defn unban-driver-handler
  [{{{:keys [driver-id admin-id notes] :as opts} :body} :parameters}]
  (let [result (admin/unban-driver driver-id admin-id notes)]
    (if result
      (ok result)
      (not-found {:error "Driver not found"}))))

(defn get-banned-drivers-handler [_]
  (ok {:drivers (admin/get-banned-drivers)}))

(defn get-approved-drivers-handler [_]
  (ok {:drivers (admin/get-approved-drivers)}))

(defn get-rejected-drivers-handler [_]
  (ok {:drivers (admin/get-rejected-drivers)}))

(defn get-stats-handler [_]
  (ok (admin/get-platform-stats)))

(defn search-drivers-handler
  [{{{:keys [q]} :query} :parameters}]
  (ok {:drivers (admin/search-drivers q)}))

(defn get-driver-details-handler
  [{{{:keys [driver-id]} :path} :parameters}]
  (let [details (admin/get-driver-details driver-id)]
    (if (:driver details)
      (ok details)
      (not-found {:error "Driver not found"}))))

;; Business Opportunities Handlers

(defn create-opportunity-handler
  [{{{:keys [title description type value valid-from valid-to terms] :as opts} :body} :parameters}]
  (let [result (opportunities/create-opportunity opts)]
    (ok result)))

(defn list-opportunities-handler
  [{{:keys [query]} :parameters}]
  (let [active-only (get query :active-only false)
        opps (opportunities/list-opportunities active-only)]
    (ok {:opportunities opps})))

(defn get-opportunity-handler
  [{{{:keys [opportunity-id]} :path} :parameters}]
  (if-let [opp (opportunities/get-opportunity-by-id opportunity-id)]
    (ok opp)
    (not-found {:error "Opportunity not found"})))

(defn assign-opportunity-handler
  [{{{:keys [opportunity-id driver-id admin-id] :as opts} :body} :parameters}]
  (let [result (opportunities/assign-opportunity-to-driver
                opportunity-id driver-id admin-id)]
    (ok result)))

(defn assign-opportunity-random-handler
  [{{{:keys [opportunity-id count admin-id] :as opts} :body} :parameters}]
  (let [result (opportunities/assign-opportunity-random
                opportunity-id count admin-id)]
    (ok result)))

(defn get-opportunity-assignments-handler
  [{{{:keys [opportunity-id]} :path} :parameters}]
  (let [assignments (opportunities/get-opportunity-assignments opportunity-id)]
    (ok {:assignments assignments})))

(defn deactivate-opportunity-handler
  [{{{:keys [opportunity-id]} :body} :parameters}]
  (let [result (opportunities/deactivate-opportunity opportunity-id)]
    (if result
      (ok result)
      (not-found {:error "Opportunity not found"}))))

;; Admin Routes

(defn admin-routes []
  ["/admin"
   {:swagger {:tags ["admin"]}
    :middleware [wrap-admin]}  ; All admin routes require admin authentication

   ;; Driver Management
   ["/drivers"
    ["/pending"
     {:get {:summary "get pending driver applications"
            :responses {200 {:body {:drivers vector?}}}
            :handler get-pending-drivers-handler}}]
    ["/approved"
     {:get {:summary "get approved drivers"
            :responses {200 {:body {:drivers vector?}}}
            :handler get-approved-drivers-handler}}]
    ["/banned"
     {:get {:summary "get banned drivers"
            :responses {200 {:body {:drivers vector?}}}
            :handler get-banned-drivers-handler}}]
    ["/rejected"
     {:get {:summary "get rejected driver applications"
            :responses {200 {:body {:drivers vector?}}}
            :handler get-rejected-drivers-handler}}]
    ["/approve"
     {:post {:summary "approve a driver application"
             :parameters {:body {:driver-id uuid?
                                 :admin-id uuid?
                                 :notes string?}}
             :responses {200 {:body {:status keyword?}}}
             :handler approve-driver-handler}}]
    ["/reject"
     {:post {:summary "reject a driver application"
             :parameters {:body {:driver-id uuid?
                                 :admin-id uuid?
                                 :notes string?}}
             :responses {200 {:body {:status keyword?}}}
             :handler reject-driver-handler}}]
    ["/ban"
     {:post {:summary "ban a driver from the platform"
             :parameters {:body {:driver-id uuid?
                                 :admin-id uuid?
                                 :reason string?}}
             :responses {200 {:body {:status keyword?}}}
             :handler ban-driver-handler}}]
    ["/unban"
     {:post {:summary "unban a driver"
             :parameters {:body {:driver-id uuid?
                                 :admin-id uuid?
                                 :notes string?}}
             :responses {200 {:body {:status keyword?}}}
             :handler unban-driver-handler}}]
    ["/search"
     {:get {:summary "search drivers by name or email"
            :responses {200 {:body {:drivers vector?}}}
            :handler search-drivers-handler}}]
    ["/:driver-id"
     {:get {:summary "get detailed driver information"
            :parameters {:path {:driver-id uuid?}}
            :responses {200 {:body map?}}
            :handler get-driver-details-handler}}]]

   ;; Business Opportunities
   ["/opportunities"
    ["/create"
     {:post {:summary "create a business opportunity for drivers"
             :parameters {:body {:title string?
                                 :description string?
                                 :type keyword?}}
             :responses {200 {:body {:status keyword?}}}
             :handler create-opportunity-handler}}]
    ["/list"
     {:get {:summary "list all opportunities"
            :responses {200 {:body {:opportunities vector?}}}
            :handler list-opportunities-handler}}]
    ["/:opportunity-id"
     {:get {:summary "get opportunity details"
            :parameters {:path {:opportunity-id uuid?}}
            :responses {200 {:body map?}}
            :handler get-opportunity-handler}}]
    ["/:opportunity-id/assignments"
     {:get {:summary "get all assignments for an opportunity"
            :parameters {:path {:opportunity-id uuid?}}
            :responses {200 {:body {:assignments vector?}}}
            :handler get-opportunity-assignments-handler}}]
    ["/assign"
     {:post {:summary "assign opportunity to specific driver"
             :parameters {:body {:opportunity-id uuid?
                                 :driver-id uuid?
                                 :admin-id uuid?}}
             :responses {200 {:body {:status keyword?}}}
             :handler assign-opportunity-handler}}]
    ["/assign-random"
     {:post {:summary "assign opportunity to N random drivers"
             :parameters {:body {:opportunity-id uuid?
                                 :count int?
                                 :admin-id uuid?}}
             :responses {200 {:body {:status keyword?}}}
             :handler assign-opportunity-random-handler}}]
    ["/deactivate"
     {:post {:summary "deactivate an opportunity"
             :parameters {:body {:opportunity-id uuid?}}
             :responses {200 {:body {:status keyword?}}}
             :handler deactivate-opportunity-handler}}]]

   ;; Dashboard Statistics
   ["/stats"
    {:get {:summary "get platform statistics for dashboard"
           :responses {200 {:body map?}}
           :handler get-stats-handler}}]])
