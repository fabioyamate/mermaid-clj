# mermaid-clj

__mermaid-clj__ is a library DSL in clojure to build [mermaidjs](https://mermaid-js.github.io/mermaid/) diagrams. The main purpose is to support reuse of subdiagrams and also leverage structural editing to manage them.

# Usage

A [sample diagram](https://mermaid-js.github.io/mermaid-live-editor/#/edit/eyJjb2RlIjoic2VxdWVuY2VEaWFncmFtXG5wYXJ0aWNpcGFudCBcImJcIlxucGFydGljaXBhbnQgXCJjXCJcbnBhcnRpY2lwYW50IFwiZFwiXG5wYXJ0aWNpcGFudCBcImVcIlxucGFydGljaXBhbnQgXCJhXCJcbiAgXCJhXCItPj4rXCJiXCI6IHNlbmRcbiAgXCJiXCItPj4rXCJjXCI6IGNhbGxzXG5sb29wIHJldHJ5XG5yZWN0IHJnYigyNTUsMjU1LDAsMC41KVxuICBcImNcIi0-PlwiY1wiOiBxdWVyeSBkYlxuICBcImNcIi0-PitcImFcIjogZmV0Y2ggZGF0YVxuICBcImFcIi0tPj4tXCJjXCI6IHJlcGx5XG4gIFwiZFwiLT4-K1wiZlwiOiB3b29wc1xuICBcImZcIi0tPj4tXCJkXCI6IHJlcGx5XG5lbmRcbmVuZFxuICBcImNcIi0tPj4tXCJiXCI6IHJlcGx5XG4gIFwiYlwiLT4-K1wiZFwiOiBvdXRcbiAgXCJkXCItLT4-LVwiYlwiOiBkXG4gIFwiYlwiLS0-Pi1cImFcIjogcmVwbHlcbmFsdCB0cnVlXG5sb29wIHJldHJ5XG4gIFwiY1wiLT4-K1wiZFwiOiBxdWVyeSBkYlxuTm90ZSBvdmVyIFwiY1wiLFwiZFwiOiBvdmVyIGMgYW5kIGRcbiAgXCJkXCItLT4-LVwiY1wiOiByZXBseVxuICBcImNcIi0-PlwiY1wiOiBxdWVyeSBkYlxuTm90ZSBvdmVyIFwiY1wiOiBvdmVyIGNcbmVuZFxuZWxzZSBmYWxzZS1hc3luY1xuICBcImNcIi14XCJkXCI6IFdBSVRcbmVsc2UgZmFsc2Utc3luY1xuICBcImNcIi0-PitcImRcIjogcXVlcnkgZGJcbiAgXCJkXCItLT4-LVwiY1wiOiByZXBseVxuZW5kXG5yZWN0IHJnYigwLDAsMCwwLjEpXG5vcHQgYWx0ZXJuYXRpdmVcbnJlY3QgcmdiKDI1NSwwLDAsMC4yKVxuICBcImNcIi0-PitcImFcIjogY2FsbHNcbk5vdGUgbGVmdCBvZiBcImFcIjogdGhpcyBpcyBub3RlIGxlZnQ8YnI-b2YgYVxuICBcImFcIi0tPj4tXCJjXCI6IHJlcGx5XG5lbmRcbiAgXCJkXCItPj4rXCJlXCI6IGNhbGxzXG4gIFwiZVwiLS0-Pi1cImRcIjogcmVwbHlcbmVuZFxuZW5kXG5wYXIgc2VuZCBtZXNzYWdlXG4gIFwiYVwiLXhcImdcIjogTUVTU0FHRVxuICBcImdcIi0-PitcImhcIjogc2F2ZSBkYXRhXG4gIFwiaFwiLS0-Pi1cImdcIjogcmVwbHlcbmFuZCBkZWxpdmVyIGVtYWlsXG4gIFwiYlwiLXhcImVcIjogQVJDSElWRVxuICBcImVcIi14XCJhXCI6IEFSQ0hJVkVEXG5hbmQgd2ViaG9va1xuICBcImFcIi0-PitcInRoaXJkIHBhcnR5XCI6IFBPU1QgL2FwaS93ZWJob29rXG4gIFwidGhpcmQgcGFydHlcIi0tPj4tXCJhXCI6IDIwMCBPS1xuZW5kXG5yZWN0IHJnYigwLDAsMjU1LDAuMSlcbk5vdGUgcmlnaHQgb2YgXCJhXCI6IHRoaXMgaXMgbm90ZSByaWdodDxicj5vZiBhXG4gIFwiYVwiLT4-K1wiYlwiOiBzZW5kXG4gIFwiYlwiLT4-K1wiY1wiOiBjYWxsc1xubG9vcCByZXRyeVxuICBcImNcIi0-PlwiY1wiOiBxdWVyeSBkYlxuICBcImNcIi0-PitcImFcIjogZmV0Y2ggZGF0YVxuICBcImFcIi0tPj4tXCJjXCI6IHJlcGx5XG4gIFwiZFwiLT4-K1wiZlwiOiB3b29wc1xuICBcImZcIi0tPj4tXCJkXCI6IHJlcGx5XG5lbmRcbiAgXCJjXCItLT4-LVwiYlwiOiByZXBseVxuICBcImJcIi0-PitcImRcIjogb3V0XG4gIFwiZFwiLS0-Pi1cImJcIjogZFxuICBcImJcIi0tPj4tXCJhXCI6IHJlcGx5XG5lbmQiLCJtZXJtYWlkIjp7InRoZW1lIjoiZGVmYXVsdCJ9LCJ1cGRhdGVFZGl0b3IiOmZhbHNlfQ) that renders all syntax support for mermaid can be described as:

```clojure
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
                    (synchronous :c :c "query db"
                                 (note-over :c "over c")))
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
```

From this you can output the string version of this diagram for debugging:

```
(println sample)
```

Our you can either:

```
(download-image sample) ;; rendering a png image from mermaidjs website
(browser-edit sample)   ;; viewing render data on mermaidjs website
```

## Reusable blocks

Each expression is a group in the diagram, so the above diagram could be break into:

```clojure
(def sync-1
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
(def sync-2
  (synchronous
   :b :d "out" "d"))

;; joining the groups

(def full
  (sequence-diagram
    sync-1
    sync-2))

;; grouping is possible
(def group-1
  (group sync-1 sync-2))

(sequence-diagram
  (autonumber)
  group-1)
```
