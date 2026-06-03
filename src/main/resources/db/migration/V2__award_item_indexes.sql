CREATE INDEX IF NOT EXISTS idx_competition_items_track
    ON competition_items(competition_id, track_id, name);

CREATE INDEX IF NOT EXISTS idx_awards_item
    ON awards(item_id);

CREATE INDEX IF NOT EXISTS idx_awards_competition_track_item
    ON awards(competition_id, track_id, item_id);
