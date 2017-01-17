(ns ledger.gsheets
  (:require [clojure.java.io :as io]
            [clojure.data.json :as json])
  (:require [google-apps-clj [google-sheets-v4 :as gs]]))

(def goog-creds-file "client_secret.json")

(defn- parse-google-credentials [file]
  (let [cs-json (json/read-str (slurp file))
        cs (cs-json "installed")]
    [(cs "client_id") (cs "client_secret")]))

(defn- build-service-from-credentials [file]
  (let [[client-id client-secret] (parse-google-credentials file)]
    (gs/build-service client-id client-secret)))

(defn- get-spreadsheet-info [service ssid sheet-name]
  [ssid (gs/find-sheet-by-title service ssid sheet-name)])

(defn upload-data [rows ssid sheet-name]
  (let [service (build-service-from-credentials goog-creds-file)
        [ssid sid] (get-spreadsheet-info service ssid sheet-name)]
    (gs/write-sheet service ssid sid rows)))
