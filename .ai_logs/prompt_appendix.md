# Assignment 2 Prompt Appendix
---
AI Contribution Weight: [ 0% ] AI Generated / [100%] Human Refactored
---

*User prompt: Draft a comprehensive technical functional specification (spec.md) for a Spring Boot 3 / Java 17 backend
service. It must include \- REST endpoints \- JSON schemas \- JPA entity models with relationships \- Static mock data
structure adhering to enterprise standards To make the spec.md focused, the service is a backend micro-service designed
to catalog public Git repository profiles, academic manuscript documents, and video presentation metadata. It links
these artifacts directly to academic labs, department programs, and course affiliations. Before implementing the
spec.md, drill down on questions until an understanding is found and the feture/design is clarified. If an answer an be
derived or is found from previous answers or from spec.md, use that to derive your question instead of repeating
yourself.*

---

*User prompt: For \#1 For external data dependencies, they must be managed using local static mock data structures. For
\#2 Yes. Single artifact can be associated with multiple academic labs, departments, or courses. For \#3 Yes, we require
advanced filtering endpoints. And we should include paginated responses as part of the standard response JSON schemas.
For \#4 Lets use standard relations mapping for now to keep it simple. For static mock, yes use a JSON fixture file.*

---

*User prompt: [[PLAN]] Analyze the spec.md to understand the stack of the project. Then help draft a fully functional
GitHub actions pipeline script. This DevSecOps pipeline must be configured to automatically build the Docker container
environment, scan the dependencies for known vulnerabilities (Trivy) and execute static application security testing
(Semgrep).*

---

*User prompt: Export the whole conversation in Prompt_test_log.md. Follow existing template as such:
`### Prompt <#> <Chat description>`
`<dump whole conversation>`*

---

*User prompt: [[PLAN]] There is currently one limitation when i try to run the applicaiton IDE task, spring boot runs
then fails because it expects the DB to be running, but its not. This will make development very difficult.

```
2026-11-01T23:41:46.546-07:00 ERROR 41988 --- [project-portal] [           main] o.s.boot.SpringApplication : Application run failed

org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'entityManagerFactory'
defined in class path resource
[org/springframework/boot/autoconfigure/orm/jpa/HibernateJpaConfiguration.class]: Failed to initialize dependency
'flywayInitializer' of LoadTimeWeaverAware bean 'entityManagerFactory': Error creating bean with name
'flywayInitializer' defined in class path resource
[org/springframework/boot/autoconfigure/flyway/FlywayAutoConfiguration$FlywayConfiguration.class]: Unable to obtain
connection from database: Connection to localhost:5432 refused. Check that the hostname and port are correct and that
the postmaster is accepting TCP/IP connections.

SQL State  : 08001
Error Code : 0
Message    : Connection to localhost:5432 refused. Check that the hostname and port are correct and that the
postmaster is accepting TCP/IP connections.
```

---

*User prompt: commit*

---


*User prompt: Make sure to also put all the user prompt from converstations done in code_AI_history.md into
Prompt_test_log.md. Update your S:Agent_environment.md or S:copilot-instructions.md so you remember.*

---

*User prompt: [[PLAN]] /plan compare the changes against main branch to understand what jobs have been refactored into
their own yml files. I have added sonar-project.properties and now need to add SonarQube Cloud Scan job*

---

*User prompt: Lets build the core features purely functional, happy-path approach of the master specification (C:
\Users\arsh\ProjectPortalDomainServices.md) , deliberately omitting any mention of security, input validation or
encryption. Update our spec as that takes precedence.*

*User response: project*

*User response: all*

*User response: local_reference*

*User response: database*

*User response: id_and_role*

*User response: master_only*

*User response: project_fields_only*

*User response: use soft delete, by deleting the field deleted to 1. An internal endpoint called "/projects/cleanup"
actually deleted projects marked with deleted=1 and its data*

*User response: Hit it immediately from GIT and search. also rename "projects/cleanup" to "projects/purge"*

*User response: post*

*User response: Soft-delete the artifact until project purge. This way we can rollback artifacts if such feature comes*

*User response: hide*

*User response: sha256*

*User response: Provide a filter object,which contains a timeRange object whcih has createdFrom,createdTo fields*

*User response: it should be json request object*

*User response: follow master*

*User response: and*

*User response: all_artifact_metadata*

*User response: any*

*User response: string*

*User response: project_fields*

*User response: one_current*

*User response: file_only*

*User response: dotted*

*User response: omit*

*User response: project_payload*

*User response: replace*

*User response: names*

*User response: inclusive*

*User response: defer*

*User response: application.properties sets spring.profiles.default=dev. If production starts without a profile, the dev
profile is active and your mock data loads. A safer design fails closed: no default profile, or a production default,
with dev and test enabled explicitly.*

*User response: none*

*User response: filename_media_type*

*User response: separate_record*

*User response: count*

*User response: dev_test_only*

*User response: remove the probe as a scaffold. Also this project isn't release, so migrations files can
edited/deleted/updated*

*User response: defer*

*User response: string*

*User response: happy_only*

*User response: scalar_only*

*User response: structured publisher object*

*User response: name, venue*

*User response: type_specific*

*User response: changed_only*

*User response: deployment_data*

*User prompt: save the plan so can resume*

*User prompt: lets continue from where left*

*User response: keep*

*User response: If the master spec clearly specifies a design, then it takes precedence. if the master spec doesn't
specify it, only then ask me for clarificaiton*

*User response: short_names*

*User response: Just to refresh. we are to build purely functional, happy-path approach to build the standardized
service. build the core features of the master specification (C:\Users\arsh\Downloads\ProjectPortalDomainServices.md),
deliberately omitting any mention of security, input validation, or encryption.*

*User response: keep*

*User response: conventional*

*User response: multipart*

*User response: artifact_directory*

*User response: active_with_files*

*User response: string*

*User response: active_only*

*User response: Can one person have multiple roles on same project? If so, give examples*

*User response: multiple*

*User response: independent*

*User response: separate*

*User response: array*

*User response: Bare JSON array. Also lets not introduce pagination yet. Keep it out since master spec doesn't specify
it*

*User response: omit_all*

*User response: If the master spec doesn't specify providing ability of multiple artifact_types then keep it to one
only*

*User response: distinguish*

*User response: lets make the endpoints work in epoch UTC only*

*User response: milliseconds*

*User response: milliseconds*

*User response: artifactType*

*User response: existing*

*User response: server*

*User response: internal*

*User response: array*

*User response: updated_at_only*

*User response: omit*

*User prompt: why did you put all functional test in single SampleFunctionalTest, refactor into classes which foce on a
class/service. Also create unit tests. Verfiy both tests pass, and docker builds, run tests against the docker
containsers*

---

*User prompt: verify all the functional requiremtns have been implemented and tested via unit and functional: C:
\Users\arsh\Downloads\ProjectPortalDomainServices.md*


---

*User prompt: what are you doing in the test methods. They don't follow industry standard where each test method focues
on a single (generally) logic/flow. And unit test methods should focue on specific logic of method.*
