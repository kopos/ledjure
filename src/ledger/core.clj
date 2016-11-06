(ns ledger.core)

(def re #"([-+*])?([0-9]+)\s(.*)\s*bal*\s*([0-9]+)*")

(defn amt [a]
  (let [[_ t n] (re-matches #"([-+*])?(\d+)" a)]
    {:amt (Integer. n)
     :txn (get {"-" :debit "+" :credit "*" :update} t)}))

(defn info [d]
  {:desc "meds das"
   :bal 540})

(defn text->entry [txt]
  (let [[_ a i] (re-matches #"([-+*]?\d+)\s(.*)" txt)]
    (merge (amt a) (info i))))
