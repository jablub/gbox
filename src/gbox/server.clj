(ns gbox.server
  (:require
   [gbox.dropbox :as dbx]
   [clojure.edn :as edn]
   [clojure.java.io :as io]
   [integrant.core :as ig]
   [integrant.repl.state :as sys]
   [com.walmartlabs.lacinia.pedestal2 :as p2]
   [com.walmartlabs.lacinia.schema :as schema]
   [com.walmartlabs.lacinia.util :as util]
   [io.pedestal.http :as http]
   [cheshire.core :as json]
   [cheshire.generate :as enc]))

(enc/add-encoder java.lang.Object
                  (fn [obj jsonGenerator]
                    (.writeString jsonGenerator (str obj))))

(defn add-route [service-map route]
  (assoc service-map :io.pedestal.http/routes
         (-> service-map
             :io.pedestal.http/routes
             (conj route))))

(defn add-system-routes [service-map]
  (add-route service-map ["/system"
                          :get
                          (fn [req] {:status 200
                                     :headers {"Content-Type" "application/json"}
                                     :body (json/encode sys/system)})
                          :route-name :system]))

(defn add-wehook-routes [service-map {:keys [challenge-handler webhook-handler]}]
  (add-route service-map ["/webhook" :get challenge-handler :route-name :webhook-challenge])
  (add-route service-map ["/webhook" :post webhook-handler :route-name :webhook-notification]))

(defn start-server [port {:keys [schema]} dropbox]
  (-> schema
      (p2/default-service nil)
      (add-wehook-routes dropbox)
      add-system-routes
      http/create-server
      http/start))

(defn stop-server [service-map]
  (http/stop service-map))

(defmethod ig/init-key :server/pedestal [_ {:keys [port gql dropbox] :as opts}]
  (start-server port gql dropbox))

(defmethod ig/halt-key! :server/pedestal [_ service-map]
  (stop-server service-map))
