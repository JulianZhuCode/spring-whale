package io.github.springwhale.platform.rbac.service;

import io.github.springwhale.database.JpaQueryWrapper;
import io.github.springwhale.framework.core.exception.BusinessException;
import io.github.springwhale.platform.rbac.dao.entity.GroupEntity;
import io.github.springwhale.platform.rbac.dao.mapper.GroupMapper;
import io.github.springwhale.platform.rbac.dto.request.GroupRequest;
import io.github.springwhale.platform.rbac.dto.vo.GroupVO;
import io.github.springwhale.platform.rbac.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.util.Optional;

/**
 * Group (department) service
 */
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
     * Find groups with filter
     */
    public Page<GroupVO> findWithFilter(String keyword, Integer status, Pageable pageable) {
        var spec = JpaQueryWrapper.of(GroupEntity.class)
                .or(!ObjectUtils.isEmpty(keyword), w -> w
                        .likeIgnoreCase(GroupEntity::getCode, keyword)
                        .likeIgnoreCase(GroupEntity::getName, keyword)
                        .likeIgnoreCase(GroupEntity::getLeader, keyword))
                .eq(status != null, GroupEntity::getStatus, status)
                .buildSpec();
        return groupRepository.findAll(spec, pageable).map(groupMapper::toVO);
    }

    /**
     * Find department by ID
     */
    public Optional<GroupVO> findById(Integer id) {
        return groupRepository.findById(id).map(groupMapper::toVO);
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