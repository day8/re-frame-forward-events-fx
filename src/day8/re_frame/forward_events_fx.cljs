(ns day8.re-frame.forward-events-fx
  (:require [re-frame.core :as re-frame]))


(defn as-callback-pred
  "Looks at the required-events items and returns a predicate which
  will either
  - match only the event-keyword if a keyword is supplied
  - match the entire event vector if a collection is supplied
  - returns a callback-pred if it is a fn"
  [callback-pred]
  (when callback-pred
    (cond (fn? callback-pred) callback-pred
          (keyword? callback-pred) (fn [[event-id _]]
                                     (= callback-pred event-id))
          (coll? callback-pred) (fn [event-v]
                                  (= callback-pred event-v))
          :else (throw
                  (ex-info (str (pr-str callback-pred)
                             " isn't an event predicate")
                    {:callback-pred callback-pred})))))


(re-frame/reg-fx
  :forward-events
  (let [process-one-entry (fn [{:as m :keys [unregister register events dispatch-to]}]
                            (let [_ (assert (map? m) (str "re-frame: effects handler for :forward-events expected a map or a list of maps. Got: " m))
                                  _ (assert (or (= #{:unregister} (-> m keys set))
                                                (= #{:register :events :dispatch-to} (-> m keys set))) (str "re-frame: effects handler for :forward-events given wrong map keys" (-> m keys set)))]
                              (if unregister
                                (re-frame/remove-post-event-callback unregister)
                                (let [events-preds           (map as-callback-pred events)
                                      post-event-callback-fn (fn [event-v _]
                                                               (when (some (fn [pred] (pred event-v))
                                                                           events-preds)
                                                                 (re-frame/dispatch (conj dispatch-to event-v))))]
                                  (re-frame/add-post-event-callback register post-event-callback-fn)))))]
    (fn [val]
      (cond
        (map? val) (process-one-entry val)
        (sequential? val) (doall (map process-one-entry val))
        :else (re-frame/console :error ":forward-events expected a map or a list of maps, but got: " val)))))
