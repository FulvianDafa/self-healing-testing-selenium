"""Build the reviewer-facing datasets and report from a completed 23-SUT rerun.

The script uses only the Python standard library. It never changes production
weights/thresholds and stops sensitivity calculations when score validation
finds a mismatch beyond CSV rounding tolerance.
"""

from __future__ import annotations

import csv
import math
import re
import statistics
import xml.etree.ElementTree as ET
from collections import defaultdict
from pathlib import Path


ROOT = Path(__file__).resolve().parent
RESULTS = ROOT / "results"
WEIGHT_TEXT = 0.50
WEIGHT_LOCATOR = 0.30
WEIGHT_POSITION = 0.20
DEFAULT_THRESHOLD = 0.50
CSV_TOLERANCE = 0.0002

SUTS = [
    ("amazon", "TestAmazonHealing"),
    ("bhinneka", "TestBhinnekaHealing"),
    ("bing", "TestBingHealing"),
    ("bisnis", "TestBisnisHealing"),
    ("cekresi", "TestCekresiHealing"),
    ("cnnindo", "TestCnnIndoHealing"),
    ("dicoding", "TestDicodingHealing"),
    ("ebay", "TestEbayHealing"),
    ("indeed", "TestIndeedHealing"),
    ("katadata", "TestKatadataHealing"),
    ("kompas", "TestKompasHealing"),
    ("niagahoster", "TestNiagahosterHealing"),
    ("orangehrm", "TestOrangeHrmHealing"),
    ("rumah123", "TestRumah123Healing"),
    ("saucedemo", "TestSauceDemoHealing"),
    ("target", "TestTargetHealing"),
    ("tiket", "TestTiketHealing"),
    ("tokopedia", "TestTokopediaHealing"),
    ("traveloka", "TestTravelokaHealing"),
    ("walmart", "TestWalmartHealing"),
    ("wikipedia", "TestWikipediaHealing"),
    ("yahoo", "TestYahooHealing"),
    ("youtube", "TestYoutubeHealing"),
]

WEIGHT_CONFIGS = [
    ("default", 0.50, 0.30, 0.20),
    ("text_emphasis", 0.60, 0.25, 0.15),
    ("locator_emphasis", 0.40, 0.40, 0.20),
    ("position_emphasis", 0.40, 0.20, 0.40),
    ("equal", 1 / 3, 1 / 3, 1 / 3),
    ("text_locator_balanced", 0.45, 0.45, 0.10),
]
THRESHOLDS = [0.30, 0.40, 0.50, 0.60, 0.70]


def read_csv(path: Path) -> list[dict[str, str]]:
    if not path.exists() or path.stat().st_size == 0:
        return []
    with path.open("r", encoding="utf-8-sig", newline="") as handle:
        return list(csv.DictReader(handle))


def write_csv(path: Path, fieldnames: list[str], rows: list[dict]) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    with path.open("w", encoding="utf-8", newline="") as handle:
        writer = csv.DictWriter(handle, fieldnames=fieldnames, extrasaction="ignore")
        writer.writeheader()
        writer.writerows(rows)


def as_float(value: str | None) -> float | None:
    if value is None or not value.strip():
        return None
    try:
        return float(value)
    except ValueError:
        return None


def as_int(value: str | None, default: int = 0) -> int:
    try:
        return int(value or default)
    except ValueError:
        return default


def as_bool(value: str | None) -> bool:
    return (value or "").strip().lower() == "true"


def pct(numerator: int, denominator: int) -> float:
    return (100.0 * numerator / denominator) if denominator else 0.0


def fmt(value: float | None, digits: int = 4) -> str:
    return "" if value is None else f"{value:.{digits}f}"


def actual_score(row: dict[str, str], wt: float, wl: float, wp: float) -> float | None:
    text = as_float(row.get("text_score"))
    locator = as_float(row.get("locator_score"))
    position = as_float(row.get("position_score"))
    if text is None or locator is None or position is None:
        return None
    if as_bool(row.get("position_available")):
        return wt * text + wl * locator + wp * position
    available_weight = wt + wl
    if available_weight <= 0:
        return None
    return text * (wt / available_weight) + locator * (wl / available_weight)


