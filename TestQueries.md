# DMS AI Debug Console — Test Queries

20 sample queries to paste into the chat input on the Debug tab.
All queries target the `CurrentStockWeight` report (loaded via `report-queries.xml`).

> Replace branch codes like `KLDEL01` with real codes from your DB for non-zero results.

---

## Group A — `fetchCSVData` Happy Path (single branch, no filters)

```
1. Current stock weight report CSV is blank for distributor branch KLDEL01
```
```
2. The stock weight CSV export shows no data for KLMUM01
```
```
3. Stock weight report not loading for branch KLCHE01 — CSV download is empty
```
```
4. CSV file downloaded from current stock weight report is empty for KLHYD01
```
```
5. Why is the current stock weight report blank for distributor KLBNG01?
```

---

## Group B — `fetchCSVData` With Godown Filter

```
6. Current stock weight CSV blank for KLDEL01, godown code GDN001
```
```
7. Stock weight report empty for branch KLMUM01 and godown GDNMUM01
```

---

## Group C — `fetchCSVData` With Product / Batch Status Filter

```
8. Current stock weight report for KLDEL01 — only active products should appear but CSV is blank
```
```
9. Stock weight report shows no results for KLCHE01 when filtering for active products only
```
```
10. CSV blank for KLDEL01, I need only active batches to show in the stock weight report
```
```
11. Stock weight report for KLHYD01 showing nothing — filter is set to inactive batches only
```
```
12. Current stock weight CSV blank for KLBNG01 with inactive products filter
```

---

## Group D — `fetchCSVDataTotal` (Aggregated / Total Report)

```
13. Stock weight total summary report is blank for branch KLDEL01
```
```
14. The fetchCSVDataTotal report is showing no rows for distributor KLMUM01
```
```
15. Total stock weight summary is empty for KLCHE01 — CSV has no data
```

---

## Group E — Multiple Distributor Branches

```
16. Current stock weight CSV is blank for both KLDEL01 and KLMUM01
```
```
17. Stock weight report empty for branches KLCHE01 and KLHYD01 — CSV download issue
```

---

## Group F — Alternate Phrasings / Symptom-First

```
18. The CSV download button on the current stock weight screen produces an empty file for KLDEL01
```
```
19. L3 issue: current stock weight report not returning any rows for KLBNG01, need to diagnose
```
```
20. Stock weight screen — distributor KLDEL01 sees blank table and empty CSV, other distributors work fine
```

---

## Expected Behaviour (after all bug fixes applied)

| What you see | Meaning |
|---|---|
| SQL block rendered with `SELECT DISTINCT D.DistrCode...` | XML query extracted correctly |
| `OK rows=0` | Branch code not in DB — use a real code |
| `OK rows=N` (N > 0) | Full end-to-end working — data returned |
| Warning banner: "No distributor branch code found..." | Forgot to include a branch code in the issue |
| No SQL block + explanation text | Issue type has no matching query in uploaded XML |
