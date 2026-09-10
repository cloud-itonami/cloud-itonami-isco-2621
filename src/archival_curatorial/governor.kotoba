(ns archival-curatorial.governor
  "ArchivalCuratorialGovernor — the independent safety/traceability
  layer for the ISCO-08 2621 independent archival-and-curatorial
  actor. Wired as its own `:govern` node in
  `archival-curatorial.actor`'s StateGraph, downstream of `:advise` —
  the Advisor has no notion of item provenance or deaccession/
  restricted-access risk, so this MUST be a separate system able to
  reject a proposal (itonami actor pattern, per ADR-2607011000 /
  CLAUDE.md Actors section).

  `check` is a pure function of (request, context, proposal, store) ->
  verdict; it never mutates the store. The StateGraph's `:decide` node
  routes on the verdict:
    :hard? true                → :hold  (irreversible, no write)
    :escalate? true            → :request-approval (interrupt-before)
    otherwise                  → :commit

  HARD invariants (:hard? true, ALWAYS :hold, never overridable):
    1. item provenance  — the request's item must be registered.
    2. no-actuation       — proposal :effect must be :propose.
  ESCALATION invariants (:escalate? true, ALWAYS human sign-off, per the
  README robotics-premise: deaccessioning an item and releasing a
  restricted collection item for public access always require human
  sign-off):
    3. :op :deaccession.
    4. :op :release-restricted-item.
    5. low confidence (< `confidence-floor`)."
  (:require [archival-curatorial.store :as store]))

(def confidence-floor 0.6)
(def ^:private escalating-ops #{:deaccession :release-restricted-item})

(defn- hard-violations [{:keys [proposal]} item-record]
  (cond-> []
    (nil? item-record)
    (conj {:rule :no-item :detail "未登録 item"})

    (not= :propose (:effect proposal))
    (conj {:rule :no-actuation :detail "effect は :propose のみ許可（直接書込禁止）"})))

(defn check
  "Assess a proposal against `request`/`context`/`proposal` and a
  `store` implementing `archival-curatorial.store/Store`. Returns
  `{:ok? bool :violations [...] :confidence n :hard? bool :escalate? bool}`."
  [request context proposal store]
  (let [item-record (store/item store (:item-id request))
        hard (hard-violations {:proposal proposal} item-record)
        hard? (boolean (seq hard))
        conf (or (:confidence proposal) 0.0)
        low? (< conf confidence-floor)
        risky-op? (contains? escalating-ops (:op proposal))]
    {:ok? (and (not hard?) (not low?) (not risky-op?))
     :violations hard
     :confidence conf
     :hard? hard?
     :escalate? (and (not hard?) (or low? risky-op?))}))
