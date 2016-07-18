(ns day8.re-frame.forward-events-fx.core-test
  (:require [cljs.test :refer-macros [is deftest]]
            [day8.re-frame.forward-events-fx :as core]))

(deftest stub
  "stub test to excercise test framework and devtools"
  (let [data {:feed {:flint 5}}]
    (js-debugger))
  (is (= 5 5)))


