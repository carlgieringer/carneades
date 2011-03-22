;;; Copyright © 2010 Fraunhofer Gesellschaft 
;;; Licensed under the EUPL V.1.1

(ns carneades.editor.utils.core
  (:use clojure.contrib.monads))

(defmacro m-let [binding & body]
  "allows multiple binding to occur, m-let stops on the first binding
   that is assigned nil or, if no binding is nil, executes body"
  (let [body (cons 'do body)]
    `(domonad maybe-m ~binding ~body)))

(defmacro with-out-file [pathname & body]
  `(with-open [stream# (java.io.FileWriter. ~pathname)]
     (binding [*out* stream#
               *err* stream#]
       ~@body)))