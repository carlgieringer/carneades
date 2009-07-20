;;; Carneades Argumentation Library and Tools.
;;; Copyright (C) 2008 Thomas F. Gordon, Fraunhofer FOKUS, Berlin
;;; 
;;; This program is free software: you can redistribute it and/or modify
;;; it under the terms of the GNU Lesser General Public License version 3 (LGPL-3)
;;; as published by the Free Software Foundation.
;;; 
;;; This program is distributed in the hope that it will be useful, but
;;; WITHOUT ANY WARRANTY; without even the implied warranty of
;;; MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
;;; General Public License for details.
;;; 
;;; You should have received a copy of the GNU Lesser General Public License
;;; along with this program.  If not, see <http://www.gnu.org/licenses/>.

#!r6rs

(library
 (carneades rule)
  ; This is an implementation of the argumentation scheme for
  ; for arguments from defeasible rules.  Rules may have multiple
  ; conclusions, as in SWRL, and be subject to exceptions. Priorities are used
  ; to resolve conflicts among rules.  Unlike in SWRL, compound terms
  ; are allowed in statements.  That is, this rule language is not restricted
  ; to "datalog".  Indeed, datalog may not be sufficient for modeling
  ; legal rules.  For example, we need to be able to reason about whether some statement
  ; hold at some time, using fluents in the event calculus. 
  ; And we need to reason about the applicability of rules to statements.
  
 (export rule rule* make-rule rule? rule-id rule-strict rule-head rule-body 
         rule-critical-questions empty-rulebase rulebase rulebase? get-rule
         add-rules rulebase-rules generate-arguments-from-rules rule->datum
         rulebase->datum (rename (make-head make-rule-head)) (rename (make-body make-rule-body))
         get-clauses)
 
 (import (rnrs)
         (rnrs lists)
         (rnrs hashtables)
         (carneades base)
         (only (carneades system) gensym)
         (carneades dnf)
         (carneades statement)
         (carneades unify)
         (prefix (carneades lib srfi lists) list:)
         (prefix (carneades argument) argument:)
         (carneades stream)
         (prefix (carneades argument-search) as:)
         (carneades lib match)
         (prefix (carneades table) table:)
         (carneades lib srfi format))
  
  (define *debug* #f)
 
  
  ; Negation, exceptions and assumptions. Statements of the form (not P), (unless P)
  ; and (assuming P) have special meaning. 
  
  ; Goals of the form (not P) may occur in the head
  ; rules, to allow rules to be used to generate con arguments.
  ; Since multiple atoms may be in the head of a rule, this 
  ; approach allows a single rule to generate both pro and con arguments.
  
  ; The (not P), (unless P) and (assuming P) forms may occur 
  ; in the body of a rule.  These cause negations, exceptions and assumptions, respectively,
  ; to be included in the premises of the arguments generated by the rule.
  
  ; The form (not P) has a slightly different meaning when used in the head than
  ; in the body of a rule.  In the head, it means the rule can be used
  ; to generate an argument con P. In the body (not P) means dialectical,
  ; not classical negation. (not P) is satisifed if the *complement of the
  ; proof standard* for P is met by P.  Where the complement of some proof standard
  ; is constructed by reversing the roles of pro and con arguments in the standard.
  ; For example, the complement of SE is satisified iff there is a least 
  ; one defensible con argument.  And the complement
  ; of DV is satisfied iff there is at least one defensible
  ; con argument and no defensible pro arguments.  
  
  ; The form (unless P) is a weaker form of negation than (not P). Whereas (unless P)
  ; holds if P is not acceptable, (not P) holds only if P is rejectable.
  
  ; <condition> := <statement> | (unless <statement>) | (assuming <statement>)

  ; type clause = (list-of condition) ; representing a *conjunction* of conditions
  
  ; Predicates with special meaning for rules:
  ; (applies <symbol> <statement>)
  ; (excluded <symbol> <statement>)
  ; (rebuts <symbol> <symbol> <statement>)
  
  ; condition-statement:  condition -> statement
  ; the statement of a condition
  (define (condition-statement c)
    (match c
      (('unless a) a)
      (('assuming a) a)
      (_ c)))
  
  ; TO DO: represent the roles of conditions, e.g. "major", "minor"
  
  ; predicate: (statement | condition) -> symbol
  ; By convention, the "predicate" of literal sentences, e.g. represented
  ; as a string or symbol, is the sentence itself.
  
;  (define (predicate s1)
;    (let ((s2 (match s1 
;                (('not s3) (predicate s3))
;                (('unless s3) (predicate s3))
;                (('assuming s3) (predicate s3))
;                (('applies _ s3) (predicate s3))
;                (_ s1))))
;      (if (pair? s2) (car s2) s2)))
  
  (define (predicate s1)
    (let ((s2 (match s1 
                (('not s3) s3)
                (('unless s3) s3)
                (('assuming s3) s3)
                (('applies _ s3) s3)
                (_ s1))))
      (statement-predicate s2)))

  
  (define-record-type named-clause
    (fields id       ; symbol
            rule     ; rule-id
            strict   ; rule-strict?
            head     ; rule-head
            clause)) ; the actual clause
  
  ; Note: Renamed this structure from "rule" to "%rule" to avoid a name conflict with
  ; the "rule" macro.
  (define-record-type %rule
    (fields id      ; symbol
            strict  ; boolean, critical questions apply only if this is #f
            head    ; (list-of statement), allow multiple conclusions
            body))  ; (list-of clause) ; disjunction of conjunctions, i.e. disjunctive normal form
  
  (define make-rule make-%rule)
  (define rule? %rule?)
  (define rule-strict %rule-strict)
  (define rule-id %rule-id)
  (define rule-head %rule-head)
  (define rule-body %rule-body)
    
  (define (rule-predicates r)
    (map predicate (rule-head r)))
  
  ; Note: A strict rule is still defeasible in this model. A strict rule is simply a rule for 
  ; which the usual critical questions about rules do not apply and may not be asked.
  
  ; make-head: expression -> (list-of statement)
  (define (make-head expr)
    (if (and (list? expr) (eq? (car expr) 'and))
        (cdr expr)
        (list expr)))
 
  
  ; make-body: expr -> (list-of clause)
  (define (make-body expr)
    ; process-disjunct: expression -> clause
    (define (process-disjunct expr)
      (if (list? expr) 
          (if (eq? (car expr) 'and)
              (cdr expr)
              (list expr))
          (list expr)))
    (let ((expr (to-dnf expr)))
      (cond ((and (list? expr) (eq? (car expr) 'and))
             (list (cdr expr)))
            ((and (list? expr) (eq? (car expr) 'or))
             (map process-disjunct (cdr expr)))
            (else (list (list expr)))))) ; single condition
           
  (define-syntax rule
    (syntax-rules (if)
      ((_ id (if conditions conclusions))
       (make-%rule (quote id) 
                  #f
                  (make-head (quote conclusions))
                  (make-body (quote conditions))))
      ((_ id conclusion ...)
       (make-%rule (quote id)
                  #f
                  (quote (conclusion ...))
                  null))))
  
  ; rule*: defines "strict" rules, i.e. without validity assumptions and not 
  ; subject to exclusions. To do: is there some way to share code between this
  ; macro and the rule macro?
  (define-syntax rule*
    (syntax-rules (if)
      ((_ id (if conditions conclusions))
       (make-%rule (quote id) 
                  #t
                  (make-head (quote conclusions))
                  (make-body (quote conditions))))
      ((_ id conclusion ...)
       (make-%rule (quote id)
                  #t
                  (quote (conclusion ...))
                  null))))
  
  ; statement->premise: datum -> premise
  (define (statement->premise s)
    (match s
      (('unless a) (argument:ex a))
      (('assuming a) (argument:am a))
      (_ (argument:pr s))))
  
  ; type question-type = excluded | priority | valid
  (define question-types '(excluded priority valid))
    
  ; rule-critical-questions: rule-id (list-of question-type) statement bool -> (list-of premise)
  ; The critical questions for an argument about a statement s
  ; generated from a rule r:
  ; 1) Is r a valid rule?
  ; 2) Is r excluded with respect to s?
  ; 3) Is there another rule of higher priority which rebuts r?
  (define (rule-critical-questions rid qs s strict?)
    (define (f question) 
      (case question
        ((excluded) 
         (argument:ex (make-fatom "Rule ~a is excluded for ~a." `(excluded ,rid ,s))))
         ; (argument:ex `(excluded ,rid ,s)))
        ((priority) 
         (argument:ex (make-fatom "Rule ~a has priority over rule ~a with respect to ~a."
                                             `(priority ,(genvar) ,rid ,s))))
        ((valid)
         (argument:ex `(not ,(make-fatom "Rule ~a is valid." `(valid ,rid)))))))
    (if strict? 
        null
        ; filter out unknown questions:
        (map f (list:lset-intersection eq? qs question-types))))  
  
  ; clause-exceptions: clause -> (list-of statement)
  ; The exceptions in a clause
  (define (clause-exceptions cl)
    (map condition-statement
         (filter (lambda (s) 
                   (match s
                     (('unless a) #t)
                     (_ #f)))
                 cl))) 
  
  ; clause-assumptions: clause -> (list-of statement)
  ; The assumptions in a clause
  (define (clause-assumptions cl)
    (map condition-statement
         (filter (lambda (s) 
                   (match s
                     (('assuming a) #t)
                     (_ #f)))
                 cl)))
  

  ; rename-rule-variables: rule -> rule
  (define (rename-rule-variables r)
    (let ((tbl (make-eq-hashtable)))
      (make-%rule (rule-id r) 
                 (rule-strict r)
                 (rename-variables tbl (rule-head r))
                 (rename-variables tbl (rule-body r)))))
  
  (define (rename-clause-variables c)
    (let ((tbl (make-eq-hashtable)))
      (make-named-clause (named-clause-id c)
                         (named-clause-rule c)
                         (named-clause-strict c)
                         (rename-variables tbl (named-clause-head c))
                         (rename-variables tbl (named-clause-clause c)))))
  
  (define-record-type %rulebase 
    (fields table    ; finite map from a predicate symbol to a list of rules about the predicate
            rules))  ; list of the rules

  (define rulebase? %rulebase?)
  (define rulebase-rules %rulebase-rules)
  
  (define empty-rulebase
    (make-%rulebase (table:make-eq-table null) null))
  
  ; add-rule:  rulebase rule -> rulebase
  ; Add a rule to the rule base, for each conclusion of the rule,
  ; indexing it by the predicate of the conclusion.  There will be a copy
  ; of the rule, each with the same id, for each conclusion of the rule. This
  ; is an optimization, so that we don't have to iterate over the conclusions when
  ; trying to unify some goal with the conclusion of the rule with some goal.
  (define (add-rule rb1 r)
    (fold-right (lambda (conclusion rb2)
                  (let* ((pred (predicate conclusion))
                         (tbl (%rulebase-table rb2))
                         (current-rules (table:lookup tbl pred null))
                         (new-rules (if (not (memq r current-rules))
                                        (cons r current-rules)
                                        current-rules)))
                    (make-%rulebase (table:insert tbl pred new-rules)
                                    (cons r (%rulebase-rules rb1)))))
                rb1
                (rule-head r)))
  
  ; add-rules: rulebase (list-of any)) -> rulebase
  (define (add-rules rb1 l)
    (fold-right 
     (lambda (r rb) (if (rule? r) (add-rule rb r) rb)) 
     rb1 
     l))
  
;  (define-syntax define-rulebase
;    (syntax-rules ()
;      ((_ id rule ...)
;       (define id (list:fold-right (lambda (r rb) (add-rule rb r))
;                                   empty-rulebase
;                                   (list rule ...))))))
 
  ; rulebase: rule ... -> rulebase
  (define (rulebase . l)  (add-rules empty-rulebase l))
  
  (define (get-rule id rb1)
    (find (lambda (r) (eq? (rule-id r) id)) (%rulebase-rules rb1)))
  
  (define clause-counter 0)
  
  (define (init-clause-counter)
    (set! clause-counter 0))
  
  (define (add-clause)
    (set! clause-counter (+ clause-counter 1))
    (string->symbol (string-append "-c" (number->string clause-counter))))
  
  (define (get-clauses args rb1 goal subs)
    (let* ((pred (predicate (subs goal)))
           (applicable-rules (table:lookup (%rulebase-table rb1)
                                           pred
                                           null))
           (applicable-clauses (flatmap (lambda (rule)
                                          (init-clause-counter)
                                          (let ((rule-clauses (rule-body rule)))
                                            (if (null? rule-clauses)
                                                (list (make-named-clause (add-clause)
                                                                         (rule-id rule)
                                                                         (rule-strict rule)
                                                                         (rule-head rule)
                                                                         '()))
                                                (map (lambda (c) (make-named-clause (add-clause)
                                                                                    (rule-id rule)
                                                                                    (rule-strict rule)
                                                                                    (rule-head rule)
                                                                                    c))
                                                     rule-clauses))))
                                        applicable-rules))
           (applied-clauses (map string->symbol (argument:schemes-applied args (subs (statement-atom goal)))))
           (remaining-clauses (filter (lambda (c)
                                        (not (member (string->symbol (string-append (symbol->string (named-clause-rule c))
                                                                                    (symbol->string (named-clause-id c))
                                                                                    ))
                                                     applied-clauses)))
                                      applicable-clauses)))
      ; (if *debug* (printf "get-clauses: goal ~a has ~a remaining clauses~%" (subs goal) (length remaining-clauses)))
      remaining-clauses
      ))
  
  ; flatmap: (any -> list) list -> list
  (define (flatmap f l) (apply append (map f l)))
  
  
  ; Problems:
  ; Q. How to identify the premises to work on 
  ; A. Relevant but unacceptable and undecided statements in the argument
  ; graph are subgoals to choose from.  The argument module offers a subgoals function.
  ; Q. How to generate con arguments from rules
  ; A. (not P) atoms are allowed in the head of a rule.  A
  ; rule with (not A) in its head can be used to generate an argument con A.
  ; Q. How to identify the schemes applied in prior arguments, so as
  ;   to be able to attack them by revealing implicit premises or
  ;   asking critical questions.
  ; A. Arguments have been extended to now include a list of questions. It is
  ; the responsibility of an inference engine for some argumentation scheme
  ; to explicitly list the critical questions in the argument generated.  Thus,
  ; to later ask the question, one need not know which scheme was applied.
  ; Q. How to override rules using priorities
  ; A. By asking the appropriate critical question, from the scheme for arguments
  ; from rules.  This adds an exception to the argument, which can then be used
  ; to attack the rule, by looking for arguments pro the exception.  If a rule r1
  ; is applied to prove a proposition p1, then the exception added has the 
  ; form (priority ?r2 r1 p1), meaning there exists a rule, ?r2, which
  ; has priority over r1 with respect to p1.  The priority relation can be defined as
  ; (if (and (prior ?r2 ?r1)
  ;          (applies ?r2 (complement ?p1)))
  ;     (priority ?r2 ?r1 ?p1))
  ; Rules, such as Lex Superior, can be used to define the prior relation in applications.
  ; Q. How to limit the applicability of rules, based on their temporal properties.
  ; A. Time dependent rules must include temporal conditions.  
  
  
  ; type generator : statement state -> (stream-of response)
  
  ; generate-arguments-from-rules: rulebase (list-of question-types) -> generator
  (define (generate-arguments-from-rules rb qs)
    (lambda (subgoal state) 
      (let* ((args (as:state-arguments state))
             (subs (as:state-substitutions state)))
        
        ; apply-clause: ; clause rule -> (list-of response)
        (define (apply-clause clause) 
          
          (define (unify1 t1 t2) 
            (unify* t1 t2 
                    subs
                    (lambda (t) t) 
                    (lambda (msg) #f)
                    #f))
          
          
          (define (apply-for-conclusion c) ; -> response | #f
            ; Apply the clause for conclusion c in the head of the rule. 
            (if *debug* (begin (display "unifying conclusion ")
                               (display c)
                               (display " of rule ")
                               (display (named-clause-rule clause))
                               (display " with goal ")
                               (display subgoal)
                               (newline)))
            (let ((subs2 (or (unify1 c subgoal)
                             (unify1 `(unless ,c) subgoal)
                             (unify1 `(assuming ,c) subgoal)
                             (unify1 `(applies ,(named-clause-rule clause) ,c) subgoal))))
              (if (not subs2)
                  ; fail
                  (begin 
;                    (if *debug* (begin (display "unification failed")
;                                       (newline))) 
                    #f)
                  ; succeed
                  (begin 
                    (let ((arg-id (gensym 'a)))
;                      (if *debug*
;                          (begin (display "argument constructed by rule-generator:")
;                                 (newline)
;                                 (display "argument-id: ")
;                                 (display arg-id)
;                                 (newline) 
;                                 (display "argument-scheme: ")
;                                 (display (string-append (symbol->string (named-clause-rule clause))
;                                                         (symbol->string (named-clause-id clause))
;                                                         ))
;                                 (newline)
;                                 (display "argument-conclusion: ")
;                                 (display (statement-atom (condition-statement subgoal)))
;                                 (newline)
;                                 (display "argument-premises: ")
;                                 (display (append (map statement->premise (named-clause-clause clause))
;                                                       (rule-critical-questions (named-clause-rule clause) qs subgoal (named-clause-strict clause))))
;                                 (newline)
;                                 (newline)
;                                 ;(printf "unification succeeded~%")
;                                 ))
                      (as:make-response 
                       ; (statement-atom (condition-statement subgoal)) ; the affected statement, the conclusion of the argument
                       subs2 
                       (argument:make-argument arg-id ; id
                                               ; applicable
                                               #f
                                               ; weight
                                               argument:default-weight
                                               ; direction
                                               (match subgoal
                                                 (('not _) 'con)
                                                 (_ 'pro))
                                               ; conclusion:
                                               (statement-atom (condition-statement subgoal))
                                               ; premises:
                                               (append (map statement->premise (named-clause-clause clause))
                                                       (rule-critical-questions (named-clause-rule clause) qs subgoal (named-clause-strict clause)))
                                               ; scheme:
                                               (string-append (symbol->string (named-clause-rule clause))
                                                              (symbol->string (named-clause-id clause))
                                                              )
                                               )))))))
          
          (filter as:response? (map apply-for-conclusion (named-clause-head clause))))
        
        (list->stream (flatmap (lambda (clause) 
                                 (if *debug* (printf "applying clause ~a~a~%" (named-clause-rule clause) (named-clause-id clause)))
                                 (apply-clause clause))
                               (map rename-clause-variables (get-clauses args rb subgoal subs)))))))
  ; end of generate-arguments-from-rules
    
  
  (define (rule->datum r)
    (define (clause->datum c)
      (if (< 1 (length c))
          `(and ,@c) 
          (car c)))
    (let ((tag (if (rule-strict r) 'rule* 'rule)))
      (cond ((null? (rule-body r))
             `(,tag ,(rule-id r) ,@(rule-head r)))
            (else `(,tag ,(rule-id r) 
                         (if ,(if (< 1 (length (rule-body r)))
                                  `(or ,@(map clause->datum (rule-body r)))
                                  (clause->datum (car (rule-body r))))
                             ,(if (< 1 (length (rule-head r)))
                                  `(and ,@(rule-head r))
                                  (car (rule-head r)))))))))
                          
  ; rulebase->datum: rulebase -> datum
  (define (rulebase->datum rb)
    (map rule->datum (%rulebase-rules rb)))
  
  ) ; end of rules module