(ns gbox.gql
  (:require
   [gbox.dropbox :as dbx]
   [clojure.edn :as edn]
   [integrant.core :as ig]
   [com.walmartlabs.lacinia.pedestal2 :as p2]
   [com.walmartlabs.lacinia.schema :as schema]
   [com.walmartlabs.lacinia.util :as util]
   ))

(defn- dropbox->gbox [entry]
  (-> entry
      (select-keys [:name :path_lower])
      (clojure.set/rename-keys {:path_lower :path})))

(defn- clean-path [{:keys [path] :as args}]
  (let [l (count path)
        f (first path)
        p (cond (and (= l 1) (= \/ f)) ""
                (and (> l 1) (not= \/ f)) (str "/" path)
                :all-good path)]
    (assoc args :path p)))

(defn resolve-file [dropbox]
  (fn [context args value]
    (->> (:files (dbx/ls dropbox value))
         (map dropbox->gbox)
         (map (fn [f] (assoc f :link (dbx/get-link dropbox f)))))))

(defn resolve-folder [dropbox]
  (fn [context args value]
    (map dropbox->gbox (:folders (dbx/ls dropbox value)))))

(defn resolve-ls [dropbox]
  (fn [context args value]
    (clean-path args)))

(defn resolve-mkdir [dropbox]
  (fn [context args value]
    (dropbox->gbox (dbx/mkdir dropbox (clean-path args)))))

;; NOTE This is polling from some reason while the graphiql subscription is running
;; Even when stream-callback is not called.
;; As if it's happening in graphiql.... it is.... 
;; Maybe thats how WS work???
;; So I moved to an atom from channels
(defn stream-last-modified [dropbox]
  (fn [context args stream-callback]
    (stream-callback (:last-modified @(:webhook-value dropbox)))
    (fn cleanup [])))

(defn gbox-schema [dropbox]
  (-> "gbox-schema.edn"
      slurp
      edn/read-string
      (util/attach-resolvers {:resolve-ls (resolve-ls dropbox)
                              :resolve-file (resolve-file dropbox)
                              :resolve-folder (resolve-folder dropbox)
                              :resolve-mkdir (resolve-mkdir dropbox)})
      (util/attach-streamers {:stream-last-modified (stream-last-modified dropbox)})
      schema/compile))

(defmethod ig/init-key :handler/gql [_ opts]
  {:schema (gbox-schema (:dropbox opts))})
