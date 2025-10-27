# API Testing Project

A comprehensive API testing framework using RestAssured, TestNG, and Gatling for testing the [ReqRes API](https://reqres.in).


## 🎯 Overview

This project contains two types of automated tests:

1. **Functional API Tests** - Verify API correctness using RestAssured and TestNG
2. **Performance Tests** - Load and performance testing using Gatling (Java-based)


---

## 🛠 Technologies Used

| Technology | Purpose | Version |
|------------|---------|---------|
| **Java** | Programming language | 11 |
| **Maven** | Build and dependency management | 3.8+ |
| **RestAssured** | API testing framework | 5.0.0 |
| **TestNG** | Test framework | 7.8.0 |
| **Gatling** | Performance testing | 3.10.3 |
| **Jackson** | JSON processing | 2.15.2 |
| **Hamcrest** | Assertion matchers | 2.2 |

---


---

## ✅ Prerequisites

Before running tests, ensure you have:

- **Java JDK 11** or higher installed
- **Maven 3.8+** installed
- **Internet connection** (to access ReqRes API)
- **PowerShell** or **Command Prompt** (Windows)

### Verify Installation

```powershell
# Check Java version
java -version

# Check Maven version
mvn -version
```

Expected output should show Java 11+ and Maven 3.8+.

---

## 🚀 Running Tests

### Functional API Tests

These tests verify the API behaves correctly (functional testing).

#### Run All Tests

```powershell
mvn test
```

#### Run All Tests (Alternative)

```powershell
mvn clean test
```

#### Run Specific Test Class

```powershell
mvn test -Dtest=ApiTest
```

#### Run Specific Test Method

```powershell
mvn test -Dtest=ApiTest#testGetSingleUserValid
```

---

### Performance Tests (Gatling)

These tests measure API performance under load.

#### Run Basic Performance Test (Recommended First)

```powershell
mvn gatling:test "-Dgatling.simulationClass=simulations.BasicPerformanceSimulation"
```

**What it does:**
- Tests GET /users/2 endpoint
- Ramps from 1 to 50 concurrent users over 30 seconds
- Takes ~30 seconds to run
- Generates HTML report with performance metrics

#### Run Comprehensive Load Test

```powershell
mvn gatling:test "-Dgatling.simulationClass=simulations.ApiLoadSimulation"
```

**What it does:**
- Tests all API endpoints (GET, PUT, PATCH, DELETE)
- Multiple concurrent scenarios
- Simulates realistic user workflows
- Takes ~1 minute to run

#### Run All Gatling Simulations

```powershell
mvn gatling:test
```

This will prompt you to choose which simulation to run.

#### Run Gatling with Clean Build

```powershell
mvn clean gatling:test "-Dgatling.simulationClass=simulations.BasicPerformanceSimulation"
```

---

## 📊 Test Reports

Test reports are automatically generated after running tests:

- **Functional tests**: `target/surefire-reports/index.html`
- **Performance tests**: `target/gatling/<simulation-name>-<timestamp>/index.html`

Open the HTML files in your web browser to view detailed test results.

---

## 🎯 Quick Command Reference

### Most Common Commands

```powershell
# Run functional tests
mvn test

# Run basic performance test
mvn gatling:test "-Dgatling.simulationClass=simulations.BasicPerformanceSimulation"

# Run comprehensive performance test
mvn gatling:test "-Dgatling.simulationClass=simulations.ApiLoadSimulation"

# Clean build and run all tests
mvn clean test

# Run specific test
mvn test -Dtest=ApiTest#testGetSingleUserValid

# Compile only (no tests)
mvn clean compile test-compile -DskipTests
```

---

## 📊 Expected Results

### Functional Tests
```
Tests run: 20+
Failures: 0
Errors: 0
Skipped: 0
Success rate: 100%
Time elapsed: ~10 seconds
```

### Performance Tests (BasicPerformanceSimulation)
```
Request count: 50 (OK=50, KO=0)
Mean response time: ~100ms
95th percentile: ~200ms
Success rate: 100%
Time elapsed: ~30 seconds
```

---

## 🌟 Test Execution Flow

### Complete Test Run

```powershell
# Step 1: Clean previous build
mvn clean

# Step 2: Compile code
mvn compile test-compile

# Step 3: Run functional tests
mvn test

# Step 4: Run performance tests
mvn gatling:test "-Dgatling.simulationClass=simulations.BasicPerformanceSimulation"

# Step 5: View reports
# - Functional: target/surefire-reports/index.html
# - Performance: target/gatling/.../index.html
```


