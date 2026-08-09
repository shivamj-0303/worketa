package com.worketa.modules.organisation.service;

import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final PasswordEncoder passwordEncoder;

    public OrganisationService(
            OrganisationRepository orgRepo,
            UserRepository userRepo,
            PasswordEncoder passwordEncoder
    ) {
        this.orgRepo = orgRepo;
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Organisation register(OrganisationCreateRequest req) {

        if (orgRepo.existsByEmail(req.getEmail())) {
            throw new ApiException(
                    "Organisation with this email already exists"
            );
        }

        // Create organisation
        Organisation org = new Organisation();
        org.setName(req.getName());
        org.setEmail(req.getEmail());
        org.setPhone(req.getPhone());

        org = orgRepo.save(org);

        // Create default admin user
        User admin = new User();

        admin.setOrganisationId(org.getId());
        admin.setFullName(req.getOwnerName());
        admin.setEmail(req.getEmail());
        admin.setPassword(
                passwordEncoder.encode(req.getPassword())
        );

        // Roles is now a simple String column
        admin.setRoles("ADMIN");

        admin.setActive(true);

        userRepo.save(admin);

        return org;
    }
}
