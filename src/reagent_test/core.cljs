(ns reagent-test.core
	(:require
		[cljsjs.material-ui]
		[reagent.core :as r :refer [atom]]
		[reagent-test.view :as view :refer [application]]
		))

(enable-console-print!)

(r/render-component [application]
					(.getElementById js/document "app"))

(defn on-js-reload []
	;; optionally touch your app-state to force rerendering depending on
	;; your application
	;; (swap! app-state update-in [:__figwheel_counter] inc)
	)
