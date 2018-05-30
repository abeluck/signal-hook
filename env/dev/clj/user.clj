(ns user
  (:require [signal-hook.config :refer [env]]
            [mount.core :as mount]
            [signal-hook.core :refer [start-app]]))

(defn start []
  (mount/start-without #'signal-hook.core/repl-server))

(defn stop []
  (mount/stop-except #'signal-hook.core/repl-server))

(defn restart []
  (stop)
  (start))


