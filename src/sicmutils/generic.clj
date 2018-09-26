;
; Copyright © 2017 Colin Smith.
; This work is based on the Scmutils system of MIT/GNU Scheme:
; Copyright © 2002 Massachusetts Institute of Technology
;
; This is free software;  you can redistribute it and/or modify
; it under the terms of the GNU General Public License as published by
; the Free Software Foundation; either version 3 of the License, or (at
; your option) any later version.
;
; This software is distributed in the hope that it will be useful, but
; WITHOUT ANY WARRANTY; without even the implied warranty of
; MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
; General Public License for more details.
;
; You should have received a copy of the GNU General Public License
; along with this code; if not, see <http://www.gnu.org/licenses/>.
;

(ns sicmutils.generic
  (:refer-clojure :exclude [+ - * / zero?])
  (:require [sicmutils
             [value :as v]])
  (:import (clojure.lang Keyword)))

;;; classifiers

(defmacro ^:private def-generic-function
  "Defines a mutlifn using the provided symbol. Arranges for the multifn
  to answer the :arity message, reporting either [:exactly a] or
  [:between a b], according to the arguments given."
  [f a & b]
  (let [arity (if b `[:between ~a ~@b] [:exactly a])
        docstring (str "generic " f)]
    `(do
       (defmulti ~f ~docstring v/argument-kind)
       (defmethod ~f :arity [k#] ~arity)
       (defmethod ~f :name [k#] '~f))))

(def-generic-function add 2)
(def-generic-function mul 2)
(def-generic-function sub 2)
(def-generic-function div 2)

(def-generic-function cos 1)
(def-generic-function sin 1)
(def-generic-function tan 1)
(def-generic-function asin 1)
(def-generic-function acos 1)
(def-generic-function atan 1 2)
(def-generic-function cross-product 2)
(def-generic-function negative? 1)
(def-generic-function transpose 1)
(def-generic-function magnitude 1)
(def-generic-function determinant 1)

(def-generic-function invert 1)
(def-generic-function negate 1)
(def-generic-function square 1)
(def-generic-function cube 1)
(def-generic-function exp 1)
(def-generic-function log 1)
(def-generic-function abs 1)
(def-generic-function sqrt 1)

(def-generic-function exact-divide 2)
(def-generic-function quotient 2)
(def-generic-function remainder 2)
(def-generic-function expt 2)
(def-generic-function gcd 2)

(def-generic-function zero-like 1)
(defmethod zero-like :default [_] 0)

(def-generic-function one-like 1)
(defmethod one-like :default [_] 1)

(def-generic-function zero? 1)
(defmethod zero? :default [_] false)

(def-generic-function Lie-derivative 1)

(defmulti partial-derivative v/argument-kind)
(defmulti simplify v/argument-kind)

(defmulti numerical? v/argument-kind)
(defmethod numerical? :default [_] false)

(defmulti exact? v/argument-kind)
(defmethod exact? :default [_] false)

(defmulti freeze v/argument-kind)

(defn abstract-quantity?
  [x]
  (= (:type x) :sicmutils.expression/numerical-expression))

(defn ^:private bin+ [a b]
  (cond (zero? a) b
        (zero? b) a
        :else (add a b)))

(defn + [& args]
  (reduce bin+ 0 args))

(defn ^:private bin- [a b]
  (cond (zero? b) a
        (zero? a) (negate b)
        :else (sub a b)))

(defn - [& args]
  (cond (nil? args) 0
        (nil? (next args)) (negate (first args))
        :else (bin- (first args) (reduce bin+ (next args)))))

(defn ^:private bin* [a b]
  (cond (v/unity? a) b
        (v/unity? b) a
        :else (mul a b)))

;;; In bin* we test for exact (numerical) zero
;;; because it is possible to produce a wrong-type
;;; zero here, as follows:
;;;
;;;               |0|             |0|
;;;       |a b c| |0|   |0|       |0|
;;;       |d e f| |0| = |0|, not  |0|
;;;
;;; We are less worried about the g/zero? below,
;;; because any invertible matrix is square.

(defn * [& args]
  (reduce bin* 1 args))

(defn ^:private bin-div [a b]
  (cond (v/unity? b) a
        :else (div a b)))

(defn / [& args]
  (cond (nil? args) 1
        (nil? (next args)) (invert (first args))
        :else (bin-div (first args) (reduce bin* (next args)))))

(def divide /)

;; TODO
;;(v/add-object-symbols! {+ '+ * '* - '- / (symbol "/")})
