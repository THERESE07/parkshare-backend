package rw.parkshare.backend.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rw.parkshare.backend.user.entity.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    Optional<User> findByPhone(String phone);
}
