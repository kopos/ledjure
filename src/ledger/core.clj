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

(defn lines [file]
  (map s/trim (s/split-lines (slurp file))))

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

(defn |>>
  "Pipelines the execution of each of the functions with the result of one
  function piped to the next. Every function must take a collection as the
  argument and produce a vector containing 2 collections. The first
  collection is accumulated while the second collection is passed as argument
  to the next function.

  Each function returns a 2 tuple. The first item is accumulated and the second
  item passed as the argument to the next function
  
  Essentially |>> converts a function call flow from

  (let [[m1 t1] (f1 t)
        [m2 t2] (f2 t1)
        [m3 t3] (f3 t2)
        m (concat [] m1 m2 m3])
        r t3]
    ...)

  to

  (let [m r] [(|>> t f1 f2 f3)]
    ...)
  "
  [value & fns]
  (loop [accum [], value value, fns fns]
  (if-not (seq fns)
    [accum value]
    (let [fn (first fns)
    [result others] (fn value)]
      (recur (conj accum result) others (rest fns))))))

(defn partition-by-char
  "Partitions the collection of words into a 2 item list. The first one is
  either nil or concatenated string of all words starting with the character
  ch. The second item contains the list of all words not starting with ch.
  The relative order of words in both the resultant lists are maintained
  as in the original collection"
  [ch coll]
  (let [pred #(= ch (first %))
        result (group-by pred coll)
        [r others] [(result true) (result false)]]
    [(if-not (seq r) nil (s/join " " r)) (result false)]))

(def tags (partial partition-by-char \#))

(def mentions (partial partition-by-char \@))

(defn balance [tokens]
  (let [a (last tokens)
        b (last (butlast tokens))
        r (butlast (butlast tokens))
        bal (if (= "bal" a) b (if (= "bal" b) a))]
  [bal (if (nil? bal) tokens r)]))

(defn info [i]
  (let [tokens (s/split i #" ")
        [[tag mention bal] data] (|>> tokens tags mentions balance)]
    {:desc (s/join " " data)
     :acct tag
     :for mention
     :bal (if-not (nil? bal) (Integer. bal))}))

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
    (if-not (seq s)
      entries
      (let [[date' s'] (if (date-entry? (first s))
                         [(text->date (first s)) (rest s)]
                         [date s])]
          (recur date'
                 (rest s')
                 (conj entries (text->full-entry (first s') date' id))
                 (inc id))))))
