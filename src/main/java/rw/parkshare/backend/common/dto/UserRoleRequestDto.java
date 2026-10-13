package rw.parkshare.backend.common.dto;

public class UserRoleRequestDto {

    private Long userId;
    private Long roleId;

    public UserRoleRequestDto() {
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }
}
