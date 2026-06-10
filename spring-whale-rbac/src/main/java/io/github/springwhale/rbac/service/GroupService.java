package io.github.springwhale.rbac.service;

import io.github.springwhale.framework.core.exception.BusinessException;
import io.github.springwhale.rbac.dto.request.GroupRequest;
import io.github.springwhale.rbac.dto.vo.GroupVO;
import io.github.springwhale.rbac.entity.GroupEntity;
import io.github.springwhale.rbac.mapper.GroupMapper;
import io.github.springwhale.rbac.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Group (department) service
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GroupService {

    private final GroupRepository groupRepository;
    private final GroupMapper groupMapper;

    /**
     * Find all departments with pagination
     */
    public Page<GroupVO> findAll(Pageable pageable) {
        return groupRepository.findAll(pageable).map(groupMapper::toVO);
    }

    /**
     * Find department by ID
     */
    public Optional<GroupVO> findById(Integer id) {
        return groupRepository.findById(id).map(groupMapper::toVO);
    }

    /**
     * Find department by exact code
     */
    public Optional<GroupVO> findByCode(String code) {
        return groupRepository.findByCode(code).map(groupMapper::toVO);
    }

    /**
     * Find departments by parent ID
     */
    public List<GroupVO> findByParentId(Integer parentId) {
        return groupMapper.toVOList(groupRepository.findByParentId(parentId));
    }

    /**
     * Search departments by name (fuzzy)
     */
    public List<GroupVO> search(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return List.of();
        }
        return groupMapper.toVOList(groupRepository.findByNameContaining(keyword));
    }

    /**
     * Find by status
     */
    public List<GroupVO> findByStatus(Integer status) {
        return groupMapper.toVOList(groupRepository.findByStatus(status));
    }

    /**
     * Get all root departments (parentId is null)
     */
    public List<GroupVO> findRootGroups() {
        return groupMapper.toVOList(groupRepository.findByParentId(null));
    }

    /**
     * Create department
     */
    @Transactional
    public GroupVO create(GroupRequest request) {
        GroupEntity entity = new GroupEntity();
        entity.setParentId(request.getParentId());
        entity.setCode(request.getCode());
        entity.setName(request.getName());
        entity.setDescription(request.getDescription());
        entity.setLeader(request.getLeader());
        entity.setPhone(request.getPhone());
        entity.setEmail(request.getEmail());
        entity.setSort(request.getSort());
        entity.setStatus(request.getStatus());
        return groupMapper.toVO(groupRepository.save(entity));
    }

    /**
     * Update department
     */
    @Transactional
    public GroupVO update(Integer id, GroupRequest request) {
        GroupEntity group = groupRepository.findById(id)
                .orElseThrow(() -> BusinessException.create("GROUP_NOT_FOUND", "Department not found, ID: " + id));

        group.setParentId(request.getParentId());
        group.setCode(request.getCode());
        group.setName(request.getName());
        group.setDescription(request.getDescription());
        group.setLeader(request.getLeader());
        group.setPhone(request.getPhone());
        group.setEmail(request.getEmail());
        group.setSort(request.getSort());
        group.setStatus(request.getStatus());

        return groupMapper.toVO(groupRepository.save(group));
    }

    /**
     * Delete department
     */
    @Transactional
    public void delete(Integer id) {
        GroupEntity group = groupRepository.findById(id)
                .orElseThrow(() -> BusinessException.create("GROUP_NOT_FOUND", "Department not found, ID: " + id));
        groupRepository.delete(group);
    }
}
