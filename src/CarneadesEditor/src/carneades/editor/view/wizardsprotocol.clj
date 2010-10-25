;;; Copyright © 2010 Fraunhofer Gesellschaft 
;;; Licensed under the EUPL V.1.1

(ns carneades.editor.view.wizardsprotocol)

(defrecord StatementItem [stmt formatted] Object
  (toString
   [this]
   formatted))

(defprotocol SwingGoalWizard
  (set-main-issue [this mainissue])
  (get-proponent-panel [this])
  (get-abduction-panel [this])
  (get-positions-panel [this])
  (reset-position [this])
  (display-position [this position posidx nbpos statement-formatted])
  (display-statements [this statements statement-formatted])
  (set-first-position-button-listener [this f args])
  (set-last-position-button-listener [this f args])
  (set-previous-position-button-listener [this f args])
  (set-next-position-button-listener [this f args]))
