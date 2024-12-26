(ns sanide-frontend.events-test
  (:require [sanide-frontend.events :as events]
            [sanide-frontend.subs :as subs]
            [re-frame.test :as rf-test]
            [re-frame.core :as re-frame]
            [clojure.test :as t]))

(t/deftest event-tests
  (rf-test/run-test-sync
   (re-frame/dispatch [::events/initialize-db])
   (re-frame/dispatch [::events/set-active-item :irc])
   (let [active-item (re-frame/subscribe [::subs/active-item])]
     (t/is (= :irc @active-item))))

  (rf-test/run-test-sync
   (re-frame/dispatch [::events/initialize-db])
   (re-frame/dispatch [::events/set-active-file "/tmp/example"])
   (let [active-file (re-frame/subscribe [::subs/active-file])]
     (t/is (= "/tmp/example" @active-file))))
  
  (rf-test/run-test-sync
   (re-frame/dispatch [::events/initialize-db])
   (re-frame/dispatch [::events/set-active-file "/tmp/example"])
   (let [active-file (re-frame/subscribe [::subs/active-file])]
     (t/is (= "/tmp/example" @active-file))))
  
  (rf-test/run-test-sync
   (re-frame/dispatch [::events/initialize-db])
   (re-frame/dispatch [::events/set-latest-payload "print \"Hello\""])
   (let [latest-payload (re-frame/subscribe [::subs/latest-payload])]
     (t/is (= "print \"Hello\"" @latest-payload))))
  
  (rf-test/run-test-sync
   (re-frame/dispatch [::events/initialize-db])
   (re-frame/dispatch [::events/set-latest-config "mode=auto"])
   (let [latest-config (re-frame/subscribe [::subs/latest-config])]
     (t/is (= "mode=auto" @latest-config))))
  
  (rf-test/run-test-sync
   (re-frame/dispatch [::events/initialize-db])
   (re-frame/dispatch [::events/set-server-address "irc.libera.chat"])
   (let [server-address (re-frame/subscribe [::subs/server-address])]
     (t/is (= "irc.libera.chat" @server-address))))
  
  (rf-test/run-test-sync
   (re-frame/dispatch [::events/initialize-db])
   (re-frame/dispatch [::events/set-server-port 6667])
   (let [server-port (re-frame/subscribe [::subs/server-port])]
     (t/is (= 6667 @server-port)))))

(t/run-tests)