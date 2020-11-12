(ns gbox.system
  (:require
   [gbox.gql]
   [gbox.server]
   [gbox.dropbox]
   [integrant.core :as ig]
   [integrant.repl :refer [clear go halt prep init reset reset-all]]
   ))

(defn config []
  (ig/read-string (slurp "config.edn")))

(integrant.repl/set-prep! config)
