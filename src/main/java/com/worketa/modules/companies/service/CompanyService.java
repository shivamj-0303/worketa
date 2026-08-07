package com.worketa.modules.companies.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.worketa.common.exception.ApiException;
import com.worketa.common.security.OrganisationContext;
import com.worketa.modules.companies.entity.Company;
import com.worketa.modules.companies.repository.CompanyRepository;

@Service
public class CompanyService {

    private final CompanyRepository repo;

    public CompanyService(CompanyRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public Company create(Company company) {
        company.setOrganisationId(OrganisationContext.get());
        return repo.save(company);
    }

    @Transactional
    public Company update(UUID id, Company company) {
        var existing = repo.findByIdAndOrganisationId(id, OrganisationContext.get())
                .orElseThrow(() -> new ApiException("Company not found"));
        
        existing.setName(company.getName());
        existing.setContactPerson(company.getContactPerson());
        existing.setPhone(company.getPhone());
        existing.setEmail(company.getEmail());
        existing.setAddress(company.getAddress());
        return repo.save(existing);
    }

    @Transactional
    public void delete(UUID id) {
        var company = repo.findByIdAndOrganisationId(id, OrganisationContext.get())
                .orElseThrow(() -> new ApiException("Company not found"));
        company.setActive(false);
        repo.save(company);
    }

    public List<Company> listByOrg() {
        return repo.findByOrganisationId(OrganisationContext.get());
    }

    public Company getById(UUID id) {
        return repo.findByIdAndOrganisationId(id, OrganisationContext.get())
                .orElseThrow(() -> new ApiException("Company not found"));
    }
}
