(ns ledger.app
  (require [clojure.tools.cli :refer [parse-opts]]
           [clojure.java.io :as io]
           [clojure.string :refer [join]]
           [ledger.core :refer [lines sorted-entries day-str]]
           [ledger.gsheets :refer [upload-data]])
  (:gen-class))

(def required-opts #{:file})

(def gs-required-opts #{:ssid :sname})

(def cli-opts
  [["-f" "--file FILE" "Input file for parsing"
    :validate [#(.exists (io/as-file %)) "File does not exist"]]
   ["-o" "--out OUT" "Output format"
    :id :out
    :default "csv"
    :validate [#(contains? #{"csv" "gs"} %) "Output format is invalid"]]
   ["-s" "--ssid SSID" "Spread Sheet Id (Mandatory if output format is gs)"
    :id :ssid]
   ["-n" "--name NAME" "Worksheet Name (Mandatory if ouput format is gs)"
    :id :sname]
   ["-v" "--verbose" "Verbosity level"
    :id :verbosity
    :default 0
    :assoc-fn (fn [m k _] (update-in m [k] inc))]
   ["-h" "--help" "Print this help"
    :default false]])

(defn missing-required? [opts]
  (not-every? opts required-opts))

(defn missing-gs-required? [opts]
  (if (= "gs" (:out opts))
    (not-every? opts gs-required-opts)
    false))

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

(defn upload-to-google-sheets [entries ssid sname]
  (println "Uploading to google sheets ...")
  (upload-data (into [headers] (map entry->list entries)) ssid sname)
  (println "OK"))

(def usage 
  (str "Usage: "
       "lein run -- "
       "--file <FILE-PATH> "
       "--out [csv|gs] --ssid <SSID> --sname <NAME>"))

(defn -main [& args]
  (let [{:keys [options arguments summary errors]} (parse-opts args cli-opts)]
    (when (not (empty? errors))
      (println errors)
      (println usage)
      (System/exit 0))
    (when (or (:help options) (missing-required? options) (missing-gs-required? options))
      (println summary)
      (println usage)
      (print options)
      (System/exit 0))
    (let [file (:file options)
          lines (lines file)
          entries (sorted-entries lines)]
      (condp = (:out options)
        "csv" (print-csv entries)
        "gs" (upload-to-google-sheets entries (:ssid options) (:sname options))))))
