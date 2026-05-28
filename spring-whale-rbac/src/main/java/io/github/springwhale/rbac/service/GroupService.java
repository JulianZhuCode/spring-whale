package io.github.springwhale.rbac.service;

import io.github.springwhale.rbac.dto.GroupDTO;
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
    public Page<GroupDTO> findAll(Pageable pageable) {
        return groupRepository.findAll(pageable).map(groupMapper::toDTO);
    }

    /**
     * 根据ID查询部门
     */
    public Optional<GroupDTO> findById(Integer id) {
        return groupRepository.findById(id).map(groupMapper::toDTO);
    }

    /**
     * 根据部门编码精确查询
     */
    public Optional<GroupDTO> findByCode(String code) {
        return groupRepository.findByCode(code).map(groupMapper::toDTO);
    }

    /**
     * 根据父部门ID查询
     */
    public List<GroupDTO> findByParentId(Integer parentId) {
        return groupMapper.toDTOList(groupRepository.findByParentId(parentId));
    }

    /**
     * 搜索部门（支持部门名称模糊查询）
     */
    public List<GroupDTO> search(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return List.of();
        }
        return groupMapper.toDTOList(groupRepository.findByNameContaining(keyword));
    }

    /**
     * 根据状态查询
     */
    public List<GroupDTO> findByStatus(Integer status) {
        return groupMapper.toDTOList(groupRepository.findByStatus(status));
    }

    /**
     * 获取所有根部门（parentId为null）
     */
    public List<GroupDTO> findRootGroups() {
        return groupMapper.toDTOList(groupRepository.findByParentId(null));
    }

    /**
     * 创建部门
     */
    @Transactional
    public GroupDTO create(GroupDTO groupDTO) {
        GroupEntity entity = groupMapper.toEntity(groupDTO);
        return groupMapper.toDTO(groupRepository.save(entity));
    }

    /**
     * 更新部门
     */
    @Transactional
    public GroupDTO update(Integer id, GroupDTO groupDTO) {
        GroupEntity group = groupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("部门不存在，ID: " + id));

        group.setParentId(groupDTO.getParentId());
        group.setCode(groupDTO.getCode());
        group.setName(groupDTO.getName());
        group.setDescription(groupDTO.getDescription());
        group.setLeader(groupDTO.getLeader());
        group.setPhone(groupDTO.getPhone());
        group.setEmail(groupDTO.getEmail());
        group.setSort(groupDTO.getSort());
        group.setStatus(groupDTO.getStatus());

        return groupMapper.toDTO(groupRepository.save(group));
    }

    /**
     * 删除部门
     */
    @Transactional
    public void delete(Integer id) {
        GroupEntity group = groupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("部门不存在，ID: " + id));
        groupRepository.delete(group);
    }
}
