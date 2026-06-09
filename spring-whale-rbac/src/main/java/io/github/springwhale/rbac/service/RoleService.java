package io.github.springwhale.rbac.service;

import io.github.springwhale.framework.core.exception.BusinessException;
import io.github.springwhale.rbac.dto.request.RoleRequest;
import io.github.springwhale.rbac.dto.vo.RoleVO;
import io.github.springwhale.rbac.entity.RoleEntity;
import io.github.springwhale.rbac.mapper.RoleMapper;
import io.github.springwhale.rbac.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 角色服务
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoleService {

    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;

    /**
     * 分页查询所有角色
     */
    public Page<RoleVO> findAll(Pageable pageable) {
        return roleRepository.findAll(pageable).map(roleMapper::toVO);
    }

    /**
     * 根据ID查询角色
     */
    public Optional<RoleVO> findById(Integer id) {
        return roleRepository.findById(id).map(roleMapper::toVO);
    }

    /**
     * 根据角色编码精确查询
     */
    public Optional<RoleVO> findByCode(String code) {
        return roleRepository.findByCode(code).map(roleMapper::toVO);
    }

    /**
     * 搜索角色（支持角色名称模糊查询）
     */
    public List<RoleVO> search(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return List.of();
        }
        return roleMapper.toVOList(roleRepository.findByNameContaining(keyword));
    }

    /**
     * 根据状态查询
     */
    public List<RoleVO> findByStatus(Integer status) {
        return roleMapper.toVOList(roleRepository.findByStatus(status));
    }

    /**
     * 创建角色
     */
    @Transactional
    public RoleVO create(RoleRequest request) {
        RoleEntity entity = new RoleEntity();
        entity.setCode(request.getCode());
        entity.setName(request.getName());
        entity.setDescription(request.getDescription());
        entity.setStatus(request.getStatus());
        entity.setSort(request.getSort());
        return roleMapper.toVO(roleRepository.save(entity));
    }

    /**
     * 更新角色
     */
    @Transactional
    public RoleVO update(Integer id, RoleRequest request) {
        RoleEntity role = roleRepository.findById(id)
                .orElseThrow(() -> BusinessException.create("ROLE_NOT_FOUND", "角色不存在，ID: " + id));

        role.setCode(request.getCode());
        role.setName(request.getName());
        role.setDescription(request.getDescription());
        role.setStatus(request.getStatus());
        role.setSort(request.getSort());

        return roleMapper.toVO(roleRepository.save(role));
    }

    /**
     * 删除角色
     */
    @Transactional
    public void delete(Integer id) {
        RoleEntity role = roleRepository.findById(id)
                .orElseThrow(() -> BusinessException.create("ROLE_NOT_FOUND", "角色不存在，ID: " + id));
        roleRepository.delete(role);
    }
}