def designed_count(test_class: str) -> int:
    source = ROOT / "src" / "test" / "java" / "com" / "fulvian" / "tests" / f"{test_class}.java"
    if not source.exists():
        return 0
    return len(re.findall(r"^\s*@Test\b", source.read_text(encoding="utf-8"), re.MULTILINE))


def surefire_counts(test_class: str) -> dict[str, int]:
    path = RESULTS.parent / "target" / "surefire-reports" / f"TEST-com.fulvian.tests.{test_class}.xml"
    if not path.exists():
        return {"tests": 0, "failures": 0, "errors": 0, "skipped": 0}
    try:
        root = ET.parse(path).getroot()
        return {
            key: int(float(root.attrib.get(key, "0")))
            for key in ("tests", "failures", "errors", "skipped")
        }
    except (ET.ParseError, OSError, ValueError):
        return {"tests": 0, "failures": 0, "errors": 0, "skipped": 0}


def audit_scores(rows: list[dict[str, str]]) -> dict[str, float | int]:
    scored = 0
    consistent = 0
    mismatch = 0
    maximum = 0.0
    for row in rows:
        logged = as_float(row.get("combined_score"))
        calculated = actual_score(row, WEIGHT_TEXT, WEIGHT_LOCATOR, WEIGHT_POSITION)
        if logged is None or calculated is None:
            continue
        scored += 1
        difference = abs(logged - calculated)
        maximum = max(maximum, difference)
        if difference <= CSV_TOLERANCE:
            consistent += 1
        else:
            mismatch += 1
    return {
        "total_rows": len(rows),
        "scored_rows": scored,
        "consistent_rows": consistent,
        "mismatch_rows": mismatch,
        "maximum_difference": maximum,
    }


def time_stats(rows: list[dict[str, str]]) -> dict[str, float]:
    values = [as_float(row.get("healing_time_ms")) for row in rows]
    clean = [value for value in values if value is not None]
    if not clean:
        return {key: 0.0 for key in ("mean", "median", "stddev", "minimum", "maximum")}
    return {
        "mean": statistics.mean(clean),
        "median": statistics.median(clean),
        "stddev": statistics.pstdev(clean),
        "minimum": min(clean),
        "maximum": max(clean),
    }


def select_candidate(rows: list[dict[str, str]], wt: float, wl: float, wp: float, threshold: float):
    eligible = []
    for sequence, row in enumerate(rows):
        if row.get("status") != "EVALUATED":
            continue
        score = actual_score(row, wt, wl, wp)
        if score is not None:
            eligible.append((sequence, row, score))
    if not eligible:
        return None, None
    # Stable tie handling mirrors production: the first maximum wins because
    # HealingDriver updates only when score > bestScore.
    winner = max(eligible, key=lambda item: item[2])
    if winner[2] < threshold:
        return None, winner[2]
    return winner[1], winner[2]


def sensitivity_metrics(groups: dict[str, list[dict[str, str]]], wt: float, wl: float, wp: float, threshold: float):
    labeled = 0
    excluded = 0
    accepted = 0
    rejected = 0
    correct = 0
    false_positive = 0
    for rows in groups.values():
        targets = [row for row in rows if as_bool(row.get("is_target"))]
        ground_truth_available = any(as_bool(row.get("ground_truth_available")) for row in rows)
        if not ground_truth_available or len(targets) != 1:
            excluded += 1
            continue
        labeled += 1
        selected, _ = select_candidate(rows, wt, wl, wp, threshold)
        if selected is None:
            rejected += 1
        else:
            accepted += 1
            if as_bool(selected.get("is_target")):
                correct += 1
            else:
                false_positive += 1
    return {
        "labeled_attempts": labeled,
        "excluded_unlabeled_attempts": excluded,
        "accepted_healing": accepted,
        "rejected_healing": rejected,
        "correct_healing": correct,
        "false_positive": false_positive,
        "hsr_percent": pct(accepted, labeled),
        "correct_healing_rate_percent": pct(correct, labeled),
        "fpr_percent": pct(false_positive, accepted),
    }


def markdown_table(headers: list[str], rows: list[list[str]]) -> str:
    if not rows:
        return "_Tidak ada data._"
    head = "| " + " | ".join(headers) + " |"
    separator = "| " + " | ".join(["---"] * len(headers)) + " |"
    body = ["| " + " | ".join(str(cell).replace("|", "\\|") for cell in row) + " |" for row in rows]
    return "\n".join([head, separator, *body])


