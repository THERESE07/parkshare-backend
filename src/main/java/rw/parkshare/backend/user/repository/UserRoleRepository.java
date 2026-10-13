package rw.parkshare.backend.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rw.parkshare.backend.user.entity.UserRole;
import rw.parkshare.backend.user.entity.UserRoleId;

import java.util.List;

public interface UserRoleRepository extends JpaRepository<UserRole, UserRoleId> {

    List<UserRole> findByUserId(Long userId);

    List<UserRole> findByRoleId(Long roleId);

    boolean existsByUserIdAndRoleId(Long userId, Long roleId);
}