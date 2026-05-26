# Candidate CV Versioning Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Let candidates upload CV files to R2 and see a simple version history, ready for later AI parsing.

**Architecture:** Add a candidate-owned CV version entity and candidate-only REST endpoints backed by the existing R2 upload flow. The Angular candidate dashboard will call those endpoints with the existing auth interceptor and show current/latest CV plus previous versions.

**Tech Stack:** Spring Boot, JPA, Cloudflare R2 via AWS S3 SDK, Angular standalone components, Tailwind utility classes.

---

## Chunk 1: Backend CV Versions

**Files:**
- Create: `backend/src/main/java/com/smarthireai/entity/CvVersion.java`
- Create: `backend/src/main/java/com/smarthireai/repository/CvVersionRepository.java`
- Create: `backend/src/main/java/com/smarthireai/service/CvService.java`
- Create: `backend/src/main/java/com/smarthireai/controller/CvController.java`
- Test: `backend/src/test/java/com/smarthireai/controller/CvControllerTest.java`
- Modify: `backend/src/main/java/com/smarthireai/config/SecurityConfig.java`
- Modify: `backend/src/main/java/com/smarthireai/service/FileUploadService.java`

- [ ] Write a failing controller test: candidate uploads two CVs and receives version 1 then version 2; history returns latest first.
- [ ] Add `CvVersion` linked to `User`, including file metadata, version number, active flag, and upload timestamp.
- [ ] Add repository query methods for candidate history and max version.
- [ ] Add service methods that validate authenticated candidate role, upload file through R2, save the next version, mark older active versions inactive, and list history.
- [ ] Add `/api/candidate/cvs` GET/POST endpoints.
- [ ] Permit candidate CV endpoints to authenticated candidates.
- [ ] Run focused backend tests.

## Chunk 2: Frontend CV UI

**Files:**
- Create: `frontend-angular/src/app/services/cv.service.ts`
- Modify: `frontend-angular/src/app/pages/candidate-dashboard-page/candidate-dashboard-page.component.ts`
- Modify: `frontend-angular/src/app/pages/candidate-dashboard-page/candidate-dashboard-page.component.html`

- [ ] Add a CV service with `getCvVersions()` and `uploadCv(file)`.
- [ ] Add dashboard state for selected file, upload loading, errors, and version history.
- [ ] Replace the placeholder CV card with file picker, upload action, current CV summary, and version history links.
- [ ] Restrict picker to `.pdf,.doc,.docx`.
- [ ] Run Angular build.

## Chunk 3: Verification

- [ ] Run `mvn test`.
- [ ] Run `pnpm build` in `frontend-angular`.
- [ ] Report exact verification results and any residual warnings.
