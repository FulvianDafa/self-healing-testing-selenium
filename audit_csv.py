import csv

def calculate_expected(text, locator, position):
    return (text * 0.625) + (locator * 0.375)

print(f"{'TC':<10} | {'Text':<8} | {'Locator':<8} | {'Logged':<8} | {'Calc':<8} | {'Diff':<8} | {'Match'}")
print("-" * 65)

mismatches = []

with open('results/healing_log.csv', 'r') as f:
    reader = csv.DictReader(f)
    for row in reader:
        tc = row['test_case_id']
        text = float(row['text_score'])
        locator = float(row['locator_score'])
        pos = float(row['position_score'])
        logged = float(row['combined_score'])
        
        calc = calculate_expected(text, locator, pos)
        diff = abs(logged - calc)
        
        match = "Yes" if diff < 0.0001 else "No"
        
        if diff >= 0.0001:
            mismatches.append(row)
            
        print(f"{tc:<10} | {text:<8.4f} | {locator:<8.4f} | {logged:<8.4f} | {calc:<8.4f} | {diff:<8.4f} | {match}")

print("\nDetail Mismatch:")
for m in mismatches:
    print(f"TC: {m['test_case_id']}, Logged: {m['combined_score']}, Text: {m['text_score']}, Locator: {m['locator_score']}")
