(ns mermaid-clj.core
  (:require [clojure.data.json :as json]
            [clojure.java.shell :as shell])
  (:import java.util.Base64))

(def direction->mermaid
  ;; Type   Meaning                      Description
  ;; ->     asynchronous message         Solid line without arrow
  ;; -->    reply asynchronous message   Dotted line without arrow
  ;; ->>    synchronous call             Solid line with arrowhead
  ;; -->>   reply synchronous call       Dotted line with arrowhead
  ;; -x     asynchronous message         Solid line with a cross at the end (async)
  ;; --x    reply asynchronous message   Dotted line with a cross at the end (async)
  {:call "->>+"
   :async "-X"
   :message "->"
   :reply "-->>-"
   :return "-->>-"
   :self "->>"})

(defn actor-name
  [v]
  (str "\""
       (clojure.string/replace (name v) #"\-" " ")
       "\""))

(defn node
  [& data]
  (with-meta (vec data) {::node true}))

(defn group
  [& data]
  (with-meta (vec data) {::group true}))

(defn render*
  [sequence-diagram]
  (condp #(contains? %2 %1) (meta sequence-diagram)
    ::node (let [[from direction to comment] sequence-diagram]
             (str "  " (actor-name from) (direction->mermaid direction) (actor-name to) ": " (or comment "return")))
    ::participants (clojure.string/join "\n"
                                        (for [actor sequence-diagram]
                                          (str "participant " (actor-name actor) "")))
    ::autonumber "autonumber"
    ::group (clojure.string/join "\n"
                                 (for [value sequence-diagram
                                       :when (seq value)]
                                   (render* value)))
    ::alt (let [[if-text if-form others] sequence-diagram]
            (str "alt " if-text "\n" (render* if-form) "\n"
                 (clojure.string/join "\n"
                                      (when (seq others)
                                        (for [[else-text else-form] (partition 2 others)]
                                          (str "else " else-text "\n" (render* else-form)))))
                 "\nend"))

    ::parallel (let [[if-text if-form others] sequence-diagram]
                 (str "par " if-text "\n" (render* if-form) "\n"
                      (clojure.string/join "\n"
                                           (when (seq others)
                                             (for [[else-text else-form] (partition 2 others)]
                                               (str "and " else-text "\n" (render* else-form)))))
                      "\nend"))

    ::note-left (let [[actor note] sequence-diagram]
                  (str "Note left of " (actor-name actor) ": " note))

    ::note-right (let [[actor note] sequence-diagram]
                   (str "Note right of " (actor-name actor) ": " note))

    ::note-over (let [[actor1 actor2 note] sequence-diagram]
                  (str "Note over " (actor-name actor1) "," (actor-name actor2) ": " note))

    ::highlight (str "rect " (::highlight (meta sequence-diagram)) "\n"
                     (render* (apply group sequence-diagram))
                     "\nend")
    ::loop (str "loop " (::loop (meta sequence-diagram)) "\n"
                (render* (apply group sequence-diagram))
                "\nend")

    ::opt (str "opt " (::opt (meta sequence-diagram)) "\n"
               (render* (apply group sequence-diagram))
               "\nend")))

(defn sequence-diagram
  [& diagrams]
  (str "sequenceDiagram\n"
       (clojure.string/join "\n"
                            (map render* diagrams))))

(defn loop-block
  [text & forms]
  (with-meta forms {::loop text}))

(defn synchronous
  [from to & params]
  (let [[message-from message-to] (take-while string? params)
        forms (drop-while string? params)]
    (if (= from to)
      (group (node from :self to (or message-from "calls"))
             (apply group forms))
      (group (node from :call to (or message-from "calls"))
             (apply group forms)
             (node to :reply from (or message-to "reply"))))))

(def color->rgb
  {:green "rgb(0,255,0,0.1)"
   :blue "rgb(0,0,255,0.1)"
   :red "rgb(255,0,0,0.2)"
   :yellow "rgb(255,255,0,0.5)"
   :gray "rgb(0,0,0,0.1)"})

(defn note-right
  [actor note]
  (with-meta [actor note] {::note-right true}))

(defn note-left
  [actor note]
  (with-meta [actor note] {::note-left true}))

(defn note-over
  [actor1 actor2 note]
  (with-meta [actor1 actor2 note] {::note-over true}))

(defn highlight
  [color & forms]
  (with-meta forms {::highlight (color->rgb color)}))

(defn async
  [from to message & forms]
  (group
   (node from :async to message)
   (apply group forms)))

(defn message
  [from to message & forms]
  (group
   (node from :message to message)
   (apply group forms)))

(defn alt
  [text form & others]
  (with-meta [text form others] {::alt true}))

(defn parallel
  [text form & others]
  (with-meta [text form others] {::parallel true}))

(defn opt
  [text & forms]
  (with-meta forms {::opt text}))

(defn autonumber
  []
  (with-meta {} {::autonumber true}))

(defn participants
  [& actors]
  (with-meta actors {::participants true}))

(def mermaid-edit "https://mermaid-js.github.io/mermaid-live-editor/#/edit/%s")
(def mermaid-view "https://mermaid-js.github.io/mermaid-live-editor/#/view/%s")
(def mermaid-img "https://mermaid.ink/img/%s")

(defn encode-base64
  [s]
  (.encodeToString (Base64/getEncoder) (.getBytes s "UTF-8")))

(defn browser-view
  [diagram]
  (let [current-env (into {} (System/getenv))]
    (shell/sh "xdg-open"
              (format mermaid-view
                      (encode-base64
                       (json/write-str
                        {:code diagram
                         :mermaid {:theme "default"}
                         :updateEditor false})))
              :env (assoc current-env "BROWSER" "firefox"))))

(defn browser-edit
  [diagram]
  (let [current-env (into {} (System/getenv))]
    (shell/sh "xdg-open"
              (format mermaid-edit
                      (encode-base64
                       (json/write-str
                        {:code diagram
                         :mermaid {:theme "default"}
                         :updateEditor false})))
              :env (assoc current-env "BROWSER" "firefox"))))

(defn download-image
  [diagram]
  (let [current-env (into {} (System/getenv))]
    (spit "/tmp/output.png" (slurp (format mermaid-img
                                           (encode-base64
                                            (json/write-str
                                             {:code diagram
                                              :mermaid {:theme "default"}})))))))

(comment
  (def sample
    (sequence-diagram
     (participants :b :c :d :e :a)
     (synchronous
      :a :b "send"
      (synchronous
       :b :c
       (loop-block
        "retry"
        (highlight :yellow
                   (synchronous :c :c "query db")
                   (synchronous :c :a "fetch data")
                   (synchronous :d :f "woops"))))
      (synchronous
       :b :d "out" "d"))
     (alt "true"
          (loop-block "retry"
                      (synchronous :c :d "query db"
                                   (note-over :c :d "over c and d"))
                      (synchronous :c :c "query db"))
          "false-async"
          (async :c :d "WAIT")
          "false-sync"
          (synchronous :c :d "query db"))
     (highlight :gray
                (opt "alternative"
                     (highlight :red
                                (synchronous :c :a
                                             (note-left :a "this is note left of a")))
                     (synchronous :d :e)))
     (parallel "send message"
               (async :a :g "MESSAGE"
                      (synchronous :g :h "save data"))
               "deliver email"
               (async :b :e "ARCHIVE"
                      (message :e :a "ARCHIVED"))
               "webhook"
               (synchronous :a :third-party "POST /api/webhook" "200 OK"))
     (highlight :blue
                (note-right :a "this is note right of a")
                (synchronous :a :b "send"
                             (synchronous :b :c
                                          (loop-block "retry"
                                                      (synchronous :c :c "query db")
                                                      (synchronous :c :a "fetch data")
                                                      (synchronous :d :f "woops")))
                             (synchronous :b :d "out" "d")))))
  (println
   sample)

  (download-image sample)
  (browser-edit sample)

  )
