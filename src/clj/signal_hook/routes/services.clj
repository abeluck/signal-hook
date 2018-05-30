(ns signal-hook.routes.services
  (:require [ring.util.http-response :refer :all]
            [clojure.tools.logging :as log]
            [compojure.api.sweet :refer :all]
            [schema.core :as s]
            [compojure.api.meta :refer [restructure-param]]
            [buddy.auth.accessrules :refer [restrict]]
            [buddy.auth :refer [authenticated?]]
            [signal-hook.config :refer [env]]
            [signal-hook.signal.ISignalClient :refer [send-message! send-group-message! is-ready?]]
            [signal-hook.signal.api :refer [signal-client]]
            [clojure.string :as string]))

(defn access-error [_ _]
  (unauthorized {:error "unauthorized"}))

(defn wrap-restricted [handler rule]
  (restrict handler {:handler  rule
                     :on-error access-error}))

(defmethod restructure-param :auth-rules
  [_ rule acc]
  (update-in acc [:middleware] conj [wrap-restricted rule]))

(defmethod restructure-param :current-user
  [_ binding acc]
  (update-in acc [:letks] into [binding `(:identity ~'+compojure-api-request+)]))

(def number-regex #"\+\d+")
(defn is-phone-number? [in] (and (string? in) (re-matches number-regex in)))

(def group-regex #"group:.*==")
(defn is-group-id? [in] (and (string? in) (re-matches group-regex in)))

(s/defschema PhoneNumber (s/pred is-phone-number? 'is-phone-number?))
(s/defschema Group (s/pred is-group-id? 'is-group-id?))
(s/defschema Recipient (s/either PhoneNumber Group))

(s/defschema Payload {:To   (s/either [Recipient] Recipient)
                      :Body s/Str
                      })


(defn split-recipients [to-list]
  (cond
    (is-group-id? to-list) {:groups [to-list]}
    (is-phone-number? to-list) {:users [to-list]}
    :else (group-by #(if (string/starts-with? % "group:") :groups :users) to-list)))

(defn process-recipients
  "Given the to-list, a list of PhoneNumbers and Groups, returns a map containing the groups and users split. Groups also have the group: prefix removed."
  [to-list]
  (let [recipients (split-recipients to-list)]
    (update recipients
            :groups (fn [v] (mapv #(subs % 6) v)) ; cut off the group: prefix
            )))

(defn execute-send [payload]
  (let [{groups :groups users :users} (process-recipients (:To payload))]
    (when (not-empty users) (send-message! signal-client (:Body payload) users))
    (when (not-empty groups) (send-group-message! signal-client (:Body payload) groups))
    payload))

(defapi service-routes
  {:swagger {:ui "/swagger-ui"
             :spec "/swagger.json"
             :data {:info {:version "0.0.1"
                           :title "Signal Hook API"
                           :description "Send Signal Messages"}}}}

  (GET "/authenticated" []
    :auth-rules authenticated?
    :current-user user
    (ok {:user user}))

  (context "/" []
    :tags ["signal-hook"]

    (POST "/send" []
      :auth-rules authenticated?
      :return Payload
      :body [body Payload]
      :summary "Send a message"
      (log/info "/send")
      (if (is-ready? signal-client)
        (ok (execute-send body))
        (internal-server-error {:error "signal client not ready"})
        ))
    ))
