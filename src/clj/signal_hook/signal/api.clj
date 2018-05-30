(ns signal-hook.signal.api
  (:require
            [mount.core :refer [defstate]]
            [signal-hook.config :refer [env]]
            [signal-hook.signal.cli :refer [new-signal-client]]
            ))

(defstate signal-client
          :start (new-signal-client))
