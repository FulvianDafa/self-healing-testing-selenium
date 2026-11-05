# Reviewer Analysis Report

Generated from the repository data on 2026-10-02. No value below is fabricated. Production configuration remains `0.50 / 0.30 / 0.20` with threshold `0.50`.

## 1. Files changed

- `SimilarityEngine.java`: `calculateCombinedScore()` now delegates to `calculateCombinedScoreWithDetail()` so `ScoreResult` is the single calculation source.
- `HealingDriver.java`: uses `ScoreResult` for ranking and both logging levels; records all discovered candidates, selection, SUT, attempt ID, position availability, and offline-only ground truth markers.
- `CandidateResult.java`, `HealingResult.java`, `HealingLogger.java`: expanded auditable CSV schema and per-SUT candidate-log routing.
- `run_reviewer_analysis.ps1`: reproducible sequential runner for the 23 SUTs found in the pre-rerun dataset.
- `reviewer_analysis.py`: validation, aggregation, metrics, and sensitivity analysis.
- Required outputs in `results/`: `final_dataset_summary.csv`, `candidate_sensitivity_results.csv`, `weight_sensitivity_results.csv`, `threshold_sensitivity_results.csv`, and this report.

The core SmartSimilarity, Levenshtein, token coverage, substring matching, candidate filtering, compatibility filtering, selection behavior, scenario IDs, production weights, and production threshold were not changed.

## 2. Compile and test result

- `mvn -q -DskipTests compile`: PASS.
- `TestAnugrahJayaHealing`: 13 tests started, 13 errors before healing because `http://anugrah_jaya.test/app/index.html` returned `ERR_CONNECTION_REFUSED`. This is classified as a blocked SUT/precondition failure, not a healing failure.
- 23-SUT execution records available: 23 of 23.

## 3. Healing-level logging validation

- Total rows: 88
- Rows with scores: 88
- Consistent rows: 88
- Mismatch rows: 0
- Maximum absolute difference: 0.000063
- Tolerance: 0.0002, accounting for four-decimal CSV rounding.

## 4. Candidate-level logging validation

- Total candidate rows: 8341
- Scored/evaluated rows: 5917
- Consistent scored rows: 5917
- Mismatch rows: 0
- Maximum absolute difference: 0.000088
- Healing attempts represented in candidate data: 88
- Ground-truth-labeled attempts: 76
- Production selection attempts replayed: 88
- Winner/threshold replay mismatches: 0
- Healing status replay mismatches: 0
- Best-score replay mismatches: 0

Filtered-out and evaluation-error candidates remain in the candidate dataset but do not receive invented scores. Every populated score comes from `ScoreResult`; sensitivity ranking uses only rows marked `EVALUATED`, preserving production compatibility filtering.

## 5. Per-SUT execution and metrics

| SUT | Designed | Executed | Attempts | Success | Failed | FP | Blocked | HSR % | FPR % | Evidence |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| amazon | 5 | 5 | 5 | 5 | 0 | 0 | 0 | 100.00 | 0.00 | none |
| bhinneka | 5 | 5 | 2 | 2 | 0 | 0 | 3 | 100.00 | 0.00 | element_or_page_timeout |
| bing | 10 | 10 | 10 | 6 | 4 | 1 | 0 | 60.00 | 16.67 | element_not_rendered |
| bisnis | 5 | 5 | 3 | 2 | 1 | 0 | 2 | 66.67 | 0.00 | element_or_page_timeout;element_not_rendered |
| cekresi | 5 | 5 | 4 | 4 | 0 | 1 | 1 | 100.00 | 25.00 | element_or_page_timeout |
| cnnindo | 5 | 5 | 3 | 3 | 0 | 0 | 2 | 100.00 | 0.00 | element_or_page_timeout |
| dicoding | 5 | 5 | 3 | 3 | 0 | 0 | 2 | 100.00 | 0.00 | element_or_page_timeout |
| ebay | 5 | 5 | 2 | 2 | 0 | 0 | 3 | 100.00 | 0.00 | element_or_page_timeout |
| indeed | 5 | 5 | 4 | 4 | 0 | 1 | 1 | 100.00 | 25.00 | element_or_page_timeout |
| katadata | 5 | 5 | 5 | 5 | 0 | 1 | 0 | 100.00 | 20.00 | test_failure_review_log |
| kompas | 5 | 5 | 2 | 2 | 0 | 1 | 3 | 100.00 | 50.00 | element_or_page_timeout |
| niagahoster | 5 | 5 | 1 | 1 | 0 | 0 | 4 | 100.00 | 0.00 | element_or_page_timeout |
| orangehrm | 10 | 10 | 10 | 10 | 0 | 0 | 0 | 100.00 | 0.00 | none |
| rumah123 | 5 | 5 | 5 | 5 | 0 | 1 | 0 | 100.00 | 20.00 | test_failure_review_log |
| saucedemo | 10 | 10 | 8 | 7 | 1 | 2 | 2 | 87.50 | 28.57 | element_or_page_timeout;element_not_rendered |
| target | 5 | 5 | 4 | 4 | 0 | 0 | 1 | 100.00 | 0.00 | CAPTCHA;element_or_page_timeout |
| tiket | 5 | 5 | 3 | 2 | 1 | 0 | 2 | 66.67 | 0.00 | element_or_page_timeout;element_not_rendered |
| tokopedia | 5 | 5 | 1 | 1 | 0 | 0 | 4 | 100.00 | 0.00 | element_or_page_timeout |
| traveloka | 5 | 5 | 3 | 3 | 0 | 0 | 2 | 100.00 | 0.00 | element_or_page_timeout |
| walmart | 5 | 5 | 2 | 2 | 0 | 0 | 3 | 100.00 | 0.00 | element_or_page_timeout |
| wikipedia | 4 | 4 | 2 | 2 | 0 | 0 | 2 | 100.00 | 0.00 | element_or_page_timeout |
| yahoo | 5 | 5 | 3 | 3 | 0 | 0 | 2 | 100.00 | 0.00 | element_or_page_timeout |
| youtube | 5 | 5 | 3 | 3 | 0 | 0 | 2 | 100.00 | 0.00 | element_or_page_timeout |

