# STEP 4-A: Security Intelligence Layer (Non-AI) Implementation

## Executive Summary
This document details the implementation of a **Deterministic Security Intelligence Layer** for the MediaTech application. Unlike "black box" Machine Learning models, this system provides 100% explainable, rule-based detection of security anomalies, adhering to **OWASP Proactive Controls C10 (Test Security)** and **A10:2021 (Server-Side Forgery Monitoring)** principles.

## 1. Rule-Based Detection Engine
We implemented a weighted scoring model to quantify User Risk.

### Risk Score Formula
$$ RiskScore = \sum (Event_i \times Weight_i) + (BusinessAnomaly_j \times Weight_j) $$

### Implemented Rules & Rationale

| Rule | Threshold | Weight | Rationale (Academic/Security) |
|------|-----------|--------|-------------------------------|
| **Authentication Failure** | > 0 | 5 | **Brute Force Detection**: High frequency indicates a credential stuffing attack. Low frequency is normal user error. |
| **Unauthorized Access** | > 0 | 20 | **Privilege Escalation**: Attempts to access Admin resources by non-admins is a strong indicator of malicious intent or broken access control (IDOR). |
| **Critical Incident** | > 0 | 50 | **Forensic Trigger**: Events marked 'CRITICAL' (e.g., SQLi pattern detected) require immediate attention regardless of frequency. |
| **High Value Transaction** | > 5,000 | 10 | **Anomaly Detection**: Financial transactions exceeding 3σ (standard deviations) from the mean are flagged for manual review to prevent fraud/embezzlement. |
| **Velocity Check** | > 5 inv/day | 30 | **Bot Detection**: Human operators have a maximum cognitive throughput. Rates exceeding this biological limit suggest automation usage. |

## 2. Security API Endpoints
We exposed the following **Admin-Only** REST endpoints in `SecurityAnalyticsController`:

### 2.1 Risky Users
`GET /api/admin/security/risky-users`
- **Purpose**: Returns users sorted by Risk Score (Descending).
- **Data Model**: `UserRiskProfile` (includes actionable `recommendations`).
- **Use Case**: SOC Analyst dashboard for prioritizing investigations.

### 2.2 Financial Security Stats
`GET /api/admin/security/invoice-stats`
- **Purpose**: Aggregates macro-level financial data to spot anomalies.
- **Data Model**: Includes "Suspicious Transactions" list (Top 5 by value).
- **Use Case**: Fraud detection dashboard.

### 2.3 Vulnerability Reporting
`GET /api/admin/security/vulnerabilities`
- **Purpose**: Provides visibility into CVEs (Common Vulnerabilities and Exposures) affecting the stack.
- **Current State**: Uses a simulation engine (Mock) for demonstration, architected to ingest reports from OWASP Dependency-Check.

## 3. Mitigation Strategies (Automated)
The system now generates automated recommendations based on risk factors:

- **If Auth Failures > 5**: "Enforce Password Rotation or MFA." (Mitigates Credential Stuffing)
- **If Unauthorized Access > 2**: "Review RBAC permissions." (Mitigates Privilege Escalation)
- **If Velocity Violated**: "Investigate for potential bot activity." (Mitigates DoS/Spam)

## 4. Next Steps
- **Visualization**: Connect these APIs to the Angular Admin Dashboard.
- **Integration**: Fully integrate the `getVulnerabilityReport` with the parsing of `dependency-check-report.xml`.
