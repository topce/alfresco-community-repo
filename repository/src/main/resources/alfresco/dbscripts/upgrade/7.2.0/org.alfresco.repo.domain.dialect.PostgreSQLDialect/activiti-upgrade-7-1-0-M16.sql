

--
-- Record script finish
--
DELETE FROM alf_applied_patch WHERE id = 'patch.db-V7.2-activiti-upgrade-7-1-0-M16';
INSERT INTO alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  VALUES
  (
    'patch.db-V7.2-activiti-upgrade-7-1-0-M16', 'Manually executed script upgrade patch.db-V7.2-activiti-upgrade-7-1-0-M16',
    0, 16000, -1, 16001, null, 'UNKNOWN', ${TRUE}, ${TRUE}, 'Script completed'
  );
