(ns ^{:doc "Save questions and answers of the dialog between the engine and the user (when using ask.clj)"}
  carneades.engine.dialog
  (:use carneades.engine.statement))

(defrecord Dialog [questions answers])

(defn add-questions
  [dialog questions]
  (update-in dialog [:questions] concat questions))

(defn add-answers
  [dialog answers]
  {:pre [(>= (count answers) 1)]}
  (reduce (fn [dialog answer]
            (update-in dialog [:answers (literal-predicate answer)] conj answer))
          dialog
          answers))

(defn get-answers
  [dialog question]
  (when question
    (get-in dialog [:answers (literal-predicate question)])))

(defn get-nthquestion
  [dialog n]
  (first (filter (fn [q] (= (:id q) n)) (:questions dialog))))

(defn make-dialog
  []
  (->Dialog () {}))