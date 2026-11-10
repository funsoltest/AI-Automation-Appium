# AI Automation Appium

**An AI-assisted Android test-code generation workflow built with Java, Maven, Appium, and TestNG.**

This project accepts a QA requirement written in natural language, sends it to a configured AI API, parses the generated Java source files, and stores them in a reviewable session. The QA engineer can then approve, reject, or refine the generated code. Approved files are written into the Maven test source tree.

> **Current scope:** This repository implements AI-assisted test-code generation and human review. It does not currently demonstrate an end-to-end AI agent that independently inspects an app, executes tests, diagnoses failures, or repairs tests on a device. Appium/TestNG execution is a separate step after code generation and approval.

## How It Works

1. **Requirement input** – The QA engineer describes the desired mobile test in plain language.
2. **Prompt construction** – `PromptBuilder` adds framework rules requiring Java, Appium, TestNG, Page Object Model (POM), modern `WebElement` usage, explicit waits, and a defined multi-file response format.
3. **AI API request** – `AiClient` sends the prompt to the configured AI endpoint using the credentials and request settings in `config/credentials.json`.
4. **Code parsing** – `ResponseParser` extracts individual Java files from `### FILE: FileName.java` response sections. If the expected markers are missing, it treats the response as one default file.
5. **Session creation** – `SessionStore` stores the original requirement, generated source files, timestamp, and status (`pending`) in `sessions/sessions.json`.
6. **Human review** – The engineer reviews the generated code and chooses one of three actions:
   - **Approve:** save the generated files into the test source tree.
   - **Reject:** mark the session as rejected without saving its files.
   - **Refine:** send one selected file and feedback back to the AI for regeneration. The session returns to `pending` for another review.
7. **Test execution** – After approval, reload the Maven project and execute the relevant TestNG tests against a configured Android device or emulator.

## Workflow Overview

```text
QA requirement (plain language)
          |
          v
PromptBuilder (QA / Java / Appium / TestNG / POM rules)
          |
          v
AiClient (configured AI API)
          |
          v
AI-generated Java source files
          |
          v
ResponseParser (extract files)
          |
          v
SessionStore (pending review session)
          |
          +----------------+----------------+
          |                |                |
          v                v                v
       Approve           Reject           Refine
          |                |                |
          v                v                v
 Save to src/test     Mark rejected    Send feedback to AI
          |                                 |
          v                                 v
 Reload Maven                         Review again
          |
          v
 Run TestNG / Appium tests
```

## Main Capabilities

- Generate Java Appium page objects and TestNG test classes from a natural-language requirement.
- Encourage Page Object Model separation: page locators and actions in page classes; test flow and assertions in test classes.
- Parse AI responses containing multiple Java file blocks.
- Store generated code and review state in local JSON sessions.
- Approve generated files into the Maven test source tree.
- Refine a selected generated file using reviewer feedback while retaining the other files in the session.
- Reject a generation without writing its files into the test source tree.
- Reload configuration for each AI request, allowing updated API credentials/settings to be read without restarting the Java process.

## Project Structure

```text
AI-automation-Appium/
├── config/
│   └── credentials.json          # Local AI endpoint and credentials (do not commit)
├── generated_output/             # Existing generated/example Java output
│   ├── pages/
│   └── tests/
├── sessions/
│   └── sessions.json             # Local generation sessions and generated source
├── src/
│   ├── main/java/com/practiceapi/aiclient/
│   │   ├── AiClient.java          # Sends prompts to the AI API
│   │   ├── Config.java            # Loads local configuration
│   │   ├── PromptBuilder.java     # Builds generation/refinement prompts
│   │   ├── GenerateCommand.java   # Starts a new generation session
│   │   ├── ResponseParser.java    # Extracts Java files from AI response
│   │   ├── SessionStore.java      # Persists session data/status
│   │   ├── ConfirmCommand.java    # Approves and writes generated files
│   │   ├── RejectCommand.java     # Rejects a generation session
│   │   └── RefineCommand.java     # Regenerates a selected file with feedback
│   └── test/java/com/practiceapi/
│       ├── pages/                 # Appium Page Object classes
│       └── tests/                 # TestNG test classes
├── pom.xml
└── README.md
```

**Generated-file destinations:** filenames ending in `Page.java` are saved under `src/test/java/com/practiceapi/pages/`; filenames ending in `Test.java` are saved under `src/test/java/com/practiceapi/tests/`; other files are saved under `src/test/java/com/practiceapi/`.

`generated_output/` contains existing generated/example files. The current `ConfirmCommand` writes approved files directly into `src/test/java`, not into `generated_output/`.

## Technology Stack

| Technology | Version / Purpose |
|---|---|
| Java | 21 |
| Maven | Build and dependency management |
| Appium Java Client | 9.3.0 |
| Selenium Java | 4.21.0 |
| TestNG | 7.10.2 |
| Jackson Databind | 2.17.0; JSON configuration, API responses, and sessions |
| Java HTTP Client | Sends HTTP requests to the configured AI service |
| Android UiAutomator2 | Appium automation driver for Android |

## Prerequisites

