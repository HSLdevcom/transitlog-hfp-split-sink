package fi.hsl.transitlog.hfp.domain.repositories;

import fi.hsl.transitlog.hfp.domain.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventsRepository extends JpaRepository<Event, Long> {
}
