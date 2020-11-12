(ns gbox.gql
  (:require
   [gbox.dropbox :as dbx]
   [clojure.edn :as edn]
   [clojure.java.io :as io]
   [integrant.core :as ig]
   [com.walmartlabs.lacinia.pedestal2 :as p2]
   [com.walmartlabs.lacinia.schema :as schema]
   [com.walmartlabs.lacinia.util :as util]
   [cheshire.core :as json]
   ))

(defn- dropbox->gbox [entry]
  (-> entry
      (select-keys [:name :path_lower])
      (clojure.set/rename-keys {:path_lower :path})))

(defn resolve-file [dropbox]
  (fn [context args value]
    (map dropbox->gbox (:files (dbx/ls dropbox (:path value))))))

(defn resolve-folder [dropbox]
  (fn [context args value]
    (map dropbox->gbox (:folders (dbx/ls dropbox (:path value))))))

(defn resolve-ls [dropbox]
  (fn [context args value]
    (let [path (if (= "/" (:path args)) {:path ""} args )]
      path)))

(defn gbox-schema [dropbox]
  (-> "gbox-schema.edn"
      slurp
      edn/read-string
      (util/attach-resolvers {:resolve-ls (resolve-ls dropbox)
                              :resolve-file (resolve-file dropbox)
                              :resolve-folder (resolve-folder dropbox)})
      schema/compile))

(defmethod ig/init-key :handler/gql [_ opts]
  {:schema (gbox-schema (:dropbox opts))})

