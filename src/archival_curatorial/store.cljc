(ns archival-curatorial.store
  "SSoT for the ISCO-08 2621 independent archival-and-curatorial sole-
  proprietor actor. Store is a protocol injected into the
  `archival-curatorial.actor` StateGraph — `MemStore` is the default,
  deterministic, zero-dep backend; a Datomic/kotoba-server-backed
  implementation can be swapped in without touching the actor or
  governor (itonami actor pattern, per ADR-2607011000 / CLAUDE.md
  Actors section).

  Domain:

    item     — a registered collection item (:item-id, :name)
    record   — a committed operating record under an item (catalogue
               entry, preservation note, deaccession, restricted-item
               release) — written ONLY via commit-record!, never
               mutated in place
    ledger   — an append-only audit trail of every proposal/verdict/
               disposition, regardless of outcome (commit or hold)")

(defprotocol Store
  (item [s item-id])
  (records-of [s item-id])
  (ledger [s])
  (register-item! [s item])
  (commit-record! [s record])
  (append-ledger! [s fact]))

(defrecord MemStore [a]
  Store
  (item [_ item-id] (get-in @a [:items item-id]))
  (records-of [_ item-id] (filter #(= item-id (:item-id %)) (:records @a)))
  (ledger [_] (:ledger @a))
  (register-item! [s item]
    (swap! a assoc-in [:items (:item-id item)] item) s)
  (commit-record! [s record]
    (swap! a update :records (fnil conj []) record) s)
  (append-ledger! [s fact]
    (swap! a update :ledger (fnil conj []) fact) s))

(defn mem-store
  ([] (mem-store {}))
  ([seed] (->MemStore (atom (merge {:items {} :records [] :ledger []} seed)))))
