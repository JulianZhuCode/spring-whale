package io.github.springwhale.rbac.repository;

import io.github.springwhale.rbac.entity.MenuEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 菜单数据访问接口
 */
@Repository
public interface MenuRepository extends JpaRepository<MenuEntity, Integer> {

    /**
     * 根据菜单编码精确查询
     */
    Optional<MenuEntity> findByCode(String code);

    /**
     * 根据父菜单ID查询
     */
    List<MenuEntity> findByParentId(Integer parentId);

    /**
     * 根据菜单名称模糊查询
     */
    List<MenuEntity> findByNameContaining(String name);

    /**
     * 根据类型查询
     */
    List<MenuEntity> findByType(Integer type);

    /**
     * 根据状态查询
     */
    List<MenuEntity> findByStatus(Integer status);

    /**
     * 根据可见性查询
     */
    List<MenuEntity> findByVisible(Integer visible);
}
