-- ================================================================
-- Initialize RBAC tables
-- ================================================================

-- 1. Department / Group
CREATE TABLE IF NOT EXISTS rbac_group (
    id          SERIAL PRIMARY KEY,
    parent_id   INTEGER,
    path        VARCHAR(500),
    code        VARCHAR(50)  NOT NULL,
    name        VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    leader      VARCHAR(50),
    phone       VARCHAR(20),
    email       VARCHAR(100),
    sort        INTEGER      NOT NULL DEFAULT 0,
    status      INTEGER      NOT NULL DEFAULT 1,
    create_time TIMESTAMP,
    update_time TIMESTAMP,
    create_by   INTEGER,
    update_by   INTEGER,
    version     INTEGER      NOT NULL DEFAULT 0,
    del_flag    INTEGER      NOT NULL DEFAULT 0
);
CREATE INDEX IF NOT EXISTS idx_group_parent_id ON rbac_group (parent_id);
CREATE INDEX IF NOT EXISTS idx_group_path      ON rbac_group (path);
CREATE UNIQUE INDEX IF NOT EXISTS idx_group_code ON rbac_group (code) WHERE del_flag = 0;

-- 2. Role
CREATE TABLE IF NOT EXISTS rbac_role (
    id          SERIAL PRIMARY KEY,
    code        VARCHAR(50),
    name        VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    status      INTEGER      NOT NULL DEFAULT 1,
    sort        INTEGER      NOT NULL DEFAULT 0,
    group_id    INTEGER,
    data_scope  VARCHAR(50),
    create_time TIMESTAMP,
    update_time TIMESTAMP,
    create_by   INTEGER,
    update_by   INTEGER,
    version     INTEGER      NOT NULL DEFAULT 0,
    del_flag    INTEGER      NOT NULL DEFAULT 0
);
CREATE UNIQUE INDEX IF NOT EXISTS idx_role_code ON rbac_role (code) WHERE del_flag = 0;

-- 3. Menu
CREATE TABLE IF NOT EXISTS rbac_menu (
    id            SERIAL PRIMARY KEY,
    parent_id     INTEGER,
    code          VARCHAR(100) NOT NULL,
    name          VARCHAR(100) NOT NULL,
    name_i18n_key VARCHAR(200),
    type          VARCHAR(50)  NOT NULL DEFAULT 'MENU',
    path          VARCHAR(200),
    component     VARCHAR(200),
    permission    VARCHAR(100),
    icon          VARCHAR(50),
    sort          INTEGER      NOT NULL DEFAULT 0,
    visible       INTEGER      NOT NULL DEFAULT 1,
    status        INTEGER      NOT NULL DEFAULT 1,
    create_time   TIMESTAMP,
    update_time   TIMESTAMP,
    create_by     INTEGER,
    update_by     INTEGER,
    version       INTEGER      NOT NULL DEFAULT 0,
    del_flag      INTEGER      NOT NULL DEFAULT 0
);
CREATE INDEX IF NOT EXISTS idx_menu_parent_id ON rbac_menu (parent_id);
CREATE UNIQUE INDEX IF NOT EXISTS idx_menu_code ON rbac_menu (code) WHERE del_flag = 0;

-- 4. User
CREATE TABLE IF NOT EXISTS rbac_user (
    id          SERIAL PRIMARY KEY,
    username    VARCHAR(50)  NOT NULL,
    password    VARCHAR(200) NOT NULL,
    real_name   VARCHAR(50),
    email       VARCHAR(100),
    phone       VARCHAR(20),
    avatar      VARCHAR(500),
    status      INTEGER      NOT NULL DEFAULT 1,
    group_id    INTEGER,
    create_time TIMESTAMP,
    update_time TIMESTAMP,
    create_by   INTEGER,
    update_by   INTEGER,
    version     INTEGER      NOT NULL DEFAULT 0,
    del_flag    INTEGER      NOT NULL DEFAULT 0
);
CREATE UNIQUE INDEX IF NOT EXISTS idx_user_username ON rbac_user (username) WHERE del_flag = 0;
CREATE INDEX IF NOT EXISTS idx_user_email    ON rbac_user (email);
CREATE INDEX IF NOT EXISTS idx_user_phone    ON rbac_user (phone);

