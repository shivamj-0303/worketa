package com.worketa.modules.organisation.service;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.worketa.common.exception.ApiException;
import com.worketa.modules.organisation.dto.OrganisationCreateRequest;
import com.worketa.modules.organisation.entity.Organisation;
import com.worketa.modules.organisation.repository.OrganisationRepository;
import com.worketa.modules.users.entity.Role;
import com.worketa.modules.users.entity.User;
import com.worketa.modules.users.repository.RoleRepository;
import com.worketa.modules.users.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class OrganisationServiceTest {

    @Mock
    private OrganisationRepository orgRepo;

    @Mock
    private UserRepository userRepo;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RoleRepository roleRepo;

    @InjectMocks
    private OrganisationService service;

    private OrganisationCreateRequest validRequest;

    @BeforeEach
    void setUp() {
        validRequest = new OrganisationCreateRequest();
        validRequest.setName("Test Org");
        validRequest.setOwnerName("John Doe");
        validRequest.setEmail("john@test.com");
        validRequest.setPassword("password123");
        validRequest.setPhone("1234567890");
    }

    @Test
    void testRegisterOrganisationSuccess() {
        when(orgRepo.existsByEmail(validRequest.getEmail())).thenReturn(false);
        when(orgRepo.save(any(Organisation.class))).thenAnswer(invocation -> {
            Organisation org = invocation.getArgument(0);
            org.setId(java.util.UUID.randomUUID());
            return org;
        });
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        
        Role adminRole = new Role();
        adminRole.setName("ADMIN");
        when(roleRepo.findByName("ADMIN")).thenReturn(Optional.of(adminRole));
        when(userRepo.save(any(User.class))).thenReturn(new User());

        Organisation result = service.register(validRequest);

        assertNotNull(result);
        assertEquals(validRequest.getName(), result.getName());
        verify(orgRepo, times(1)).save(any(Organisation.class));
        verify(userRepo, times(1)).save(any(User.class));
    }

    @Test
    void testRegisterOrganisationDuplicateEmail() {
        when(orgRepo.existsByEmail(validRequest.getEmail())).thenReturn(true);

        try {
            service.register(validRequest);
            fail("Should throw ApiException");
        } catch (ApiException e) {
            assertTrue(true);
        }
        verify(orgRepo, never()).save(any());
    }
}