`blocked_precondition_failure` is `scenario_designed - healing_attempts`: scenarios that never reached `HealingDriver` are not counted as healing failures. Detailed process evidence is in `results/reviewer_run_logs/`.

## 6. Blocked SUTs

| SUT | Blocked scenarios | Evidence |
| --- | --- | --- |
| bhinneka | 3 | element_or_page_timeout |
| bisnis | 2 | element_or_page_timeout;element_not_rendered |
| cekresi | 1 | element_or_page_timeout |
| cnnindo | 2 | element_or_page_timeout |
| dicoding | 2 | element_or_page_timeout |
| ebay | 3 | element_or_page_timeout |
| indeed | 1 | element_or_page_timeout |
| kompas | 3 | element_or_page_timeout |
| niagahoster | 4 | element_or_page_timeout |
| saucedemo | 2 | element_or_page_timeout;element_not_rendered |
| target | 1 | CAPTCHA;element_or_page_timeout |
| tiket | 2 | element_or_page_timeout;element_not_rendered |
| tokopedia | 4 | element_or_page_timeout |
| traveloka | 2 | element_or_page_timeout |
| walmart | 3 | element_or_page_timeout |
| wikipedia | 2 | element_or_page_timeout |
| yahoo | 2 | element_or_page_timeout |
| youtube | 2 | element_or_page_timeout |

## 7. Overall metrics

- Scenario designed: 129
- Scenario executed by JUnit: 129
- Healing attempts: 88
- Successful/accepted healing: 81
- Healing failed: 7
- Objectively evaluable accepted healing: 74
- False positives in evaluable accepted healing: 8
- HSR: 92.05%
- FPR: 9.88% (same denominator convention as the historical result: all accepted healing)
- FPR among objectively evaluable accepted healing: 10.81%
- Accepted healing with unavailable objective label: 7

Observed false positives (offline ground truth only; these labels were never provided to candidate selection):

| SUT | Test case | Selected candidate | Target candidate | Score |
| --- | --- | --- | --- | --- |
| bing | BING-SH-001 | candidate-0029\|tag=input\|id=sb_imgpst\|name= | candidate-0025\|tag=textarea\|id=bingSearchInputRefactor\|name=q | 0.7132 |
| cekresi | CKR-SH-003 | candidate-0001\|tag=button\|id=cekresi\|name= | candidate-0003\|tag=a\|id=ckrLogoRefactor\|name= | 0.6406 |
| indeed | IND-SH-001 | candidate-0024\|tag=input\|id=text-input-where\|name=l | candidate-0023\|tag=input\|id=indWhatInputRefactor\|name=q | 0.6563 |
| katadata | KTD-SH-002 | candidate-0015\|tag=a\|id=\|name= | candidate-0018\|tag=a\|id=ktdLogoRefactor\|name= | 0.6250 |
| kompas | KMP-SH-002 | candidate-0007\|tag=a\|id=menu-plus\|name= | candidate-0005\|tag=a\|id=kmpLogoRefactor\|name= | 0.6667 |
| rumah123 | RMH-SH-003 | candidate-0039\|tag=a\|id=\|name= | candidate-0016\|tag=a\|id=rmhLogoRefactor\|name= | 0.6250 |
| saucedemo | SCD-SH-005 | candidate-0003\|tag=button\|id=add-to-cart-sauce-labs-bike-light\|name=add-to-cart-sauce-labs-bike-light | candidate-0002\|tag=button\|id=addToCartItem1Refactor\|name=add-to-cart-sauce-labs-backpack | 0.9375 |
| saucedemo | SCD-SH-006 | candidate-0004\|tag=button\|id=add-to-cart-sauce-labs-bolt-t-shirt\|name=add-to-cart-sauce-labs-bolt-t-shirt | candidate-0003\|tag=button\|id=addToCartItem2Refactor\|name=add-to-cart-sauce-labs-bike-light | 0.9143 |

