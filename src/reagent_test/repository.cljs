(ns reagent-test.repository
	(:require [datascript.core :as d]
			  ))

(enable-console-print!)

(def schema {:basket        {:db/valueType   :db.type/ref
							 :db/cardinality :db.cardinality/many}
			 :product/ref   {:db/valueType :db.type/ref}
			 :product/id    {:db/valueType :db.type/ref}
			 :product/title {:db/unique :db.unique/value}
			 :person/name   {:db/unique :db.unique/value}
			 })


(def initial-data
	[{:db/id           -1
	  :product/title   "Mayo"
	  :product/measure "pcs"
	  :product/price   22}

	 {:db/id           -2
	  :product/title   "Tofu"
	  :product/measure "kg"
	  :product/price   48}

	 {:db/id       -3
	  :person/name "akond"
	  :basket      [{:product/ref -1 :product/quantity 4
					 } {:product/ref -2 :product/quantity 2.5}]
	  }




	 ;{:db/id -3 :product/quantity 12 :product/id -1}
	 ;{:basket "akond" :product/title "Tofu" :product/quantity 2.3}
	 ])

(def initial-data2
	[[:db/add 1 :product/title "Mayo"]
	 [:db/add 1 :product/measure "pcs"]
	 [:db/add 1 :product/price 22]

	 [:db/add 2 :product/title "Tofu"]
	 [:db/add 2 :product/measure "kg"]
	 [:db/add 2 :product/price 48]

	 [:db/add 10 :person/name "akond"]
	 ;[:db/add 10 :basket/amount 1]
	 ;[:db/add 10 :basket/item 1]
	 ;[:db/add 10 :basket [1 3]]
	 ;[:db/add 10 :basket [2 4]]

	 [:db/add 11 :person/name "flora"]
	 ;[:db/add 11 :basket [1 0.4]]
	 ;[:db/add 11 :basket [2 11]]
	 ])

(defn dbg [x]
	(do (prn x)
		x))

(def conn (-> (d/empty-db schema)
			  (d/db-with initial-data)
			  (d/conn-from-db)))

(pr @conn)
#_(d/transact! conn [
					 {:product/title   "Mayo"
					  :product/id      :mayo
					  :product/measure "pc"
					  :product/price   22}

					 {:product/title   "Tofu"
					  :product/measure "kg"
					  :product/price   48}

					 {:product/title   "Abba"
					  :product/measure "pc"
					  :product/price   10}
					 ])

(defn q [query & rest]
	(apply d/q (into [query @conn] rest)))


(defn pull [query & rest]
	(d/pull-many @conn '[*] (apply q (into [query] rest))))


(defn products []
	(let [rows (pull '[:find [?e ...] :where [?e :product/title]])]
		(sort-by :product/title rows)))


(defn- add-record [record]
	(let [id (d/tempid true)]
		(d/datom))
	#_(let [tx (d/transact! conn [record])
			id (d/tempid true)]

		  (.-e (first (.-tx_data tx)))))

(defn add-user [name]
	(add-record {:person/name name})
	#_(let [tx (d/transact! conn [{:person/name name}])]
		  (.-e (first (.-tx_data tx)))))



(defn get-users []
	(->> (pull '[:find [?p ...]
				 :where
				 [?p :person/name]])
		 (sort-by :person/name)))


(defn user-buys [user product]
	(d/transact! conn [{:db/id user :person/order product}]))

(pr (d/tempid true))
(pr (d/datom 1 2 3))


;(prn "Added user" (js-keys (add-user "fluffy")))
;(prn "Datom" (js-keys (first (.-tx_data (add-user "akond")))))
;(prn "Datom" (.-e (first (.-tx_data (add-user "akond2")))))
;(prn (.-tempids (add-user "lola")))
;
;(pr (d/entity @conn -1))
;(pr (d/entity @conn 2))
;
;(pr @conn)
;(pr "Entity" (q '[:find ?e
;				  :where
;				  [?e :person/name "akond"]]))

;(def u (add-user "akond"))
;(def p (add-record {:product/title   "Mayo"
;					:product/id      :mayo
;					:product/measure "pc"
;					:product/price   22}))
;(user-buys u p)
;;(pr @conn)
;(add-record [])


#_(pr (q '[:find ?e ?a ?v
		   :where
		   [?e ?a ?v]]))

(pr (q '[:find ?q ?title
		 :where
		 [?p :person/name "akond"]
		 [?p :basket ?b]
		 [?b :product/quantity ?q]
		 [?b :product/ref ?product]
		 [?product :product/title ?title]
		 ]))


#_(pr (pull '[:find ?person
			  :where
			  [?person :person/name "akond"]
			  ;[?person :product/id ?product]
			  ;[?product :product/title ?title]

			  ]))



#_(pr (q '[:find [?attr ...]
		   :where
		   [?e :person/order ?b]
		   [?b ?attr]]))

;(user-buys (add-user "akond") :product)
;(prn "lola" (add-user "lola"))

#_(prn "Test" (d/q '[:find [?e ...] :where [?e :product/title]] @conn))
;(prn "Sample" (d/pull-many @conn '[*] (d/q '[:find [?e ...] :where [?e :product/title]] @conn)))

;(prn (products))
;(prn (sort-by identity [3 1 2]))
#_(prn "Products" (q '[:find ?val ?attr
					   :where
					   [?e :product/title]
					   [?e ?attr ?val]]))


