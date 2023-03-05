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
  [["/" html-handler]
   ["/echo/:id" 
    {:get (fn [{{:keys [id]} :path-params}]
       (response/ok (str "<p>the value is: " id "</p>")))}]
   ["/api" {:middleware [wrap-formats]}
    ["/multiply"
     {:post (fn [{{:keys [a b]} :body-params}]
              (response/ok {:result (* a b)}))}]]])

(def handler
  (reitit/ring-handler
   (reitit/router routes)
   (reitit/routes
    (reitit/create-resource-handler {:path "/"})
    (reitit/create-default-handler
     {:not-found
      (constantly (response/not-found "404 - Page not found"))
      :method-not-allowed
      (constantly (response/method-not-allowed "405 - Not allowed"))
      :not-acceptable
      (constantly (response/not-acceptable "406 - Not acceptable"))}))))

(defn -main []
  (jetty/run-jetty 
   (-> #'handler 
       wrap-nocache
       wrap-reload/wrap-reload)
   {:port 3000 :join? false}))