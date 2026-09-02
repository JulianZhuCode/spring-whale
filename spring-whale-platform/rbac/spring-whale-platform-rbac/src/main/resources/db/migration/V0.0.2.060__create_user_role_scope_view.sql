CREATE OR REPLACE VIEW rbac_user_role_scope_view AS
SELECT ur.id        AS user_role_id,
       ur.user_id,
       ur.role_id,
       r.data_scope,
       r.code        AS role_code,
       COALESCE(rd.group_id, 0) AS dept_group_id
FROM rbac_user_role ur
         JOIN rbac_role r ON r.id = ur.role_id
         LEFT JOIN rbac_role_dept rd ON rd.role_id = r.id;