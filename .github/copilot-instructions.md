## GitHub Copilot Instructions - Project Portal

### Guidance

* Every source code file must contain a standardized header specifying the agent Contribution Weight Percentage
  reflecting the split between automated generation and manual human refactoring.
  * For example: Agent Contribution Weight: [80%] Agent Generated / [20%] Human Refactored
* Whenever an agent is used to generate or modify code, it must clearly log its interaction and how they are modified
  and/or verified in a single entry such as code_AI_history.md.
  * For example: Each git commit must be labeled to track the evolution of contributions over time.
  * AI AGENT BUILD: [Feature Name] – Used when the agent modifies code.
  * HUMAN MOD: [Feature Name] – Used when the user manually modifies code.

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