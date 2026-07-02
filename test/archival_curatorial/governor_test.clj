(ns archival-curatorial.governor-test
  (:require [clojure.test :refer [deftest is testing]]
            [archival-curatorial.store :as store]
            [archival-curatorial.governor :as governor]))

(defn- fresh-store []
  (let [st (store/mem-store)]
    (store/register-item! st {:item-id "item-1" :name "Ming Vase"})
    st))

(deftest ok-on-clean-catalogue
  (let [st (fresh-store)
        proposal {:op :catalogue :effect :propose :confidence 0.9 :stake :low}
        v (governor/check {:item-id "item-1"} {} proposal st)]
    (is (:ok? v))
    (is (not (:hard? v)))
    (is (not (:escalate? v)))))

(deftest hard-on-unregistered-item
  (let [st (fresh-store)
        proposal {:op :catalogue :effect :propose :confidence 0.9 :stake :low}
        v (governor/check {:item-id "no-such-item"} {} proposal st)]
    (is (:hard? v))
    (is (some #(= :no-item (:rule %)) (:violations v)))))

(deftest hard-on-no-actuation-violation
  (let [st (fresh-store)
        proposal {:op :catalogue :effect :direct-write :confidence 0.9 :stake :low}
        v (governor/check {:item-id "item-1"} {} proposal st)]
    (is (:hard? v))
    (is (some #(= :no-actuation (:rule %)) (:violations v)))))

(deftest escalates-on-deaccession
  (let [st (fresh-store)
        proposal {:op :deaccession :effect :propose :confidence 0.9 :stake :high}
        v (governor/check {:item-id "item-1"} {} proposal st)]
    (is (:escalate? v))
    (is (not (:hard? v)))))

(deftest escalates-on-restricted-item-release
  (let [st (fresh-store)
        proposal {:op :release-restricted-item :effect :propose :confidence 0.9 :stake :high}
        v (governor/check {:item-id "item-1"} {} proposal st)]
    (is (:escalate? v))
    (is (not (:hard? v)))))

(deftest escalates-on-low-confidence
  (let [st (fresh-store)
        proposal {:op :catalogue :effect :propose :confidence 0.2 :stake :low}
        v (governor/check {:item-id "item-1"} {} proposal st)]
    (is (:escalate? v))
    (is (not (:hard? v)))))

(deftest store-records-and-ledger-append-only
  (let [st (fresh-store)]
    (store/commit-record! st {:item-id "item-1" :op :preserve})
    (store/append-ledger! st {:disposition :commit})
    (is (= 1 (count (store/records-of st "item-1"))))
    (is (= 1 (count (store/ledger st))))))
