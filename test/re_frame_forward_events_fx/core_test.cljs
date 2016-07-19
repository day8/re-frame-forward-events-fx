(ns day8.re-frame.forward-events-fx.core-test
  (:require [cljs.test :refer-macros [is deftest]]
            [day8.re-frame.forward-events-fx]
            [re-frame.core :as re-frame]
            [re-frame.router]))

(deftest test1
  (let [dispatched-events  (atom #{})
        fake-dispatch      (fn [event]
                             (swap! dispatched-events conj event))]

    ;; setup effects handler
    (re-frame/def-event-fx
      :register-test
      (fn [world event]
        {:forward-events {:register  :my-id
                          :events     #{:1  :2 :11 :22}
                          :dispatch-to [:later :on ]}}))

    (re-frame/def-event-fx
      :unregister-test
      (fn [world event]
        {:forward-events {:unregister  :my-id}}))

    (re-frame/def-event :1 (fn [db _] db))
    (re-frame/def-event :2 (fn [db _] db))
    (re-frame/def-event :3 (fn [db _] db))
    (re-frame/def-event :4 (fn [db _] db))
    (re-frame/def-event :11 (fn [db _] db))
    (re-frame/def-event :22 (fn [db _] db))

    (re-frame/def-event :later (fn [db _] db))

    (with-redefs [re-frame/dispatch fake-dispatch]
                 (re-frame/dispatch-sync [:register-test])
                 (re-frame/dispatch-sync [:4])
                 (re-frame/dispatch-sync [:3])
                 (re-frame/dispatch-sync [:2])
                 (re-frame/dispatch-sync [:1])
                 (re-frame/dispatch-sync [:unregister-test])
                 (re-frame/dispatch-sync [:11])
                 (re-frame/dispatch-sync [:22]))

    (is (= @dispatched-events #{[:later :on [:1]]  [:later :on [:2]]}))))
