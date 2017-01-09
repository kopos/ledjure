(defproject ledger "0.1.0-SNAPSHOT"
  :description "Clojure app to parse expenses data from files and upload to Google Sheets"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/data.json "0.2.6"]
                 [clj-time "0.12.2"]
                 [google-apps-clj "0.5.3"]
                 [org.clojure/tools.cli "0.3.5"]]
  :main ledger.app)
