-- Level 0: System root directory
INSERT INTO rbac_menu (parent_id, code, name, name_i18n_key, type, path, component, permission, icon, sort, visible,
                       status, version, del_flag, create_time, update_time)
VALUES (NULL, 'system', 'System', 'menu.system', 'DIRECTORY', NULL, NULL, NULL, 'menu', 0, 0, 1, 0, 0,
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (code)
WHERE del_flag = 0 DO NOTHING;

-- Level 1: RBAC directory
INSERT INTO rbac_menu (parent_id, code, name, name_i18n_key, type, path, component, permission, icon, sort, visible,
                       status, version, del_flag, create_time, update_time)
VALUES ((SELECT id FROM rbac_menu WHERE code = 'system'), 'rbac', 'RBAC', 'menu.rbac', 'DIRECTORY', NULL, NULL, NULL,
        '🔐', 1, 1, 1, 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (code)
WHERE del_flag = 0 DO NOTHING;

-- Level 2: User Management (menu + buttons)
INSERT INTO rbac_menu (parent_id, code, name, name_i18n_key, type, path, component, permission, icon, sort, visible,
                       status, version, del_flag, create_time, update_time)
VALUES ((SELECT id FROM rbac_menu WHERE code = 'rbac'), 'rbac:user', 'User Management', 'menu.rbac.user_management',
        'MENU', '/admin/rbac/users', NULL, 'rbac:user', 'file-text', 2, 1, 1, 0, 0, CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP)
ON CONFLICT (code)
WHERE del_flag = 0 DO NOTHING;

INSERT INTO rbac_menu (parent_id, code, name, name_i18n_key, type, path, component, permission, icon, sort, visible,
                       status, version, del_flag, create_time, update_time)
VALUES ((SELECT id FROM rbac_menu WHERE code = 'rbac:user'), 'rbac:user:create', 'User Management Create', 'button.create',
        'BUTTON', NULL, NULL, 'rbac:user:create', NULL, 1, 1, 1, 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ((SELECT id FROM rbac_menu WHERE code = 'rbac:user'), 'rbac:user:update', 'User Management Update', 'button.update',
        'BUTTON', NULL, NULL, 'rbac:user:update', NULL, 2, 1, 1, 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ((SELECT id FROM rbac_menu WHERE code = 'rbac:user'), 'rbac:user:delete', 'User Management Delete', 'button.delete',
        'BUTTON', NULL, NULL, 'rbac:user:delete', NULL, 3, 1, 1, 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (code)
WHERE del_flag = 0 DO NOTHING;

-- Level 2: Role Management (menu + buttons)
INSERT INTO rbac_menu (parent_id, code, name, name_i18n_key, type, path, component, permission, icon, sort, visible,
                       status, version, del_flag, create_time, update_time)
VALUES ((SELECT id FROM rbac_menu WHERE code = 'rbac'), 'rbac:role', 'Role Management', 'menu.rbac.role_management',
        'MENU', '/admin/rbac/roles', NULL, 'rbac:role', 'users', 3, 1, 1, 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (code)
WHERE del_flag = 0 DO NOTHING;

INSERT INTO rbac_menu (parent_id, code, name, name_i18n_key, type, path, component, permission, icon, sort, visible,
                       status, version, del_flag, create_time, update_time)
VALUES ((SELECT id FROM rbac_menu WHERE code = 'rbac:role'), 'rbac:role:create', 'Role Management Create', 'button.create',
        'BUTTON', NULL, NULL, 'rbac:role:create', NULL, 1, 1, 1, 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ((SELECT id FROM rbac_menu WHERE code = 'rbac:role'), 'rbac:role:update', 'Role Management Update', 'button.update',
        'BUTTON', NULL, NULL, 'rbac:role:update', NULL, 2, 1, 1, 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ((SELECT id FROM rbac_menu WHERE code = 'rbac:role'), 'rbac:role:delete', 'Role Management Delete', 'button.delete',
        'BUTTON', NULL, NULL, 'rbac:role:delete', NULL, 3, 1, 1, 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (code)
WHERE del_flag = 0 DO NOTHING;

-- Level 2: Menu Management (menu + buttons)
INSERT INTO rbac_menu (parent_id, code, name, name_i18n_key, type, path, component, permission, icon, sort, visible,
                       status, version, del_flag, create_time, update_time)
VALUES ((SELECT id FROM rbac_menu WHERE code = 'rbac'), 'rbac:menu', 'Menu Management', 'menu.rbac.menu_management',
        'MENU', '/admin/rbac/menus', NULL, 'rbac:menu', 'menu', 4, 1, 1, 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (code)
WHERE del_flag = 0 DO NOTHING;

INSERT INTO rbac_menu (parent_id, code, name, name_i18n_key, type, path, component, permission, icon, sort, visible,
                       status, version, del_flag, create_time, update_time)
VALUES ((SELECT id FROM rbac_menu WHERE code = 'rbac:menu'), 'rbac:menu:create', 'Menu Management Create', 'button.create',
        'BUTTON', NULL, NULL, 'rbac:menu:create', NULL, 1, 1, 1, 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ((SELECT id FROM rbac_menu WHERE code = 'rbac:menu'), 'rbac:menu:update', 'Menu Management Update', 'button.update',
        'BUTTON', NULL, NULL, 'rbac:menu:update', NULL, 2, 1, 1, 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ((SELECT id FROM rbac_menu WHERE code = 'rbac:menu'), 'rbac:menu:delete', 'Menu Management Delete', 'button.delete',
        'BUTTON', NULL, NULL, 'rbac:menu:delete', NULL, 3, 1, 1, 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (code)
WHERE del_flag = 0 DO NOTHING;

-- Level 2: Group Management (menu + buttons)
INSERT INTO rbac_menu (parent_id, code, name, name_i18n_key, type, path, component, permission, icon, sort, visible,
                       status, version, del_flag, create_time, update_time)
VALUES ((SELECT id FROM rbac_menu WHERE code = 'rbac'), 'rbac:group', 'Group Management', 'menu.rbac.group_management',
        'MENU', '/admin/rbac/groups', NULL, 'rbac:group', 'layout', 5, 1, 1, 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (code)
WHERE del_flag = 0 DO NOTHING;

INSERT INTO rbac_menu (parent_id, code, name, name_i18n_key, type, path, component, permission, icon, sort, visible,
                       status, version, del_flag, create_time, update_time)
VALUES ((SELECT id FROM rbac_menu WHERE code = 'rbac:group'), 'rbac:group:create', 'Group Management Create', 'button.create',
        'BUTTON', NULL, NULL, 'rbac:group:create', NULL, 1, 1, 1, 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ((SELECT id FROM rbac_menu WHERE code = 'rbac:group'), 'rbac:group:update', 'Group Management Update', 'button.update',
        'BUTTON', NULL, NULL, 'rbac:group:update', NULL, 2, 1, 1, 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ((SELECT id FROM rbac_menu WHERE code = 'rbac:group'), 'rbac:group:delete', 'Group Management Delete', 'button.delete',
        'BUTTON', NULL, NULL, 'rbac:group:delete', NULL, 3, 1, 1, 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (code)
WHERE del_flag = 0 DO NOTHING;