(ns swarmpit.api
  (:import java.util.Base64)
  (:require [digest :as d]
            [clojure.string :as str]
            [swarmpit.docker.client :as dc]
            [swarmpit.docker.domain :as dd]
            [swarmpit.registry.client :as rc]
            [swarmpit.couchdb.client :as cc]
            [swarmpit.utils :refer [in?]]))

;;; Database API

(defn create-database
  []
  (cc/put "/swarmpit"))

;;; User API

(defn create-user
  [user]
  (let [passwd (d/digest "sha-256" (:password user))]
    (->> (assoc user :password passwd
                     :type "user"
                     :role "admin")
         (cc/post "/swarmpit"))))

(defn user-by-email
  [email]
  (->> {:selector {:type  {"$eq" "user"}
                   :email {"$eq" email}}}
       (cc/post "/swarmpit/_find")
       :docs
       (first)))

(defn user-by-token
  [token]
  (let [token-value (second (str/split token #" "))
        token-decoded (String. (.decode (Base64/getDecoder) token-value))
        token-credentials (str/split token-decoded #":")
        email (first token-credentials)
        password (second token-credentials)]
    (->> {:selector {:type     {"$eq" "user"}
                     :email    {"$eq" email}
                     :password {"$eq" (d/digest "sha-256" password)}}}
         (cc/post "/swarmpit/_find")
         :docs
         (first))))

;;; Service API

(defn services
  []
  (->> (dc/get "/services")
       (dd/<-services)))

(defn services-name
  []
  (->> (services)
       (map (fn [s] [(:id s) (:serviceName s)]))
       (into (sorted-map))))

(defn service
  [service-id]
  (->> (str "/services/" service-id)
       (dc/get)
       (dd/<-service)))

(defn delete-service
  [service-id]
  (->> (str "/services/" service-id)
       (dc/delete)))

(defn create-service
  [service]
  (->> (dd/->service service)
       (dc/post "/services/create")))

(defn update-service
  [service-id service]
  (->> (dd/->service service)
       (dc/post (str "/services/" service-id "/update?version=" (:version service)))))

;;; Network API

(defn networks
  []
  (->> (dc/get "/networks")
       (dd/<-networks)))

(defn network
  [network-id]
  (->> (str "/networks/" network-id)
       (dc/get)
       (dd/<-network)))

(defn delete-network
  [network-id]
  (->> (str "/networks/" network-id)
       (dc/delete)))

(defn create-network
  [network]
  (->> (dd/->network network)
       (dc/post "/networks/create")))

;;; Node API

(defn nodes
  []
  (->> (dc/get "/nodes")
       (dd/<-nodes)))

(defn node
  [node-id]
  (->> (str "/nodes/" node-id)
       (dc/get)
       (dd/<-node)))

(defn nodes-name
  []
  (->> (nodes)
       (map (fn [n] [(:id n) (:name n)]))
       (into (sorted-map))))

;;; Task API

(defn tasks
  []
  (let [services (services-name)
        nodes (nodes-name)]
    (-> (dc/get "/tasks")
        (dd/<-tasks services nodes))))

(defn task
  [task-id]
  (let [services (services-name)
        nodes (nodes-name)]
    (-> (str "/tasks/" task-id)
        (dc/get)
        (dd/<-task services nodes))))

;;; Registry API

(defn registries
  []
  (->> (rc/headers "25a0e035-d147-4401-a4e8-5792ef0ec8fe" "feflryjn3olmvkdb")
       (rc/get "/_catalog")
       :repositories))

;;; Registry API

(defn registry
  [registry-id]
  ())

(defn create-registry
  [registry]
  ())