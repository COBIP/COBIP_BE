ALTER TABLE template_practice_progresses
    DROP CONSTRAINT IF EXISTS template_practice_progresses_current_mission_id_fkey;

ALTER TABLE template_practice_progresses
    ADD CONSTRAINT fk_template_practice_progress_current_mission
    FOREIGN KEY (current_mission_id)
    REFERENCES template_practice_missions(id)
    ON DELETE SET NULL;
