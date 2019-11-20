package fi.hsl.transitlog.hfp.domain.repositories;

import fi.hsl.transitlog.hfp.domain.VehiclePosition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface VehiclePositionRepository extends JpaRepository<VehiclePosition, UUID> {
}
