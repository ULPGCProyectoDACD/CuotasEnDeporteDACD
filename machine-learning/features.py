import pandas as pd

class TeamHistory:
    def __init__(self, window_size=20):
        self.history = {}
        self.window_size = window_size

    def get_recent_stats(self, team_name):
        if team_name not in self.history or len(self.history[team_name]) == 0:
            return 0, 0.0, 0.0
        
        recent_matches = self.history[team_name]
        total_points = sum(m['points'] for m in recent_matches)
        avg_gf = sum(m['gf'] for m in recent_matches) / len(recent_matches)
        avg_gc = sum(m['gc'] for m in recent_matches) / len(recent_matches)
        
        return total_points, avg_gf, avg_gc

    def add_match_result(self, team_name, points, gf, gc):
        if team_name not in self.history:
            self.history[team_name] = []
        
        self.history[team_name].append({'points': points, 'gf': gf, 'gc': gc})
        
        if len(self.history[team_name]) > self.window_size:
            self.history[team_name].pop(0)


def calculate_streaks_and_save(matches, output_path, window_size=20):
    df = pd.DataFrame(matches)
    if df.empty: return None

    df['Date'] = pd.to_datetime(df['Date'])
    df = df.sort_values(by='Date').reset_index(drop=True)
    
    tracker = TeamHistory(window_size)
    
    h_points, a_points = [], []
    h_gf, h_gc = [], []
    a_gf, a_gc = [], []

    for index, row in df.iterrows():
        home_team = row['Home_Team']
        away_team = row['Away_Team']
        result = row['Match_Result']
        home_goals = row['Home_Goals']
        away_goals = row['Away_Goals']
        
        hp, hgf, hgc = tracker.get_recent_stats(home_team)
        ap, agf, agc = tracker.get_recent_stats(away_team)
        
        h_points.append(hp)
        h_gf.append(hgf)
        h_gc.append(hgc)
        
        a_points.append(ap)
        a_gf.append(agf)
        a_gc.append(agc)
        
        home_points = 3 if result == 1 else (1 if result == 0 else 0)
        away_points = 3 if result == 2 else (1 if result == 0 else 0)
        
        tracker.add_match_result(home_team, home_points, home_goals, away_goals)
        tracker.add_match_result(away_team, away_points, away_goals, home_goals)

    df['Home_Streak_Points'] = h_points
    df['Away_Streak_Points'] = a_points
    df['Home_Avg_Goals_For'] = h_gf
    df['Home_Avg_Goals_Against'] = h_gc
    df['Away_Avg_Goals_For'] = a_gf
    df['Away_Avg_Goals_Against'] = a_gc
    
    df.to_csv(output_path, index=False)
    return df