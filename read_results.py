import pandas as pd
import json

df_res = pd.read_csv('results/ai_ranking_results.csv')
df_ds = pd.read_csv('results/ai_dataset.csv')
res = []

for _, row in df_res.iterrows():
    tc = row['test_case_id']
    sub = df_ds[df_ds['test_case_id'] == tc]
    gt_row = sub[sub['is_target'] == 1].iloc[0]
    res.append({
        'tc': tc,
        'total': len(sub),
        'pos': int(sum(sub['is_target']==1)),
        'neg': int(sum(sub['is_target']==0)),
        'ai_sel': str(row['ai_selected_desc']),
        'gt': str(gt_row['candidate_description']),
        'prob': float(row['ai_prob']),
        'correct': bool(row['is_correct'])
    })

with open('read_results.json', 'w') as f:
    json.dump(res, f, indent=2)
