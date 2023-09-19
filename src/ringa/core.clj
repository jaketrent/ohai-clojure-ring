(ns ringa.core
  (:require [ring.adapter.jetty :refer [run-jetty]]
            [reitit.ring :as ring]
            [reitit.coercion.spec]
            [muuntaja.core :as m]
            [reitit.ring.middleware.muuntaja :as muuntaja])
  (:gen-class))

(def users (atom {}))

(defn create-user! [{user :body-params :as request}]
  (let [id (str (java.util.UUID/randomUUID))
        _ (println (str "JTBUG user" user))
        _ (println (str "JTBUG req" request))
        _ (println (str "JTBUG body" (:body-params request)))
        users (->> (assoc user :id id)
                   (swap! users assoc id))]
    {:status 200
     :body users}))

(defn get-users [_req]
  {:status 200
   :body @users})

(defn find-user [{{:keys [id]} :path-params}]
  {:status 200
   :body (get @users id)})

(defn err [status title]
  {:status status
   :body {:errors [{:title title}]}})

(def app
  (ring/ring-handler
    (ring/router
      ["/api"
       ["/users/:id" {:get find-user}]
       ["/users" {:get get-users
                  :post create-user!}]]
      {:data {:coercion   reitit.coercion.spec/coercion
              :muuntaja   m/instance
              :middleware [muuntaja/format-middleware]}})
    (ring/create-default-handler
      {:not-found (constantly (err 404 "Not found"))
       :method-not-allowed (constantly (err 405 "Not allowed"))
       :not-acceptable (constantly (err 406 "Not acceptable"))})))

(defn start-app []
  (run-jetty app {:port 3000
                  :join? false}))

(defn -main [& _args]
  (start-app))
