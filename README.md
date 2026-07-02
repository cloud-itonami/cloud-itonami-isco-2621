# cloud-itonami-isco-2621

Open Occupation Blueprint for **ISCO-08 2621**: Archivists and Curators.

This repository designs a forkable OSS business for an independent archivist/curator: a collection-handling robot performs physical item retrieval and environmental-condition sensing under a governor-gated actor, so the practice keeps its own cataloguing and access records instead of renting a closed collections-management SaaS.

## Robotics premise

All cloud-itonami verticals are designed on the premise that a **robot performs
the physical domain work**. Here a collection-handling robot performs physical item retrieval and environmental-condition sensing in storage areas under an actor that proposes
actions and an independent **Archival Curatorial Governor** that gates them. The governor never
dispatches hardware itself; `:high`/`:safety-critical` actions (such as
deaccessioning an item, or releasing a restricted collection item for public access) require human sign-off.

A live sample of the operator console (robotics safety console, shared template) is rendered in [docs/samples/operator-console.html](docs/samples/operator-console.html) — pure-data HTML output of `kotoba.robotics.ui`.

## Core Contract

```text
collection scope + cataloguing standard + access policy
        |
        v
Curatorial Advisor -> Archival Curatorial Governor -> catalogue/preserve, or human sign-off
        |
        v
robot actions (gated) + operating records + audit ledger
```

No automated advice can dispatch a robot action the governor refuses, suppress
an operating record, or disclose sensitive data without governor approval and
audit evidence.

## Capability layer

Resolves via [`kotoba-lang/occupation`](https://github.com/kotoba-lang/occupation)
(ISCO-08 `2621`). Required capabilities:

- :robotics
- :forms
- :identity
- :audit-ledger
- :bpmn

See [`docs/business-model.md`](docs/business-model.md) and
[`docs/operator-guide.md`](docs/operator-guide.md).

## License

AGPL-3.0-or-later.
