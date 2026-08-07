package com.worketa.modules.users.service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.worketa.common.exception.ApiException;
import com.worketa.common.security.OrganisationContext;
import com.worketa.modules.users.entity.User;
import com.worketa.modules.users.repository.RoleRepository;
import com.worketa.modules.users.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository repo;
    private final RoleRepository roleRepo;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository repo, RoleRepository roleRepo, PasswordEncoder passwordEncoder) {
        this.repo = repo;
        this.roleRepo = roleRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User createUser(String fullName, String email, String password, String roleName) {
        var orgId = OrganisationContext.get();
        
        if (repo.existsByEmailAndOrganisationId(email, orgId)) {
            throw new ApiException("User with email already exists in organisation");
        }
        
        var role = roleRepo.findByName(roleName)
                .orElseThrow(() -> new ApiException("Role not found: " + roleName));
        
        User user = new User();
        user.setOrganisationId(orgId);
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRoles(Set.of(role));
        user.setActive(true);
        
        return repo.save(user);
    }

    public List<User> listByOrg() {
        return repo.findAll(); // Filter by org in controller/filter layer
    }

    public User getById(UUID id) {
        return repo.findById(id)
                .orElseThrow(() -> new ApiException("User not found"));
    }
}
