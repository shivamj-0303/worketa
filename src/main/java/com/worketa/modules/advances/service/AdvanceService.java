package com.worketa.modules.advances.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.worketa.common.security.OrganisationContext;
import com.worketa.modules.advances.entity.Advance;
import com.worketa.modules.advances.repository.AdvanceRepository;

@Service
public class AdvanceService {

    private final AdvanceRepository repo;

    public AdvanceService(AdvanceRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public Advance create(Advance advance) {
        advance.setOrganisationId(OrganisationContext.get());
        return repo.save(advance);
    }

    public List<Advance> listByOrg() {
        return repo.findByOrganisationId(OrganisationContext.get());
    }

    public List<Advance> listByEmployee(UUID empId) {
        return repo.findByEmployeeId(empId);
    }

    public List<Advance> getUnsettledByEmployee(UUID empId) {
        return repo.findByEmployeeIdAndSettledFalse(empId);
    }

    @Transactional
    public void delete(UUID id) {
        repo.deleteById(id);
    }
}
