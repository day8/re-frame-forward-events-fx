(ns re-frame-forward-events-fx.core
  (:require [re-frame.core]))


(re-frame.core/reg-fx
  :forward-events
  (let [id->listen-fn (atom {})
        process-one-entry (fn [{:as m :keys [unlisten listen events dispatch-to]}]
                            (let [_  (assert (map? m) (str "re-frame: effects handler for :forward-events expected a map or a list of maps. Got: " m))
                                  _  (assert (or (= #{:unlisten} (-> m keys set))
                                                 (= #{:listen :events :dispatch-to} (-> m keys set))) "re-frame: effects handler for :forward-events given wrong map keys")]
                              (if unlisten
                                (let [f (@id->listen-fn unlisten)
                                      _  (assert (some? f) (str ":forward-events  asked to unregister an unknown id: " id))]
                                  (re-frame.core/remove-post-event-callback f)
                                  (swap! id->listen-fn dissoc unlisten))
                                (let [post-event-callback-fn  (fn [event-v _]
                                                                (when (events (first event-v))
                                                                  (dispatch (conj dispatch-to event-v))))]
                                  (re-frame.core/add-post-event-callback  post-event-callback-fn)
                                  (swap! id->listen-fn assoc listen post-event-callback-fn)))))]
    (fn [val]
      (cond
        (map? val) (process-one-entry val)
        (list? val) (doall (map process-one-entry val))
        :else (js/console.error  ":forward-events expected a map or a list of maps, but got: " val))
      )))
