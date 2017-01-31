(ns reagent-test.view
	(:require [cljs-react-material-ui.reagent :as rui]
			  [reagent.core :as r :refer [atom]]
			  [reagent-test.state :refer [app-state value]]
			  [reagent-test.math :as math]
			  ))

(defn enumerate [coll]
	(map-indexed (fn [a b] (merge b {:no (inc a)})) coll))


(defn event-value [evt]
	(-> evt
		(.-target)
		(.-value)))


(defn price-list [products]
	[:div {:style {:width "90%"}}
	 [:h1 "Price list"]
	 [:div {:style {:width "400px"}}
	  [rui/table {:selectable false :width 100}
	   [rui/table-header {:displaySelectAll  false
						  :adjustForCheckbox false}
		[rui/table-row
		 [rui/table-header-column "Title"]
		 [rui/table-header-column "Price"]]]


	   [rui/table-body {:displayRowCheckbox false}
		(for [product products]
			^{:key (str "price-" (:db/id product))}
			[rui/table-row
			 [rui/table-row-column (:product/title product)]
			 [rui/table-row-column (:product/price product)]])
		]]]])


(defn editable-cell [row col]
	(let [what-is-in-store (math/get-cell row col)
		  presentational-value (if (= "" what-is-in-store) "0.00" (-> what-is-in-store math/calculate-string math/format-number))
		  save-results (fn [evt] (do
									 (swap! app-state
											assoc-in
											(cons :cells (:editable-cell @app-state))
											(event-value evt))
									 (swap! app-state assoc :editable-cell nil)))
		  read-only-field [rui/table-row-column
						   (merge-with merge
									   {:style {:backgroundColor "PaleGreen"}}
									   (when (math/invalid-symbols? what-is-in-store) {:style {:color "red"}}))

						   presentational-value
						   " "
						   (math/get-measure col)]
		  editable-field (fn [value]
							 [rui/table-row-column
							  [rui/text-field {:hintText     "Enter quantity"
											   :onChange     (fn on-change [evt] (reset! value (event-value evt)))
											   :defaultValue @value
											   :onBlur       save-results
											   :auto-focus   true
											   :onKeyDown    (fn on-keydown [evt]
																 (when (= (.-keyCode evt) 13)
																	 (save-results evt)))}
							   ]])]
		(with-meta
			(if (= (:editable-cell @app-state) [row col])
				(editable-field value)
				read-only-field)
			{:key (str "cell-" col "-" row)}))
	)


(defn total-row []
	(let [users (range (count (:price-list @app-state)))]
		[rui/table-row
		 [rui/table-row-column {:style {:width 50}} "Total"]
		 [rui/table-row-column ""]

		 (interleave
			 (doall (for [col users]
						^{:key (str "measure-" col)}
						[rui/table-row-column (math/format-number (math/get-col col) (math/get-measure col))]))
			 (doall (for [col users]
						^{:key (str "measure2-" col)}
						[rui/table-row-column (math/format-number (* (math/get-col col) (math/get-price col)) "UAH")])))
		 [rui/table-row-column (math/format-number (apply + (map math/get-row users)) "UAH")]]))


(defn user-list [products users]
	(let [products (enumerate products)]
		[rui/table {:selectable  false
					:width       100
					:onCellClick (fn [row col evt]
									 (let [current-cell (:editable-cell @app-state)
										   col (- col 3)
										   new-cell [row (/ col 2)]]

										 (when (zero? (mod col 2))
											 (reset! value (apply math/get-cell new-cell))
											 (swap! app-state assoc :editable-cell
													(if (or (nil? current-cell) (= current-cell new-cell)) new-cell))
											 )))
					}
		 [rui/table-header {:displaySelectAll  false
							:adjustForCheckbox false}
		  [rui/table-row {:striped true}
		   [rui/table-header-column {:style {:width 50}} "#"]
		   [rui/table-header-column "Name"]

		   (interleave
			   (doall (for [product products]
						  ^{:key (str "title-" (:no product))}
						  [rui/table-header-column (:product/title product)]))
			   (doall (for [product products]
						  ^{:key (str "price-title-" (:no product))}
						  [rui/table-header-column (str "x " (math/format-number (:product/price product)))])))

		   [rui/table-header-column "Sub Total"]]]


		 [rui/table-body {:displayRowCheckbox false}
		  (doall (for [user (enumerate users)]
					 (let [no (:no user)
						   name (:person/name user)]
						 ^{:key (str "user-" no)}
						 [rui/table-row
						  [rui/table-row-column {:style {:width 50}} no]
						  [rui/table-row-column name]

						  #_(interleave
								(doall (for [product products]
										   ^{:key (str "editable-cell-" (:no product))}
										   (editable-cell no (:no product))))

								(doall (for [product products]
										   ^{:key (str "prod" (:no product))}
										   [rui/table-row-column "XXX"]
										   #_[rui/table-row-column (str '(math/format-number (* (math/calculate-string (math/get-cell no id)) (math/get-price id)) "UAH"))]
										   )))


						  #_[rui/table-row-column (math/format-number (math/get-row no) "UAH")]
						  [rui/table-row-column "YYY"]]
						 )))]]))


(defn new-user-dialog [dialog? add-user]
	(let [val (atom "")]
		[rui/dialog {:open  true
					 :modal false
					 :style {:width 800}}

		 [:div {:class ""}
		  [:div {:class "row"}
		   [:div {:class "col-sm-6"}
			[rui/text-field {:hintText           "Name"
							 :floatingLabelFixed true
							 :floatingLabelText  "User's name"
							 :onChange           #(reset! val (event-value %))
							 :defaultValue       @val
							 :auto-focus         true}]]]
		  [:div {:class "row"}
		   [:div {:class "col-sm-3"}
			[rui/raised-button {:label   "OK"
								:primary true
								:onClick #(do (add-user @val)
											  (reset! dialog? false))}]]
		   [:div {:class "col-sm-3"}
			[rui/raised-button {:label   "Cancel"
								:onClick #(reset! dialog? false)}]]]]
		 ]))


(defn users-component [products users add-user]
	(let [dialog? (atom false)]
		(fn []
			[:div
			 [:h1 "Users"]
			 [:div {:style {:text-align "right"}}
			  (if @dialog?
				  [new-user-dialog dialog? add-user]
				  [rui/raised-button
				   {:label   "Add new user"
					:primary true
					:onClick #(reset! dialog? true)
					}])

			  [user-list products users]]])))

#_[rui/table-footer [total-row]]
;]))

