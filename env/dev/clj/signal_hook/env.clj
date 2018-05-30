(ns signal-hook.env
  (:require [selmer.parser :as parser]
            [clojure.tools.logging :as log]
            [signal-hook.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init
   (fn []
     (parser/cache-off!)
     (log/info "\n-=[signal-hook started successfully using the development profile]=-"))
   :stop
   (fn []
     (log/info "\n-=[signal-hook has shut down successfully]=-"))
   :middleware wrap-dev})
