package com.worketa.modules.trips.service;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.worketa.common.exception.ApiException;
import com.worketa.common.security.OrganisationContext;
import com.worketa.modules.employees.entity.Employee;
import com.worketa.modules.employees.repository.EmployeeRepository;
import com.worketa.modules.trips.entity.Trip;
import com.worketa.modules.trips.repository.TripRepository;
import com.worketa.modules.vehicles.entity.Vehicle;
import com.worketa.modules.vehicles.repository.VehicleRepository;

@ExtendWith(MockitoExtension.class)
class TripServiceTest {

    @Mock
    private TripRepository tripRepo;

    @Mock
    private VehicleRepository vehicleRepo;

    @Mock
    private EmployeeRepository employeeRepo;

    @InjectMocks
    private TripService service;

    private UUID orgId;
    private Vehicle vehicle;
    private Employee driver;
    private Trip trip;

    @BeforeEach
    void setUp() {
        orgId = UUID.randomUUID();
        OrganisationContext.set(orgId);
        
        vehicle = new Vehicle();
        vehicle.setId(UUID.randomUUID());
        vehicle.setActive(true);
        
        driver = new Employee();
        driver.setId(UUID.randomUUID());
        driver.setActive(true);
        
        trip = new Trip();
        trip.setVehicleId(vehicle.getId());
        trip.setDriverId(driver.getId());
        trip.setTripDate(LocalDate.now());
        trip.setStartTime(OffsetDateTime.now());
        trip.setEndTime(OffsetDateTime.now().plusHours(2));
    }

    @Test
    void testCreateTripSuccess() {
        when(vehicleRepo.findByIdAndOrganisationId(vehicle.getId(), orgId))
                .thenReturn(Optional.of(vehicle));
        when(employeeRepo.findByIdAndOrganisationId(driver.getId(), orgId))
                .thenReturn(Optional.of(driver));
        when(tripRepo.findOverlappingTrips(any(), any(), any(), any()))
                .thenReturn(List.of());
        when(tripRepo.findByDriverIdAndTripDate(driver.getId(), LocalDate.now()))
                .thenReturn(List.of());
        when(tripRepo.save(any(Trip.class))).thenAnswer(inv -> inv.getArgument(0));

        Trip result = service.create(trip);

        assertNotNull(result);
        assertEquals(orgId, result.getOrganisationId());
    }

    @Test
    void testCreateTripInactiveVehicle() {
        vehicle.setActive(false);
        when(vehicleRepo.findByIdAndOrganisationId(vehicle.getId(), orgId))
                .thenReturn(Optional.of(vehicle));

        assertThrows(ApiException.class, () -> service.create(trip));
    }

    @Test
    void testCreateTripVehicleNotFound() {
        when(vehicleRepo.findByIdAndOrganisationId(vehicle.getId(), orgId))
                .thenReturn(Optional.empty());

        assertThrows(ApiException.class, () -> service.create(trip));
    }
}
