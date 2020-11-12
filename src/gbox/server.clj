(ns gbox.server
  (:require
   [clojure.edn :as edn]
   [clojure.java.io :as io]
   [integrant.core :as ig]
   [com.walmartlabs.lacinia.pedestal2 :as p2]
   [com.walmartlabs.lacinia.schema :as schema]
   [com.walmartlabs.lacinia.util :as util]
   [io.pedestal.http :as http]))

(defn start-server [port compiled-schema]
  (-> compiled-schema
      (p2/default-service nil)
      http/create-server
      http/start))

(defn stop-server [service-map]
  (http/stop service-map))

(defmethod ig/init-key :server/pedestal [_ {:keys [port gql] :as opts}]
  (start-server port (:schema gql)))

(defmethod ig/halt-key! :server/pedestal [_ service-map]
  (stop-server service-map))
