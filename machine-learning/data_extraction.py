import json
import os
import glob
from team_mapper import get_official_name


def get_event_files(directory_path):
    if not os.path.exists(directory_path):
        return []
    return glob.glob(os.path.join(directory_path, '*.events'))

def calculate_match_result(home_goals, away_goals):
    if home_goals > away_goals: return 1
    elif home_goals == away_goals: return 0
    else: return 2

def extract_match_data(json_line):
    event = json.loads(json_line)
    if event.get("status") != "FINISHED":
        return None
        
    home_goals = event["homeGoals"]
    away_goals = event["awayGoals"]
    
    return {
        "Date": event["date"],
        "Home_Team": get_official_name(event["homeTeam"]["name"]),
        "Away_Team": get_official_name(event["awayTeam"]["name"]),
        "Home_Goals": home_goals,
        "Away_Goals": away_goals,
        "Match_Result": calculate_match_result(home_goals, away_goals)
    }

def process_file(file_path):
    matches = []
    with open(file_path, 'r', encoding='utf-8') as f:
        for line in f:
            if line.strip():
                match_data = extract_match_data(line)
                if match_data:
                    matches.append(match_data)
    return matches