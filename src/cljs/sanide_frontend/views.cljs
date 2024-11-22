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

(defn filesystem ^clj [show-project project active-file]
  (r/with-let [expanded-fs? (r/atom false) expanded-ex? (r/atom false)]
    [:div.filesystem [:ul.filetree
                      (when (= show-project true) [:li.rootfolder [:div.rootfolder-link.treelink {:onClick #(swap! expanded-fs? not)}
                                                                   [:img {:src (if @expanded-fs? "/images/folder-open.png" "images/folder-closed.png")}] [:span.folder-name "Files"] (if @expanded-fs? [:span.folder-expand "▾"] [:span.folder-expand "▸"])]
                                                   (if @expanded-fs? [:ul.files
                                                                      [(if (= active-file (:payload_name project))
                                                                         :li.filelink.active
                                                                         :li.filelink)
                                                                       {:onClick #(re-frame/dispatch [::events/set-active-file (:payload_name project)])}
                                                                       (if (= active-file (:payload_name project))
                                                                         (str "○ " (:payload_name project))
                                                                         (:payload_name project))]
                                                                      [(if (= active-file "config.toml")
                                                                         :li.filelink.active
                                                                         :li.filelink)
                                                                       {:onClick #(re-frame/dispatch [::events/set-active-file "config.toml"])}
                                                                       (if (= active-file "config.toml")
                                                                         "○ config.toml"
                                                                         "config.toml")]] [:span])])
                      [:li.rootfolder [:div.rootfolder-link.treelink {:onClick #(swap! expanded-ex? not)}
                                       [:img {:src "/images/examples-icon.png"}] [:span.folder-name "Examples"] (if @expanded-ex? [:span.folder-expand "▾"] [:span.folder-expand "▸"])]
                       (if @expanded-ex? [:ul.files
                                          [:li.filelink "reverse-shell"]
                                          [:li.filelink "youtube"]
                                          [:li.filelink "paint"]] [:span])]]]))

(defn button [icon text onclick]
  [:button.btn {:onClick onclick} [:img {:src icon}] [:span text]])


(defn output []
  [:div.output "Output:" [:div.output-terminal]])

(defn text-editor [project active-file]
  [:div.text-editor
   [:div.editor-header [:span.filename (str (:project_path project) "/"
                                            (if (= active-file (:payload_name project))
                                              (:payload_name project)
                                              "config.toml"))]
    [:div.editor-btns
     [button "/images/build-icon.png" "Build" #()]
     [button "/images/flash-icon.png" "Flash" #()]
     [button "/images/simulate-icon.png" "Simulate" #()]]]
   [:div.code [:div.codearea [:> Editor {:height "100%"
                              ;; :defaultLanguage "javascript"
                                         :theme "vs-dark"
                                         :value (if (= active-file (:payload_name project))
                                                  (:payload_content project)
                                                  (:config_content project))
                                         :onChange #(print %1)
                                         :options (clj->js {"minimap" {"enabled" false} "automaticLayout" true})}]]
    [output]]])

(defn empty-area []
  [:div.empty-area
   [:div.welcome-text
    [:pre.ascii-art
     "_____/\\\\\\\\\\\\\\\\\\\\\\_________________________________/\\\\\\\\\\\\\\\\\\\\\\__/\\\\\\\\\\\\\\\\\\\\\\\\_____/\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\_        
 ___/\\\\\\/////////\\\\\\______________________________\\/////\\\\\\///__\\/\\\\\\////////\\\\\\__\\/\\\\\\///////////__       
  __\\//\\\\\\______\\///___________________________________\\/\\\\\\_____\\/\\\\\\______\\//\\\\\\_\\/\\\\\\_____________      
   ___\\////\\\\\\__________/\\\\\\\\\\\\\\\\\\_____/\\\\/\\\\\\\\\\\\_______\\/\\\\\\_____\\/\\\\\\_______\\/\\\\\\_\\/\\\\\\\\\\\\\\\\\\\\\\_____     
    ______\\////\\\\\\______\\////////\\\\\\___\\/\\\\\\////\\\\\\______\\/\\\\\\_____\\/\\\\\\_______\\/\\\\\\_\\/\\\\\\///////______    
     _________\\////\\\\\\_____/\\\\\\\\\\\\\\\\\\\\__\\/\\\\\\__\\//\\\\\\_____\\/\\\\\\_____\\/\\\\\\_______\\/\\\\\\_\\/\\\\\\_____________   
      __/\\\\\\______\\//\\\\\\___/\\\\\\/////\\\\\\__\\/\\\\\\___\\/\\\\\\_____\\/\\\\\\_____\\/\\\\\\_______/\\\\\\__\\/\\\\\\_____________  
       _\\///\\\\\\\\\\\\\\\\\\\\\\/___\\//\\\\\\\\\\\\\\\\/\\\\_\\/\\\\\\___\\/\\\\\\__/\\\\\\\\\\\\\\\\\\\\\\_\\/\\\\\\\\\\\\\\\\\\\\\\\\/___\\/\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\_ 
        ___\\///////////______\\////////\\//__\\///____\\///__\\///////////__\\////////////_____\\///////////////__"]
    [:div.info-text "Welcome to the IDE for SanScript and SanUSB"]
    [:div.info-text "To get started, create new project or open an existing one"]]
   [:div.project-btns
    [button "/images/build-icon.png" "New" #(re-frame/dispatch [::events/get-new-project "novi"])]
    [button "/images/flash-icon.png" "Open" #(re-frame/dispatch [::events/open-dialog])]]])

(defn editor []
  (let [show-project (re-frame/subscribe [::subs/show-project])
        project (re-frame/subscribe [::subs/project])
        active-file (re-frame/subscribe [::subs/active-file])]
    (if (= true @show-project)
      [:div.editor
       [filesystem @show-project @project @active-file]
       [text-editor @project @active-file]]
      [:div.editor
       [filesystem @show-project]
       [empty-area]])))

(defn main-panel []
  (let [name (re-frame/subscribe [::subs/name]) active-item (re-frame/subscribe [::subs/active-item])]
    [:div
     [navbar]
     [menu]
     [editor]]))