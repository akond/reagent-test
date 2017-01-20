(ns reagent-test.state
	(:require [reagent.core :as r :refer [atom]]))

(def app-state (atom {:editable-cell nil
					  :price-list    [{:title   "Mayo"
									   :price   22.00
									   :measure "pc"}
									  {:title   "Tofu"
									   :price   48.00
									   :measure "kg"
									   }
									  ]
					  :users         [{:name "akond"}
									  {:name "rocky"}
									  ]
					  :cells         {0 {0 "1 2 3" 1 "0.12 0.13"}}}))


(def value (atom ""))

(def dialog? (atom false))