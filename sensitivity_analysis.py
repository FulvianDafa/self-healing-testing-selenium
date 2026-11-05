import csv
from itertools import product

# ============================================================
# SENSITIVITY ANALYSIS — berbasis data CSV tervalidasi
# Tidak mengubah production code. Semua simulasi berbasis data.
# ============================================================

# -- Load data --
rows = []
with open('results/healing_log.csv', 'r', encoding='utf-8') as f:
    for row in csv.DictReader(f):
        rows.append({
            'tc':       row['test_case_id'],
            'text':     float(row['text_score']),
            'locator':  float(row['locator_score']),
            'position': float(row['position_score']),
            'logged':   float(row['combined_score']),
            'fp':       row['is_false_positive'].strip().lower() == 'true',
        })

# Production defaults
DEFAULT_WT   = 0.50
DEFAULT_WL   = 0.30

DEFAULT_WP   = 0.20
DEFAULT_THR  = 0.50

def simulate(rows, wt, wl, wp, threshold):
    """
    Hitung ulang combined score setiap row menggunakan bobot wt/wl/wp
    lalu bandingkan dengan threshold untuk menentukan SUCCESS/FAIL.
    Position score = 0 pada semua row ini (lastKnownLocation null),
    sehingga bobot dinormalisasi menjadi wt/(wt+wl) dan wl/(wt+wl).
    """
    healed = 0
    false_positive = 0
    total = len(rows)
    for row in rows:
        t = row['text']
        l = row['locator']
        p = row['position']
        if p == 0.0:
            tw = wt + wl
            score = t * (wt / tw) + l * (wl / tw)
        else:
            score = wt * t + wl * l + wp * p
        if score >= threshold:
            healed += 1
            if row['fp']:
                false_positive += 1
    hsr = healed / total * 100 if total > 0 else 0
    fpr = false_positive / healed * 100 if healed > 0 else 0
    return healed, false_positive, hsr, fpr

# ============================================================
# A. Weight Sensitivity
# ============================================================
print("=" * 70)
print("A. WEIGHT SENSITIVITY ANALYSIS (threshold fixed = 0.50)")
print("=" * 70)
print(f"{'Config':<30} | {'Healed':>6} | {'HSR%':>6} | {'FP':>3} | {'FPR%':>5}")
print("-" * 70)

weight_configs = [
    ("Default (0.50/0.30/0.20)",   0.50, 0.30, 0.20),
    ("Text++  (0.60/0.30/0.10)",   0.60, 0.30, 0.10),
    ("Text++  (0.70/0.20/0.10)",   0.70, 0.20, 0.10),
    ("Loc++   (0.40/0.40/0.20)",   0.40, 0.40, 0.20),
    ("Loc++   (0.30/0.50/0.20)",   0.30, 0.50, 0.20),
    ("Equal   (0.33/0.33/0.34)",   0.33, 0.33, 0.34),
    ("Pos++   (0.40/0.20/0.40)",   0.40, 0.20, 0.40),
    ("TextOnly(1.00/0.00/0.00)",   1.00, 0.00, 0.00),
    ("LocOnly (0.00/1.00/0.00)",   0.00, 1.00, 0.00),
]

for (label, wt, wl, wp) in weight_configs:
    healed, fp, hsr, fpr = simulate(rows, wt, wl, wp, DEFAULT_THR)
    marker = " <-- DEFAULT" if label.startswith("Default") else ""
    print(f"{label:<30} | {healed:>6} | {hsr:>5.1f}% | {fp:>3} | {fpr:>4.1f}%{marker}")

# ============================================================
# B. Threshold Sensitivity
# ============================================================
print()
print("=" * 70)
print("B. THRESHOLD SENSITIVITY ANALYSIS (weights fixed = default)")
print("=" * 70)
print(f"{'Threshold':<12} | {'Healed':>6} | {'HSR%':>6} | {'FP':>3} | {'FPR%':>5}")
print("-" * 70)

thresholds = [0.30, 0.40, 0.45, 0.50, 0.55, 0.60, 0.65, 0.70, 0.75, 0.80]

for thr in thresholds:
    healed, fp, hsr, fpr = simulate(rows, DEFAULT_WT, DEFAULT_WL, DEFAULT_WP, thr)
    marker = " <-- DEFAULT" if abs(thr - 0.50) < 1e-6 else ""
    print(f"{thr:<12.2f} | {healed:>6} | {hsr:>5.1f}% | {fp:>3} | {fpr:>4.1f}%{marker}")

# ============================================================
# C. Per-TC breakdown (raw scores, useful for reviewer)
# ============================================================
print()
print("=" * 70)
print("C. PER-TC SCORES (untuk verifikasi manual reviewer)")
print("=" * 70)
print(f"{'TC':<10} | {'text':>6} | {'loc':>6} | {'pos':>6} | {'combined':>8} | STATUS")
print("-" * 70)
for row in rows:
    print(f"{row['tc']:<10} | {row['text']:>6.4f} | {row['locator']:>6.4f} | {row['position']:>6.4f} | {row['logged']:>8.4f} | SUCCESS")
