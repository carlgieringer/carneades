;;; Copyright ? 2010 Fraunhofer Gesellschaft
;;; Licensed under the EUPL V.1.1


(ns ^{:doc "This is an implementation of the argumentation scheme
            for arguments from defeasible rules.  Rules may have multiple
            conclusions and be subject to exceptions. Priorities are used
            to resolve conflicts among rules.  Compound terms
            are allowed in statements.  That is, this rule language is not restricted
            to 'datalog'.  Indeed, datalog may not be sufficient for modeling
            legal rules.  For example, we need to be able to reason about whether
            some statement hold at some time, using fluents in the event calculus.
            And we need to reason about the applicability of rules to statements.
            
            Negation, exceptions and assumptions. Statements of the form (not P),
            (unless P) and (assuming P) have special meaning.

            Goals of the form (not P) may occur in the head
            rules, to allow rules to be used to generate con arguments.
            Since multiple atoms may be in the head of a rule, this
            approach allows a single rule to generate both pro and con arguments.
            
            The (not P), (unless P) and (assuming P) forms may occur
            in the body of a rule.  These cause negations, exceptions and assumptions,
            respectively, to be included in the premises of the arguments generated by
            the rule.

            The form (not P) has a slightly different meaning when used in the head than
            in the body of a rule.  In the head, it means the rule can be used
            to generate an argument con P. In the body (not P) means dialectical,
            not classical negation. (not P) is satisifed if the *complement of the
            proof standard* for P is met by P.  Where the complement of some proof
            standard is constructed by reversing the roles of pro and con arguments in
            the standard. For example, the complement of DV is satisfied iff there 
            is at least one applicable con argument and no applicable pro arguments.
            
            The form (unless P) is a weaker form of negation than (not P).
            Whereas (unless P) holds if P is not acceptable, (not P) holds only if
            (not P) is acceptable.
            
            <condition> := <statement> | (unless <statement>) | (assuming <statement>)
            
            type clause = (list-of condition) ; representing a *conjunction* of
            conditions
            
            Predicates with special meaning for rules:
            (applies <symbol> <statement>)
            (excluded <symbol> <statement>)
            (rebuts <symbol> <symbol> <statement>)"}
            
  carneades.engine.rule
  (:use clojure.set
    clojure.contrib.def
    carneades.engine.utils
    carneades.engine.argument
    carneades.engine.statement
    carneades.engine.response
    [carneades.engine.dnf :only (to-dnf)]
    [carneades.engine.unify :only (genvar unify rename-variables apply-substitution)])
  (:require [clojure.string :as str]))

(defstruct named-clause
  :id      ; symbol
  :rule    ; rule-id
  :strict  ; rule-strict?
  :domains ; (seq-of statement)
  :head    ; rule-head
  :clause) ; the actual clause

;(defn instantiate-domains
;  [nc subs]
;  (map (fn [s]
;         {:clause (struct named-clause
;                          (gensym "c")
;                          (:rule nc)
;                          (:strict nc)
;                          (:domains nc)
;                          (map (fn [t] (apply-substitution s t)) (:head nc))
;                          (map (fn [t] (apply-substitution s t)) (:clause nc))),
;          :subs subs})
;       subs))

(defn condition-statement
  "condition -> statement
   the statement of a condition"
  [c]
  (let [predicate (first c)
        statement (second c)]
    (condp = predicate
      'unless statement
      'assuming statement
      c)))

(defn condition->premise
  "condition -> premise
   constructs a premise from a condition"
  [c]
  (if (seq? c)
    (let [[predicate stmt] c]
      (condp = predicate
        'unless (pm (statement-complement stmt))
        'assuming (pm stmt)
        (pm c)))
    (pm c)))

;; TO DO: represent the roles of conditions, e.g. "major", "minor"

;; predicate: (statement | condition) -> symbol
;; By convention, the "predicate" of literal sentences, e.g. represented
;; as a string or symbol, is the sentence itself.

(defn predicate [s]
  (if (seq? s)
    (let [pred (first s)
          stmt (second s)]
      (statement-predicate
       (condp = pred
           'not stmt
           'unless stmt
           'assuming stmt
           'applies (nth s 2)
           s)))
    s))

(defstruct- rule-struct
  :id ;; symbol
  :strict ;; boolean, critical questions apply only if this is #f
  :domains ;; (seq-of statement)
  :head   ;; (seq-of statement), allow multiple conclusions
  :body ;; (seq-of clause)
  ;; disjunction of conjunctions, i.e. disjunctive normal form
  )

(defn make-rule [& keysvals]
  (apply struct rule-struct keysvals))

;; Note: A strict rule is still defeasible in this model.
;; A strict rule is simply a rule for
;; which the usual critical questions about rules do not apply
;; and may not be asked.

(defn make-rule-head [expr]
  "expression -> (seq-of statement)"
  (if (and (seq? expr) (= (first expr) 'and))
    (rest expr)
    (list expr)))

(defn make-rule-body [expr]
  "expr -> (seq-of clause)"
  (letfn [(process-disjunct [expr]
            (if (seq? expr)
              (if (= (first expr) 'and)
                (rest expr)
                (list expr))
              (list expr)))]
    (let [dnf (to-dnf expr)]
      (cond (and (seq? dnf) (= (first dnf) 'and))
        (list (rest dnf))
        (and (seq? dnf) (= (first dnf) 'or))
        (map process-disjunct (rest dnf))
        ;; single condition
        :else (list (list dnf))))))

(defmacro assertion [id conclusion]
  `(make-rule '~id false '() '(~conclusion)))

(defmacro assertions [id & conclusions]
  `(make-rule '~id false '() '(~@conclusions)))

(defmacro assertion* [id conclusion]
  `(make-rule '~id true '() '(~conclusion)))

(defmacro assertions* [id & conclusions]
  `(make-rule '~id true '() '(~@conclusions)))

(defn- rule-macro-helper [id body strict]
  (cond
    (and (not (empty? body))
      (= (count body) 3))
    (let [[ifsymbol conditions conclusions] body]
      (if (= ifsymbol 'if)
        `(make-rule '~id ~strict
           '()
           '~(make-rule-head conclusions)
           '~(make-rule-body conditions))
        (throw (IllegalArgumentException.
                 (format "Invalid symbol \"%s\", expected \"if\" "
                   ifsymbol))))),
    (and (not (empty? body))
      (= (count body) 4))
    (let [[ifsymbol domains conditions conclusions] body]
      (if (= ifsymbol 'if)
        `(make-rule '~id ~strict
           '~domains
           '~(make-rule-head conclusions)
           '~(make-rule-body conditions))
        (throw (IllegalArgumentException.
                 (format "Invalid symbol \"%s\", expected \"if\" "
                   ifsymbol))))),
    true (throw (IllegalArgumentException.
                  "Empty sequence as second argument"))))

(defmacro rule  [id body]
  "create a rule, not strict"
  (rule-macro-helper id body false))

(defmacro rule*  [id body]
  "create a strict rule"
  (rule-macro-helper id body true))

(defvar *question-types* #{'excluded 'priority 'valid}
  "question-type = excluded | priority | valid")

(defn- clause-x [cl type]
  (map condition-statement (filter #(= (first %) type) cl)))

(defn- clause-assumptions [clause]
  (reduce (fn [assumptions condition]
            (if (seq? condition)
              (let [[predicate stmt] condition]   
                (condp = predicate
                  'unless (union assumptions #{(statement-complement stmt)})
                  'assuming (union assumptions #{stmt})
                  assumptions))
              assumptions))
          #{}
          clause))

(defn rename-rule-variables [r]
  (let [[m head] (rename-variables {} (:head r))
        [m2 body] (rename-variables m (:body r))]
    (assoc r :head head :body body)))

(defn rename-clause-variables [r]
  (let [[m head] (rename-variables {} (:head r)),
        [m2 clause] (rename-variables m (:clause r)),
        [m3 domains] (rename-variables m2 (:domains r))]
    (assoc r :head head :clause clause :domains domains)))

(defstruct rulebase-struct
  :table ;; map: predicate -> (seq-of rules)
  :rules ;; (seq-of rules)
  )

(defvar *empty-rulebase* (struct rulebase-struct {} '()))

(defn add-rule
  "rulebase rule -> rulebase

   Add a rule to the rule base, for each conclusion of the rule,
   indexing it by the predicate of the conclusion.  There will be a copy
   of the rule, each with the same id, for each conclusion of the rule. This
   is an optimization, so that we don't have to iterate over the conclusions
   when trying to unify some goal with the conclusion of the rule
   with some goal."
  [rb r]
  
  (reduce (fn [rb2 conclusion]
            (let [pred (predicate conclusion)
                  table (:table rb2)
                  current-rules (table pred)
                  new-rules (conj current-rules r)]
              (if (not (.contains (map :id current-rules) (:id r)))
                (struct rulebase-struct
                        (assoc table pred new-rules)
                        (conj (:rules rb) r))
                rb2)))
          rb
          (:head r)))

(defn add-rules
  "rulebase (seq-of rule) -> rulebase"
  [rb l]
  (reduce (fn [rb2 r]
            (add-rule rb2 r))
          rb
          l))

(defn rulebase [& l]
  (add-rules *empty-rulebase* l))

(defn- concat-scheme [l]
  (str/join "-" l))

(defn- remove-inst [s]
  (let [sl (.split s "-")]
    (concat-scheme (butlast sl))))

(defn- rule->clauses [rule]                                             
  (let [rule-clauses (:body rule)]
    (if (empty? rule-clauses)
      (list (struct named-clause
                    (gensym "c")
                    (:id rule)
                    (:strict rule)
                    (:domains rule)
                    (:head rule)
                    '()))
      (map (fn [clause] 
             (struct named-clause
                     (gensym "c")
                     (:id rule)
                     (:strict rule)
                     (:domains rule)
                     (:head rule)
                     clause)) 
           rule-clauses))))

(defn get-clauses [rb goal subs]
  (let [pred (predicate (apply-substitution subs goal))
        applicable-rules ((:table rb) pred)]
    (mapinterleave rule->clauses applicable-rules)))

(defn generate-arguments-from-rules
  ([rb] (generate-arguments-from-rules rb nil))
  ([rb ont]
    (fn [subgoal subs]
      (letfn [(apply-for-conclusion
                [clause c]
                ;; apply the clause for conclusion
                ;; in the head of the rule
                (let [subs2 (or (unify c subgoal subs)
                                (unify `(~'unless ~c)
                                       subgoal subs)
                                (unify `(~'assuming ~c)
                                       subgoal subs)
                                (unify `(~'applies ~(:rule clause) ~c) subgoal subs))]
                  (if (not subs2)
                    false ; fail
                    
                    ;                    (let [inst-clauses (instantiate-domains clause subs2)]
                    ;                      (map (fn [inst-clause-map]
                    ;                             (let [ic (:clause inst-clause-map)]
                    ;                               (make-response (:subs inst-clause-map)
                    ;                                         (clause-assumptions (:clause clause))
                    ;                                         (argument (gensym "a")
                    ;                                                   false
                    ;                                                   *default-weight*
                    ;                                                   (if (= (first subgoal) 'not) :con :pro)
                    ;                                                   (statement-atom (condition-statement subgoal))
                    ;                                                   (map condition->premise (:clause ic))
                    ;                                                   (str (:rule ic))))))
                    ;                           inst-clauses)))))
                    
                    
                    (make-response subs2
                                   (clause-assumptions (:clause clause))
                                   (argument (gensym "a")
                                             false
                                             *default-weight*
                                             (if (= (first subgoal) 'not) :con :pro)
                                             (statement-atom (condition-statement subgoal))
                                             (map condition->premise (:clause clause))
                                             (str (:rule clause)))))))
              
              (apply-clause [clause]
                            (apply concat (filter identity 
                                                  (map #(apply-for-conclusion clause %) 
                                                       (:head clause)))))]
        (mapinterleave
          (fn [c] (apply-clause c))
          (map rename-clause-variables (get-clauses rb subgoal subs)))))))

