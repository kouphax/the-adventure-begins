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
    :synopsis "It’s 30 years since the first mainstream interactive fiction hit our shelves. Chris Farnell explains why the ‘choose your own adventure’ format is making a comeback in this special article, in which __you__ are the hero"
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
                                  :link :4 }]}
                :4 { :location :4
                     :text     (strip-margin
                                 "|You climb the tree, hopping with ease from branch to branch. As you get to the top of the tree a twig snaps under your weight, leaving you dangling from a great height by one hand. As you flail around, trying to find a new handhold, a friendly squirrel hops out onto the branch and scurries along to your fingers. From some magical, hidden compartment it pulls out a teeny tiny chainsaw, which it revs up and proceeds to use to chop off each of your fingers one by one. You plummet to your death. As your face collides with the ground below, you hear that the man with the eyebrows hasn’t stopped talking.
                                  |
                                  |Your adventure ends here.")}
                :5 { :location :5
                     :text     (strip-margin
                                 "|The biggest name in choose your own adventure books, however, isn’t Choose Your Own Adventure. If you’ve got nostalgic memories about these books then there’s a good chance it’s thanks to two people: Ian Livingstone and Steve Jackson, the brains behind the Fighting Fantasy series.
                                  |
                                  |More than branching storylines, these books were closer to a game of Dungeons and Dragons for kids without any friends. In titles like Deathtrap Dungeon and The Forest of Doom you had to keep track of your inventory and statistics by writing them in pencil in the back of the book (always in pencil, so you could change it with a rubber later). Combat was resolved with a series of dice rolls.
                                  |
                                  |The man with the terrible eyebrows is beginning to bore you. These Fighting Fantasy books seem to involve an awful lot of arithmetic, and pencils. You are a modern person and care only about Instagram and Nintendo Wii, you have no time for this.
                                  |
                                  |Apparently you’ve been walking alongside the man deep into the woods while he has been talking. Conveniently the path is branching two ways now. It’s clear the boring eyebrow man intends to walk East. The path to the North has a sign next to it saying “Sex Pie This Way”. You do not know what sex pie is, but you do like pie, and sex.")
                     :options  [{ :text "Go North, and discover what a Sex Pie is"
                                  :link :6 }
                                { :text "Go East, and ask the man with the eyebrows why he should care about these old books in an age of Android phones and Justin Bieber"
                                  :link :8 }]}
                :6 { :location :6
                     :text     (strip-margin
                                 "|It turns out a sex pie is a bad thing. A really bad thing.
                                  |
                                  |Your adventure ends here.") }
                :7 { :location :7
                     :text     (strip-margin
                                 "|There isn’t actually a way to get to this page, so you must be cheating. The guilt causes your eyes to melt out of your skull.
                                  |
                                  |Your adventure ends here.")}
                :8 { :location :8
                     :text     (strip-margin
                                 "|This year marks 30 years since the first Fighting Fantasy book, The Warlock of Firetop Mountain, was released, and to celebrate the books are having something of a relaunch. A brand new contract has been signed with Australian company Tin Man Games, who’ll be rereleasing a selection of the old titles as apps for iPhone and Android. Pencils and dice will be replaced by functions built into the app, while other features such as a soundtrack will also be added to the game. (Tin Man has also been hard at work building its own range of gamebooks, including one called Vampire Boyfriend.)
                                  |
                                  |Meanwhile, Ian Livingstone has brought out a brand new Fighting Fantasy book, Blood of the Zombies, which promises, alongside the zombies, a streamlined gameplay system and the inclusion of pop culture references and in-jokes.")
                     :options  [{ :text "Say “Big whoop! They’re bringing back yet another old thing nerdy 20-something’s love. What’s the point apart from milking nostalgia for the last few coins left in it? ” "
                                  :link :11 }
                                { :text "Say “Wow, that’s really great news. Why do you think the gamebook format is enjoying such a big resurgence now?”"
                                  :link :9 }]}
                :9 { :location :9
                     :text     (strip-margin
                                 "|These books are worth revisiting because Fighting Fantasy books are pretty much entirely responsible for the world we live in now. Steve Jackson and Ian Livingstone’s CVs read like a list of the things you were doing in high school instead of dating. Before they even started Fighting Fantasy they’d already founded Games Workshop and begun distributing Dungeons & Dragons and TSR products.
                                  |
                                  |After Fighting Fantasy they moved into videogames. Ian Livingstone ended up on the board of Eidos, the company that brought us Lara Croft. Steve Jackson went on to work with Peter Molyneux and found Lionhead Games.
                                  |
                                  |The company’s most successful games to date have been the Black & White and Fable series. Both games hinge heavily on a game mechanic where your character’s moral decisions affect their appearance and the way other characters respond to them.
                                  |
                                  |And the influence of these books is felt further than that. If you’re one of the many people who have lost weeks of their lives to playing Mass Effect or Skyrim, you know a huge part of the game is the navigating of dialogue trees in a way that wouldn’t be unfamiliar to someone who’s played a lot of Fighting Fantasy. Much of the architecture and mechanics that make us think of games as stories originated in Fighting Fantasy books and those like it.
                                  |
                                  |I think the reason that choose your own adventures are seeing such a resurgence in popularity right now is that they mirror the way we read. For example, how many tabs do you have open right now? Five? Six? How many of those tabs are things you opened up from links in this article? How many are from Wikipedia? Or, God forbid, TV Tropes? I’m not saying we don’t enjoy straight forward prose anymore, but more and more often when we’re reading something what we do is less going from the beginning to the end of a piece of text, and is more a matter of exploring it, looking for the bits that interest us.
                                  |
                                  |The choose your own adventure format maps onto this with absurd ease. The Internet is full of great examples of people who’ve tried it, from Cracked.com’s Choose Your Own Drug-Fuelled Misadventure series, to Sex: A Choose Your Own Adventure Game, from the Youtube nightmares of The Dark Room, to this game that asks you to help a drunk George Osborne survive the Leveson Inquiry. Between all those links you should be stuck clicking about for probably the rest of the afternoon.
                                  |
                                  |We’re coming to the other side of the woods now and once again the path is branching to the North and the South. You’re nearly home free! Which way will you go?")
                     :options   [{ :text "Head South"
                                   :link :10 }
                                 { :text "Head North"
                                   :link :11 }]}
                :10 { :location :10
                      :text     (strip-margin
                                  "You are eaten by scorpions. Your adventure ends here.") }
                :11 { :location :11
                      :text     (strip-margin
                                  "You are eaten by slightly bigger scorpions. Your adventure ends here.")}}})

; - SESSION STUFF -------------------------------------------------------------
(def session (atom { :stories { (slug (story :title)) story }}))

(defn set-view [view]
  (swap! session assoc :view view))
; -----------------------------------------------------------------------------

(defn header-partial
  ([heading-1]
   (header-partial heading-1 nil))
  ([heading-1 heading-2]
   [:div
     [:h1 heading-1]
     (when (some? heading-2)
       [:h2 heading-2])
     [:div.page-divider "•••"]]))

(defn markdown-partial [text]
  [:div.text-container
   { :dangerouslySetInnerHTML  { :__html (md->html text)} } ])

(defn list-view []
  [:div.container
   [:div.row
    [:div.col.l8.offset-l2
     [header-partial
      "The Adventure Begins"
      "Interactive Adventure Stories"]
     (doall
       (for [[slug story] (@session :stories)]
        ^{ :key slug } [:a.option { :href (str "#/" slug) }
         (story :title)]))]]])

(defn story-view []
  [:div.container
   [:div.row
    [:div.col.l8.offset-l2
     (let [story (@session :active-story)]
       [:div
        [header-partial
         (story :title)
         (str "by " (story :author))]
        [markdown-partial (story :synopsis)]
        [:a.option { :href (str "#/" (slug (story :title)) "/" 1) } "Begin" ]])]]])

(defn part-view []
  [:div.container
   [:div.row
    [:div.col.l8.offset-l2
     (let [part (@session :active-part)]
       [:div
         [header-partial (name (part :location))]
         [markdown-partial (part :text)]
         (doall (for [option (part :options)]
          ^{ :key (option :link) } [:a.option
                                    { :href (str "#/" (slug (get-in @session [ :active-story :title])) "/" (name (option :link))) }
                                    (option :text)]))])]]])

(defn pages []
  [:div
   [:div.grey.darken-3.nav-bar
    [:a.title { :href "#/" } "The Adventure Begins"]
    (when (@session :active-part)
      [:div.actions
       [:span (get-in @session [:active-story :title]) " by " (get-in @session [:active-story :author]) " | " ]
       [:a.action { :href (str "#/" (slug (get-in @session [:active-story :title]))) } "Restart Story"]])]
   [(@session :view)]])


(defroute "/" []
  (swap! session dissoc :active-story :active-part)
  (set-view list-view))

(defroute "/:story" { :as params }
  (swap! session assoc :active-story ((@session :stories) (params :story)))
  (swap! session dissoc :active-part)
  (set-view story-view))

(defroute "/:story/:part" { :as params }
  (swap! session assoc :active-story ((@session :stories) (params :story)))
  (swap! session assoc :active-part (get ((@session :active-story) :parts) (keyword (params :part)) { :location :?
                                                                                                      :text     (strip-margin
                                                                                                                  "|# Not Found
                                                                                                                   |
                                                                                                                   |## This part is not found") } ))
  (set-view part-view)
  (.scrollTo js/window 0 0))


(defn init []
  (secretary/set-config! :prefix "#")
  (let [h  (History.)]
    (goog.events/listen h EventType/NAVIGATE #(secretary/dispatch!  (.-token %)))
      (doto h  (.setEnabled true)))
  (reagent/render-component [pages]
                            (.getElementById js/document "container")))
