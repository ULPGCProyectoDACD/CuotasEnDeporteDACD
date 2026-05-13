from data_extraction import get_event_files, process_file
from features import calculate_streaks_and_save
from model_trainer import train_and_export_model
import shutil
import os

EVENTSTORE_DIR = '../eventstore/FootballResult/feeder-results' 
CSV_OUTPUT_PATH = 'historical_dataset.csv'
ONNX_LOCAL_PATH = 'match_model.onnx'
MODELS_DIR = '../models'
ONNX_EXPORT_PATH = f'{MODELS_DIR}/match_model.onnx'

def main():
    print(f"Buscando archivos en: {EVENTSTORE_DIR}")
    event_files = get_event_files(EVENTSTORE_DIR)
    
    if not event_files:
        print("No se encontraron archivos .events.")
        return
        
    all_matches = []
    for file_path in event_files:
        all_matches.extend(process_file(file_path))

    print("Calculando rachas y generando el Dataset...")
    df = calculate_streaks_and_save(all_matches, CSV_OUTPUT_PATH, window_size=20)
    
    if df is not None:
        train_and_export_model(df, ONNX_LOCAL_PATH)
        
        if not os.path.exists(MODELS_DIR):
            os.makedirs(MODELS_DIR)
            
        shutil.copy(ONNX_LOCAL_PATH, ONNX_EXPORT_PATH)
        print(f"¡Modelo copiado automáticamente para producción en: {ONNX_EXPORT_PATH}!")
        print("--- PIPELINE COMPLETADO ---")

if __name__ == "__main__":
    main()