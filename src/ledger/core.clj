(ns ledger.core
  (require [clojure.string :as s]
           [clj-time.core :as t]
           [clj-time.format :as f]))

(def current-year (t/year (t/now)))

(def amt-re #"([-+*])?(\d+)")

(def entry-re #"([-+*]?\d+)\s(.*)")

(def date-re #"^(\d+)-(\w+)$")

(defn date-entry? [line]
  (re-matches date-re line))

(defn text->date [text]
  (str (f/unparse
         (f/formatter "dd-MM")
         (f/parse (f/formatter "dd-MMM") text))
       "-"
       current-year))

(defn amt [a]
  (let [[_ t n] (re-matches #"([-+*])?(\d+)" a)]
    {:amt (Integer. n)
     :txn ({"-" :debit "+" :credit "*" :update} t)}))

(defn info [i]
  (let [tokens (s/split i #" ")
        [a the-rest] [(last tokens) (butlast tokens)]
        [b the-rest-2] [(last the-rest) (butlast the-rest)]
        bal (if (= "bal" a) b (if (= "bal" b) a))
        d (if (nil? bal) tokens the-rest-2)]
    {:desc (s/join " " d)
     :bal (if (not (nil? bal)) (Integer. bal))}))

(defn text->entry [txt]
  (let [[_ a i] (re-matches #"([-+*]?\d+)\s(.*)" txt)]
    (merge {:acct :wallet, :for :self}
           (amt a)
           (info i))))

(defn text->full-entry [txt date i]
  (merge {:id i}
         {:day date}
         (text->entry txt)))

(defn lines->entries [lines]
  (loop [date nil
         s (filter #(not (empty? %)) lines)
         entries []
         id 0]
    (if (not (seq s))
      entries
      (let [[date' s'] (if (date-entry? (first s))
                         [(text->date (first s)) (rest s)]
                         [date s])]
          (recur date'
                 (rest s')
                 (conj entries (text->full-entry (first s') date' id))
                 (inc id))))))
