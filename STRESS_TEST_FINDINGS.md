# DMS AI Debug Console — Stress Test Findings
**Date**: 2026-04-09  
**Tester**: Claude Code (automated)  
**Backend**: Spring Boot 3.2.4 @ localhost:8080  
**RAG file loaded**: `report-queries.xml` (12 chunks, 8 queries)  
**Tests run**: 10

---

## Test Results Summary

| # | Issue Description | Query Identified | SQL Built | Execution Result |
|---|---|---|---|---|
| 1 | CSV blank for KLDEL01 | ✅ `fetchCSVData` | ✅ 4127 chars | ❌ Length limit exceeded |
| 2 | Stock weight no data for KLDEL01 (alt phrasing) | ✅ `fetchCSVData` | ✅ 4127 chars | ❌ Length limit exceeded |
| 3 | Total summary blank for KLMUM02 | ✅ `fetchCSVDataTotal` | ✅ 3661 chars | ✅ 0 rows (expected) |
| 4 | CSV blank for KLDEL01 + KLMUM02 (multi-branch) | ✅ `fetchCSVData` | ✅ 4157 chars | ❌ Length limit exceeded |
| 5 | CSV blank for KLDEL01, godown GDN001 | ✅ `fetchCSVData` | ✅ 4166 chars | ❌ Length limit exceeded |
| 6 | CSV blank for KLDEL01, active products only | ✅ `fetchCSVData` | ✅ 4127 chars | ❌ Length limit exceeded |
| 7 | Collection not in ledger (unrelated issue) | ✅ `` (empty — correct) | ❌ None | — No result |
| 8 | CSV blank — no distributor code mentioned | ✅ `fetchCSVData` | ⚠️ 4085 chars (IN '') | ❌ Length limit exceeded |
| 9 | CSV blank for KLDEL01, inactive batches only | ✅ `fetchCSVData` | ✅ 4127 chars | ❌ Length limit exceeded |
| 10 | Vague: "stock data not showing up" | ⚠️ `fetchCSVData` (guessed) | ⚠️ 4085 chars (IN '') | ❌ Length limit exceeded |

---

## Issues Found — Prioritised

---

### BUG-1 🔴 CRITICAL — SQL Length Limit Blocks All `fetchCSVData` Executions

**File**: `src/main/java/com/example/reportportal/service/QuerySafetyValidator.java:10`

**What happens**: `MAX_QUERY_LENGTH = 4000`. The actual `fetchCSVData` query from the XML is **~4127 characters minimum** after parameter substitution (increases with more distributor/godown codes). Every single `fetchCSVData` test failed with:
```
SQL validation failed: Query exceeds maximum length of 4000 characters (got 4127)
```

**Impact**: The core report query — the one users will actually ask about — can never execute. `fetchCSVDataTotal` (3661 chars) passes fine.

**Fix**:
```java
// QuerySafetyValidator.java — line 10
private static final int MAX_QUERY_LENGTH = 4000;
// Change to:
private static final int MAX_QUERY_LENGTH = 10000;
```
Or make it configurable via `application.properties`:
```properties
query.max-length=10000
```

---

### BUG-2 🟠 HIGH — Empty `distrBrCodes` Produces Silent No-Result Query

**File**: `src/main/java/com/example/reportportal/service/ReportQueryService.java:130`

**What happens**: When no distributor code is mentioned (Tests 8, 10), `distrBrCodes` is `[]`. `formatInClause([])` returns `''` (empty string). This results in:
```sql
WHERE SO.DistrBrCode IN ('')
```
The SQL is built and executed, returns 0 rows, and the user gets no feedback that they forgot to provide a branch code. Silently misleading.

**Fix — Two changes needed**:

1. In `ClaudeChatController.java`, after getting `extracted_params`, check if `distrBrCodes` is empty and add a warning to the response:
```java
// After step 3b SQL build
List<String> distrBrCodes = (List<String>) extractedParams.getOrDefault("distrBrCodes", List.of());
if (distrBrCodes.isEmpty() && builtSql != null) {
    aiResponse.put("warning", "No distributor branch code found in the issue description. Results may be empty. Please specify a branch code like KLDEL01.");
}
```

2. In `ReportQueryService.buildSql()`, when `distrBrCodes` is empty, return `null` instead of building an invalid query:
```java
// In buildSql() — before resolveDbParams
if (distrBrCodes.isEmpty()) {
    log.warn("Cannot build SQL — no distrBrCodes provided");
    return null;
}
```

---

### BUG-3 🟠 HIGH — `batchStatus` Not Correctly Extracted for "Inactive" Phrasing

**Test 9 input**: *"Current stock weight report for KLDEL01 — I want to see only inactive batches"*  
**Expected**: `batchStatus = N`  
**Actual**: `batchStatus = ALL`

The Claude system prompt defines `batchStatus` extraction:
```
batchStatus: "ALL" (default), "Y" (active only), or "N" (inactive only)
```
But Claude consistently defaults to `ALL` when "inactive" is mentioned. The prompt needs clearer examples.

**File**: `src/main/java/com/example/reportportal/service/ClaudeAiService.java:144`

