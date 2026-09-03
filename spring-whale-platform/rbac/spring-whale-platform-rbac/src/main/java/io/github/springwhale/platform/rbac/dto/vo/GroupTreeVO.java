package io.github.springwhale.platform.rbac.dto.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class GroupTreeVO {
    private Long id;
    private Long parentId;
    private String code;
    private String name;
    private List<GroupTreeVO> children = new ArrayList<>();
}