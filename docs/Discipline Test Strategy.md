# Test Strategy for Discipline Management

## Scope
Unit tests for adapters and role-based UI, integration-like tests via Robolectric for user flows (submit violation), backend smoke tests for endpoints readiness. Performance seeding for large datasets.

## Tools
- Android: JUnit 4 + Robolectric.
- Backend: Simple PHP scripts and cURL. Suggest PHPUnit for future.

## Coverage
- Adapters: Image slider count correctness.
- Home visibility: Buttons for teacher/admin roles.
- Violation flow: Submit validation toast when selections missing.
- Backend banners: get_active returns array; stream SSE responds with banners or heartbeat.
- Discipline endpoints: setup tables, validate/fix, seed demo.

## Execution
- Android: `./gradlew test`
- Backend: `php Backend/tests/banners_get_active_test.php`, `php Backend/tests/stream_test.php`

## Performance
- Use `seed_demo.php?n=10000` to populate violations for load testing. Ensure DB indexing on `violations(rule_id, student_id, created_at)`.

## Reporting
- Gradle test reports under `AndroidApp/app/build/reports/tests`.
- Backend scripts echo status to console. Integrate PHPUnit for structured reports later.
