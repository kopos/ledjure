(ns ledger.gsheets
  (:require [clojure.java.io :as io]
            [clojure.data.json :as json])
  (:require [google-apps-clj [google-sheets-v4 :as gs]]))

(def goog-creds-file "client_secret.json")

(def spreadsheet-file "spreadsheets.json")

(defn- parse-google-credentials [file]
  (let [cs-json (json/read-str (slurp file))
        cs (cs-json "installed")]
    [(cs "client_id") (cs "client_secret")]))

(defn- build-service-from-credentials [file]
  (let [[client-id client-secret] (parse-google-credentials file)]
    (gs/build-service client-id client-secret)))

(defn- get-spreadsheet-info [service file]
  (let [ss-json (json/read-str (slurp file))
        ssid (ss-json "spreadsheet-id")
        sheet-name (ss-json "sheet-name")]
    [ssid (gs/find-sheet-by-title service ssid sheet-name)]))

(defn upload-data [rows]
  (let [service (build-service-from-credentials goog-creds-file)
        [ssid sid] (get-spreadsheet-info service spreadsheet-file)]
    (gs/write-sheet service ssid sid rows)))
