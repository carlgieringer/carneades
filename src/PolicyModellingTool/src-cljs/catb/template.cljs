(ns catb.template
  (:use [jayq.util :only [log clj->js]])
  (:require [catb.icanhaz.core :as ich])
  (:refer-clojure :exclude [get]))

(defn get
  "Returns an ICanHaz template,
variables from the Messages.properties file are automatically passed to the template constructor"
  [template variables]
  (let [entries (keys (js->clj js/jQuery.i18n.map))
        texts (apply hash-map (mapcat (fn [e] [(keyword e) (js/jQuery.i18n.prop e)]) entries))
        augmented (merge texts variables)]
    (ich/get template augmented)))