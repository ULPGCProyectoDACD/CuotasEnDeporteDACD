import pandas as pd


class TeamHistory:
    def __init__(self, window_size=5):
        self.history = {}
        self.window_size = window_size

    def get_recent_points(self, team_name):
        if team_name not in self.history: return 0
        return sum(self.history[team_name])

    def add_match_result(self, team_name, points):
        if team_name not in self.history:
            self.history[team_name] = []
        self.history[team_name].append(points)
        if len(self.history[team_name]) > self.window_size:
            self.history[team_name].pop(0)


def calculate_streaks_and_save(matches, output_path, window_size=5):
    df = pd.DataFrame(matches)
    if df.empty: return None

    df['Date'] = pd.to_datetime(df['Date'])
    df = df.sort_values(by='Date').reset_index(drop=True)
    
    tracker = TeamHistory(window_size)
    home_streaks = []
    away_streaks = []

    for index, row in df.iterrows():
        home_team = row['Home_Team']
        away_team = row['Away_Team']
        result = row['Match_Result']
        
        home_streaks.append(tracker.get_recent_points(home_team))
        away_streaks.append(tracker.get_recent_points(away_team))
        
        home_points = 3 if result == 1 else (1 if result == 0 else 0)
        away_points = 3 if result == 2 else (1 if result == 0 else 0)
        
        tracker.add_match_result(home_team, home_points)
        tracker.add_match_result(away_team, away_points)

    df['Home_Streak_Points'] = home_streaks
    df['Away_Streak_Points'] = away_streaks
    
    df.to_csv(output_path, index=False)
    return df