(ns suchcab.db.messages
  (:require
   [suchcab.db :refer [crux-node]]
   [crux.api :as crux]
   [java-time :as time])
  (:import (java.util UUID)))

(defn send-message
  "Send a message between driver and passenger"
  [{:keys [from-user-id to-user-id ride-id content] :as opts}]
  (let [message-uuid (UUID/randomUUID)
        new-message {:crux.db/id message-uuid
                     :message/from-user-id from-user-id
                     :message/to-user-id to-user-id
                     :message/ride-id ride-id
                     :message/content content
                     :message/read false
                     :message/created-at (time/instant)}
        result (crux/submit-tx crux-node [[:crux.tx/put new-message]])]
    {:status :success
     :message-id message-uuid
     :tx-id (:crux.tx/tx-id result)}))

(defn get-message-by-id [message-id]
  (crux/entity (crux/db crux-node) message-id))

(defn mark-message-read
  "Mark a message as read"
  [message-id]
  (let [message (get-message-by-id message-id)]
    (when message
      (let [updated-message (merge message {:message/read true})
            result (crux/submit-tx crux-node [[:crux.tx/put updated-message]])]
        {:status :success
         :message-id message-id
         :tx-id (:crux.tx/tx-id result)}))))

(defn get-conversation
  "Get all messages between two users"
  [user-id-1 user-id-2]
  (crux/q (crux/db crux-node)
          {:find '[(pull e [*])]
           :where '[(or (and [e :message/from-user-id user1]
                             [e :message/to-user-id user2])
                        (and [e :message/from-user-id user2]
                             [e :message/to-user-id user1]))]
           :args [{'user1 user-id-1 'user2 user-id-2}]}))

(defn get-ride-messages
  "Get all messages for a specific ride"
  [ride-id]
  (crux/q (crux/db crux-node)
          {:find '[(pull e [*])]
           :where '[[e :message/ride-id rid]]
           :args [{'rid ride-id}]
           :order-by '[[e :message/created-at :asc]]}))

(defn get-unread-messages
  "Get all unread messages for a user"
  [user-id]
  (crux/q (crux/db crux-node)
          {:find '[(pull e [*])]
           :where '[[e :message/to-user-id uid]
                    [e :message/read false]]
           :args [{'uid user-id}]}))

(defn get-user-conversations
  "Get list of users that a user has conversations with"
  [user-id]
  (let [sent (crux/q (crux/db crux-node)
                     {:find '[to-user]
                      :where '[[e :message/from-user-id uid]
                               [e :message/to-user-id to-user]]
                      :args [{'uid user-id}]})
        received (crux/q (crux/db crux-node)
                         {:find '[from-user]
                          :where '[[e :message/to-user-id uid]
                                   [e :message/from-user-id from-user]]
                          :args [{'uid user-id}]})]
    (distinct (concat (map first sent) (map first received)))))
