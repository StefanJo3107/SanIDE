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
     (t/is (= 6667 @server-port))))

  (rf-test/run-test-sync
   (re-frame/dispatch [::events/initialize-db])
   (re-frame/dispatch [::events/set-username "clojuretesting42"])
   (let [username (re-frame/subscribe [::subs/username])]
     (t/is (= "clojuretesting42" @username))))

  (rf-test/run-test-sync
   (re-frame/dispatch [::events/initialize-db])
   (re-frame/dispatch [::events/set-channel "#clojure-test"])
   (let [channel (re-frame/subscribe [::subs/channel])]
     (t/is (= "#clojure-test" @channel))))

  (rf-test/run-test-sync
   (re-frame/dispatch [::events/initialize-db])
   (re-frame/dispatch [::events/add-message {:from "test"
                                             :type "PRIVMSG"
                                             :time "12:00"
                                             :msg "Hello"}])
   (let [messages (re-frame/subscribe [::subs/messages])]
     (t/is (= [{:from "test" :type "PRIVMSG" :time "12:00" :msg "Hello"}] @messages))))

  (rf-test/run-test-sync
   (re-frame/dispatch [::events/initialize-db])
   (re-frame/dispatch [::events/add-message {:from "test"
                                             :type "PRIVMSG"
                                             :time "12:00"
                                             :msg "Hello"}])
   (re-frame/dispatch [::events/add-message {:from "test"
                                             :type "PRIVMSG"
                                             :time "12:00"
                                             :msg "Again"}])
   (let [messages (re-frame/subscribe [::subs/messages])]
     (t/is (= [{:from "test" :type "PRIVMSG" :time "12:00" :msg "Hello\nAgain"}] @messages))))

  (rf-test/run-test-sync
   (re-frame/dispatch [::events/initialize-db])
   (re-frame/dispatch [::events/add-message {:from "test"
                                             :type "PRIVMSG"
                                             :time "12:00"
                                             :msg "Hello"}])
   (re-frame/dispatch [::events/add-message {:from "test2"
                                             :type "PRIVMSG"
                                             :time "12:01"
                                             :msg "Hi"}])
   (let [messages (re-frame/subscribe [::subs/messages])]
     (t/is (= [{:from "test" :type "PRIVMSG" :time "12:00" :msg "Hello"}
               {:from "test2" :type "PRIVMSG" :time "12:01" :msg "Hi"}] @messages))))

  (rf-test/run-test-sync
   (re-frame/dispatch [::events/initialize-db])
   (re-frame/dispatch [::events/add-participants [{:name "user1" :color [100 100 100]} {:name "user2" :color [50 50 50]}]])
   (let [participants (re-frame/subscribe [::subs/participants])]
     (t/is (= [{:name "user1" :color [100 100 100]} {:name "user2" :color [50 50 50]}] @participants))))

  (rf-test/run-test-sync
   (re-frame/dispatch [::events/initialize-db])
   (re-frame/dispatch [::events/add-participant {:name "user1" :color [100 100 100]}])
   (let [participants (re-frame/subscribe [::subs/participants])]
     (t/is (= [{:name "user1" :color [100 100 100]}] @participants))))

  (rf-test/run-test-sync
   (re-frame/dispatch [::events/initialize-db])
   (re-frame/dispatch [::events/add-participants [{:name "user1" :color [100 100 100]} {:name "user2" :color [50 50 50]}]])
   (re-frame/dispatch [::events/remove-participant "user2"])
   (let [participants (re-frame/subscribe [::subs/participants])]
     (t/is (= [{:name "user1" :color [100 100 100]}] @participants))))

  (rf-test/run-test-sync
   (re-frame/dispatch [::events/initialize-db])
   (re-frame/dispatch [::events/set-irc-connected true])
   (let [irc-connected (re-frame/subscribe [::subs/irc-connected])]
     (t/is (= true @irc-connected))))

  (rf-test/run-test-sync
   (re-frame/dispatch [::events/initialize-db])
   (re-frame/dispatch [::events/set-irc-loading true])
   (let [irc-loading (re-frame/subscribe [::subs/irc-loading])]
     (t/is (= true @irc-loading)))))

(t/run-tests)