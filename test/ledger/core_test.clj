(ns ledger.core-test
  (:require [clojure.test :refer :all]
            [ledger.core :refer :all]))

(deftest test-amt
  (testing "testing valid debit amt fn"
    (let [a (amt "-256")]
      (is (= :debit (:txn a)))
      (is (= 256 (:amt a)))))
  (testing "testing valid credit amt fn"
    (let [a (amt "+556")]
      (is (= :credit (:txn a)))
      (is (= 556 (:amt a)))))
  (testing "testing valid bal update fn"
    (let [a (amt "*756")]
      (is (= :update (:txn a)))
      (is (= 756 (:amt a))))))

(deftest test-info
  (testing "testing info with bal fn"
    (let [i (info "groceries")]
      (is (= (:desc i) "groceries"))
      (is (= (:bal i) nil))))
  (testing "testing info with bal fn"
    (let [i (info "tea bal 460")]
      (is (= (:desc i) "tea"))
      (is (= (:bal i) 460)))
    (let [i (info "test asd 450 bal")]
      (is (= (:desc "test asd")))
      (is (= (:bal i) 450)))))

(deftest test-text->entry
  (testing "parse row"
    (let [txt "-240 meds das bal 540"
          entry (text->entry txt)]
      (is (= 240 (:amt entry)))
      (is (= :debit (:txn entry)))
      (is (= "meds das" (:desc entry)))
      (is (= 540 (:bal entry))))))
