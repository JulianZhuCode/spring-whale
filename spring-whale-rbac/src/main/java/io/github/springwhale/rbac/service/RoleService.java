package io.github.springwhale.rbac.service;

import io.github.springwhale.database.JpaQueryWrapper;
import io.github.springwhale.framework.core.exception.BusinessException;
import io.github.springwhale.rbac.dto.request.RoleRequest;
import io.github.springwhale.rbac.dto.vo.RoleVO;
import io.github.springwhale.rbac.entity.GroupEntity;
import io.github.springwhale.rbac.entity.RoleEntity;
import io.github.springwhale.rbac.mapper.RoleMapper;
import io.github.springwhale.rbac.repository.GroupRepository;
import io.github.springwhale.rbac.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Role service
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoleService {

    private final RoleRepository roleRepository;
    private final GroupRepository groupRepository;
    private final RoleMapper roleMapper;

    /**
     * Find all roles with pagination
     */
    public Page<RoleVO> findAll(Pageable pageable) {
        Page<RoleVO> page = roleRepository.findAll(pageable).map(roleMapper::toVO);
        enrichGroupNames(page.getContent());
        return page;
    }

    /**
     * Find roles with filter
     */
    public Page<RoleVO> findWithFilter(String keyword, Integer status, Pageable pageable) {
        var spec = JpaQueryWrapper.of(RoleEntity.class)
                .or(!ObjectUtils.isEmpty(keyword), w -> w
                        .likeIgnoreCase(RoleEntity::getCode, keyword)
                        .likeIgnoreCase(RoleEntity::getName, keyword)
                        .likeIgnoreCase(RoleEntity::getDescription, keyword))
                .eq(status != null, RoleEntity::getStatus, status)
                .buildSpec();
        Page<RoleVO> page = roleRepository.findAll(spec, pageable).map(roleMapper::toVO);
        enrichGroupNames(page.getContent());
        return page;
    }

    /**
     * Find role by ID
     */
    public Optional<RoleVO> findById(Integer id) {
        return roleRepository.findById(id)
                .map(roleMapper::toVO)
                .map(this::enrichGroupName);
    }

    /**
     * Find role by exact code
     */
    public Optional<RoleVO> findByCode(String code) {
        return roleRepository.findByCode(code)
                .map(roleMapper::toVO)
                .map(this::enrichGroupName);
    }

    /**
     * Search roles by name (fuzzy)
     */
    public List<RoleVO> search(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return List.of();
        }
        List<RoleVO> vos = roleMapper.toVOList(roleRepository.findByNameContaining(keyword));
        enrichGroupNames(vos);
        return vos;
    }

    /**
     * Find by status
     */
    public List<RoleVO> findByStatus(Integer status) {
        List<RoleVO> vos = roleMapper.toVOList(roleRepository.findByStatus(status));
        enrichGroupNames(vos);
        return vos;
    }

    /**
     * Create role
     */
    @Transactional
    public RoleVO create(RoleRequest request) {
        RoleEntity entity = new RoleEntity();
        entity.setCode(request.getCode());
        entity.setName(request.getName());
        entity.setDescription(request.getDescription());
        entity.setStatus(request.getStatus());
        entity.setSort(request.getSort());
        entity.setGroupId(request.getGroupId());
        return enrichGroupName(roleMapper.toVO(roleRepository.save(entity)));
    }

    /**
     * Update role
     */
    @Transactional
    public RoleVO update(Integer id, RoleRequest request) {
        RoleEntity role = roleRepository.findById(id)
                .orElseThrow(() -> BusinessException.create("ROLE_NOT_FOUND", "Role not found, ID: " + id));

        role.setCode(request.getCode());
        role.setName(request.getName());
        role.setDescription(request.getDescription());
        role.setStatus(request.getStatus());
        role.setSort(request.getSort());
        role.setGroupId(request.getGroupId());

        return enrichGroupName(roleMapper.toVO(roleRepository.save(role)));
    }

    /**
     * Delete role
     */
    @Transactional
    public void delete(Integer id) {
        RoleEntity role = roleRepository.findById(id)
                .orElseThrow(() -> BusinessException.create("ROLE_NOT_FOUND", "Role not found, ID: " + id));
        roleRepository.delete(role);
    }

    // ==================== Group name enrichment ====================

    private void enrichGroupNames(List<RoleVO> vos) {
        if (vos == null || vos.isEmpty()) return;
        List<Integer> groupIds = vos.stream()
                .map(RoleVO::getGroupId)
                .filter(gid -> gid != null)
                .distinct()
                .toList();
        if (groupIds.isEmpty()) return;
        Map<Integer, String> groupNameMap = groupRepository.findAllByIdIn(groupIds).stream()
                .collect(Collectors.toMap(GroupEntity::getId, GroupEntity::getName));
        vos.forEach(vo -> {
            if (vo.getGroupId() != null) {
                vo.setGroupName(groupNameMap.get(vo.getGroupId()));
            }
        });
    }

    private RoleVO enrichGroupName(RoleVO vo) {
        if (vo != null && vo.getGroupId() != null) {
            groupRepository.findById(vo.getGroupId())
                    .ifPresent(g -> vo.setGroupName(g.getName()));
        }
        return vo;
    }
}