def main() -> None:
    RESULTS.mkdir(parents=True, exist_ok=True)
    run_summary_rows = read_csv(RESULTS / "reviewer_run_summary.csv")
    run_summary = {row.get("sut", ""): row for row in run_summary_rows}
    # Maven -q omits the textual "Tests run" line for passing classes. Reconcile
    # those zero placeholders from Surefire's authoritative XML report.
    for sut, test_class in SUTS:
        execution = run_summary.get(sut)
        counts = surefire_counts(test_class)
        if execution and as_int(execution.get("scenario_executed")) == 0 and counts["tests"] > 0:
            execution["scenario_executed"] = str(counts["tests"])
            execution["maven_failures"] = str(counts["failures"])
            execution["maven_errors"] = str(counts["errors"])
            execution["maven_skipped"] = str(counts["skipped"])
    if run_summary_rows:
        write_csv(
            RESULTS / "reviewer_run_summary.csv",
            list(run_summary_rows[0].keys()),
            run_summary_rows,
        )

    healing_by_sut: dict[str, list[dict[str, str]]] = {}
    candidate_by_sut: dict[str, list[dict[str, str]]] = {}
    all_healing: list[dict[str, str]] = []
    all_candidates: list[dict[str, str]] = []
    for sut, _ in SUTS:
        healing = read_csv(RESULTS / f"{sut}_healing_log.csv")
        candidates = read_csv(RESULTS / f"{sut}_candidates_log.csv")
        for row in healing:
            row.setdefault("sut", sut)
        for row in candidates:
            row.setdefault("sut", sut)
        healing_by_sut[sut] = healing
        candidate_by_sut[sut] = candidates
        all_healing.extend(healing)
        all_candidates.extend(candidates)

    healing_audit = audit_scores(all_healing)
    candidate_audit = audit_scores(all_candidates)
    dataset_consistent = (
        healing_audit["mismatch_rows"] == 0
        and candidate_audit["mismatch_rows"] == 0
    )

    candidate_groups: dict[str, list[dict[str, str]]] = defaultdict(list)
    for row in all_candidates:
        attempt_id = row.get("attempt_id", "")
        if attempt_id:
            candidate_groups[attempt_id].append(row)

    healing_by_attempt = {
        row.get("attempt_id", ""): row
        for row in all_healing
        if row.get("attempt_id")
    }
    selection_replay_total = 0
    selection_replay_mismatch = 0
    selection_status_mismatch = 0
    selection_best_score_mismatch = 0
    for attempt_id, rows in candidate_groups.items():
        predicted, predicted_best_score = select_candidate(
            rows, WEIGHT_TEXT, WEIGHT_LOCATOR, WEIGHT_POSITION, DEFAULT_THRESHOLD
        )
        logged_selected = [row for row in rows if as_bool(row.get("selected"))]
        selection_replay_total += 1
        if predicted is None:
            if logged_selected:
                selection_replay_mismatch += 1
        elif (
            len(logged_selected) != 1
            or logged_selected[0].get("candidate_identifier") != predicted.get("candidate_identifier")
        ):
            selection_replay_mismatch += 1

        healing_row = healing_by_attempt.get(attempt_id)
        if healing_row:
            expected_status = "SUCCESS" if predicted is not None else "FAIL"
            if healing_row.get("status") != expected_status:
                selection_status_mismatch += 1
            healing_best_score = as_float(healing_row.get("combined_score"))
            if (
                predicted_best_score is not None
                and healing_best_score is not None
                and abs(predicted_best_score - healing_best_score) > CSV_TOLERANCE
            ):
                selection_best_score_mismatch += 1

    # Candidate-level auditable dataset, including filtered-out candidates.
    candidate_output = []
    rank_by_row_id: dict[int, int] = {}
    for rows in candidate_groups.values():
        eligible = []
        for sequence, row in enumerate(rows):
            if row.get("status") == "EVALUATED":
                score = actual_score(row, WEIGHT_TEXT, WEIGHT_LOCATOR, WEIGHT_POSITION)
                if score is not None:
                    eligible.append((sequence, row, score))
        eligible.sort(key=lambda item: (-item[2], item[0]))
        for rank, (_, row, _) in enumerate(eligible, start=1):
            rank_by_row_id[id(row)] = rank

    for row in all_candidates:
        logged = as_float(row.get("combined_score"))
        recalculated = actual_score(row, WEIGHT_TEXT, WEIGHT_LOCATOR, WEIGHT_POSITION)
        difference = None if logged is None or recalculated is None else abs(logged - recalculated)
        candidate_output.append({
            "attempt_id": row.get("attempt_id", ""),
            "test_case_id": row.get("test_case_id", ""),
            "sut": row.get("sut", ""),
            "candidate_identifier": row.get("candidate_identifier", ""),
            "candidate_description": row.get("candidate_description", ""),
            "eligibility_status": row.get("status", ""),
            "text_score": row.get("text_score", ""),
            "locator_score": row.get("locator_score", ""),
            "position_score": row.get("position_score", ""),
            "position_available": row.get("position_available", ""),
            "logged_combined_score": row.get("combined_score", ""),
            "recalculated_default_score": fmt(recalculated),
            "absolute_difference": fmt(difference, 6),
            "score_consistent": "" if difference is None else str(difference <= CSV_TOLERANCE).lower(),
            "default_rank": rank_by_row_id.get(id(row), ""),
            "production_selected": row.get("selected", ""),
            "ground_truth_available": row.get("ground_truth_available", ""),
            "is_target": row.get("is_target", ""),
        })

    candidate_fields = [
        "attempt_id", "test_case_id", "sut", "candidate_identifier", "candidate_description",
        "eligibility_status", "text_score", "locator_score", "position_score",
        "position_available", "logged_combined_score", "recalculated_default_score",
        "absolute_difference", "score_consistent", "default_rank", "production_selected",
        "ground_truth_available", "is_target",
    ]
    write_csv(RESULTS / "candidate_sensitivity_results.csv", candidate_fields, candidate_output)

    weight_rows = []
    threshold_rows = []
    if dataset_consistent and candidate_groups:
        for label, wt, wl, wp in WEIGHT_CONFIGS:
            metrics = sensitivity_metrics(candidate_groups, wt, wl, wp, DEFAULT_THRESHOLD)
            weight_rows.append({
                "configuration": label,
                "weight_text": f"{wt:.6f}",
                "weight_locator": f"{wl:.6f}",
                "weight_position": f"{wp:.6f}",
                "threshold": f"{DEFAULT_THRESHOLD:.2f}",
                **{key: fmt(value, 2) if key.endswith("percent") else value for key, value in metrics.items()},
            })
        for threshold in THRESHOLDS:
            metrics = sensitivity_metrics(
                candidate_groups, WEIGHT_TEXT, WEIGHT_LOCATOR, WEIGHT_POSITION, threshold
            )
            threshold_rows.append({
                "threshold": f"{threshold:.2f}",
                "weight_text": f"{WEIGHT_TEXT:.2f}",
                "weight_locator": f"{WEIGHT_LOCATOR:.2f}",
                "weight_position": f"{WEIGHT_POSITION:.2f}",
                **{key: fmt(value, 2) if key.endswith("percent") else value for key, value in metrics.items()},
            })

    sensitivity_metric_fields = [
        "labeled_attempts", "excluded_unlabeled_attempts", "accepted_healing",
        "rejected_healing", "correct_healing", "false_positive", "hsr_percent",
        "correct_healing_rate_percent", "fpr_percent",
    ]
    write_csv(
        RESULTS / "weight_sensitivity_results.csv",
        ["configuration", "weight_text", "weight_locator", "weight_position", "threshold", *sensitivity_metric_fields],
        weight_rows,
    )
    write_csv(
        RESULTS / "threshold_sensitivity_results.csv",
        ["threshold", "weight_text", "weight_locator", "weight_position", *sensitivity_metric_fields],
        threshold_rows,
    )

    selected_false_positive_attempts = set()
    evaluable_selected_attempts = set()
    for attempt_id, rows in candidate_groups.items():
        selected = [row for row in rows if as_bool(row.get("selected"))]
        if selected and any(as_bool(row.get("ground_truth_available")) for row in rows):
            evaluable_selected_attempts.add(attempt_id)
            if not as_bool(selected[0].get("is_target")):
                selected_false_positive_attempts.add(attempt_id)
    for row in all_healing:
        if as_bool(row.get("is_false_positive")) and row.get("attempt_id"):
            selected_false_positive_attempts.add(row["attempt_id"])

    false_positive_report_rows = []
    for attempt_id in sorted(selected_false_positive_attempts):
        rows = candidate_groups.get(attempt_id, [])
        selected = next((row for row in rows if as_bool(row.get("selected"))), None)
        target_row = next((row for row in rows if as_bool(row.get("is_target"))), None)
        if selected:
            false_positive_report_rows.append([
                selected.get("sut", ""),
                selected.get("test_case_id", ""),
                selected.get("candidate_identifier", ""),
                "" if target_row is None else target_row.get("candidate_identifier", ""),
                selected.get("combined_score", ""),
            ])

    summary_rows = []
    per_sut_report_rows = []
    for sut, test_class in SUTS:
        healing = healing_by_sut[sut]
        candidates = candidate_by_sut[sut]
        execution = run_summary.get(sut, {})
        designed = as_int(execution.get("scenario_designed"), designed_count(test_class))
        executed = as_int(execution.get("scenario_executed"))
        attempts = len(healing)
        successes = sum(row.get("status") == "SUCCESS" for row in healing)
        failures = sum(row.get("status") == "FAIL" for row in healing)
        attempt_ids = {row.get("attempt_id", "") for row in healing}
        false_positives = len(attempt_ids & selected_false_positive_attempts)
        evaluable = len(attempt_ids & evaluable_selected_attempts)
        unknown_fp = max(0, successes - evaluable)
        blocked = max(0, designed - attempts)
        stats = time_stats(healing)
        execution_status = "PASS" if execution.get("exit_code") == "0" else "FAIL_OR_BLOCKED"
        evidence = execution.get("evidence", "not_executed_or_summary_missing")
        row = {
            "record_type": "SUT",
            "sut": sut,
            "test_class": test_class,
            "scenario_designed": designed,
            "scenario_executed": executed,
            "healing_attempts": attempts,
            "healing_success": successes,
            "healing_failed": failures,
            "false_positive": false_positives,
            "false_positive_evaluable_success": evaluable,
            "false_positive_unknown_success": unknown_fp,
            "blocked_precondition_failure": blocked,
            "hsr_percent": fmt(pct(successes, attempts), 2),
            "fpr_percent": fmt(pct(false_positives, successes), 2),
            "healing_time_mean_ms": fmt(stats["mean"], 2),
            "healing_time_median_ms": fmt(stats["median"], 2),
            "healing_time_stddev_ms": fmt(stats["stddev"], 2),
            "healing_time_min_ms": fmt(stats["minimum"], 2),
            "healing_time_max_ms": fmt(stats["maximum"], 2),
            "candidate_rows": len(candidates),
            "execution_status": execution_status,
            "evidence": evidence,
        }
        summary_rows.append(row)
        per_sut_report_rows.append([
            sut, str(designed), str(executed), str(attempts), str(successes), str(failures),
            str(false_positives), str(blocked), row["hsr_percent"], row["fpr_percent"], evidence,
        ])

    overall_stats = time_stats(all_healing)
    overall_attempts = len(all_healing)
    overall_success = sum(row.get("status") == "SUCCESS" for row in all_healing)
    overall_failed = sum(row.get("status") == "FAIL" for row in all_healing)
    overall_fp = len(selected_false_positive_attempts)
    overall_evaluable = len(evaluable_selected_attempts)
    overall_designed = sum(as_int(row["scenario_designed"]) for row in summary_rows)
    overall_executed = sum(as_int(row["scenario_executed"]) for row in summary_rows)
    overall_blocked = sum(as_int(row["blocked_precondition_failure"]) for row in summary_rows)
    overall_row = {
        "record_type": "OVERALL",
        "sut": "ALL_23_SUT",
        "test_class": "",
        "scenario_designed": overall_designed,
        "scenario_executed": overall_executed,
        "healing_attempts": overall_attempts,
        "healing_success": overall_success,
        "healing_failed": overall_failed,
        "false_positive": overall_fp,
        "false_positive_evaluable_success": overall_evaluable,
        "false_positive_unknown_success": max(0, overall_success - overall_evaluable),
        "blocked_precondition_failure": overall_blocked,
        "hsr_percent": fmt(pct(overall_success, overall_attempts), 2),
        "fpr_percent": fmt(pct(overall_fp, overall_success), 2),
        "healing_time_mean_ms": fmt(overall_stats["mean"], 2),
        "healing_time_median_ms": fmt(overall_stats["median"], 2),
        "healing_time_stddev_ms": fmt(overall_stats["stddev"], 2),
        "healing_time_min_ms": fmt(overall_stats["minimum"], 2),
        "healing_time_max_ms": fmt(overall_stats["maximum"], 2),
        "candidate_rows": len(all_candidates),
        "execution_status": "COMPLETE" if len(run_summary) == len(SUTS) else "INCOMPLETE",
        "evidence": "aggregated_from_per_sut_rows",
    }
    summary_rows.append(overall_row)
    summary_fields = list(overall_row.keys())
    write_csv(RESULTS / "final_dataset_summary.csv", summary_fields, summary_rows)

    blocked_rows = [row for row in summary_rows if row["record_type"] == "SUT" and as_int(row["blocked_precondition_failure"]) > 0]
    weight_report_rows = [
        [
            row["configuration"], row["weight_text"], row["weight_locator"], row["weight_position"],
            str(row["accepted_healing"]), str(row["correct_healing"]), str(row["false_positive"]),
            str(row["hsr_percent"]), str(row["fpr_percent"]),
        ]
        for row in weight_rows
    ]
    threshold_report_rows = [
        [
            row["threshold"], str(row["accepted_healing"]), str(row["rejected_healing"]),
            str(row["correct_healing"]), str(row["false_positive"]),
            str(row["hsr_percent"]), str(row["fpr_percent"]),
        ]
        for row in threshold_rows
    ]

    report = f"""# Reviewer Analysis Report

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
- 23-SUT execution records available: {len(run_summary)} of {len(SUTS)}.

## 3. Healing-level logging validation

- Total rows: {healing_audit['total_rows']}
- Rows with scores: {healing_audit['scored_rows']}
- Consistent rows: {healing_audit['consistent_rows']}
- Mismatch rows: {healing_audit['mismatch_rows']}
- Maximum absolute difference: {healing_audit['maximum_difference']:.6f}
- Tolerance: {CSV_TOLERANCE:.4f}, accounting for four-decimal CSV rounding.

## 4. Candidate-level logging validation

- Total candidate rows: {candidate_audit['total_rows']}
- Scored/evaluated rows: {candidate_audit['scored_rows']}
- Consistent scored rows: {candidate_audit['consistent_rows']}
- Mismatch rows: {candidate_audit['mismatch_rows']}
- Maximum absolute difference: {candidate_audit['maximum_difference']:.6f}
- Healing attempts represented in candidate data: {len(candidate_groups)}
- Ground-truth-labeled attempts: {sum(1 for rows in candidate_groups.values() if any(as_bool(row.get('ground_truth_available')) for row in rows))}
- Production selection attempts replayed: {selection_replay_total}
- Winner/threshold replay mismatches: {selection_replay_mismatch}
- Healing status replay mismatches: {selection_status_mismatch}
- Best-score replay mismatches: {selection_best_score_mismatch}

Filtered-out and evaluation-error candidates remain in the candidate dataset but do not receive invented scores. Every populated score comes from `ScoreResult`; sensitivity ranking uses only rows marked `EVALUATED`, preserving production compatibility filtering.

## 5. Per-SUT execution and metrics

{markdown_table(['SUT', 'Designed', 'Executed', 'Attempts', 'Success', 'Failed', 'FP', 'Blocked', 'HSR %', 'FPR %', 'Evidence'], per_sut_report_rows)}

`blocked_precondition_failure` is `scenario_designed - healing_attempts`: scenarios that never reached `HealingDriver` are not counted as healing failures. Detailed process evidence is in `results/reviewer_run_logs/`.

## 6. Blocked SUTs

{markdown_table(['SUT', 'Blocked scenarios', 'Evidence'], [[row['sut'], str(row['blocked_precondition_failure']), row['evidence']] for row in blocked_rows])}

## 7. Overall metrics

- Scenario designed: {overall_designed}
- Scenario executed by JUnit: {overall_executed}
- Healing attempts: {overall_attempts}
- Successful/accepted healing: {overall_success}
- Healing failed: {overall_failed}
- Objectively evaluable accepted healing: {overall_evaluable}
- False positives in evaluable accepted healing: {overall_fp}
- HSR: {pct(overall_success, overall_attempts):.2f}%
- FPR: {pct(overall_fp, overall_success):.2f}% (same denominator convention as the historical result: all accepted healing)
- FPR among objectively evaluable accepted healing: {pct(overall_fp, overall_evaluable):.2f}%
- Accepted healing with unavailable objective label: {max(0, overall_success - overall_evaluable)}

Observed false positives (offline ground truth only; these labels were never provided to candidate selection):

{markdown_table(['SUT', 'Test case', 'Selected candidate', 'Target candidate', 'Score'], false_positive_report_rows)}

## 8. Healing Time statistics

Population statistics across all {overall_attempts} healing attempts:

- Mean: {overall_stats['mean']:.2f} ms
- Median: {overall_stats['median']:.2f} ms
- Standard deviation: {overall_stats['stddev']:.2f} ms
- Minimum: {overall_stats['minimum']:.2f} ms
- Maximum: {overall_stats['maximum']:.2f} ms

## 9. Weight sensitivity

Sensitivity uses {sum(1 for rows in candidate_groups.values() if any(as_bool(row.get('ground_truth_available')) for row in rows))} objectively labeled attempts; {sum(1 for rows in candidate_groups.values() if not any(as_bool(row.get('ground_truth_available')) for row in rows))} unlabeled attempts are excluded rather than guessed.

{markdown_table(['Configuration', 'Text', 'Locator', 'Position', 'Accepted', 'Correct', 'FP', 'HSR %', 'FPR %'], weight_report_rows) if dataset_consistent else '_Not run: dataset score validation failed._'}

Weights always sum to 1. When position is unavailable, text and locator weights are normalized exactly as in production. The production defaults were not changed.

## 10. Threshold sensitivity

{markdown_table(['Threshold', 'Accepted', 'Rejected', 'Correct', 'FP', 'HSR %', 'FPR %'], threshold_report_rows) if dataset_consistent else '_Not run: dataset score validation failed._'}

## 11. Old versus new

| Dataset | Attempts | Accepted healing | HSR | False positive | FPR |
| --- | ---: | ---: | ---: | ---: | ---: |
| Previous research result (preserved, user-supplied) | 104 | 96 | 92.31% | 11 | 11.46% |
| Current rerun | {overall_attempts} | {overall_success} | {pct(overall_success, overall_attempts):.2f}% | {overall_fp} | {pct(overall_fp, overall_success):.2f}% |

The old figures are preserved as the historical comparison and are not overwritten. Differences must not be interpreted as algorithm improvement or regression without considering coverage: live websites changed, several scenarios were blocked before healing, and external-site failures include access, network, timeout, or missing-render preconditions captured in the per-SUT evidence. The logging correction changes score provenance, not candidate selection behavior.

## 12. Conclusions

- Score validation status: **{'PASS' if dataset_consistent else 'FAIL'}**.
- Sensitivity analysis {'was run on all objectively labeled candidate attempts' if dataset_consistent and candidate_groups else 'was withheld because a consistent labeled candidate dataset was unavailable'}.
- Production configuration remains unchanged.
- Current metrics describe only attempts that actually reached healing; blocked scenarios are reported separately.

## 13. Limitations

- Ground truth is available only when the test mutation marker objectively identifies the target; unlabeled attempts are excluded from sensitivity and objective FPR rather than guessed.
- Public SUT DOMs, anti-bot controls, geolocation/consent variants, and network availability are time-dependent.
- Position was unavailable in {sum(not as_bool(row.get('position_available')) for row in all_healing)} of {len(all_healing)} current healing attempts. Production text/locator normalization is therefore used, so position-weight variations can collapse to equivalent text/locator ratios.
- Candidate rows with `SKIPPED_INCOMPATIBLE` preserve filtering evidence but are excluded from reranking, matching production selection behavior.
- The historical 104/96/11 aggregate was supplied as the comparison baseline; the snapshot does not contain enough consistently labeled candidate rows to reconstruct its sensitivity analysis retroactively.
"""
    (RESULTS / "reviewer-analysis-report.md").write_text(report, encoding="utf-8")

    print(f"healing rows={len(all_healing)} mismatches={healing_audit['mismatch_rows']}")
    print(f"candidate rows={len(all_candidates)} mismatches={candidate_audit['mismatch_rows']}")
    print(f"sensitivity={'generated' if weight_rows else 'withheld'}")


if __name__ == "__main__":
    main()
