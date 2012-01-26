;;; Copyright (c) 2010-2011 Fraunhofer Gesellschaft
;;; Licensed under the EUPL V.1.1

(ns carneades.maps.lacij-export
  (:use clojure.pprint
        carneades.engine.argument-graph
        carneades.engine.argument-evaluation
        carneades.engine.caes
        lacij.graph.svg.graph
        lacij.graph.core
        lacij.layouts.layout
        lacij.view.core
        lacij.opt.annealing
        carneades.engine.statement
        [carneades.maps lacij-params format-statement subset-ag])
  (:require [analemma.xml :as xml]
            [analemma.svg :as svg]
            [clojure.string :as s]
            [tikkba.utils.dom :as dom])
  (:import (java.io InputStreamReader ByteArrayInputStream
                    ByteArrayOutputStream)))

(defn gen-arg-id
  [arg]
  (keyword (str "a" (str (Math/abs (hash arg))))))

(defn gen-stmt-id
  ;; Please think twice before making changes to this function!
  ;; some ids with special characters could not be accepted by batik
  ;; the generation of the layout would still works but with
  ;; a strange output
  [stmt]
  (let [stmtstr (literal->str (map->statement stmt))
        size 50
        s (if (> (count stmtstr) size)
            (subs stmtstr 0 size)
            stmtstr)
        s (s/replace s #"\W" "_")]
    (keyword (str "s_" s "_" (str (Math/abs (hash stmt)))))))

(defn pick-stmt-params
  [ag stmt params]
  (cond  (in-node? stmt) (:stmt-in-params params)
         (out-node? stmt) (:stmt-out-params params)
         :else (:stmt-params params)))

(defn pick-arg-params
  [ag arg params]
  (cond (and (in-node? arg) (:pro arg))
        (:arg-pro-applicable-params params)
        
        (and (in-node? arg) (:con arg))
        (:arg-con-applicable-params params)
        
        (:pro arg)
        (:arg-pro-notapplicable-params params)
        
        :else
        (:arg-con-notapplicable-params params)))

(defn add-statement
  [svgmap stmt ag stmt-str params]
  ;; TODO take the lang as a parameter
  (let [stmtstr (stmt-to-str stmt stmt-str)
        id (gen-stmt-id stmt)
        stmt-params (merge (pick-stmt-params ag stmt params)
                           {:label "" :x 0 :y 0 :shape :rect})
        svgmap (add-node-kv svgmap id stmt-params)
        svgmap (add-label-kv svgmap id
                          (trunk-line stmtstr)
                          (:stmtlabel-params params))]
    svgmap))

(defn add-arg-decorator
  [svgmap arg argid]
  {:pre [(argument-node? arg)]}
  (if (:pro arg)
    (add-decorator svgmap argid (make-plusdecorator))
    (add-decorator svgmap argid (make-minusdecorator))))

(defn add-argument-node
  [svgmap arg ag params]
  (let [arg-params (pick-arg-params ag arg params)
        argid (gen-arg-id arg)
        svgmap (add-node-kv svgmap argid arg-params)
        svgmap (add-arg-decorator svgmap arg argid)]
    svgmap))

(defn add-conclusion-edge
  [svgmap conclusion arg]
  (let [id (geneid)
        conclusionid (gen-stmt-id conclusion)
        argid (gen-arg-id arg)]
    (if (:pro arg)
      (apply add-edge svgmap id argid conclusionid pro-argument-params)
      (apply add-edge svgmap id argid conclusionid con-argument-params))))

(defn add-premise-edge
  [svgmap ag premise arg]
  (let [edgeid (geneid)
        argid (gen-arg-id arg)
        stmtid (gen-stmt-id (get (:statement-nodes ag) (:statement premise)))]
    (if (:positive premise)
      (apply add-edge svgmap edgeid stmtid argid premise-params)
      (apply add-edge svgmap edgeid stmtid argid neg-premise-params))))

(defn add-premises-edges
  [svgmap ag premises arg]
  (reduce (fn [svgmap premise]
            (add-premise-edge svgmap ag premise arg))
          svgmap premises))

(defn add-argument
  [svgmap arg ag params]
  (let [conclusion (get (:statement-nodes ag) (literal-atom (:conclusion arg)))
        ; premises (map #(get (:statement-nodes ag) (:statement %)) (:premises arg))
        premises (:premises arg)
        svgmap (add-argument-node svgmap arg ag params)
        svgmap (add-conclusion-edge svgmap conclusion arg)
        svgmap (add-premises-edges svgmap ag premises arg)]
    svgmap))

(defn add-entities
  [svgmap ag stmt-str params]
  (let [statements (vals (:statement-nodes ag))
        arguments (vals (:argument-nodes ag))]
    (let [svgmap (reduce (fn [svgmap stmt]
                           (add-statement svgmap stmt ag stmt-str params))
                         svgmap statements)
          svgmap (reduce (fn [svgmap arg]
                           (add-argument svgmap arg ag params))
                         svgmap arguments)]
      svgmap)))

(defn add-markers
  [map]
  (reduce (fn [map marker]
            (add-def map marker))
          map
          default-markers))

(defn export-ag-helper
  [ag stmt-str options]
  (let [; ag (evaluate carneades-evaluator ag)
        width (get options :width 1280)
        height (get options :height 1024)
        layouttype (get options :layout :hierarchical)
        svgmap (create-graph :width width :height height)
        svgmap (add-markers svgmap)
        params (merge default-params options)
        ag (apply subset-ag ag options)
        svgmap (add-entities svgmap ag stmt-str params)
        optionsseq (flatten (seq options))
        svgmap (time (apply layout svgmap layouttype optionsseq))
        svgmap (build svgmap)]
    svgmap))

(defn export-ag
  "Exports an argument graph to a SVG file.

   Options are :treeify, :full-duplication, :depth,
   :layout and all options supported by the layout"
  [ag stmt-str filename options]
  (let [map (export-ag-helper ag stmt-str options)]
    (export map filename :indent "yes")))

(defn export-ag-str
  "Exports an argument graph to a string.

   Options are :treeify, :full-duplication, :depth,
   :layout and all options supported by the layout"
  [ag stmt-str options]
  (let [map (export-ag-helper ag stmt-str options)
        os (ByteArrayOutputStream.)
        v (view map)]
    (dom/spit-xml os v :indent "yes")
    (slurp
     (InputStreamReader.
      (ByteArrayInputStream. (.toByteArray os))
      "UTF8"))))
