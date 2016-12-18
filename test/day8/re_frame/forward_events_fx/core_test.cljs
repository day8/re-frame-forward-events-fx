(ns day8.re-frame.forward-events-fx.core-test
  (:require [cljs.test :refer-macros [is deftest testing]]
            [day8.re-frame.forward-events-fx]
            [re-frame.core :as re-frame]
            [re-frame.router]))


(defn register-event-dbs []
  (re-frame/reg-event-db  :1 (fn [db _] db))
  (re-frame/reg-event-db  :2 (fn [db _] db))
  (re-frame/reg-event-db  :3 (fn [db _] db))
  (re-frame/reg-event-db  :4 (fn [db _] db))
  (re-frame/reg-event-db  :11 (fn [db _] db))
  (re-frame/reg-event-db  :22 (fn [db _] db))

  (re-frame/reg-event-db  :later (fn [db _] db)))

(defn dispatch-events [fake-dispatch]
  (with-redefs [re-frame/dispatch fake-dispatch]
    (re-frame/dispatch-sync [:register-test])
    (re-frame/dispatch-sync [:4])
    (re-frame/dispatch-sync [:3])
    (re-frame/dispatch-sync [:2])
    (re-frame/dispatch-sync [:1])
    (re-frame/dispatch-sync [:unregister-test])
    (re-frame/dispatch-sync [:11])
    (re-frame/dispatch-sync [:22])))


(deftest test1
  (let [dispatched-events (atom #{})
        fake-dispatch     (fn [event]
                            (swap! dispatched-events conj event))]

    ;; setup effects handler
    (re-frame/reg-event-fx
      :register-test
      (fn [world event]
        {:forward-events {:register  :my-id
                          :events     #{:1  :2 :11 :22}
                          :dispatch-to [:later :on ]}}))

    (re-frame/reg-event-fx
      :unregister-test
      (fn [world event]
        {:forward-events {:unregister  :my-id}}))

    (register-event-dbs)
    (dispatch-events fake-dispatch)

    (is (= @dispatched-events #{[:later :on [:1]]  [:later :on [:2]]}))))


(deftest test2
  (testing "collection of forward-events"
    (let [dispatched-events (atom #{})
          fake-dispatch     (fn [event]
                              (swap! dispatched-events conj event))]

      ;; setup effects handler
      (re-frame/reg-event-fx
       :register-test
       (fn [world event]
         {:forward-events [{:register    :my-id
                            :events      #{:1  :11}
                            :dispatch-to [:later :on ]}
                           {:register    :my-id2
                            :events      #{:2 :22}
                            :dispatch-to [:later :on ]}]}))

      (re-frame/reg-event-fx
       :unregister-test
       (fn [world event]
         {:forward-events [{:unregister :my-id }
                           {:unregister :my-id2 }]}))

      (register-event-dbs)
      (dispatch-events fake-dispatch)

      (is (= @dispatched-events #{[:later :on [:1]]  [:later :on [:2]]})))))


(deftest test3
  (testing "unregister a collection of ids"
    (let [dispatched-events (atom #{})
          fake-dispatch     (fn [event]
                              (swap! dispatched-events conj event))]

      ;; setup effects handler
      (re-frame/reg-event-fx
       :register-test
       (fn [world event]
         {:forward-events [{:register    :my-id
                            :events      #{:1  :11}
                            :dispatch-to [:later :on ]}
                           {:register    :my-id2
                            :events      #{:2 :22}
                            :dispatch-to [:later :on ]}]}))

      (re-frame/reg-event-fx
       :unregister-test
       (fn [world event]
         {:forward-events [{:unregister [:my-id :my-id2]}]}))

      (register-event-dbs)
      (dispatch-events fake-dispatch)

      (is (= @dispatched-events #{[:later :on [:1]]  [:later :on [:2]]})))))
