package io.github.springwhale.platform.rbac.entity;

import io.github.springwhale.database.BaseEntity;
import io.github.springwhale.platform.rbac.enums.MenuType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Menu entity
 */
@Entity
@Table(name = "rbac_menu", indexes = {
        @Index(name = "idx_menu_parent_id", columnList = "parent_id"),
        @Index(name = "idx_menu_code", columnList = "code")
})
@Data
@EqualsAndHashCode(callSuper = true)
public class MenuEntity extends BaseEntity {

    /**
     * Parent menu ID
     */
    private Integer parentId;

    /**
     * Menu code
     */
    @Column(nullable = false, unique = true, length = 100)
    private String code;

    /**
     * Menu name (fallback when i18n key is not resolved)
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * I18n message key for menu name (e.g. "menu.rbac.user_management").
     * When set, the UI resolves the display name from i18n messages;
     * {@link #name} serves as fallback.
     */
    @Column(length = 200)
    private String nameI18nKey;

    /**
     * Menu type: DIRECTORY, MENU, or BUTTON
     */
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private MenuType type = MenuType.MENU;

    /**
     * Route path
     */
    @Column(length = 200)
    private String path;

    /**
     * Component path
     */
    @Column(length = 200)
    private String component;

    /**
     * Permission identifier
     */
    @Column(length = 100)
    private String permission;

    /**
     * Icon
     */
    @Column(length = 50)
    private String icon;

    /**
     * Sort order
     */
    private Integer sort = 0;

    /**
     * Visibility: 0=hidden, 1=visible
     */
    private Integer visible = 1;

    /**
     * Status: 0=disabled, 1=enabled
     */
    @Column(nullable = false)
    private Integer status = 1;
}