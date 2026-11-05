import pandas as pd
import numpy as np
from sklearn.linear_model import LogisticRegression
from sklearn.model_selection import GroupKFold
import time
import json
import sys
import os

os.makedirs('results', exist_ok=True)

# 1. Read dataset
try:
    df = pd.read_csv('results/candidates_log.csv')
except Exception as e:
    print(f"Error reading CSV: {e}")
    sys.exit(1)

# 2. Filter EVALUATED
df = df[df['status'] == 'EVALUATED'].copy()

# 3. Exclude AUTO-001 (Edit button row 1)
df = df[df['test_case_id'] != 'AUTO-001'].copy()

if df.empty:
    print("Dataset kosong setelah filter.")
    sys.exit(1)

# 4. Map ground truth IDs
def get_is_target(row):
    desc = str(row['candidate_description'])
    tc = str(row['test_case_id'])
    orig = str(row['original_locator'])
    
    # Ground truth mapping based on audit
    targets = {
        'AUTO-003': 'saveTransactionButtonRefactor',
        'AUTO-004': 'paidInputRefactor',
        'AUTO-005': 'addDebtorButtonRefactor',
        'AUTO-006': 'inputDebtorNameRefactor',
        'AUTO-007': 'tabDebtRefactor',
        'AUTO-008': 'productSearchDashboardRefactor',
        'AUTO-010': 'layoutTableButtonRefactor'
    }
    
    target_id = targets.get(tc)
    
    if target_id is None and "searchInput" in orig:
        possible_targets = [
            "id='productSearchInputRefactor'", 
            "id='transactionSearchInputRefactor'", 
            "id='printSearchInputRefactor'"
        ]
        for pt in possible_targets:
            if pt in desc:
                return 1
        return 0
        
    if target_id and f"id='{target_id}'" in desc:
        return 1
    return 0

df['is_target'] = df.apply(get_is_target, axis=1)

# Check label distribution per test_case_id
summary = []
for tc in df['test_case_id'].unique():
    pos = df[(df['test_case_id'] == tc) & (df['is_target'] == 1)].shape[0]
    neg = df[(df['test_case_id'] == tc) & (df['is_target'] == 0)].shape[0]
    summary.append({'test_case_id': tc, 'positive': pos, 'negative': neg})

# Write ai_dataset.csv
df.to_csv('results/ai_dataset.csv', index=False)

# Check if dataset is valid
invalid_scenarios = [s['test_case_id'] for s in summary if s['positive'] != 1]
if invalid_scenarios:
    print(f"ERROR: Skenario dengan ground truth tidak valid/ambigu (positif != 1): {invalid_scenarios}")
    for s in summary:
        print(f"  {s['test_case_id']}: Pos={s['positive']}, Neg={s['negative']}")
    sys.exit(1)

print("Ground Truth Valid. Mulai training...")

# 5. Train & Evaluate using Grouped Cross-Validation (Leave-One-Group-Out)
X = df[['text_score', 'locator_score']].values
y = df['is_target'].values
groups = df['test_case_id'].values

gkf = GroupKFold(n_splits=len(np.unique(groups)))

start_time = time.time()
df['ai_prob'] = 0.0

total_inference_time = 0.0
num_inferences = 0

for train_idx, test_idx in gkf.split(X, y, groups=groups):
    X_train, y_train = X[train_idx], y[train_idx]
    X_test, y_test = X[test_idx], y[test_idx]
    
    # Train
    clf = LogisticRegression(class_weight='balanced', random_state=42)
    clf.fit(X_train, y_train)
    
    # Inference
    t0 = time.time()
    probs = clf.predict_proba(X_test)[:, 1]
    total_inference_time += (time.time() - t0)
    num_inferences += len(X_test)
    
    df.iloc[test_idx, df.columns.get_loc('ai_prob')] = probs

avg_inference_time = (total_inference_time / num_inferences) * 1000 if num_inferences > 0 else 0

# 6. Rank and compare
correct_count = 0
total_scenarios = len(summary)
ranking_results = []
false_positives = 0

for tc in df['test_case_id'].unique():
    subset = df[df['test_case_id'] == tc]
    # AI memilih probabilitas tertinggi
    best_idx = subset['ai_prob'].idxmax()
    best_candidate = subset.loc[best_idx]
    
    is_correct = (best_candidate['is_target'] == 1)
    
    if is_correct:
        correct_count += 1
    else:
        false_positives += 1
        
    ranking_results.append({
        'test_case_id': tc,
        'ai_selected_desc': best_candidate['candidate_description'],
        'ai_prob': best_candidate['ai_prob'],
        'is_correct': is_correct
    })

pd.DataFrame(ranking_results).to_csv('results/ai_ranking_results.csv', index=False)

top1_accuracy = correct_count / total_scenarios * 100
fpr = false_positives / total_scenarios * 100

summary_text = f"=== SUMMARY AI ===\n"
summary_text += f"Total Scenarios Evaluated: {total_scenarios}\n"
summary_text += f"Correctly Selected (Top-1): {correct_count}\n"
summary_text += f"Top-1 Accuracy: {top1_accuracy:.2f}%\n"
summary_text += f"False Positive Rate: {fpr:.2f}%\n"
summary_text += f"Average Inference Time per Candidate: {avg_inference_time:.4f} ms\n\n"
summary_text += "Label Distribution per Test Case:\n"
for s in summary:
    summary_text += f" - {s['test_case_id']}: {s['positive']} Positive, {s['negative']} Negative\n"

with open('results/summary_ai.txt', 'w', encoding='utf-8') as f:
    f.write(summary_text)

print("Selesai. Hasil:")
print(summary_text)