-- 5. Role-Department association
CREATE TABLE IF NOT EXISTS rbac_role_dept (
    id          SERIAL PRIMARY KEY,
    role_id     INTEGER NOT NULL,
    group_id    INTEGER NOT NULL,
    create_time TIMESTAMP,
    update_time TIMESTAMP,
    create_by   INTEGER,
    update_by   INTEGER,
    version     INTEGER NOT NULL DEFAULT 0,
    del_flag    INTEGER NOT NULL DEFAULT 0
);
CREATE INDEX IF NOT EXISTS idx_role_dept_role_id  ON rbac_role_dept (role_id);
CREATE INDEX IF NOT EXISTS idx_role_dept_group_id ON rbac_role_dept (group_id);

-- 6. Role-Menu association
CREATE TABLE IF NOT EXISTS rbac_role_menu (
    id          BIGSERIAL PRIMARY KEY,
    role_id     INTEGER NOT NULL,
    menu_id     INTEGER NOT NULL,
    create_time TIMESTAMP,
    create_by   INTEGER,
    UNIQUE (role_id, menu_id)
);
CREATE INDEX IF NOT EXISTS idx_role_menu_role_id ON rbac_role_menu (role_id);
CREATE INDEX IF NOT EXISTS idx_role_menu_menu_id ON rbac_role_menu (menu_id);

-- 7. User-Role association
CREATE TABLE IF NOT EXISTS rbac_user_role (
    id          BIGSERIAL PRIMARY KEY,
    user_id     INTEGER NOT NULL,
    role_id     INTEGER NOT NULL,
    create_time TIMESTAMP,
    create_by   INTEGER,
    UNIQUE (user_id, role_id)
);
CREATE INDEX IF NOT EXISTS idx_user_role_user_id ON rbac_user_role (user_id);
CREATE INDEX IF NOT EXISTS idx_user_role_role_id ON rbac_user_role (role_id);

-- ================================================================
-- View: user-role-scope for efficient data scope resolution
-- ================================================================
DROP VIEW IF EXISTS rbac_user_role_scope_view;
CREATE VIEW rbac_user_role_scope_view AS
SELECT ur.id                    AS user_role_id,
       ur.user_id,
       ur.role_id,
       r.data_scope,
       r.code                   AS role_code,
       COALESCE(rd.group_id, 0) AS dept_group_id
FROM rbac_user_role ur
         JOIN rbac_role r ON r.id = ur.role_id
         LEFT JOIN rbac_role_dept rd ON rd.role_id = r.id;

-- ================================================================
-- Initialize default data
-- ================================================================

-- Root group
INSERT INTO rbac_group (code, name, description, path, sort, status, create_time, update_time)
VALUES ('ROOT', 'Root Group', 'Built-in root group', '/', 0, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (code) WHERE del_flag = 0 DO NOTHING;

-- SUPER_ADMIN role
INSERT INTO rbac_role (code, name, description, status, sort, group_id,
                       create_time, update_time)
VALUES ('SUPER_ADMIN', 'Super Administrator',
        'Built-in super administrator role with full permissions', 1, 0,
        (SELECT id FROM rbac_group WHERE code = 'ROOT' AND del_flag = 0),
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (code) WHERE del_flag = 0 DO NOTHING;

-- Admin user (password is BCrypt-encoded "admin")
INSERT INTO rbac_user (username, password, real_name, status, group_id,
                       create_time, update_time)
VALUES ('admin',
        '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW',
        'Super Administrator', 1,
        (SELECT id FROM rbac_group WHERE code = 'ROOT' AND del_flag = 0),
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (username) WHERE del_flag = 0 DO NOTHING;

-- Assign SUPER_ADMIN role to admin user
INSERT INTO rbac_user_role (user_id, role_id, create_time)
VALUES ((SELECT id FROM rbac_user WHERE username = 'admin' AND del_flag = 0),
        (SELECT id FROM rbac_role WHERE code = 'SUPER_ADMIN' AND del_flag = 0),
        CURRENT_TIMESTAMP)
ON CONFLICT (user_id, role_id) DO NOTHING;