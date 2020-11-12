(ns gbox.dropbox
  (:require
   [integrant.core :as ig]
   [environ.core :refer [env]]
   [org.httpkit.client :as http]
   [cheshire.core :as json]))

(defn req [{:keys [api-root dropbox-access-token]} cmd body]
  (let [req {:url (str api-root cmd)
             :method :post
             :headers {"Authorization" (str "Bearer " dropbox-access-token)
                       "Content-Type" "application/json"}
             :body (json/encode body)
             :insecure? true}

        {:keys [status headers body error] :as resp} @(http/request req)
        ]
    (if error
      (println "Failed, exception: " error)
      (json/decode body keyword))))

(defn ls [dropbox path]
  (let [rnf (fn [m] (clojure.set/rename-keys m {"folder" :folders
                                                "file" :files}))]
    (->> (req dropbox "files/list_folder" {:path path :recursive false})
         :entries
         (group-by :.tag)
         rnf)))

(defmethod ig/init-key :client/dropbox [_ opts]
  (assoc opts :dropbox-access-token (env :dropbox-access-token)))


