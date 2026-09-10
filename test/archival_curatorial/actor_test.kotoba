(ns archival-curatorial.actor-test
  (:require [clojure.test :refer [deftest is testing]]
            [archival-curatorial.actor :as actor]
            [archival-curatorial.store :as store]))

(defn- fresh-store []
  (let [st (store/mem-store)]
    (store/register-item! st {:item-id "item-1" :name "Ming Vase"})
    st))

(deftest commits-a-clean-low-risk-request
  (let [st (fresh-store)
        graph (actor/build-graph {:store st})
        request {:item-id "item-1" :op :catalogue :stake :low}
        result (actor/run-request! graph request {} "thread-1")]
    (is (= :done (:status result)))
    (is (some? (get-in result [:state :record])))
    (is (= 1 (count (store/records-of st "item-1"))))))

(deftest holds-on-unregistered-item-without-committing
  (let [st (fresh-store)
        graph (actor/build-graph {:store st})
        request {:item-id "no-such-item" :op :catalogue :stake :low}
        result (actor/run-request! graph request {} "thread-2")]
    (is (= :done (:status result)))
    (is (nil? (get-in result [:state :record])))
    (is (empty? (store/records-of st "no-such-item")))
    (is (= :hold (:disposition (:state result))))))

(deftest interrupts-then-commits-on-human-approval
  (let [st (fresh-store)
        graph (actor/build-graph {:store st})
        ;; deaccession always escalates (governor invariant)
        request {:item-id "item-1" :op :deaccession :stake :high}
        interrupted (actor/run-request! graph request {} "thread-3")]
    (is (= :interrupted (:status interrupted)))
    (is (empty? (store/records-of st "item-1")))
    (let [resumed (actor/approve! graph "thread-3")]
      (is (= :done (:status resumed)))
      (is (some? (get-in resumed [:state :record])))
      (is (= 1 (count (store/records-of st "item-1")))))))
