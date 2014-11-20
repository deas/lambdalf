(ns alfresco.transact_itest
  (:use [midje.sweet]
        ;; [clojure.test]
        ;; [clojure.tools.nrepl]
        [alfresco.itest])
  (:require [alfresco.auth :as a]
            [alfresco.nodes :as n]
            [alfresco.transact :as t]
            ;; [clj-http.client :as http]
            )
  )

;; Fixtures do not kick in here
;; (defn init-fixture [f]
;;   (ensure-context)
;;   (let [c (client (connect :port 7888) 1000)]
;;     (message c {:op :eval :code (code (require '[alfresco.auth :as a]))})
;;     (message c {:op :eval :code (code (require '[alfresco.nodes :as n]))})
;;     (message c {:op :eval :code (code (require '[alfresco.transact :as t]))})
;;     (f))
;;
;; (use-fixtures :once init-fixture)

(background (before :facts (ensure-context)))

;; Old school
;; (deftest transact-itests
;;  (testing "Adding up as admin in tx failed."
;;    (is (= 2 (t/in-ro-tx-as (a/admin) (+ 1 1))))))

;; (deftest can-connect
;;  (let [response (call-wscript "/index")]
;;    (is (= 200 (:status response)))))


;;(defftest transact-ftests
;;  (do
;;    (are [result f expr] (= result (f (repl-eval client (code expr))))
;;         ;; Run a simple clojure expression within an Alfresco txn
;;         2 repl-value (t/in-ro-tx-as (a/admin) (+ 1 1))
;;
;;         ;; Get the name of Company Home within an Alfresco txn
;;         "Company Home" repl-value (t/in-ro-tx-as (a/admin)
;;                                    (n/property (n/company-home)
;;                                                :cm/name))
;;         ;; Grab the first Share site and validate that it is indeed a Share site
;;         true repl-value (t/in-ro-tx-as (a/admin) (n/site? (first (n/children (n/sites-home)))))
;;         )))

(facts "About transactions" :it
  (fact "Adding up as admin in tx" :it
    (t/in-ro-tx-as (a/admin) (+ 1 1)) => 2
    ;; (is (= 2 (t/in-ro-tx-as (a/admin) (+ 1 1)))) => 2)
    )
  (fact "Obtaining company home as admin in tx works" :it
    (t/in-ro-tx-as (a/admin)
                   (n/property (n/company-home)
                               :cm/name)) => "Company Home"
                               ;; (first-element [] :default) => :default
                               )
  (fact "Obtaining first child in sites-of as admin in tx works" :it
    (t/in-ro-tx-as
     (a/admin)
     ;; Could be surf-config folder n/site?
     (n/dir? (first (n/children (n/sites-home))))) => true))
