(ns sanide-frontend.views
  (:require
   [re-frame.core :as re-frame]
   [sanide-frontend.subs :as subs]))


(defn navlink [name href]
  [:a.navlink {:href href} name])

(defn navbar []
  [:nav.navbar>div.container
   [:div.title-container [:img {:src "src"}] [:span.title "SanIDE"]]
   [:div.navlinks [navlink "Editor" "/editor"]
    [navlink "IRC Client" "/irc"]
    [navlink "Simulator" "/simulator"]]])

(defn main-panel []
  (let [name (re-frame/subscribe [::subs/name])]
    [:div
     [navbar]]))
