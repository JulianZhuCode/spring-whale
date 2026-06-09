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
 * 分组（部门）服务
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GroupService {

    private final GroupRepository groupRepository;
    private final GroupMapper groupMapper;

    /**
     * 分页查询所有部门
     */
    public Page<GroupVO> findAll(Pageable pageable) {
        return groupRepository.findAll(pageable).map(groupMapper::toVO);
    }

    /**
     * 根据ID查询部门
     */
    public Optional<GroupVO> findById(Integer id) {
        return groupRepository.findById(id).map(groupMapper::toVO);
    }

    /**
     * 根据部门编码精确查询
     */
    public Optional<GroupVO> findByCode(String code) {
        return groupRepository.findByCode(code).map(groupMapper::toVO);
    }

    /**
     * 根据父部门ID查询
     */
    public List<GroupVO> findByParentId(Integer parentId) {
        return groupMapper.toVOList(groupRepository.findByParentId(parentId));
    }

    /**
     * 搜索部门（支持部门名称模糊查询）
     */
    public List<GroupVO> search(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return List.of();
        }
        return groupMapper.toVOList(groupRepository.findByNameContaining(keyword));
    }

    /**
     * 根据状态查询
     */
    public List<GroupVO> findByStatus(Integer status) {
        return groupMapper.toVOList(groupRepository.findByStatus(status));
    }

    /**
     * 获取所有根部门（parentId为null）
     */
    public List<GroupVO> findRootGroups() {
        return groupMapper.toVOList(groupRepository.findByParentId(null));
    }

    /**
     * 创建部门
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
     * 更新部门
     */
    @Transactional
    public GroupVO update(Integer id, GroupRequest request) {
        GroupEntity group = groupRepository.findById(id)
                .orElseThrow(() -> BusinessException.create("GROUP_NOT_FOUND", "部门不存在，ID: " + id));

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
     * 删除部门
     */
    @Transactional
    public void delete(Integer id) {
        GroupEntity group = groupRepository.findById(id)
                .orElseThrow(() -> BusinessException.create("GROUP_NOT_FOUND", "部门不存在，ID: " + id));
        groupRepository.delete(group);
    }
}
