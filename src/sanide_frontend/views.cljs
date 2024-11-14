(ns sanide-frontend.views
  (:require
   [reagent.core :as r]
   [re-frame.core :as re-frame]
   [sanide-frontend.events :as events]
   [sanide-frontend.subs :as subs]
   [goog.dom :as dom]
   ["@monaco-editor/react$default" :as Editor]))

(defn nav-item [name onclick]
  [:button.navitem {:onClick onclick} [:span.item-char (first name)] [:span.item-rest (rest name)]])

(defn navbar []
  [:nav.navbar>div.container
   [:div.title-container [:img {:src "/images/san_logo_rounded.png"}] [:span.title "SanIDE"]]
   [:div.navitems [nav-item "File"]
    [nav-item "Edit"]
    [nav-item "Help"]]])

(defn tab-backgroud [active]
  (if active "/images/tab-active.png" "images/tab-inactive.png"))

(defn tab-item [name active onclick]
  [:button.tabitem {:onClick onclick} [:img.tabback {:src (tab-backgroud active)}] [:span.tabtext name]])

(defn menu []
  (let [active-item (re-frame/subscribe [::subs/active-item])]
    [:div.menu
     [tab-item "Editor" (= @active-item :editor) #(re-frame/dispatch [::events/set-active-item :editor])]
     [tab-item "IRC Client" (= @active-item :irc) #(re-frame/dispatch [::events/set-active-item :irc])]
     [tab-item "Simulator" (= @active-item :simulator) #(re-frame/dispatch [::events/set-active-item :simulator])]]))

(defn filesystem ^clj []
  (r/with-let [expanded-fs? (r/atom false) expanded-ex? (r/atom false)]
    [:div.filesystem [:ul.filetree
                      [:li.rootfolder [:div.rootfolder-link.treelink {:onClick #(swap! expanded-fs? not)}
                                       [:img {:src (if @expanded-fs? "/images/folder-open.png" "images/folder-closed.png")}] [:span.folder-name "Files"] (if @expanded-fs? [:span.folder-expand "▾"] [:span.folder-expand "▸"])]
                       (if @expanded-fs? [:ul.files
                                          [:li.filelink "payload.san"]
                                          [:li.filelink "config.toml"]] [:span])]
                      [:li.rootfolder [:div.rootfolder-link.treelink {:onClick #(swap! expanded-ex? not)}
                                       [:img {:src "/images/examples-icon.png"}] [:span.folder-name "Examples"] (if @expanded-ex? [:span.folder-expand "▾"] [:span.folder-expand "▸"])]
                       (if @expanded-ex? [:ul.files
                                          [:li.filelink "reverse-shell"]
                                          [:li.filelink "youtube"]
                                          [:li.filelink "paint"]] [:span])]]]))

(defn button [icon text]
  [:button.btn [:img {:src icon}] [:span text]])


(defn output []
  [:div.output "Output:"])

(defn texteditor []
  [:div.text-editor
   [:div.editor-header [:span.filename "reverse-shell/payload.san"]
    [:div.editor-btns
     [button "/images/build-icon.png" "Build"]
     [button "/images/flash-icon.png" "Flash"]
     [button "/images/simulate-icon.png" "Simulate"]]]
   [:div.code [:div.codearea [:> Editor {:height "100%"
                              ;; :defaultLanguage "javascript"
                                         :theme "vs-dark"
                                         :options (clj->js {"minimap" {"enabled" false} "automaticLayout" true})}]]
    [output]]])

(defn editor []
  [:div.editor
   [filesystem]
   [texteditor]])

(defn main-panel []
  (let [name (re-frame/subscribe [::subs/name]) active-item (re-frame/subscribe [::subs/active-item])]
    [:div
     [navbar]
     [menu]
     [editor]]))