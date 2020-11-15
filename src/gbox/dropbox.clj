(ns gbox.dropbox
  (:require
   [clojure.core.async :as a]
   [integrant.core :as ig]
   [environ.core :refer [env]]
   [org.httpkit.client :as http]
   [cheshire.core :as json]))


;; API Calls

(defn req [{:keys [api-root dropbox-access-token]} cmd body]
  (let [req {:url (str api-root cmd)
             :method :post
             :headers {"Authorization" (str "Bearer " dropbox-access-token)
                       "Content-Type" "application/json"}
             :body (json/encode body)
             :insecure? true}

        {:keys [status headers body error] :as resp} @(http/request req)
        ]
    (if (or error (not= 200 status))
      (println "Failed, exception: " error \newline body)
      (json/decode body keyword))))

(defn ls [dropbox {:keys [path]}]
  (let [rnf (fn [m] (clojure.set/rename-keys m {"folder" :folders
                                                "file" :files}))]
    (->> (req dropbox "files/list_folder" {:path path :recursive false})
         :entries
         (group-by :.tag)
         rnf)))

(defn mkdir [dropbox {:keys [path]}]
  (:metadata (req dropbox "files/create_folder_v2" {:path path})))

(defn get-link [dropbox {:keys [path]}]
  (-> (req dropbox "files/get_temporary_link" {:path path})
      :link))


;; Webhooks

(defn challenge-handler
  [request]
  {:status 200
   :headers {"Content-Type" "text/plain"
             "X-Content-Type-Options" "nosniff"}
   :body (-> request :query-params :challenge)})


(defn webhook-handler [webhook-value]
  (fn [request]
    (reset! webhook-value
            (-> request
                :body
                slurp
                (json/decode keyword)
                (assoc :last-modified (str (new java.util.Date)))))
    {:status 200 :body "OK"}))


;; Integrant

(defmethod ig/init-key :client/dropbox [_ opts]
  (let [webhook-value (atom {:last-modified "Initiated"})]
    (assoc opts
           :dropbox-access-token (env :dropbox-access-token)
           :challenge-handler challenge-handler
           :webhook-handler (webhook-handler webhook-value)
           :webhook-value webhook-value)))

(defmethod ig/halt-key! :client/dropbox [_ dropbox]
  )


