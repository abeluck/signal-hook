(ns signal-hook.signal.cli
  (:require
    [me.raynes.conch :refer [programs with-programs let-programs] :as sh]
    [signal-hook.config :refer [env]]
    [signal-hook.signal.ISignalClient :refer [ISignalClient]]
    [clojure.tools.logging :as log]
    ))


(defn get-path [] (get-in env [:signal-cli :path]))
(defn get-state-dir [] (get-in env [:signal-cli :state-dir]))
(defn get-username [] (get-in env [:signal-cli :username]))

(defn make-args [args]
  (let [final-args (concat ["--config" (get-state-dir)] (vec args) [{:throw false, :verbose true}])]
    (log/info "args: " final-args)
    final-args))

(defn signal-cli [& args]
  (let [output (sh/let-programs [_signal-cli (get-path)]
                         (apply _signal-cli (make-args args)))]
    (log/info "signal-cli: " output)
    output
    ))

(defn is-user-valid? []
  (try (signal-cli "--username" (get-username) "listDevices")
       true
       (catch Exception e false)))

(defn log-config []
  (log/info "signal-cli config: ")
  (log/info "  " (get-path))
  (log/info "  " (get-state-dir))
  (log/info "  " (get-username)))

(defn is-config-valid? []
  (log-config)
  (and
    (not-empty (get-path))
    (not-empty (get-state-dir))
    (not-empty (get-username))))

(defrecord SignalClient []
   ISignalClient

   (is-ready? [_]
     (and (is-config-valid?) (is-user-valid?)))

   (send-message! [_ body recipients]
     (apply signal-cli (concat ["--username" (get-username) "send" "--message" body] recipients)))

   (send-group-message! [_ body groups]
     (doall
       (map #(apply signal-cli ["--username" (get-username) "send" "--group" % "--message" body]) groups)))
   )

(defn new-signal-client []
  (->SignalClient))



