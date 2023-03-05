(ns simpleweb.core
  (:require [ring.adapter.jetty :as jetty]
            [ring.middleware.reload :as wrap-reload]
            [ring.util.http-response :as response]
            [muuntaja.middleware :as muuntaja]
            [reitit.ring :as reitit]
            ))
;; Handler
(defn html-handler [request-map] 
  (response/ok
   (str "<html><body> your IP is: " 
        (:remote-addr request-map) 
        "</body></html>")))

(defn json-handler [request]
  (response/ok
   {:result (get-in request [:body-params :id])}))

;; Middleware
(defn wrap-nocache [handler]
  (fn [request]
  (-> request
      handler
      (assoc-in [:headers "Pragma"] "no-cache"))))

(defn wrap-formats [handler]
  (-> handler
      (muuntaja/wrap-format)))

;; Route
(def routes
  [["/" {:get html-handler
         :post html-handler}]])

(def handler
  (reitit/ring-handler(reitit/router routes)))

(defn -main []
  (jetty/run-jetty 
   (-> #'handler 
       wrap-nocache
       wrap-formats
       wrap-reload/wrap-reload)
   {:port 3000 :join? false}))

