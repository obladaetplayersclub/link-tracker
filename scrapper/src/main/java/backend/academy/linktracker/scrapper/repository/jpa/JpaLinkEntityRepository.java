package backend.academy.linktracker.scrapper.repository.jpa;

import backend.academy.linktracker.scrapper.entity.LinkEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaLinkEntityRepository extends JpaRepository<LinkEntity, Long> {

    Optional<LinkEntity> findByUrl(String url);

    boolean existsByUrl(String url);

    List<LinkEntity> findAllByOrderByLastUpdatedAsc(Pageable pageable);
}
