# PlaywrightRediffTest

A proof-of-concept UI test automation framework for the Rediff registration flow, built with **Java + Playwright + JUnit 5**, with **Allure** reporting and **GitHub Actions** continuous integration.

## What this project does

It automates the user-registration journey on Rediff and validates it end to end in a real browser. Two scenarios are covered:

- **Positive flow** — completes a full registration (name, ID selection, password, date of birth, country) and asserts that the selected country and the generated ID are correct.
- **Negative flow** — submits a blank ID on purpose and asserts that the system correctly shows no ID suggestions.

Every run produces a rich, visual **Allure report** with step details, severity labels, and full-page screenshots attached to each test.

## Tech stack

| Area | Tool |
|------|------|
| Language | Java 17 |
| Browser automation | Playwright 1.60 |
| Test framework | JUnit 5 (Jupiter) |
| Test data | JSON (via Gson) |
| Reporting | Allure 2.x |
| Build | Maven |
| CI | GitHub Actions |

## Project structure

```
PlaywrightRediffTest/
├── src/test/java/
│   ├── tests/
│   │   └── RegistrationTest.java     # Positive & negative test scenarios
│   ├── pages/
│   │   ├── LoginPage.java            # Landing-page actions
│   │   └── RegisterPage.java         # Registration-form actions
│   └── utils/
│       └── DriverUtils.java          # Browser launch / teardown
├── src/test/resources/
│   ├── config.json                   # Base URL, headless flag
│   └── testdata.json                 # Test input data
├── .github/workflows/tests.yml       # CI pipeline
├── pom.xml                           # Dependencies & build config
└── README.md
```

## Design highlights

- **Page Object Model** — each page (`LoginPage`, `RegisterPage`) wraps its own elements and actions, so tests read like plain steps and locators live in one place. Easy to maintain.
- **Data-driven** — inputs and config live in JSON files, not hardcoded in tests. Change data without touching code.
- **Centralized driver management** — `DriverUtils` handles browser launch and cleanup, so tests stay focused on the scenario.
- **Visual reporting** — Allure attaches a screenshot to every test and groups results by feature, story, and severity.

## How to run locally

Prerequisites: Java 17, Maven, and (for viewing reports) the Allure CLI.

```bash
# 1. Run the tests — results are written to target/allure-results
mvn clean test

# 2. Open the visual report in your browser
allure serve target/allure-results
```

> Tests are run via `mvn clean test` (not the IDE's run button) so the AspectJ weaver fires and screenshots attach to the report correctly.

## Continuous Integration

On every push and pull request to `main`, GitHub Actions automatically:

1. Checks out the code and sets up Java 17
2. Installs the Playwright browser
3. Runs the full test suite headless
4. Uploads the Allure results as a downloadable build artifact

You can see every run under the repository's **Actions** tab.

## What this proves

This POC demonstrates a complete, repeatable automation setup: write a scenario once, run it on any machine or in CI, and get a clear visual report of what passed, what failed, and exactly what the screen looked like at each point. It's a foundation that scales to more pages and scenarios with minimal added effort.
