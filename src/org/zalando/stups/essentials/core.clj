; Copyright 2015 Zalando SE
;
; Licensed under the Apache License, Version 2.0 (the "License")
; you may not use this file except in compliance with the License.
; You may obtain a copy of the License at
;
;     http://www.apache.org/licenses/LICENSE-2.0
;
; Unless required by applicable law or agreed to in writing, software
; distributed under the License is distributed on an "AS IS" BASIS,
; WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
; See the License for the specific language governing permissions and
; limitations under the License.

(ns org.zalando.stups.essentials.core
  (:require [com.stuartsierra.component :as component]
            [org.zalando.stups.friboo.zalando-internal.config :as config]
            [org.zalando.stups.friboo.system :as system]
            [org.zalando.stups.friboo.log :as log]
            [org.zalando.stups.friboo.zalando-internal.system.http :as http]
            [org.zalando.stups.friboo.system.mgmt-http :as mgmt-http]
            [org.zalando.stups.friboo.system.db :as db]
            [org.zalando.stups.friboo.system.metrics :as metrics]
            [org.zalando.stups.friboo.zalando-internal.auth :as auth]
            [org.zalando.stups.friboo.zalando-internal.system.audit-log :as audit-log])
  (:gen-class))

(def default-http-config
  {:http-port 8080})

(def default-db-config
  {:db-classname       "org.postgresql.Driver"
   :db-subprotocol     "postgresql"
   :db-subname         "//localhost:5432/essentials"
   :db-user            "postgres"
   :db-password        "postgres"
   :db-init-sql        "SET search_path TO ze_data, public"
   :db-auto-migration? true})

(def default-controller-config
  {:api-allowed-realms "services,employees"})

(defn config-true?
  "Determine if a config element reflects truth in a boolean context."
  [setting]
  (some? (#{"true" "1" true 1} setting)))

(defn load-config [defaults]
  (let [config (config/load-config (merge default-db-config
                                          default-http-config
                                          default-controller-config
                                          defaults)
                                   [:http :db :api :metrics :mgmt-http :auth :audit-log])
        migration-setting (some-> (get-in config [:db :auto-migration])
                                  config-true?)]
    (if (some? migration-setting)
      (assoc-in config [:db :auto-migration?] migration-setting)
      config)))

(defn run
  "Initializes and starts the whole system."
  [default-config]
  true
  (let [config (load-config default-config)
        system (component/map->SystemMap
                 {:http       (component/using
                                (http/make-zalando-http
                                  "api/essentials-api.yaml"
                                  (:http config)
                                  (-> config :global :tokeninfo-url))
                                [:controller :metrics])
                  :controller (component/using {:configuration (:api config)} [:db :auth])
                  :auth       (auth/map->Authorizer {:configuration (:auth config)})
                  :audit-log  (audit-log/map->AuditLog {:configuration (:audit-log config)})
                  :db         (db/map->DB {:configuration (:db config)})
                  :metrics    (metrics/map->Metrics {:configuration (:metrics config)})
                  :mgmt-http  (component/using
                                (mgmt-http/map->MgmtHTTP {:configuration (:mgmt-http config)})
                                [:metrics])})]
    (system/run config system)))

(defn -main
  "The actual main for our uberjar."
  [& args]
  (try
    (run {})
    (catch Exception e
      (log/error e "Could not start system because of %s." (str e))
      (System/exit 1))))
