(ns ledger.core)

(defn amt [a]
  (let [[_ t n] (re-matches #"([-+*])?(\d+)" a)]
    {:amt (Integer. n)
     :txn ({"-" :debit "+" :credit "*" :update} t)}))

(defn info [i]
  (let [tokens (clojure.string/split i #" ")
        [a b & the-rest] (reverse tokens)
        bal (if (= "bal" a) b (if (= "bal" b) a))
        d (if (nil? bal) tokens (reverse the-rest))]
    {:desc (clojure.string/join " " d)
     :bal (if (not (nil? bal)) (Integer. bal))}))

(defn text->entry [txt]
  (let [[_ a i] (re-matches #"([-+*]?\d+)\s(.*)" txt)]
    (merge (amt a) (info i))))
