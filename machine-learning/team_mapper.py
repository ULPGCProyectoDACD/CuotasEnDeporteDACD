import json
import os

def load_dictionary():
    # Buscamos el archivo en la carpeta compartida de recursos
    json_path = os.path.join(os.path.dirname(__file__), '../business-unit/src/main/resources/teams.json')
    try:
        with open(json_path, 'r', encoding='utf-8') as f:
            return json.load(f)
    except FileNotFoundError:
        print(f"⚠️ Advertencia: No se encontró {json_path}. Usando diccionario vacío.")
        return {}

_DICTIONARY = load_dictionary()

def get_official_name(name):
    return _DICTIONARY.get(name, name)
