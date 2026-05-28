package io.github.springwhale.rbac.service;

import io.github.springwhale.rbac.dto.RoleDTO;
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
    public Page<RoleDTO> findAll(Pageable pageable) {
        return roleRepository.findAll(pageable).map(roleMapper::toDTO);
    }

    /**
     * 根据ID查询角色
     */
    public Optional<RoleDTO> findById(Integer id) {
        return roleRepository.findById(id).map(roleMapper::toDTO);
    }

    /**
     * 根据角色编码精确查询
     */
    public Optional<RoleDTO> findByCode(String code) {
        return roleRepository.findByCode(code).map(roleMapper::toDTO);
    }

    /**
     * 搜索角色（支持角色名称模糊查询）
     */
    public List<RoleDTO> search(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return List.of();
        }
        return roleMapper.toDTOList(roleRepository.findByNameContaining(keyword));
    }

    /**
     * 根据状态查询
     */
    public List<RoleDTO> findByStatus(Integer status) {
        return roleMapper.toDTOList(roleRepository.findByStatus(status));
    }

    /**
     * 创建角色
     */
    @Transactional
    public RoleDTO create(RoleDTO roleDTO) {
        RoleEntity entity = roleMapper.toEntity(roleDTO);
        return roleMapper.toDTO(roleRepository.save(entity));
    }

    /**
     * 更新角色
     */
    @Transactional
    public RoleDTO update(Integer id, RoleDTO roleDTO) {
        RoleEntity role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("角色不存在，ID: " + id));

        role.setCode(roleDTO.getCode());
        role.setName(roleDTO.getName());
        role.setDescription(roleDTO.getDescription());
        role.setStatus(roleDTO.getStatus());
        role.setSort(roleDTO.getSort());

        return roleMapper.toDTO(roleRepository.save(role));
    }

    /**
     * 删除角色
     */
    @Transactional
    public void delete(Integer id) {
        RoleEntity role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("角色不存在，ID: " + id));
        roleRepository.delete(role);
    }
}
