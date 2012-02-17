;;; Copyright ? 2010 Fraunhofer Gesellschaft 
;;; Licensed under the EUPL V.1.1

(ns carneades.examples.copyright-policies
  (:use carneades.engine.dublin-core
        carneades.engine.scheme))

;; This example illustrates the use of schemes for policy modeling, using
;; policies proposed in response to the EU Green Paper on Copyright in the Knowledge Economy.

(def copyright-policies 
  (make-theory
   :header 
   (make-metadata :title "Copyright Policies"
                  :description {:en "TO DO"})
   
   :language
   {'announcement (make-individual :symbol 'announcement :text {:en "Announcement" :de "Bekanntmachung"})
    'commercial (make-individual :symbol 'commercial :text {:en "Commerical Use"})
    'purpose (make-individual :symbol 'purpose :text {:en "Purpose"})
    'name  (make-individual :symbol 'name :text {:en "Name"})
    'non-commercial (make-individual :symbol 'non-commercial :text {:en "Non-commerical Use"})
    'none (make-individual :symbol 'none :text {:en "None"})
    'professional (make-individual :symbol 'professional :text {:en "Professional Documented Search"})
    'search (make-individual :symbol 'search :text {:en "Search"})
    'standard (make-individual :symbol 'standard :text {:en "Standard DocumentedSearch"})

    'may-publish
    (make-predicate
     :symbol 'may-publish
     :arity 2
     :forms {:en (make-form :positive "%s may publish the work %s."
                            :negative "%s may not publish the work %s."
                            :question "May %s publish the work %s?")})

    
    'person
    (make-predicate
     :symbol 'person
     :arity 1
     :forms {:en (make-form :positive "%s is a person."
                            :negative "%s is not a person."
                            :question "Is %s a person?")
             
             :de (make-form :positive "%s ist ein Rechtsperson."
                            :negative "% ist nicht ein Rechtsperson."
                            :question "Ist %s ein Rechtsperson?")}
     :category 'name  ; was a map !!
     :hint {:en "Please provide an identifier for the person, such as P1."}
     :widget "text"   ; was "type".  Should symbols be used to name the widget types, instead of strings?
     :followups ['work])   ; would "next" be a better name?

    'work
    (make-predicate
     :symbol 'work
     :arity 1
     :forms {:de (make-form :positive "%s ist ein Werk."
                            :negative "%s is nicht ein Werk."
                            :question "Ist %s ein Werk?")
             :en (make-form :positive "%s is a work."
                            :negative "%s is not a work."
                            :question "Is %s a work?")}
     :widget "text"
     :category 'work)  ;  should this be the "name" category?  See above.
    
    'type-of-use
    (make-predicate
     :symbol 'type-of-use
     :arity 3
     :forms {:de (make-form :positive "%s nutzt %s für folgender Zwecken: %S."
                            :negative "%s nutzt %s nicht für folgender Zwecken: %s."
                            :question "Nutzt %s den Werk %s für folgender Zwecken: %s?")
             :en (make-form :positive "%s uses %s for the following purposes: %s."
                            :negative "%s does not use %s for the following purposes: %s."
                            :question "Does %s use %s for the following purposes: %s?")}
     :hint {:en "Will the work be used for commercial or non-commercial purposes?"}
     :answers ['commercial, 'non-commercial]
     :category 'purpose
     :widget "select")

   'search-type
   (make-predicate
    :symbol 'search-type
    :arity 1
    :forms {:de (make-form :positive "Art der Suche nach dem Urheber: %s."
                           :negative "Die Suche nach dem Urheber war nicht: %s."
                           :question "Welche Art der Suche nachdem Urheber erfolgte?")
            :en (make-form :positive "Type of search for the copyright owner: %s."
                           :negative "The type of search for the copyright owner was not: %s."
                           :question "What type of search for the copyright owner was performed?")}
    :hint {:en "Please select the type of search for the copyright owner performed."}
    :category 'search
    :answers ['standard, 'professional, 'none]
    :widget "select"
    :followups ['announcement])

   'announcement
   (make-predicate
    :symbol 'announcement
    :arity 0
    :forms {:en (make-form :positive "The search was publically announced."
                           :negative "The search was not publically announced."
                           :question "Was the search publically announced?")
            :de (make-form :positive "Es gab eine öffentliche Bekanntmachung der Suche."
                           :negative "Es gab keine öffentliche Bekanntmachung der Suche."
                           :question "Erfolgte eine öffentliche Bekanntmachung der Suche?")}
    :category 'announcement
    :widget "checkbox")  ; Shouldn't checkboxes, instead of radio buttons, be used to query boolean values of propositions?
     
    'valid
    (make-predicate
     :symbol 'valid
     :arity 1
     :form {:en (make-form :positive "%s is valid law."
                           :negative "%s is not valid law."
                           :question "Is %s valid law?")
            :de (make-form :positive "%s is gültiges Recht."
                           :negative "%s ist nicht gültiges Recht."
                           :question "Ist %s gültiges Recht?")})}

   :sections
   [(make-section
     :header (make-metadata :title "Question 12. Cross-Border Aspects of Orphaned Works"
                            :description {:en "Question 12 of the Green Paper on Copyright in the Knowledge Economy [@GreenPaper, p. 12] asks:

> (12) How should the cross-border aspects of the orphan works issue be tackled to ensure EU-wide recognition of
> the solutions adopted in different Member States?

This arguments pro and con the policy proposals for this issue can be browsed in the [argument map]().

    To do: add the URL to this issue in the map in the link above.
"})


     
     ;; one section below for each policy proposed for this issue as well as for current polices on this issue.

     :sections  
     [(make-section
       :header (make-metadata :title "Orphaned Works Policy Proposed by the Aktionsbündnisses ‟Urheberrecht für Bildung und Wissenschaft”"
                              :description {:en "The German “Action Alliance” on copyright for education and science proposes the following
policies for handling orphaned works [@Aktionsbündnis, pp. 6-7]."})


       :schemes
       [(make-scheme                            
         :id 'AB-52c-1-a
         :header (make-metadata :title "§ 52c (1) (a)"
                                :description {:de "
> (1) Öffentliche Zugänglichmachung für nicht-gewerbliche und private Zwecke, insbesondere
> durch Nutzer für Zwecke der Archivierung und für Forschung und Ausbildung 
>     (a)  Zulässig  ist  die  öffentliche  Zugänglichmachung  von  Werken, deren Urheber oder Rechteinhaber 
> nach einer dokumentierten Standardsuche [alternativ: einer zeitlich auf 30 Tage öffentlichen 
> Bekanntmachung] nicht ermittelt werden können.
"
         :conclusion '(may-publish ?P ?W)
         :premises [(make-premise :statement '(person ?P))
                    (make-premise :statement '(work ?W))
                    (make-premise :statement '(type-of-use ?P ?W non-commercial))
                    (make-premise :statement '(search-type standard))
                    (make-premise :statement '(valid AB-52c-1-a)) ])

        
        (make-scheme
         :id 'AB-52c-2-a
         :header (make-metadata :title "§ 52c (2) (a)"
                                :description {:de "
> (2) Öffentliche Zugänglichmachung für gewerbliche Zwecke 
>    (a)  Zulässig  ist  die  öffentliche  Zugänglichmachung  von  Werken, deren Urheber oder Rechteinhaber 
> nach einer angemessenen professionellen und dokumentierten Suche und einer öffentlichen 
> Bekanntmachung nicht ermittelt werden können.
"
         :conclusion '(may-publish ?P ?W)
         :premises [(make-premise :statement '(person ?P))
                    (make-premise :statement '(work ?W))
                    (make-premise :statement '(type-of-use ?P ?W commercial))
                    (make-premise :statement '(search-type professional))
                    (make-premise :statement '(announcement))
                    (make-premise :statement '(valid AB-52c-2-c))])])])]))