## 8. Healing Time statistics

Population statistics across all 88 healing attempts:

- Mean: 18390.67 ms
- Median: 9301.00 ms
- Standard deviation: 19719.53 ms
- Minimum: 494.00 ms
- Maximum: 79555.00 ms

## 9. Weight sensitivity

Sensitivity uses 76 objectively labeled attempts; 12 unlabeled attempts are excluded rather than guessed.

| Configuration | Text | Locator | Position | Accepted | Correct | FP | HSR % | FPR % |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| default | 0.500000 | 0.300000 | 0.200000 | 74 | 66 | 8 | 97.37 | 10.81 |
| text_emphasis | 0.600000 | 0.250000 | 0.150000 | 74 | 66 | 8 | 97.37 | 10.81 |
| locator_emphasis | 0.400000 | 0.400000 | 0.200000 | 74 | 70 | 4 | 97.37 | 5.41 |
| position_emphasis | 0.400000 | 0.200000 | 0.400000 | 74 | 66 | 8 | 97.37 | 10.81 |
| equal | 0.333333 | 0.333333 | 0.333333 | 74 | 70 | 4 | 97.37 | 5.41 |
| text_locator_balanced | 0.450000 | 0.450000 | 0.100000 | 74 | 70 | 4 | 97.37 | 5.41 |

Weights always sum to 1. When position is unavailable, text and locator weights are normalized exactly as in production. The production defaults were not changed.

## 10. Threshold sensitivity

| Threshold | Accepted | Rejected | Correct | FP | HSR % | FPR % |
| --- | --- | --- | --- | --- | --- | --- |
| 0.30 | 74 | 2 | 66 | 8 | 97.37 | 10.81 |
| 0.40 | 74 | 2 | 66 | 8 | 97.37 | 10.81 |
| 0.50 | 74 | 2 | 66 | 8 | 97.37 | 10.81 |
| 0.60 | 74 | 2 | 66 | 8 | 97.37 | 10.81 |
| 0.70 | 67 | 9 | 64 | 3 | 88.16 | 4.48 |

## 11. Old versus new

| Dataset | Attempts | Accepted healing | HSR | False positive | FPR |
| --- | ---: | ---: | ---: | ---: | ---: |
| Previous research result (preserved, user-supplied) | 104 | 96 | 92.31% | 11 | 11.46% |
| Current rerun | 88 | 81 | 92.05% | 8 | 9.88% |

The old figures are preserved as the historical comparison and are not overwritten. Differences must not be interpreted as algorithm improvement or regression without considering coverage: live websites changed, several scenarios were blocked before healing, and external-site failures include access, network, timeout, or missing-render preconditions captured in the per-SUT evidence. The logging correction changes score provenance, not candidate selection behavior.

## 12. Conclusions

- Score validation status: **PASS**.
- Sensitivity analysis was run on all objectively labeled candidate attempts.
- Production configuration remains unchanged.
- Current metrics describe only attempts that actually reached healing; blocked scenarios are reported separately.

## 13. Limitations

- Ground truth is available only when the test mutation marker objectively identifies the target; unlabeled attempts are excluded from sensitivity and objective FPR rather than guessed.
- Public SUT DOMs, anti-bot controls, geolocation/consent variants, and network availability are time-dependent.
- Position was unavailable in 88 of 88 current healing attempts. Production text/locator normalization is therefore used, so position-weight variations can collapse to equivalent text/locator ratios.
- Candidate rows with `SKIPPED_INCOMPATIBLE` preserve filtering evidence but are excluded from reranking, matching production selection behavior.
- The historical 104/96/11 aggregate was supplied as the comparison baseline; the snapshot does not contain enough consistently labeled candidate rows to reconstruct its sensitivity analysis retroactively.
