import csv
import json

csv_file = r'd:\PERKULIAHAN\SKRIPSI\Project\self-healing-selenium\results\candidates_log.csv'

mapping = {}
try:
    with open(csv_file, 'r', encoding='utf-8-sig') as f:
        reader = csv.DictReader(f)
        for row in reader:
            tc = row.get('test_case_id')
            loc = row.get('original_locator')
            if tc and tc not in mapping:
                mapping[tc] = loc
except Exception as e:
    print(f"Error: {e}")

print(json.dumps(mapping, indent=2))
