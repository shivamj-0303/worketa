package com.worketa.modules.organisation.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.worketa.common.exception.ApiException;
import com.worketa.modules.organisation.dto.OrganisationCreateRequest;
import com.worketa.modules.organisation.entity.Organisation;
import com.worketa.modules.organisation.repository.OrganisationRepository;
import com.worketa.modules.users.entity.User;
import com.worketa.modules.users.repository.UserRepository;

@Service
public class OrganisationService {

    private final OrganisationRepository orgRepo;
    private final UserRepository userRepo;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;
    private final com.worketa.modules.users.repository.RoleRepository roleRepo;

    public OrganisationService(OrganisationRepository orgRepo,
                               UserRepository userRepo,
                               org.springframework.security.crypto.password.PasswordEncoder passwordEncoder,
                               com.worketa.modules.users.repository.RoleRepository roleRepo) {
        this.orgRepo = orgRepo;
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.roleRepo = roleRepo;
    }

    @Transactional
    public Organisation register(OrganisationCreateRequest req) {
        if (orgRepo.existsByEmail(req.getEmail())) {
            throw new ApiException("Organisation with this email already exists");
        }

        Organisation org = new Organisation();
        org.setName(req.getName());
        org.setEmail(req.getEmail());
        org.setPhone(req.getPhone());
        org = orgRepo.save(org);

        // create default admin user
        User admin = new User();
        admin.setOrganisationId(org.getId());
        admin.setFullName(req.getOwnerName());
        admin.setEmail(req.getEmail());
        admin.setPassword(passwordEncoder.encode(req.getPassword()));
        var adminRole = roleRepo.findByName("ADMIN").orElseGet(() -> {
            var r = new com.worketa.modules.users.entity.Role();
            r.setName("ADMIN");
            r.setDescription("Organisation admin");
            return roleRepo.save(r);
        });
        admin.setRoles(java.util.Set.of(adminRole));
        userRepo.save(admin);

        return org;
    }
}
