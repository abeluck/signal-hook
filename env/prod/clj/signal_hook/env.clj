(ns signal-hook.env
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[signal-hook started successfully]=-"))
   :stop
   (fn []
     (log/info "\n-=[signal-hook has shut down successfully]=-"))
   :middleware identity})
