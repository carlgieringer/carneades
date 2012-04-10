;;; Copyright (c) 2012 Fraunhofer Gesellschaft
;;; Licensed under the EUPL V.1.1

(ns 
    ^{:doc "A version of Carneades Argument Evaluation Structures 
            which can handle cycles, via a mapping to Dung Argumentation Frameworks."}
  
  carneades.engine.caes3
  
  (:use clojure.pprint
        clojure.set
        carneades.engine.statement
        carneades.engine.argument
        carneades.engine.dung
        carneades.engine.argument-graph
        carneades.engine.argument-evaluation
        carneades.engine.search))


(defrecord State
    [goals           ; list of literals, id or '(not id), where the id is a statement node id
     arguments       ; set of argument node ids
     assumptions])   ; set of literals

(defn- goal-state?
  "state -> boolean"
  [s]
  (empty? (:goals s)))

(def- transitions
  "argument-graph -> state -> seq of state"
  [ag]
  (fn [s]
    (if (empty? (:goals s))
      []
      (let [goal (peek (:goals s))
            sn (get (:statement-nodes ag) (literal-atom goal))
            pop-goal (fn [] [(assoc s :goals (pop (:goals s)))])
            assume (fn [literal]
                     (if (contains? (:assumptions s) (literal-complement literal))
                       [] ; inconsistent set of assumptions, dead-end
                                        ; otherwise pop the goal and add the literal to the set of assumptions
                       [(assoc (pop-goal) :assumptions (conj (:assumptions s) literal))]))]                     

        (cond (and (:weight sn)
                   (if (literal-pos? goal)
                     (= (:weight sn) 1.0)   ; positive premise is accepted
                     (= (:weight sn) 0.0))) ; negative premise is accepted
              (pop-goal),

              (and (:weight sn)
                   (if (literal-positive? goal)
                     (>= (:weight sn) 0.75)    ; positive premise is assumable
                     (<= (:weight sn) 0.25)))  ; negative premise is assumable
              (assume goal)

              :else ; apply the arguments pro the goal
              (reduce (fn [v arg]
                        (if (contains? (:arguments s) arg) ; support cycle
                          v
                          (let [an (get (:argument-nodes ag) arg)]
                            (conj v
                                  (assoc s
                                    :arguments (conj (:arguments s) arg)
                                    :goals (concat (map premise-literal (:premises an))  ; depth-first
                                                   (pop (:goals s))))))))
                      []
                      (if (literal-pos? goal) (:pro sn) (:con sn))))))))              

(defrecord Position
    [id              ; symbol
     argument        ; argument node id of the last link
     subargs         ; set of argument node ids, including the last link
     assumptions])   ; map from statement node id to boolean

(defn- position-map
  "argument-graph ->  map from argument node ids to a vector of positions"
  [ag]
  (let [argument-node-positions
        (fn [pm an]
          (let [p (struct problem (State. (map premise-literal (:premises an)) ; goal literals
                                          #{(:id an)}                          ; argument node ids 
                                          #{})                                 ; assumptions
                          (transitions ag)
                          goal-state?)]
            (assoc pm
              (:id an)
              (map (fn [s] (Position. (gensym "position-")
                                      (:id an) ; id of the last link argument node
                                      (:arguments s)
                                      (:assumptions s)))
                   (search p depth-first)))))]                                        
    (reduce argument-node-positions
            {}
            (vals (:argument-nodes ag)))))

(defn- undercuts?
  "argument-graph position-id position-id -> boolean
   Returns true if position p1 undercuts some subargument of position p2.
   Strict arguments cannot be undercut."
  [ag p1 p2]
  (some (fn [arg]
          (let [an1 (get (:argument-nodes ag) (:argument p1))
                c1 (:atom (get (:statement-nodes ag) (:conclusion an1)))]
            (and (not (:strict (get (:argument-nodes ag) arg)))
                 (:pro an1)
                 (= c1 `(~'undercut ~arg)))))
        (:subargs p2)))

(defn- rebuts?
  "argument-graph position-id position-id -> boolean
   Returns true if position p1 rebuts position p2.
   Only the last link arguments of the positions are compared.
   Strict arguments cannot be rebutted."
  [ag p1 p2]
  (let [an1 (get (:argument-nodes ag) (:argument p1)),
        an2 (get (:argument-nodes ag) (:argument p2)),
        sn  (get (:statement-nodes ag) (:conclusion an1))
        alpha 0.5 ; minimum weight of pro for :cce and :brd
        beta 0.3  ; minimum difference between pro and con for :cce
        gamma 0.2] ; maximum weight for con for :brd
    (and (not (:strict an2))
         (= (:conclusion an1) (:conclusion an2))
         (not (= (:pro an1) (:pro an2))) ; one argument is pro and the other con
         (case (:standard sn)
	       :dv true
               ;; with :pe the con argument need only be >= the pro arg to defeat it.
               ;; the pro argument is also defeated if either arg has no weight
	       :pe (or (nil? (:weight an1)) 
		       (nil? (:weight an2))
		       (>= (:weight an1) (:weight an2)))
               ;; with :cce the con arg defeats the pro arg unless pro's weight
               ;; is >= than alpha the difference between pro and con is >= gamma
	       :cce (or (nil? (:weight an1))
			(nil? (:weight an2))
			(>= (:weight an1) (:weight an2))
			(< (:weight an2) alpha)
			(< (- (:weight an2) (:weight an1)) beta))
	       :brd (or (nil? (:weight an1))
			(nil? (:weight an2))
			(>= (:weight an1) (:weight an2))
			(< (:weight an2) alpha)
			(< (- (:weight an2) (:weight an1)) beta)
			(>= (:weight an1) gamma))))))

(defn- undermines?
  "argument-graph position-id position-id -> boolean
   Returns true if position p1 undermines position p2."
  [ag p1 p2]
  (let [an1 (get (:argument-nodes ag) (:argument p1))
        sn1  (get (:statement-nodes ag) (:conclusion an1))
        c1 (if (:pro an1) (:id sn1) (literal-complement (:id sn1)))]
    (some (fn [lit] (= (literal-complement lit) c1))
          (:assumptions p2))))

;; START HERE

(defn- attackers
  "argument-graph position-id (set-of position-ids) -> set of argument ids
   Returns the subset of the positions which attack the given position."
  [ag p1 positions]
  (reduce (fn [s p2] 
            (if (or (undercuts? ag p2 p1)
                    (rebuts? ag p2 p1)
                    (undermines? ag p2 p1))
              (conj s p2)
              s))
          #{}
	  positions))

(defn position-map-to-argumentation-framework
  "argument-graph position-map -> argumentation-framework
   Constructs a Dung argumentation framework from a position map.
   Positions play the role of arguments in the framework."
  [ag pm]
  (let [args (map :id (filter (fn [an] (applicable? ag an)) 
                              (vals (:argument-nodes ag)))),
        attacks (reduce (fn [m arg] (assoc m arg (attackers ag arg args)))
			{}
			args)]
    (make-argumentation-framework args attacks)))

(defn- initialize-statement-values
  "argument-graph -> argument-graph
   Sets the initial value of statements. These values
   can be overridden when the arguments are evaluated."
  [ag]
  (reduce (fn [ag2 sn]
            (update-statement-node 
              	     ag2
              	     sn 
              	     :value 
              	     (when (:weight sn) 
                      	    (cond 
                             	      (>= (:weight sn) 0.75) 1.0
                             	      (<= (:weight sn) 0.25) 0.0
                             	     :else nil)))) 
          ag
          (vals (:statement-nodes ag))))

(defn- evaluate-grounded
  "argument-graph -> argument-graph"
  [ag]
  (let [af (argument-graph-to-framework ag)
        l (grounded-labelling af)]
    (reduce (fn [ag2 arg-id]
	      (let [an (get (:argument-nodes ag2) arg-id)  
		  sn (get (:statement-nodes ag2) (:conclusion an))
                       update-conclusion (fn [ag sn] 
                                           ; don't change value of facts
                                           (cond (= (:weight sn) 0.0)  ag
                                                 (= (:weight sn) 1.0)  ag
                                                 (= :in (get l arg-id)) 
                                                     (update-statement-node ag sn 
                                                         :value (if (:pro an) 1.0 0.0))
                                                 :else ag))]
		(-> ag2
		    (update-argument-node an 
                            :value (case (get l arg-id)
			       :in 1.0
			       :out 0.0
			       :else 0.5))
		    (update-conclusion sn))))	         
	    (initialize-statement-values ag)
	    (keys l))))

;; The caes-grounded evaluator uses grounded semantics
(def caes-grounded 
     (reify ArgumentEvaluator
    	    (evaluate [this ag] (evaluate-grounded (reset-node-values ag)))
    	    (label [this node] (node-standard-label node))))





