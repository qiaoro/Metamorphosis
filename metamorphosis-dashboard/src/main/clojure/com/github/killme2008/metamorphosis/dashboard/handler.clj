(ns com.github.killme2008.metamorphosis.dashboard.handler
  (:use compojure.core)
  (:use [ring.velocity.core :only [render]]
        [environ.core])
  (:require [compojure.handler :as handler]
            [com.github.killme2008.metamorphosis.dashboard.util :as u]
            [compojure.route :as route]))

(defonce broker-ref (atom nil))

(defmacro with-broker [ & body]
  `(-> @broker-ref ~@body))

(defn- render-tpl [tpl & vs]
  (apply render (str "templates/" tpl) vs))

(defn-  index [req]
  (render-tpl "index.vm" :topics (with-broker (.getStoreManager) (.getAllTopics))))

(defn- instance []
  {:start (u/pretty-time (with-broker (.getStatsManager) (.getStartupTimestamp)))
   :host (with-broker (.getBrokerZooKeeper) (.getBrokerHostName))
   :data (with-broker (.getMetaConfig) (.getDataPath))
   :data_log (with-broker (.getMetaConfig) (.getDataLogPath))
   :cwd (System/getProperty "user.dir")})

(defn- version []
  {:metaq (with-broker (.getStatsManager) (.getVersion))})

(defn- jvm []
  {:runtime (System/getProperty "java.vm.name")
   :Processors (-> (Runtime/getRuntime) (.availableProcessors))
   :args (u/vm-args)})

(defn- system []
  {:sys_memory (u/sys-memory)
   :sys_memory_used (u/sys-memory-used)
   :swap_space (u/swap-space)
   :ss_used  (u/swap-space-used)
   :fdc (u/file-descriptors)
   :fdc_used (u/file-descriptors-used)
   :jvm_memory_max (-> (Runtime/getRuntime) (.maxMemory))
   :jvm_memory_total (-> (Runtime/getRuntime) (.totalMemory))
   :jvm_memory_used (- (-> (Runtime/getRuntime) (.totalMemory)) (-> (Runtime/getRuntime) (.freeMemory)))})

(defn- dashboard [req]
  (render-tpl "dashboard.vm"
              :instance (u/stringfy-map-keys (instance))
              :version (u/stringfy-map-keys (version)) 
              :jvm (u/stringfy-map-keys (jvm))
              :system (u/stringfy-map-keys (system))))

(defn- logging [req]
  )

(defn- java-properties [req]
  )

(defn- thread-dump [req]
  )

(defn- config [req]
  )
(defn- topic-list [req]
  )

(defroutes app-routes
  (GET "/" [] index)
  (GET "/dashboard" [] dashboard)
  (GET "/logging" [] logging)
  (GET "/java-properties" [] java-properties)
  (GET "/config" [] config)
  (GET "/topic-list" [] topic-list)
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (handler/site app-routes))