- JDK 21
- IntelliJ IDEA or another Java IDE
- Maven
- Node.js and npm
- Appium Server
- Appium UiAutomator2 driver
- Android SDK / Android Platform Tools (`adb`)
- Android emulator or USB-debugging-enabled Android device
- Access to the AI API configured for this project

## Setup

### 1. Clone the repository

```bash
git clone https://github.com/funsoltest/AI-Automation-Appium.git
cd AI-Automation-Appium
```

### 2. Configure Java and Maven

Set the project SDK to JDK 21, then reload the Maven project so dependencies are downloaded.

### 3. Install Appium and UiAutomator2

```bash
npm install -g appium
appium driver install uiautomator2
appium --version
appium driver list --installed
```

### 4. Connect an Android device

Enable Developer Options and USB Debugging, connect the device, and verify it is visible:

```bash
adb devices
```

### 5. Configure the AI API

Create your local `config/credentials.json` using the configuration structure expected by `AiClient`. The code reads these fields:

| Field | Purpose |
|---|---|
| `base_url` | AI API endpoint URL |
| `authorization` | Authorization header value |
| `ut_token` | `UT-Token` request header |
| `x_app_id` | `X-App-Id` request header |
| `session_id` | AI service session identifier |
| `mode` | Request mode; defaults to `auto` if omitted |
| `temperature` | Generation temperature; defaults to `0.3` |
| `max_tokens` | Maximum response tokens; defaults to `2000` |

`Config` reads this file relative to the project root. Keep real credentials private. Do not publish API tokens, authorization values, or private session identifiers in Git. If any real credentials from a local config file have already been pushed to a remote repository, revoke/rotate them and remove the secrets from repository history as appropriate.

## Generate, Review, and Save Code

Run these commands from the project root. The examples use the package names in the current source tree.

### 1. Generate code from a requirement

```bash
mvn exec:java "-Dexec.mainClass=com.practiceapi.aiclient.GenerateCommand" "-Dexec.args=Create an Appium test for the login screen with valid and invalid login scenarios"
```

The command sends the requirement to the AI service, prints the generated files, creates a session, and displays its session ID. The new session starts with `pending` status.

### 2. Approve a session

Replace `<SESSION_ID>` with the ID printed by the generation command:

```bash
mvn exec:java "-Dexec.mainClass=com.practiceapi.aiclient.ConfirmCommand" "-Dexec.args=<SESSION_ID>"
```

The command writes the session's Java files into the appropriate `src/test/java/com/practiceapi/` folders and marks the session `approved`. Reload Maven in the IDE afterwards.

### 3. Refine a generated file

Provide the session ID, exact filename, and feedback:

```bash
mvn exec:java "-Dexec.mainClass=com.practiceapi.aiclient.RefineCommand" "-Dexec.args=<SESSION_ID> LoginPage.java Add explicit waits for all login fields and the login button"
```

The selected file is regenerated and the session status is set back to `pending`. Review it again before approving.

### 4. Reject a session

```bash
mvn exec:java "-Dexec.mainClass=com.practiceapi.aiclient.RejectCommand" "-Dexec.args=<SESSION_ID>"
```

The session is marked `rejected`; its generated files are not saved by this command.

## Run Appium / TestNG Tests

Start the Appium server in a separate terminal:

```bash
appium
```

Check and update the generated test class capabilities for your device and application, including:

- Appium server URL
- Device name or UDID
- Application package and launch activity
- Any required app-specific test data and locators

Then run the Maven tests:

```bash
mvn test
```

Run one TestNG class (if configured/discoverable by the project build):

```bash
mvn -Dtest=LoginTest test
```

Test execution depends on valid app capabilities, real UI locators, test data, and a reachable Appium server. AI-generated sample code must be reviewed and adapted to the target application before relying on its results.

## Important Implementation Notes / Current Limitations

- AI API credentials are read from a local JSON file; this project currently expects that file at `config/credentials.json`.
- AI response parsing expects `### FILE: FileName.java` markers for multi-file output. If the AI response does not contain these markers, the parser falls back to one default file.
- Sessions are stored in a single local JSON file (`sessions/sessions.json`); this is a lightweight local store, not a database or multi-user session service.
- Approval writes files based on filename suffix. Review AI-generated package declarations, imports, class names, and destination paths before execution.
- The sample login page/test currently contains example locators and placeholder app details/test data. Replace them with values from the actual application under test.
- The AI generation workflow does not itself install an APK, inspect live screens, launch the app, execute generated tests, or automatically analyze test failures.
- API authentication failures (HTTP 401/403) require valid, current credentials in the local configuration.

## Security

The local configuration file can contain sensitive API credentials. Ensure `config/credentials.json` is ignored by Git and never include real tokens in commits, screenshots, logs, or public documentation. The repository's `.gitignore` should include:

```gitignore
config/credentials.json
```

Also avoid committing private session data if `sessions/sessions.json` contains internal requirements or generated code that should remain confidential.

## Maintainer

**QA Automation Team – Funsol**

Repository: https://github.com/funsoltest/AI-Automation-Appium
