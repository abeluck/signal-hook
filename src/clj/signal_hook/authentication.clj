(ns signal-hook.authentication
  (:require
    [buddy.auth.backends.httpbasic :refer [http-basic-backend]]
    [signal-hook.config :refer [env]]
    ))


(defn authdata [] (:api-creds env))

(defn authfn
  [req {:keys [username password]}]
  (when-let [user-password (get (authdata) (keyword username))]
    (when (= password user-password)
      (keyword username))))

(defn basic-auth-backend []
  (http-basic-backend {
                   :realm  "SignalHook"
                   :authfn authfn
                   }))

