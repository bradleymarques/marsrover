(ns marsrover.core
  (:gen-class)
  (:require [clojure
             [set :as set]
             [string :as str]]
            [clojure.spec.alpha :as s]
            [clojure.tools.cli :refer [parse-opts]]))

;; Validation for input

(def instruction-regex #"^[MLR]*")
(def direction-regex #"^[NESW]")

(s/def ::instruction-tape (s/and string? #(re-matches instruction-regex %)))

(s/def ::x int?)
(s/def ::y int?)
(s/def ::direction (s/and string? #(re-matches direction-regex %)))

(s/def ::grid (s/keys :req-un [::x ::y]))

(s/def ::position (s/keys :req-un [::x ::y ::direction]))

(s/def ::input-map (s/keys :req-un [::grid ::position ::instruction-tape]))

(defn valid-instructions? [instructions]
  (if (s/valid? ::input-map instructions) instructions
      (throw (Exception. (str "Instruction Validation Error " (s/explain ::input-map instructions))))))

;; Instruction Parsing

(defn instructString->Map [instructions]
  "Takes raw instruction string and creates instruction map"
  (-> (zipmap [:grid :position :instruction-tape]
              (str/split instructions #"\n"))
      (update :grid (fn [x] (zipmap [:x :y] (map read-string (str/split x #" ")))))
      (update :position (fn [x] (zipmap [:x :y :direction] (str/split x #" "))))
      (update-in [:position :x] read-string)
      (update-in [:position :y] read-string)))

(defn read-instruction-set
  "IO for instructions"
  ([]
   (slurp "resources/instructions.txt"))
  ([instructions]
   (slurp instructions)))

(def direction-map {0 "N" 1 "E" 2 "S" 3 "W"})

(defn move-by-direction [pos]
  (let [direction (:direction pos)]
    (cond (= direction "N") (update pos :y inc)
          (= direction "E") (update pos :x inc)
          (= direction "S") (update pos :y dec)
          (= direction "W") (update pos :x dec))))

(defn change-direction [instruct pos]
  (let [direction-val (get (set/map-invert direction-map) (:direction pos))]
    (assoc pos :direction
           (get direction-map (mod (if (= instruct \L) (dec direction-val) (inc direction-val)) 4)))))

(defn parse-instruction [instruct pos]
  (if (= instruct \M) (move-by-direction pos)
      (change-direction instruct pos)))

(defn out-of-boundary? [grid pos]
  (if (or (neg?  (:x pos))
          (neg?  (:y pos))
          (> (:x pos) (:x grid))
          (> (:y pos) (:y grid))) (throw (Exception. (str "Instructions will move rover out of grid"))) false))

(defn move-rover [{:keys [position instruction-tape]}]
  "Runs through instruction set and moves rover, sending back it's final co-ordinates"
  (if (empty? instruction-tape) (println (str "Final co-ordinates: " (:x position) " " (:y position) " " (:direction position)))
      (move-rover {:position (parse-instruction (first instruction-tape) position) :instruction-tape (rest instruction-tape)})))

(defn grid-collisions? [{:keys [grid position instruction-tape]}]
  "Runs through the instruction set to see if they will force the rover to move out of bounds"
  (if (empty? instruction-tape) (out-of-boundary? grid position)
      (if-not  (out-of-boundary? grid position)
        (grid-collisions? {:grid grid :position (parse-instruction (first instruction-tape) position) :instruction-tape (rest instruction-tape)})
        (throw (Exception. (str "Instructions will move rover out of grid"))))))

(defn run [instructions]
  (if (grid-collisions? instructions) (throw (Exception. "Instructions force rover out of grid."))
      (move-rover instructions)))

;; Application Execution

(def cli-options
  [["-f" "--file"  "File location of input instructions, defaults to resources/instructions.txt"
    :default "resources/instructions.txt"]
   ["-h" "--help"]])

(defn -main [& args]
  (let [cli-options (parse-opts args cli-options)
        input       (if-not (empty? (-> cli-options :arguments))
                      (first (-> cli-options :arguments))
                      (-> cli-options :options :file))]
    (if (or (true? (-> cli-options :options :help))
            (:errors cli-options))
      (println (str (str/join " " (concat (some-> (:errors cli-options)) " ")) "See the help options below for available flags \n" (:summary cli-options)))
      (try
        (-> (read-instruction-set input)
            instructString->Map
            valid-instructions?
            run)
        (catch Exception e (println "Error: " (.getMessage e)))))))
