;;; Copyright (c) 2012 Fraunhofer Gesellschaft
;;; Licensed under the EUPL V.1.1

(ns catb.repl
  (:require
    [clojure.browser.repl :as repl]))

(defn ^:export connect []
  (repl/connect "http://localhost:9000/repl"))
