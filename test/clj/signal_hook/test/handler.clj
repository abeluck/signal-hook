(ns signal-hook.test.handler
  (:require [clojure.test :refer :all]
            [ring.mock.request :refer :all]
            [signal-hook.handler :refer :all]
            [mount.core :as mount]
            [signal-hook.signal.ISignalClient :refer [ISignalClient]]
            ))

(defrecord DummySignalClient []
  ISignalClient
  (is-ready? [_]  true)
  (send-message! [_ body recipients] (println "SEND MESSAGE" body recipients))
  (send-group-message![_ body groups] (println "SEND GROUP MESSAGE" body groups)))

(defn new-dummy-signal-client []
  (->DummySignalClient))

(use-fixtures
  :once
  (fn [f]
    (mount/start #'signal-hook.config/env)
    (mount/start-with {#'signal-hook.signal.api/signal-client (new-dummy-signal-client)})
    (mount/start #'signal-hook.handler/app)
    (f)))


(defn post-request [route json] (app (-> (request :post route) (json-body json))))

(def send-route "/send")

(deftest test-app
  ; (testing "main route"
  ; (let [response (app (request :get "/"))]
  ; (is (= 200 (:status response)))))

  (testing "send invalid params"
    (let [response (post-request send-route {})]
      (is (= 400 (:status response)))))

  (testing "send invalid params 2"
    (let [response (post-request send-route {:To ["55555555"] :Body "jaguar"})]
      (is (= 400 (:status response)))))

  (testing "send invalid params 3"
    (let [response (post-request send-route {:To ["+55555555", "missinggroupprefix=="] :Body "jaguar"})]
      (is (= 400 (:status response)))))

  (testing "send invalid params 4"
    (let [response (post-request send-route {:Body "jaguar"})]
      (is (= 400 (:status response)))))

  (testing "send valid params"
    (let [response (post-request send-route {:To ["+15555555", "group:somegroupidXX=="] :Body "jaguar"})]
      (is (= 200 (:status response)))))

  (testing "send with single recipient"
    (let [response (post-request send-route {:To "+15555555" :Body "jaguar"})]
      (is (= 200 (:status response)))))
  )
