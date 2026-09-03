package io.github.springwhale.platform.rbac.service;

import io.github.springwhale.database.JpaQueryWrapper;
import io.github.springwhale.framework.core.exception.BusinessException;
import io.github.springwhale.platform.rbac.dao.entity.GroupEntity;
import io.github.springwhale.platform.rbac.dao.entity.UserEntity;
import io.github.springwhale.platform.rbac.dao.repository.GroupRepository;
import io.github.springwhale.platform.rbac.dao.repository.UserRepository;
import io.github.springwhale.platform.rbac.dto.mapper.GroupMapper;
import io.github.springwhale.platform.rbac.dto.request.GroupRequest;
import io.github.springwhale.platform.rbac.dto.vo.GroupTreeVO;
import io.github.springwhale.platform.rbac.dto.vo.GroupVO;
import io.github.springwhale.framework.event.EventPublisher;
import io.github.springwhale.platform.rbac.event.GroupChangedEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Group (department) service
 */
@Transactional(readOnly = true)
public class GroupService {

    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final GroupMapper groupMapper;
    private final EventPublisher eventPublisher;

    public GroupService(GroupRepository groupRepository,
                        UserRepository userRepository,
                        GroupMapper groupMapper,
                        EventPublisher eventPublisher) {
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
        this.groupMapper = groupMapper;
        this.eventPublisher = eventPublisher;
    }

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

        entity.setPath(buildPath(request.getParentId()));

        GroupVO result = groupMapper.toVO(groupRepository.save(entity));
        eventPublisher.publishAfterCommit(new GroupChangedEvent(result.getId()));
        return result;
    }

    /**
     * Update department
     */
    @Transactional
    public GroupVO update(Integer id, GroupRequest request) {
        GroupEntity group = groupRepository.findById(id)
                .orElseThrow(() -> BusinessException.create("GROUP_NOT_FOUND", "Department not found, ID: " + id));

        Integer oldParentId = group.getParentId();
        group.setParentId(request.getParentId());
        group.setCode(request.getCode());
        group.setName(request.getName());
        group.setDescription(request.getDescription());
        group.setLeader(request.getLeader());
        group.setPhone(request.getPhone());
        group.setEmail(request.getEmail());
        group.setSort(request.getSort());
        group.setStatus(request.getStatus());

        if (!Objects.equals(oldParentId, request.getParentId())) {
            String oldPath = group.getPath();
            group.setPath(buildPath(request.getParentId()));
            updateDescendantPaths(group, oldPath);
        }

        GroupVO result = groupMapper.toVO(groupRepository.save(group));
        eventPublisher.publishAfterCommit(new GroupChangedEvent(id));
        return result;
    }

    /**
     * Delete department. Child departments and users are moved to the
     * parent department before deletion.
     */
    @Transactional
    public void delete(Integer id) {
        GroupEntity group = groupRepository.findById(id)
                .orElseThrow(() -> BusinessException.create("GROUP_NOT_FOUND", "Department not found, ID: " + id));

        Integer parentId = group.getParentId();

        // 1. Move child departments to the parent of the deleted department
        List<GroupEntity> children = groupRepository.findByPathStartingWith(group.getPath() + id + "/");
        if (!children.isEmpty()) {
            String newParentPath = parentId != null ? buildPath(parentId) : "/";
            String deletedPrefix = group.getPath() + id + "/";
            for (GroupEntity child : children) {
                child.setParentId(parentId);
                String remainder = child.getPath().substring(deletedPrefix.length());
                child.setPath(newParentPath + remainder);
            }
            groupRepository.saveAll(children);
        }

        // 2. Move users in this department to the parent department
        List<UserEntity> users = userRepository.findByGroupId(id);
        if (!users.isEmpty()) {
            for (UserEntity user : users) {
                user.setGroupId(parentId);
            }
            userRepository.saveAll(users);
        }

        // 3. Delete the department
        groupRepository.delete(group);
        eventPublisher.publishAfterCommit(new GroupChangedEvent(id));
    }

    /**
     * Build department tree
     */
    public List<GroupTreeVO> buildDeptTree() {
        List<GroupEntity> all = groupRepository.findAll();

        Map<Integer, List<GroupTreeVO>> parentMap = all.stream()
                .sorted(Comparator.comparing(GroupEntity::getSort, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(this::toTreeVO)
                .collect(Collectors.groupingBy(
                        vo -> vo.getParentId() != null ? vo.getParentId() : 0,
                        LinkedHashMap::new,
                        Collectors.toList()));

        List<GroupTreeVO> roots = parentMap.getOrDefault(0, List.of());
        for (GroupTreeVO root : roots) {
            buildChildren(root, parentMap);
        }
        return roots;
    }

    private GroupTreeVO toTreeVO(GroupEntity entity) {
        GroupTreeVO vo = new GroupTreeVO();
        vo.setId(entity.getId());
        vo.setParentId(entity.getParentId());
        vo.setCode(entity.getCode());
        vo.setName(entity.getName());
        return vo;
    }

    private void buildChildren(GroupTreeVO parent, Map<Integer, List<GroupTreeVO>> parentMap) {
        List<GroupTreeVO> children = parentMap.get(parent.getId());
        if (children != null) {
            parent.setChildren(children);
            for (GroupTreeVO child : children) {
                buildChildren(child, parentMap);
            }
        }
    }

    // ==================== Materialized path helpers ====================

    /**
     * Find all descendants of a department.
     *
     * @param deptId the department ID
     * @return all descendant departments (excluding the department itself)
     */
    public List<GroupVO> findDescendants(Integer deptId) {
        GroupEntity dept = groupRepository.findById(deptId)
                .orElseThrow(() -> BusinessException.create("GROUP_NOT_FOUND", "Department not found, ID: " + deptId));
        String prefix = dept.getPath() + deptId + "/";
        return groupRepository.findByPathStartingWith(prefix).stream()
                .map(groupMapper::toVO)
                .collect(Collectors.toList());
    }

    /**
     * Build materialized path for a new department based on its parent.
     * Root nodes (parentId is null) get path "/".
     */
    private String buildPath(Integer parentId) {
        if (parentId == null) {
            return "/";
        }
        GroupEntity parent = groupRepository.findById(parentId)
                .orElseThrow(() -> BusinessException.create("GROUP_NOT_FOUND", "Parent department not found, ID: " + parentId));
        return parent.getPath() + parentId + "/";
    }

    /**
     * When a department is moved to a new parent, update the path of all its descendants.
     */
    private void updateDescendantPaths(GroupEntity dept, String oldPath) {
        String oldPrefix = oldPath + dept.getId() + "/";
        List<GroupEntity> descendants = groupRepository.findByPathStartingWith(oldPrefix);
        if (descendants.isEmpty()) {
            return;
        }
        String newPrefix = dept.getPath() + dept.getId() + "/";
        for (GroupEntity descendant : descendants) {
            String newPath = newPrefix + descendant.getPath().substring(oldPrefix.length());
            descendant.setPath(newPath);
        }
        groupRepository.saveAll(descendants);
    }
}