(ns reagent-test.view
	(:require [cljs-react-material-ui.reagent :as rui]
			  [reagent-test.state :refer [app-state value dialog?]]
			  [reagent-test.math :as math]
			  ))



(defn event-value [evt]
	(-> evt
		(.-target)
		(.-value)))


(defn price-list []
	[rui/table {:selectable false :width 100}
	 [rui/table-header {:displaySelectAll  false
						:adjustForCheckbox false}
	  [rui/table-row
	   [rui/table-header-column "Title"]
	   [rui/table-header-column "Price"]]]


	 [rui/table-body {:displayRowCheckbox false}
	  (for [[id {:keys [title price]}] (map-indexed vector (:price-list @app-state))]
		  ^{:key (str "price-" id)}
		  [rui/table-row
		   [rui/table-row-column title]
		   [rui/table-row-column price]])
	  ]])


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



(defn user-list []
	(let [products (map-indexed vector (:price-list @app-state))
		  users (map-indexed vector (:users @app-state))]
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
			   (doall (for [[id {:keys [title]}] products]
						  ^{:key (str "title-" id)}
						  [rui/table-header-column title]))
			   (doall (for [[id {:keys [title]}] products]
						  ^{:key (str "price-title-" id)}
						  [rui/table-header-column (str "x " (math/format-number (math/get-price id)))])))

		   [rui/table-header-column "Sub Total"]]]


		 [rui/table-body {:displayRowCheckbox false}
		  (doall (for [[no user] users]
					 ^{:key (str "user-" no)}
					 [rui/table-row
					  [rui/table-row-column {:style {:width 50}} (inc no)]
					  [rui/table-row-column (:name user)]

					  (interleave
						  (doall (for [[id product] products]
									 ^{:key (str "editable-cell-" id)}
									 (editable-cell no id)))

						  (doall (for [[id product] products]
									 ^{:key (str "prod" id)}
									 [rui/table-row-column (math/format-number (* (math/calculate-string (math/get-cell no id)) (math/get-price id)) "UAH")])))

					  [rui/table-row-column (math/format-number (math/get-row no) "UAH")]]))]
		 [rui/table-footer [total-row]]
		 ]))

(defn new-user-dialog []
	(let [val (atom "")]
		[rui/dialog {:open  true
					 :modal false
					 :style {:width 800}}

		 [:div {:class ""}
		  [:div {:class "row"}
		   [:div {:class "col-sm-6"}
			[rui/text-field {:hintText           "Name"
							 :floatingLabelFixed true
							 :floatingLabelText  "New user's name"
							 :onChange           #(reset! val (event-value %))
							 :defaultValue       @val
							 :auto-focus         true
							 }
			 ]]]
		  [:div {:class "row"}
		   [:div {:class "col-sm-3"}
			[rui/raised-button {:label   "OK"
								:primary true
								:onClick #(do (swap! app-state assoc-in [:users] (cons {:name @val} (:users @app-state)))
											  (reset! dialog? false))}]]
		   [:div {:class "col-sm-3"}
			[rui/raised-button {:label   "Cancel"
								:onClick #(reset! dialog? false)}]]]]
		 ]))


(defn application []
	[rui/mui-theme-provider
	 [:div {:style {:width "90%"}}
	  [:h1 "Price list"]

	  [:div {:style {:width "400px"}}
	   [price-list]]


	  [:h1 "Users"]
	  [:div {:style {:text-align "right"}}
	   (if (true? @dialog?)
		   [new-user-dialog]
		   [rui/raised-button
			{:label   "Add new user"
			 :primary true
			 :onClick #(reset! dialog? true)
			 }])

	   [user-list]]]])
