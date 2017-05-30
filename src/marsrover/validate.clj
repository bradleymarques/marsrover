(ns marsrover.validate
  (:require [clojure.spec.alpha :as s]))

(def instruction-regex #"^[MLR]*")
(def direction-regex #"^[NESW]")

(s/def ::instruction (s/and string? #(re-matches instruction-regex %)))

(s/def ::x int?)
(s/def ::y int?)
(s/def ::direction (s/and string? #(re-matches direction-regex %)))

(s/def ::grid (s/keys :req [::x ::y]))

(s/def ::start-pos (s/keys :req [::x ::y ::direction]))
