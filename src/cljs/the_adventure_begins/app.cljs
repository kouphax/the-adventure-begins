(ns the-adventure-begins.app
  (:require [reagent.core           :as reagent :refer [atom]]
            [secretary.core         :as secretary :refer-macros  [defroute]]
            [goog.events            :as events]
            [goog.history.EventType :as EventType]

            [markdown.core :refer  [md->html]])
  (:import goog.History))

; - UTILITIES -----------------------------------------------------------------
(defn slug [input] (-> input
                       (clojure.string/lower-case)
                       (clojure.string/replace #"\ " "-")))

(defn strip-margin [input]
  (clojure.string/replace input #"(^\|)|(.+\|)" ""))
; -----------------------------------------------------------------------------

; - DATA ----------------------------------------------------------------------
(def story
  { :title    "Your Adventure Ends Here"
    :author   "Chris Parnell"
    :synopsis "It’s 30 years since the first mainstream interactive fiction hit our shelves. Chris Farnell explains why the ‘choose your own adventure’ format is making a comeback in this special article, in which YOU are the hero"
    :parts    { :1 { :location :1
                     :text     "It is a warm, sunny day, and the air is filled with the sound of birdsong and the smell of flowers and animal droppings. A brave adventurer, you have journeyed far from the Kingdom, but after many miles you approach the edge of a forest and the path branches in two directions. To the North the path falls into shadow, the trees look more jagged and menacing, and the skulls of previous adventurers litter the ground.  At the end of this dark road you can see a cackling mad man with unkempt eyebrows. He seems desperate to talk to you about choose your own adventure games. The road to the South also leads into the woods, but along a high road still kissed with sunlight and lined with pretty flowers all the colours of the rainbow. At the end of this road you can see a cute white fluffy bunny rabbit."
                     :options  [{ :text "Go North"
                                  :link :3 }
                                { :text "Go South"
                                  :link :2 }]}
                :2 { :location :2
                     :text     (strip-margin
                                 "|You stroll along the upward path, whistling a jaunty tune and waving to the cute little bunny rabbit. Puzzlingly, you find that walking is becoming harder the further up the path you go. Glancing down, you see your feet have sunk into the path up to your shins. To your astonishment the road has magically transformed into hot magma, and your skin and muscles are being burned away from the bone as you start screaming in agony. Looking up the path ahead you see that the bunny has started grinning at you. It has human teeth.
                                  |
                                  |Your adventure ends here.") }
                :3 { :location :3
                     :text     (strip-margin
                                 "|Yes, we’re here to talk about choose your own adventure games. If you grew up in the 70s, 80s or 90s you’ve probably read one of these on a rainy afternoon. As well as [Choose Your Own Adventure](http://en.wikipedia.org/wiki/Choose_Your_Own_Adventure)’s own brand of books which covered space adventures, spy stories and monster hunts, for a long time you could find gamebooks for any franchise you cared to mention.
                                  |
                                  |I personally enjoyed the Super Mario Bros Adventure Books and a Transformers Dinobots Find Your Fate book where a fairground ride turned out to be a time warp that sent you to aid dinosaur Transformers in prehistoric times. I still think Michael Bay missed a trick there. It’s also a format that includes this, of which the less is said, the better.
                                  |
                                  |One thing all these books had in common was your tendency to die. A lot. If you ever want a glimpse of the infinite myriad of ways a person can shuffle of this mortal coil, spend a while perusing You Chose Wrong, a selection of gruesome gamebook deaths.
                                  |
                                  |You can tell the man with the unkempt eyebrows has much more to say on this subject, but behind him there’s a cool looking tree you’d like to climb.")
                      :options [{ :text "Listen further to the eyebrow man’s fascinating insights into choose your own adventures"
                                  :link :5 }
                                { :text "Climb the tree"
                                  :link :4 }]}}})

; - SESSION STUFF -------------------------------------------------------------
(def session (atom { :stories { (slug (story :title)) story }}))

(defn set-view [view]
  (swap! session assoc :view view))
; -----------------------------------------------------------------------------


(defn list-view []
  [:div.container
   [:div.row
    [:div.col.l8.offset-l2
     [:h1 "The Adventure Begins"]
     [:h2 "Interactive Adventure Stories"]
     [:div.page-divider "•••"]
     (for [[slug story] (@session :stories)]
      [:a.option { :href (str "#/" slug) }
       (story :title)])]]])

(defn story-view []
  [:div.container
   [:div.row
    [:div.col.l8.offset-l2
     (let [story (@session :active-story)]
       [:div
        [:h1 (story :title)]
         [:h2 "by " (story :author)]
         [:div.page-divider "•••"]
         [:p (story :synopsis)]
         [:a.option { :href (str "#/" (slug (story :title)) "/" 1) } "Begin" ]])]]])

(defn part-view []
  [:div.container
   [:div.row
    [:div.col.l8.offset-l2
     (let [part (@session :active-part)]
       [:div
         [:h1 (name (part :location))]
         [:div.page-divider "•••"]
         [:p {:dangerouslySetInnerHTML  { :__html (md->html (part :text))} } ]
         (for [option (part :options)]
          [:a.option { :href (str "#/" (slug (get-in @session [ :active-story :title])) "/" (name (option :link)))
                       :key (option :link) }
                     (option :text)])])]]])

(defroute "/" []
  (set-view list-view))

(defroute "/:story" { :as params }
  (swap! session assoc :active-story ((@session :stories) (params :story)))
  (set-view story-view))

(defroute "/:story/:part" { :as params }
  (swap! session assoc :active-story ((@session :stories) (params :story)))
  (swap! session assoc :active-part (get ((@session :active-story) :parts) (keyword (params :part)) { :location :?
                                                                                                      :text     (strip-margin
                                                                                                                  "|# Not Found
                                                                                                                   |
                                                                                                                   |## This part is not found") } ))
  (set-view part-view))

(defn page [] [(@session :view)])

(defn init []
  (secretary/set-config! :prefix "#")
  (let [h  (History.)]
    (goog.events/listen h EventType/NAVIGATE #(secretary/dispatch!  (.-token %)))
      (doto h  (.setEnabled true)))
  (reagent/render-component [page]
                            (.getElementById js/document "container")))
