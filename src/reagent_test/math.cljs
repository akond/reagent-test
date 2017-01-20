(ns reagent-test.math
	(:require [goog.string :as gstring]
			  [goog.string.format]
			  [cljs.reader :as reader]
			  [clojure.string :as string]
			  [reagent-test.state :refer [app-state value]]))


(defn format-number
	([n] (gstring/format "%.2f" n))
	([n s] (gstring/format "%.2f %s" n s)))


(defn valid-number? [a]
	((every-pred number? pos?) a))

(defn- add-leading-zero [s]
	(if (and (string? s) (re-matches #"^\.\d+" s)) (str "0" s) s))

(defn- convert-string-to-symbols [s]
	(map (comp reader/read-string add-leading-zero) (string/split s #" ")))

(defn calculate-string [s]
	(apply + (filter valid-number? (convert-string-to-symbols s))))

(defn invalid-symbols? [s]
	((complement empty?) (filter (complement valid-number?) (convert-string-to-symbols s))))


(defn get-cell [row col]
	(get-in (:cells @app-state) [row col] ""))

(defn get-col [col]
	(apply + (map calculate-string (map #(get-in (:cells @app-state) [% col]) (range (count (:users @app-state)))))))


(defn get-price [n]
	(get-in (:price-list @app-state) [n :price]))

(defn get-measure [n]
	(get-in (:price-list @app-state) [n :measure]))


(defn get-row [n]
	(let [price-list (:price-list @app-state)
		  cols (range (count price-list))
		  prices (map :price price-list)
		  cells (map calculate-string (map (partial get-cell n) cols))]
		(apply + (map * prices cells))))
