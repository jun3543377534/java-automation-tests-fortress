# java-automation-tests-fortress

這個 repo 只放了兩個示範用的 Java 自動化測試檔，路徑依照檔案內的 package
`academy.teenfuture.crse.utility` 建立。

## 檔案說明

- `academy/teenfuture/crse/utility/FortressRegisterPWTest.java`  
  使用 **Playwright for Java + JUnit 5** 的端對端(E2E)自動化測試。

- `academy/teenfuture/crse/utility/GoogleOpenTest.java`  
  使用 **Selenium WebDriver**（假設一樣用 JUnit 5；如果你用 TestNG，見下方「如果你是用 TestNG」）。

---

## 需求 (建議)

- **JDK 17**（或 11 以上）
- **Maven 3.8+**（建議用 Maven 來管理 Playwright & Selenium 的相依性）
- 能連網（Playwright 第一次執行會自動下載瀏覽器）

---

## 快速開始（用 Maven 管理依賴）

> 如果你目前的 repo 還沒有 `pom.xml`，可以在本地新建一個 Maven 專案，然後把這兩個檔案放進去（步驟在下方也有說明）。

### 1) 建立最小化 Maven 專案（如果你本地還沒有）

```bash
mvn -B archetype:generate \
  -DgroupId=demo \
  -DartifactId=ui-tests \
  -DarchetypeArtifactId=maven-archetype-quickstart \
  -DarchetypeVersion=1.4 \
  -DinteractiveMode=false
cd ui-tests
