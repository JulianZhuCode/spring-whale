package io.github.springwhale.platform.rbac.enums;

import io.github.springwhale.framework.core.enums.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Menu type enumeration.
 *
 * <table>
 *   <tr><th>Type</th><th>DB Value</th><th>Description</th></tr>
 *   <tr><td>{@code DIRECTORY}</td><td>DIRECTORY</td><td>Directory (container for menus)</td></tr>
 *   <tr><td>{@code MENU}</td><td>MENU</td><td>Menu (navigable page)</td></tr>
 *   <tr><td>{@code BUTTON}</td><td>BUTTON</td><td>Button (action permission)</td></tr>
 * </table>
 */
@AllArgsConstructor
@Getter
public enum MenuType implements BaseEnum {

    DIRECTORY("rbac.menus.type.directory", "Directory"),

    MENU("rbac.menus.type.menu", "Menu"),

    BUTTON("rbac.menus.type.button", "Button");

    private final String id;
    private final String desc;
}