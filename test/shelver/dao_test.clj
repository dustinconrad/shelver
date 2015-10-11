(ns shelver.dao-test
  (:require [clojure.test :refer :all]
            [shelver.util :refer :all]
            [shelver.dao :refer :all]))

(deftest test-resolve-pull-pattern
  (testing "Testing no star, no sub patterns"
    (are [entity-ns pattern model expected]
      (= (set expected) (set (resolve-pattern entity-ns pattern model)))

      "e" [:e/a :e/b :e/c] nil [:e/a :e/b :e/c]
      "e" [:e/a :e/b :e/c] {:e 1 :f 2} [:e/a :e/b :e/c]))

  (testing "Testing star no sub patterns or fields"
    (are [entity-ns pattern model expected]
      (= (set expected) (set (resolve-pattern entity-ns pattern model)))

      "e" [*] {:a 1 :c 2} [:e/a :e/c]
      "e" [*] {:z/a 1 :z/c 2} [:e/a :e/c]))

  (testing "Testing star with fields"
    (are [entity-ns pattern model expected]
      (= (set expected) (set (resolve-pattern entity-ns pattern model)))

      "e" [* :e/b] {:z/a 1 :z/c 2} [:e/a :e/b :e/c]))

  (testing "Testing star with sub pattern"
    (are [entity-ns pattern model expected]
      (= (set expected) (set (resolve-pattern entity-ns pattern model)))

      "e" [* {:e/a [*]}] {:a {:d 4 :e 5} :b 2 :c 3} [{:e/a [*]} :e/b :e/c]
      "e" [* {:e/a [*]}] {:b 2 :c 3} [{:e/a [*]} :e/b :e/c]))

  (testing "Testing id"
    (are [entity-ns pattern model expected]
      (= (set expected) (set (resolve-pattern entity-ns pattern model)))

      "e" [*] {:id 1} [:db/id]
      "e" [* {:e/a [*]}] {:b 2 :c 3 :id 4} [{:e/a [*]} :e/b :e/c :db/id])))