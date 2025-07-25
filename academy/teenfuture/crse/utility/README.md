### java-automation-tests-fortress

This repository contains **two sample Java UI automation test files**, organized according to the package
`academy.teenfuture.crse.utility`.

## Files

- `academy/teenfuture/crse/utility/FortressRegisterPWTest.java`  
  End-to-end (E2E) test implemented with **Playwright for Java + JUnit 5**.

- `academy/teenfuture/crse/utility/GoogleOpenTest.java`  
  Implemented with **Selenium WebDriver** (assumed to use JUnit 5).

---

## Prerequisites (recommended)

- **JDK 17** (or 11+)
- **Maven 3.8+** (recommended to manage Playwright & Selenium dependencies)
- Internet access (Playwright will download browsers on the first run)

---

## Quick start (manage dependencies with Maven)

> If this repo does not yet contain a `pom.xml`, create a minimal Maven project locally and then copy these two files into it (steps below).

### 1) Generate a minimal Maven project (if you don’t already have one locally)

```bash
mvn -B archetype:generate \
  -DgroupId=demo \
  -DartifactId=ui-tests \
  -DarchetypeArtifactId=maven-archetype-quickstart \
  -DarchetypeVersion=1.4 \
  -DinteractiveMode=false
cd ui-tests
