(ns carneades.web.modules.project.routes-test
  (:require [midje.sweet :refer :all]
            [ring.mock.request :refer :all]
            [taoensso.timbre :as timbre :refer [debug info spy]]
            [carneades.web.handler :refer [app]]
            [cheshire.core :refer [parse-string encode]]
            [carneades.engine.utils :as utils]
            [carneades.engine.uuid :refer [make-uuid-str]]
            [carneades.project.admin :as project]
            [carneades.database.admin :as db]))

(def base-url "/carneades/api")
(def user "root")
(def password "pw1")

(def state (atom nil))

(defn initial-state-value
  []
  {:project-name (str "testproject-" (make-uuid-str))})

(defn create-project
  []
  (reset! state (initial-state-value))
  (db/create-missing-dbs (:project-name @state) user password))

(defn delete-project
  []
  (utils/delete-file-recursively
   (project/get-project-path (:project-name @state))))

(defn parse
  [s]
  (parse-string s true))

(defn get-rule-value
  [rules sid]
  (:value (first (filter #(= (:ruleid %) sid) rules))))

(defn post-profile
  [project profile]
  (app (-> (request :post
                    (str base-url
                         "/projects/"
                         (:project-name @state)
                         "/legalprofiles/"))
           (body (encode profile))
           (content-type "application/json"))))

(defn get-profile
  [project id]
  (app (-> (request :get
                    (str base-url
                         "/projects/"
                         (:project-name @state)
                         "/legalprofiles/"
                         id))
           (content-type "application/json"))))

(defn put-profile
  [project id update]
  (app (-> (request :put
                    (str base-url
                         "/projects/"
                         project
                         "/legalprofiles/"
                         id))
           (body (encode update))
           (content-type "application/json"))))

(with-state-changes [(before :facts (create-project))
                     (after :facts (delete-project))]
  (fact "It is possible to post a profile and read it back."
        (let [project (:project-name @state)
              profile {:metadata {:title "One profile"}
                       :rules '[{:ruleid r1-a
                                 :value 1.0}
                                {:ruleid r2-b
                                 :value 0.0}
                                {:ruleid r3-c
                                 :value 0.5}]
                       :default true}
              response (post-profile project profile)
              body-content (parse (:body response))
              id (:id body-content)
              response2 (get-profile project id)
              profile' (parse (:body response2))]
         (expect (select-keys (:metadata profile') (keys (:metadata profile))) =>
                 (:metadata profile))
         (expect (:default profile') => true)
         (expect (get-rule-value (:rules profile') "r1-a") => 1.0)
         (expect (get-rule-value (:rules profile') "r2-b") => 0.0)
         (expect (get-rule-value (:rules profile') "r3-c") => 0.5))))

(with-state-changes [(before :facts (create-project))
                     (after :facts (delete-project))]
  (fact "It is possible to update the metadata of a profile."
        (let [project (:project-name @state)
              profile {:metadata {:title "A profile without update"}
                       :rules '[{:ruleid r1-a
                                 :value 1.0}
                                {:ruleid r2-b
                                 :value 0.0}
                                {:ruleid r3-c
                                 :value 0.5}]
                       :default true}
              response (post-profile project profile)
              body-content (parse (:body response))
              id (:id body-content)
              update {:metadata {:title "A profile with update"}}
              response2 (put-profile project id update)
              response3 (get-profile project id)
              profile' (parse (:body response3))]
          (expect (-> profile' :metadata :title) => "A profile with update"))))

(with-state-changes [(before :facts (create-project))
                     (after :facts (delete-project))]
  (fact "It is possible to update the rules of a profile."
        (let [project (:project-name @state)
              profile {:metadata {:title "A profile without update"}
                       :rules '[{:ruleid r1-a
                                 :value 1.0}
                                {:ruleid r2-b
                                 :value 0.0}
                                {:ruleid r3-c
                                 :value 0.5}]
                       :default true}
              response (post-profile project profile)
              body-content (parse (:body response))
              id (:id body-content)
              update '{:rules [{:ruleid "ra"
                                :value 0.0}
                               {:ruleid "rb"
                                :value 0.5}
                               {:ruleid "rc"
                                :value 1.0}]}
              response2 (put-profile project id update)
              response3 (get-profile project id)
              profile' (parse (:body response3))]
          (expect (-> profile' :rules) => (:rules update)))))

(with-state-changes [(before :facts (create-project))
                     (after :facts (delete-project))]
  (fact "It is not possible to directly set the default property of a
  profile to false."
        (let [project (:project-name @state)])))

(with-state-changes [(before :facts (create-project))
                     (after :facts (delete-project))]
  (fact "It is not possible to delete the default profile."
        (let [project (:project-name @state)])))
