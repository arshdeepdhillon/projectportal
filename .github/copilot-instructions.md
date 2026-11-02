## GitHub Copilot Instructions - Project Portal

### Guidance

* Every source code file must contain a standardized header specifying the agent Contribution Weight Percentage
  reflecting the split between automated generation and manual human refactoring.
  * For example: Agent Contribution Weight: [80%] Agent Generated / [20%] Human Refactored
  * Recalculate and update this percentage after the final changes to each file, including human edits or commits made after agent changes. Do not leave a stale 100% Agent Generated / 0% Human Refactored header after a human has modified the file; the percentages must add up to 100%.
  * Update the header in the same change as the final code modifications so it reflects the final contribution split.
  * Use `code_AI_history.md` as the source of truth for authorship. For every source-file change, add or update a per-file contribution snapshot there with retained substantive logical-line counts for agent and human authorship. Use this format: `File: <path> — Agent lines: <A>; Human lines: <H>; Agent: <P1>%; Human: <P2>%`.
  * Make each history entry traceable to the file version it describes (commit hash, or identify it as an uncommitted working-tree change) and record the changed file hunks with their contributor. Reconstruct ownership by replaying those logged diffs in order: unchanged substantive lines keep their prior attribution; added or substantively rewritten lines are attributed to the contributor who supplied the current implementation; deleted lines are removed from the previous owner's count. Keep agent and human changes in separate hunks where possible, and explicitly identify their lines if a hunk contains both. Ignore blank lines, formatting-only changes, and the contribution header itself. Count comments or documentation only when they convey substantive file content.
  * For a file without a prior snapshot, use its documented creation history to classify existing lines; if provenance cannot be established, count those baseline lines as human-authored. Do not infer authorship from who committed a change alone. Record each file's final counts in the same history entry as its change, and update the file header from that snapshot.
  * Calculate using the snapshot counts, where `A` is retained agent-authored lines and `H` is retained human-authored lines: `Agent % = round(100 × A / (A + H))`; `Human % = 100 − Agent %`. If `A + H` is zero, use 0% / 100%. Round to whole percentages and set the human percentage as the remainder so the total is exactly 100%.
* Whenever an agent is used to generate or modify code, it must clearly log its interaction and how they are modified
  and/or verified in a single entry such as code_AI_history.md.
  * For example: Each git commit must be labeled to track the evolution of contributions over time.
  * AI AGENT BUILD: [Feature Name] – Used when the agent modifies code.
  * HUMAN MOD: [Feature Name] – Used when the user manually modifies code.
  * When asked to create a commit for agent work, use the subject format `AI AGENT BUILD: <Feature Name>` and record the commit hash in the matching history entry. Commit labels help trace the diffs for contribution accounting; determine the line counts from the actual committed changes and per-file snapshots, not from the label alone.
  * Always ask the user for explicit confirmation immediately before creating a commit. This requirement applies even when the user asks to commit; do not run `git commit` until the user confirms.

### Project Overview

* Project Portal (PP) serves as a centralized, campus-wide ecosystem designed to showcase, share and manage academic
  research, course capstones, and innovative student designs.
* PP is broken down into 4 isolated backend services. **This project focuses on Domain 1**.
  * Domain 1
    * Project Artifact & Repository Indexer: Service manages public Git repository profiles, academic manuscript
      documents, and video presentation metadata, cataloging all project artifacts based on academic lab, department
      program, and course affiliations.
  * Domain 2
    * Resource Allocation & Help Desk Hub: Service handles the distribution of critical project assets, logging
      tracking parameters, software license keys, and cloud credit vouchers across active course groups and student
      project teams.
  * Domain 3
    * Peer Review & Evaluation Gateway: Service handles the formal academic grading matrix, enabling faculty, course
      instructors, and student peers to process, aggregate, and review structured evaluation rubrics and scores for
      published works.
  * Domain 4
    * Research Blog & Project Discussion Board: Service coordinates collaborative project updates, announcements,
      and interactive comment feeds to streamline communication within active academic project teams and course
      groups.

### What Not To Do

* No cross domain communication.
* For external data dependencies, they must be managed using local static mock data structures. 
  * For each domain which requires a response from, a dedicated domain module in 'mockresponse' must be created. 
  * For example: "/mockresponse/domain2" will contain mock data for Domain 2.