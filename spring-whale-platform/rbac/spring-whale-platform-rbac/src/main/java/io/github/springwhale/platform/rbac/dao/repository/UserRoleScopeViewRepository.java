package io.github.springwhale.platform.rbac.dao.repository;

import io.github.springwhale.platform.rbac.dao.view.UserRoleScopeView;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRoleScopeViewRepository extends JpaRepository<UserRoleScopeView, UserRoleScopeView.UserRoleScopeViewId> {

    List<UserRoleScopeView> findByUserId(Integer userId);
}