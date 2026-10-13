package rw.parkshare.backend.security;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import rw.parkshare.backend.user.entity.Role;
import rw.parkshare.backend.user.entity.User;
import rw.parkshare.backend.user.entity.UserRole;
import rw.parkshare.backend.user.repository.RoleRepository;
import rw.parkshare.backend.user.repository.UserRepository;
import rw.parkshare.backend.user.repository.UserRoleRepository;

import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;

    public CustomUserDetailsService(
            UserRepository userRepository,
            UserRoleRepository userRoleRepository,
            RoleRepository roleRepository
    ) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "User not found with email: " + email
                        )
                );

        List<SimpleGrantedAuthority> authorities = userRoleRepository
                .findByUserId(user.getId())
                .stream()
                .map(UserRole::getRoleId)
                .map(roleRepository::findById)
                .filter(java.util.Optional::isPresent)
                .map(java.util.Optional::get)
                .map(Role::getName)
                .map(String::toUpperCase)
                .map(roleName -> new SimpleGrantedAuthority("ROLE_" + roleName))
                .toList();

        boolean enabled = "ACTIVE".equalsIgnoreCase(user.getStatus());

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPasswordHash())
                .authorities(authorities)
                .disabled(!enabled)
                .build();
    }
}
