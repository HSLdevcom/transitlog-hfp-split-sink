package fi.hsl.transitlog.hfp.domain.repositories;

import fi.hsl.transitlog.hfp.domain.Event;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventsRepository extends JpaRepository<Event, Long> {
}
