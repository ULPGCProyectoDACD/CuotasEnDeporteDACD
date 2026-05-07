from sklearn.linear_model import LogisticRegression
from skl2onnx import to_onnx
import numpy as np


def train_and_export_model(df, onnx_output_path):
    print("\n--- Iniciando Entrenamiento del Modelo ---")
    
    X = df[['Home_Streak_Points', 'Away_Streak_Points', 
            'Home_Avg_Goals_For', 'Home_Avg_Goals_Against', 
            'Away_Avg_Goals_For', 'Away_Avg_Goals_Against']]
    
    y = df['Match_Result']

    model = LogisticRegression(solver='lbfgs', max_iter=1000)
    model.fit(X, y)
    print("¡Modelo entrenado con éxito!")
    
    X_sample = X.to_numpy(dtype=np.float32)
    onnx_model = to_onnx(model, X_sample[:1])
    
    with open(onnx_output_path, "wb") as f:
        f.write(onnx_model.SerializeToString())
        
    print(f"¡Cerebro exportado con éxito a: {onnx_output_path}!")