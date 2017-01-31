(ns reagent-test.core
	(:require
		[cljsjs.material-ui]
		[reagent.core :as r :refer [atom]]
		[reagent-test.view :as view]
		[reagent-test.repository :as repository]
		[cljs-react-material-ui.reagent :as rui]))

(enable-console-print!)




;(defonce conn1
;		 (browser/connect "http://localhost:9000/repl"))


(defn application []
	(let [products (repository/products)
		  users (repository/get-users)]
		[rui/mui-theme-provider
		 [:div
		  [view/price-list products]
		  [view/users-component products users repository/add-user]]]))


(r/render-component [application]
					(.getElementById js/document "app"))

(defn on-js-reload []
	;; optionally touch your app-state to force rerendering depending on
	;; your application
	;; (swap! app-state update-in [:__figwheel_counter] inc)
	)
