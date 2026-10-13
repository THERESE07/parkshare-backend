package rw.parkshare.backend.common.dto;

public class UserRoleResponseDto {

    private Long userId;
    private Long roleId;

    public UserRoleResponseDto() {
    }

    public UserRoleResponseDto(
            Long userId,
            Long roleId
    ) {
        this.userId = userId;
        this.roleId = roleId;
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