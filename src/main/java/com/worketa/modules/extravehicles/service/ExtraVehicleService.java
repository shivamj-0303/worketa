package com.worketa.modules.extravehicles.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.worketa.common.exception.ApiException;
import com.worketa.common.security.OrganisationContext;
import com.worketa.common.time.WorketaClock;
import com.worketa.modules.extravehicles.entity.ExtraVehicle;
import com.worketa.modules.extravehicles.repository.ExtraVehicleRepository;

@Service
public class ExtraVehicleService {

    private final ExtraVehicleRepository repo;

    public ExtraVehicleService(ExtraVehicleRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public ExtraVehicle create(ExtraVehicle extraVehicle) {
        validate(extraVehicle);
        extraVehicle.setOrganisationId(OrganisationContext.get());
        if (extraVehicle.getVehicleDate() == null) {
            extraVehicle.setVehicleDate(WorketaClock.businessDate());
        }
        return repo.save(extraVehicle);
    }

    public List<ExtraVehicle> listByOrganisation() {
        return repo.findByOrganisationIdOrderByVehicleDateDesc(OrganisationContext.get());
    }

    public ExtraVehicle getById(UUID id) {
        return repo.findById(id)
                .filter(item -> item.getOrganisationId().equals(OrganisationContext.get()))
                .orElseThrow(() -> new ApiException("Extra vehicle not found"));
    }

    @Transactional
    public ExtraVehicle update(UUID id, ExtraVehicle update) {
        ExtraVehicle existing = getById(id);
        validate(update);
        existing.setDriverName(update.getDriverName());
        existing.setAmount(update.getAmount());
        existing.setVehicleDate(update.getVehicleDate());
        existing.setVehicleNumber(update.getVehicleNumber());
        existing.setRouteName(update.getRouteName());
        existing.setVehicleType(update.getVehicleType());
        return repo.save(existing);
    }

    @Transactional
    public void delete(UUID id) {
        repo.delete(getById(id));
    }

    private void validate(ExtraVehicle extraVehicle) {
        if (extraVehicle.getDriverName() == null || extraVehicle.getDriverName().isBlank()
                || extraVehicle.getAmount() == null || extraVehicle.getAmount().signum() <= 0
                || extraVehicle.getVehicleDate() == null
                || extraVehicle.getVehicleNumber() == null || extraVehicle.getVehicleNumber().isBlank()
                || extraVehicle.getRouteName() == null || extraVehicle.getRouteName().isBlank()
                || extraVehicle.getVehicleType() == null || extraVehicle.getVehicleType().isBlank()) {
            throw new ApiException("Driver, amount, date, vehicle number, route and vehicle type are required");
        }
    }
}