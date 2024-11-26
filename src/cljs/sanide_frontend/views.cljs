(ns sanide-frontend.views
  (:require
   [reagent.core :as r]
   [re-frame.core :as re-frame]
   [sanide-frontend.events :as events]
   [sanide-frontend.subs :as subs]
   ["@monaco-editor/react$default" :as Editor]
   [re-pressed.core :as rp]))

(defn show-new-project-dialog []
  (let [dialog (js/document.querySelector ".modal")]
    (-> dialog .showModal)))

(defn close-new-project-dialog []
  (let [dialog (js/document.querySelector ".modal")]
    (-> dialog .close)))

(defn nav-menu [actions expanded?]
  (into [:ul.nav-menu] (map #(vector :li.nav-action {:onClick (fn []  ((:action %) (swap! expanded? not)))} (:name %)) actions)))

(defn nav-item [name actions]
  (r/with-let [expanded-menu? (r/atom false)]
    [:div.navitem-container
     [:button.navitem {:onClick #(swap! expanded-menu? not)} [:span.item-char (first name)] [:span.item-rest (rest name)]]
     (when @expanded-menu? [nav-menu actions expanded-menu?])]))

(defn navbar []
  (let [project (re-frame/subscribe [::subs/project]) active-file (re-frame/subscribe [::subs/active-file])
        latest-payload (re-frame/subscribe [::subs/latest-payload]) latest-config (re-frame/subscribe [::subs/latest-config])]
    [:nav.navbar>div.container
     [:div.title-container [:img {:src "/images/san_logo_rounded.png"}] [:span.title "SanIDE"]]
     [:div.navitems [nav-item "File" [{:name "New project" :action show-new-project-dialog}
                                      {:name "Open project" :action #(re-frame/dispatch [::events/open-dialog])}
                                      {:name "Save project" :action #(re-frame/dispatch [::events/save-file
                                                                                         {:file_path (str (:project_path @project) "/" @active-file)
                                                                                          :content (if (= @active-file (:payload_name @project))
                                                                                                     @latest-payload @latest-config)}])}
                                      {:name "Close project" :action #(re-frame/dispatch [::events/close-project])}]]
      [nav-item "Edit"]
      [nav-item "Help"]]]))

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
  (r/with-let [expanded-fs? (r/atom false) expanded-ex? (r/atom false)
               latest-payload (re-frame/subscribe [::subs/latest-payload])
               latest-config (re-frame/subscribe [::subs/latest-config])
               examples (re-frame/subscribe [::subs/examples])]
    [:div.filesystem [:ul.filetree
                      (when (= show-project true) [:li.rootfolder [:div.rootfolder-link.treelink {:onClick #(swap! expanded-fs? not)}
                                                                   [:img {:src (if @expanded-fs? "/images/folder-open.png" "images/folder-closed.png")}]
                                                                   [:span.folder-name "Files"]
                                                                   (if @expanded-fs? [:span.folder-expand "▾"] [:span.folder-expand "▸"])]
                                                   (when @expanded-fs? [:ul.files
                                                                        [(if (= active-file (:payload_name project))
                                                                           :li.filelink.active
                                                                           :li.filelink)
                                                                         {:onClick #(re-frame/dispatch [::events/set-active-file (:payload_name project)])}
                                                                         (if (= active-file (:payload_name project))
                                                                           (str (if (= @latest-payload (:payload_content project))
                                                                                  "○ " "● ") (:payload_name project))
                                                                           (:payload_name project))]
                                                                        [(if (= active-file "config.toml")
                                                                           :li.filelink.active
                                                                           :li.filelink)
                                                                         {:onClick #(re-frame/dispatch [::events/set-active-file "config.toml"])}
                                                                         (if (= active-file "config.toml")
                                                                           (str (if (= @latest-config (:config_content project)) "○ " "● ") "config.toml")
                                                                           "config.toml")]])])
                      [:li.rootfolder [:div.rootfolder-link.treelink {:onClick #(swap! expanded-ex? not)}
                                       [:img {:src "/images/examples-icon.png"}] [:span.folder-name "Examples"]
                                       (if @expanded-ex? [:span.folder-expand "▾"] [:span.folder-expand "▸"])]
                       (when @expanded-ex? (into [:ul.files] (map
                                                              #(vector :li.filelink
                                                                       {:onClick (fn [] (re-frame/dispatch [::events/open-example %]))} %)
                                                              @examples)))]]]))

(defn button
  ([icon text onclick]
   [:button.btn {:onClick onclick} [:img {:src icon}] [:span text]])
  ([text onclick] [:button.btn.small {:onClick onclick} [:span text]]))

(defn output []
  [:div.output "Output:" [:div.output-terminal]])

(defn text-editor [project latest-payload latest-config active-file]
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
                                                  latest-payload
                                                  latest-config)
                                         :path (str (:project_path project) "/" active-file)
                                         :onChange #(if (= active-file  (:payload_name project))
                                                      (re-frame/dispatch [::events/set-latest-payload %1])
                                                      (re-frame/dispatch [::events/set-latest-config %1]))
                                         :options (clj->js {"minimap" {"enabled" false} "automaticLayout" true})}]]
    [output]]])

(defn modal [children]
  [:dialog.modal
   children])

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
    [button "/images/build-icon.png" "New" show-new-project-dialog]
    [button "/images/flash-icon.png" "Open" #(re-frame/dispatch [::events/open-dialog])]]])

(defn editor []
  (let [show-project (re-frame/subscribe [::subs/show-project])
        project (re-frame/subscribe [::subs/project])
        latest-payload (re-frame/subscribe [::subs/latest-payload])
        latest-config (re-frame/subscribe [::subs/latest-config])
        active-file (re-frame/subscribe [::subs/active-file])]
    (if (= true @show-project)
      [:div.editor
       [filesystem @show-project @project @active-file]
       [text-editor @project @latest-payload @latest-config @active-file]]
      [:div.editor
       [filesystem @show-project]
       [empty-area]])))

(defn main-panel []
  (let [project (re-frame/subscribe [::subs/project]) active-file (re-frame/subscribe [::subs/active-file])
        latest-payload (re-frame/subscribe [::subs/latest-payload]) latest-config (re-frame/subscribe [::subs/latest-config])]
    (re-frame/dispatch
     [::rp/set-keydown-rules
      {:event-keys [[[::events/save-file {:file_path (str (:project_path @project) "/" @active-file)
                                          :content (if (= @active-file (:payload_name @project)) @latest-payload @latest-config)}]
                         ;; enter
                     [{:keyCode 83
                       :ctrlKey true}]]]

       :prevent-default-keys [;; ctrl+s
                              {:keyCode 83
                               :ctrlKey true}]}])
    [:div
     [navbar]
     [menu]
     [editor]
     (r/with-let [project-name (r/atom "")]
       [modal [:div.new-project-form
               [button "❌" close-new-project-dialog]
               [:h3.new-project-title "New project"]
               [:div.name-field [:label.input-label {:htmlFor "project-name-input"} "Project name"]
                [:input#project-name-input.text-input {:type "text"
                                                       :value @project-name
                                                       :on-change #(reset! project-name (-> % .-target .-value))}]]
               [button "/images/build-icon.png" "Create" #(re-frame/dispatch [::events/get-new-project @project-name])]]])]))