(ns ledger.app
  (require [clojure.tools.cli :refer [parse-opts]]
           [clojure.java.io :as io]
           [clojure.string :refer [join]]
           [ledger.core :refer [lines sorted-entries day-str]]
           [ledger.gsheets :refer [upload-data]])
  (:gen-class))

(def required-opts #{:file})

(def cli-opts
  [["-f" "--file FILE" "Input file for parsing"
    :validate [#(.exists (io/as-file %)) "File does not exist"]]
   ["-o" "--out OUT" "Output format"
    :id :out
    :default "csv"
    :validate [#(contains? #{"csv" "gs"} %) "Output format is invalid"]]
   ["-v" "--verbose" "Verbosity level"
    :id :verbosity
    :default 0
    :assoc-fn (fn [m k _] (update-in m [k] inc))]
   ["-h" "--help" "Print this help"
    :default false]])

(defn missing-required? [opts]
  (not-every? opts required-opts))

(defn entry->list [entry]
  (if (contains? entry :error)
     [(or (day-str (:day entry)) "") (:desc entry) "" "" "" "" "" "" ""]
    (let [{:keys [day desc txn amt bal acct]} entry]
      [(day-str day)
       (if (= txn :update) (str desc " atm withdrawal") desc)
       (if (= txn :credit) amt "")
       (if (= txn :debit) amt "")
       (or bal (if (= txn :update) amt ""))
       (if (= acct "#hdfc") amt "")
       (if (= acct "#corp") amt "")
       (if (or (= acct "#citi") (= acct "#cc")) amt "")])))

(def headers
  ["When"
   "What"
   "Incomes"
   "Expenses"
   "Balance"
   "Corp"
   "HDFC"
   "Citi"
   "Tags"])

(defn to-csv [entry]
  (let [l (entry->list entry)
        d (first l)
        r (rest l)]
    (str (day-str d) "," (join "," r))))

(defn print-csv [entries]
  (println (join "," headers))
  (doseq [e entries]
    (println (join "," (entry->list e)))))

(defn upload-to-google-sheets [entries]
  (println "Uploading to google sheets ...")
  (upload-data (into [headers] (map entry->list entries)))
  (println "OK"))

(def usage 
  (str "Usage: "
       "lein run -- "
       "--file <FILE-PATH> "
       "--out gs"))

(defn -main [& args]
  (let [{:keys [options arguments summary errors]} (parse-opts args cli-opts)]
    (when (not (empty? errors))
      (println errors)
      (println usage)
      (System/exit 0))
    (when (or (:help options) (missing-required? options))
      (println summary)
      (println usage)
      (print options)
      (System/exit 0))
    (let [file (:file options)
          lines (lines file)
          entries (sorted-entries lines)]
      (condp = (:out options)
        "csv" (print-csv entries)
        "gs" (upload-to-google-sheets entries)))))
