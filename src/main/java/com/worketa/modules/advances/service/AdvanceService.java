package com.worketa.modules.advances.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.worketa.common.exception.ApiException;
import com.worketa.common.security.OrganisationContext;
import com.worketa.common.time.WorketaClock;
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
        if (advance.getAmount() == null || advance.getAmount().signum() <= 0) {
            throw new ApiException("Advance amount must be greater than zero");
        }
        advance.setAdvanceDate(WorketaClock.businessDate());
        if (advance.getId() == null) {
            advance.setStatus(Advance.AdvanceStatus.PENDING);
        }
        return repo.save(advance);
    }

    @Transactional
    public Advance approve(UUID id) {
        Advance advance = getById(id);
        advance.setStatus(Advance.AdvanceStatus.APPROVED);
        return repo.save(advance);
    }

    @Transactional
    public Advance reject(UUID id) {
        Advance advance = getById(id);
        advance.setStatus(Advance.AdvanceStatus.REJECTED);
        return repo.save(advance);
    }

    public Advance getById(UUID id) {
        return repo.findById(id).filter(advance -> advance.getOrganisationId().equals(OrganisationContext.get()))
                .orElseThrow(() -> new ApiException("Advance not found"));
    }

    public List<Advance> listByOrg() {
        return repo.findByOrganisationId(OrganisationContext.get());
    }

    public List<Advance> listByEmployee(UUID empId) {
        return repo.findByEmployeeIdAndOrganisationId(empId, OrganisationContext.get());
    }

    public List<Advance> getUnsettledByEmployee(UUID empId) {
        return repo.findByEmployeeIdAndOrganisationIdAndSettledFalse(empId, OrganisationContext.get());
    }

    @Transactional
    public void delete(UUID id) {
        repo.deleteById(id);
    }
}
