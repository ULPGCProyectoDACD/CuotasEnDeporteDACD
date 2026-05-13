import pandas as pd
from sklearn.linear_model import LogisticRegression

df = pd.read_csv('machine-learning/historical_dataset.csv')
features = ['Home_Streak_Points', 'Away_Streak_Points', 
            'Home_Avg_Goals_For', 'Home_Avg_Goals_Against', 
            'Away_Avg_Goals_For', 'Away_Avg_Goals_Against']
X = df[features]
y = df['Match_Result']

model = LogisticRegression(solver='lbfgs', max_iter=1000)
model.fit(X, y)

print("Classes:", model.classes_)
for i, cls in enumerate(model.classes_):
    print(f"\nCoefficients for Class {cls}:")
    for feature, coef in zip(features, model.coef_[i]):
        print(f"  {feature}: {coef:.4f}")