**Fix — Update system prompt extraction rules**:
```
batchStatus : "ALL" (default if not mentioned), "Y" if user says "active only" / "active batches", "N" if user says "inactive only" / "inactive batches" / "expired batches"
productStatus: "ALL" (default if not mentioned), "Y" if user says "active only" / "active products", "N" if user says "inactive only" / "discontinued products"
```
Add explicit examples:
```
Examples:
  "I want to see only inactive batches" → batchStatus: "N"
  "show only active products" → productStatus: "Y"
  "all products including discontinued" → productStatus: "ALL"
```

---

### BUG-4 🟡 MEDIUM — No User Guidance When `query_name` is Empty (Unrelated Issue)

**Test 7**: Collection ledger issue (not in XML) → `query_name = ""`, no SQL, no `query_result`.

The frontend gets `analysis.sql = ""` and renders nothing where the SQL block should be. The user sees issue summary + root cause but no query, with no explanation of why.

**Fix — `ClaudeChatController.java`**: When `query_name` is empty, set a meaningful `sql_explanation`:
```java
if (queryName == null || queryName.isBlank()) {
    aiResponse.put("sql_explanation", "No matching report query found in the uploaded XML files for this issue. The diagnosis is based on schema context only. To enable SQL execution, upload the relevant query XML file.");
}
```

---

### BUG-5 🟡 MEDIUM — Vague Issues Guess `fetchCSVData` Without Confidence

**Test 10**: *"Stock data not showing up in the report"*  
Claude assigned `query_name = CurrentStockWeight.fetchCSVData` with no distributor code. This is a guess based on the XML content in RAG, but may be wrong for a completely different report.

**Fix — System prompt guidance**: Add to `ClaudeAiService.chatAnalyze()` prompt:
```
If the issue description is too vague to confidently identify a specific query (no report name, no branch code, no specific symptom), set query_name to "" and explain in root_cause that more details are needed.
```

---

### OBSERVATION-1 🟢 GOOD — Query Name Identification is Accurate

All 9 issues related to `fetchCSVData` were correctly identified (Tests 1–6, 8–10). Alternate phrasings ("stock weight report", "CSV blank", "stock data not showing") all resolved correctly. Test 3 correctly identified `fetchCSVDataTotal`. Test 7 correctly returned an empty `query_name`.

---

### OBSERVATION-2 🟢 GOOD — Parameter Extraction Works for Happy Path

- Single branch code extracted correctly (Tests 1, 2, 6, 9)
- Multiple branch codes extracted correctly (Test 4)
- Godown code extracted correctly (Test 5)
- `productStatus: Y` correctly extracted from "active products only" (Test 6)

---

### OBSERVATION-3 ⚠️ MINOR — Severity Is Always MEDIUM

9 of 10 tests returned `severity: MEDIUM`. One vague query got `HIGH`, one inactive-batch got `LOW`. The severity assignment appears arbitrary — not driven by actual data anomaly or business impact.

**Fix**: Add clearer severity guidance to system prompt — e.g., blank CSV export that blocks operations = HIGH, slow report = MEDIUM, cosmetic = LOW.

---

## Actionable Fix List (Priority Order)

| Priority | File | Change |
|---|---|---|
| 🔴 1 | `QuerySafetyValidator.java:10` | Raise `MAX_QUERY_LENGTH` from 4000 → 10000 |
| 🔴 2 | `application.properties` | Add `query.max-length=10000` and wire it in validator |
| 🟠 3 | `ReportQueryService.java:buildSql()` | Return `null` if `distrBrCodes` is empty |
| 🟠 4 | `ClaudeChatController.java` | Add `"warning"` field in response when no distrBrCodes |
| 🟠 5 | `ClaudeAiService.java:144` | Improve `batchStatus`/`productStatus` extraction examples in prompt |
| 🟡 6 | `ClaudeChatController.java` | Set `sql_explanation` when `query_name` is empty |
| 🟡 7 | `ClaudeAiService.java` | Add confidence threshold: vague issues → empty `query_name` |
| ⚪ 8 | `ClaudeAiService.java` | Add severity guidance tied to business impact |

---

## What to Test Next (Requires Real DB Data)

The following tests were blocked because real distributor branch codes from the live DB (`kellogg_uat`) were not available. Once BUG-1 is fixed, re-run with real codes:

1. **Happy path end-to-end**: Real `DistrBrCode` that has stock → SQL executes → rows returned → verify columns match report screen
2. **Branch code not in DB**: Valid-format code but doesn't exist → 0 rows → confirm user gets clear feedback
3. **DB params resolution**: Verify `DISTRIBUTOR_LST` and `GEOPROD_LST` are correctly resolved from `DistributorBranch` / `SupplyChainMaster`
4. **LOB filter bypass**: When `LOBCODE_LIST` is empty (no LOBs configured), verify `AND 1=1` bypass works
5. **Query timeout**: Fire a query with many distributors and verify 10s timeout fires cleanly

---

## Next Steps

1. Fix BUG-1 first (1-line change) — this unblocks 8 of 10 tests
2. Fix BUG-2 (guard + warning) — prevents misleading silent empty results
3. Fix BUG-3 (prompt improvement) — re-test Test 9
4. Re-run full battery with a real `DistrBrCode` from the live DB
5. Plan Phase 2 based on remaining failures