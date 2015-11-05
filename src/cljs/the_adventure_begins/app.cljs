(ns the-adventure-begins.app
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [reagent.core           :as reagent :refer [atom]]
            [secretary.core         :as secretary :refer-macros  [defroute]]
            [goog.events            :as events]
            [goog.history.EventType :as EventType]
            [markdown.core :refer  [md->html]]
            [cljs-http.client :as http]
            [cljs.core.async :refer [<!]])
  (:import goog.History))

(enable-console-print!)

(def stories (atom []))

(defn- load-stories []
  (go
   (let [response (<! (http/get "http://localhost:3000/stories" { :with-credentials? false }))]
     (prn (:body response))
     (reset! stories (:body response)))))


(defn header-partial
  ([heading-1]
   (header-partial heading-1 nil))
  ([heading-1 heading-2]
   [:div
     [:h1 heading-1]
     (when (some? heading-2)
       [:h2 heading-2])
     [:div.page-divider "•••"]]))

;(defn list-view []
;  [:div.container
;   [:div.row
;    [:div.col.l8.offset-l2
;     [header-partial
;      "The Adventure Begins"
;      "Interactive Adventure Stories"]
;     (doall
;       (for [[slug story] (@session :stories)]
;        ^{ :key slug } [:a.option { :href (str "#/" slug) }
;         (story :title)]))]]])
(defn stories-component []
  (do
    (load-stories)
    (fn []
      [:div.container
       [:div.row
        [:div.col.l8.offset-l2
         [header-partial "The Adventure Begins" "Interactive Adventure Stories"]
         (for [story @stories]
           ^{ :key (:id story) } [:a.option { :href (str "#/" (:id story)) } (story :title)])]]])))

(defn init []
  (reagent/render-component [stories-component] (.getElementById js/document "container")))

; - UTILITIES -----------------------------------------------------------------
;(defn slug [input] (-> input
;                       (clojure.string/lower-case)
;                       (clojure.string/replace #"\ " "-")))
;
;(defn strip-margin [input]
  ;(clojure.string/replace input #"(^\|)|(.+\|)" ""))
;; -----------------------------------------------------------------------------
;
; ;- SESSION STUFF -------------------------------------------------------------
;(def session (atom { :stories { (slug (story :title)) story }}))
;
;(defn set-view [view]
;  (swap! session assoc :view view))
; -----------------------------------------------------------------------------
;
;(defn header-partial
;  ([heading-1]
;   (header-partial heading-1 nil))
;  ([heading-1 heading-2]
;   [:div
;     [:h1 heading-1]
;     (when (some? heading-2)
;       [:h2 heading-2])
;     [:div.page-divider "•••"]]))
;
;(defn markdown-partial [text]
;  [:div.text-container
;   { :dangerouslySetInnerHTML  { :__html (md->html text)} } ])
;
;(defn list-view []
;  [:div.container
;   [:div.row
;    [:div.col.l8.offset-l2
;     [header-partial
;      "The Adventure Begins"
;      "Interactive Adventure Stories"]
;     (doall
;       (for [[slug story] (@session :stories)]
;        ^{ :key slug } [:a.option { :href (str "#/" slug) }
;         (story :title)]))]]])
;
;(defn story-view []
;  [:div.container
;   [:div.row
;    [:div.col.l8.offset-l2
;     (let [story (@session :active-story)]
;       [:div
;        [header-partial
;         (story :title)
;         (str "by " (story :author))]
;        [markdown-partial (story :synopsis)]
;        [:a.option { :href (str "#/" (slug (story :title)) "/" 1) } "Begin" ]])]]])
;
;(defn part-view []
;  [:div.container
;   [:div.row
;    [:div.col.l8.offset-l2
;     (let [part (@session :active-part)]
;       [:div
;         [header-partial (name (part :location))]
;         [markdown-partial (part :text)]
;         (doall (for [option (part :options)]
;          ^{ :key (option :link) } [:a.option
;                                    { :href (str "#/" (slug (get-in @session [ :active-story :title])) "/" (name (option :link))) }
;                                    (option :text)]))])]]])
;
;(defn pages []
;  [:div
;   [:div.grey.darken-3.nav-bar
;    [:a.title { :href "#/" } "The Adventure Begins"]
;    (when (@session :active-part)
;      [:div.actions
;       [:span (get-in @session [:active-story :title]) " by " (get-in @session [:active-story :author]) " | " ]
;       [:a.action { :href (str "#/" (slug (get-in @session [:active-story :title]))) } "Restart Story"]])]
;   [(@session :view)]])
;
;
;(defroute "/" []
;  (swap! session dissoc :active-story :active-part)
;  (set-view list-view))
;
;(defroute "/:story" { :as params }
;  (swap! session assoc :active-story ((@session :stories) (params :story)))
;  (swap! session dissoc :active-part)
;  (set-view story-view))
;
;(defroute "/:story/:part" { :as params }
;  (swap! session assoc :active-story ((@session :stories) (params :story)))
;  (swap! session assoc :active-part (get ((@session :active-story) :parts) (keyword (params :part)) { :location :?
;                                                                                                      :text     (strip-margin
;                                                                                                                  "|# Not Found
;                                                                                                                   |
;                                                                                                                   |## This part is not found") } ))
;  (set-view part-view)
;  (.scrollTo js/window 0 0))
;
;
;(defn init []
;  (secretary/set-config! :prefix "#")
;  (let [h  (History.)]
;    (goog.events/listen h EventType/NAVIGATE #(secretary/dispatch!  (.-token %)))
;      (doto h  (.setEnabled true)))
;  (reagent/render-component [pages]
;                            (.getElementById js/document "container")))
