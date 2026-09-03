-- ================================================================
-- Initialize Task Management UI menus
-- ================================================================

-- Level 1: Task Management directory (under System)
INSERT INTO rbac_menu (parent_id, code, name, name_i18n_key, type, path, component, permission, icon, sort, visible,
                       status, version, del_flag, create_time, update_time)
VALUES ((SELECT id FROM rbac_menu WHERE code = 'system'), 'task', 'Task Management', 'menu.task', 'DIRECTORY', NULL, NULL,
        NULL, 'list', 6, 1, 1, 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (code) DO NOTHING;

-- Level 2: Batch Task (menu + buttons)
INSERT INTO rbac_menu (parent_id, code, name, name_i18n_key, type, path, component, permission, icon, sort, visible,
                       status, version, del_flag, create_time, update_time)
VALUES ((SELECT id FROM rbac_menu WHERE code = 'task'), 'task:batch', 'Batch Task', 'menu.task.batch', 'MENU',
        '/admin/task/batches', NULL, 'task:batch', 'activity', 1, 1, 1, 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (code) DO NOTHING;

INSERT INTO rbac_menu (parent_id, code, name, name_i18n_key, type, path, component, permission, icon, sort, visible,
                       status, version, del_flag, create_time, update_time)
VALUES ((SELECT id FROM rbac_menu WHERE code = 'task:batch'), 'task:batch:delete', 'Batch Task Delete', NULL, 'BUTTON',
        NULL, NULL, 'task:batch:delete', NULL, 1, 1, 1, 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (code) DO NOTHING;