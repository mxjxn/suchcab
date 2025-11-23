(ns suchcab.routes.services.messages
  (:require
   [ring.util.http-response :refer :all]
   [suchcab.db.messages :as messages]))

(defn send-message-handler
  [{{{:keys [from-user-id to-user-id ride-id content] :as opts} :body} :parameters}]
  (let [result (messages/send-message opts)]
    (ok result)))

(defn mark-read-handler
  [{{{:keys [message-id]} :body} :parameters}]
  (let [result (messages/mark-message-read message-id)]
    (ok result)))

(defn get-conversation-handler
  [{{{:keys [user-id-1 user-id-2]} :query} :parameters}]
  (let [conversation (messages/get-conversation user-id-1 user-id-2)]
    (ok {:messages conversation})))

(defn get-ride-messages-handler
  [{{{:keys [ride-id]} :path} :parameters}]
  (let [ride-messages (messages/get-ride-messages ride-id)]
    (ok {:messages ride-messages})))

(defn get-unread-handler
  [{{{:keys [user-id]} :path} :parameters}]
  (let [unread (messages/get-unread-messages user-id)]
    (ok {:messages unread})))

(defn get-user-conversations-handler
  [{{{:keys [user-id]} :path} :parameters}]
  (let [conversation-users (messages/get-user-conversations user-id)]
    (ok {:users conversation-users})))

(defn message-routes []
  ["/messages"
   {:swagger {:tags ["messages"]}}
   ["/send"
    {:post {:summary "send a message"
            :parameters {:body {:from-user-id uuid?
                                :to-user-id uuid?
                                :content string?}}
            :responses {200 {:body {:status keyword?}}}
            :handler send-message-handler}}]
   ["/read"
    {:put {:summary "mark message as read"
           :parameters {:body {:message-id uuid?}}
           :responses {200 {:body {:status keyword?}}}
           :handler mark-read-handler}}]
   ["/conversation"
    {:get {:summary "get conversation between two users"
           :responses {200 {:body {:messages vector?}}}
           :handler get-conversation-handler}}]
   ["/ride/:ride-id"
    {:get {:summary "get messages for a ride"
           :parameters {:path {:ride-id uuid?}}
           :responses {200 {:body {:messages vector?}}}
           :handler get-ride-messages-handler}}]
   ["/unread/:user-id"
    {:get {:summary "get unread messages for user"
           :parameters {:path {:user-id uuid?}}
           :responses {200 {:body {:messages vector?}}}
           :handler get-unread-handler}}]
   ["/conversations/:user-id"
    {:get {:summary "get list of users with conversations"
           :parameters {:path {:user-id uuid?}}
           :responses {200 {:body {:users vector?}}}
           :handler get-user-conversations-handler}}]])
