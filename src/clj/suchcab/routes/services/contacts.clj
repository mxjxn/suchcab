(ns suchcab.routes.services.contacts
  (:require
   [ring.util.http-response :refer :all]
   [suchcab.db.contacts :as contacts]))

(defn add-contact-handler
  [{{{:keys [passenger-id driver-id notes] :as opts} :body} :parameters}]
  (let [result (contacts/add-contact opts)]
    (ok result)))

(defn remove-contact-handler
  [{{{:keys [passenger-id driver-id] :as opts} :body} :parameters}]
  (let [result (contacts/remove-contact passenger-id driver-id)]
    (ok result)))

(defn get-contacts-handler
  [{{{:keys [passenger-id]} :path} :parameters}]
  (let [contacts-list (contacts/get-passenger-contacts passenger-id)]
    (ok {:contacts contacts-list})))

(defn check-favorite-handler
  [{{{:keys [passenger-id driver-id]} :query} :parameters}]
  (let [is-fav (contacts/is-favorite? passenger-id driver-id)]
    (ok {:is-favorite is-fav})))

(defn contact-routes []
  ["/contacts"
   {:swagger {:tags ["contacts"]}}
   ["/add"
    {:post {:summary "add driver to favorites/contacts"
            :parameters {:body {:passenger-id uuid?
                                :driver-id uuid?}}
            :responses {200 {:body {:status keyword?}}}
            :handler add-contact-handler}}]
   ["/remove"
    {:delete {:summary "remove driver from contacts"
              :parameters {:body {:passenger-id uuid?
                                  :driver-id uuid?}}
              :responses {200 {:body {:status keyword?}}}
              :handler remove-contact-handler}}]
   ["/:passenger-id"
    {:get {:summary "get all contacts for passenger"
           :parameters {:path {:passenger-id uuid?}}
           :responses {200 {:body {:contacts vector?}}}
           :handler get-contacts-handler}}]
   ["/check"
    {:get {:summary "check if driver is favorite"
           :responses {200 {:body {:is-favorite boolean?}}}
           :handler check-favorite-handler}}]])
