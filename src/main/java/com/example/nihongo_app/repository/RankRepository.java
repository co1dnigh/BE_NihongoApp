package com.example.nihongo_app.repository;

import com.example.nihongo_app.entity.Rank;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RankRepository extends JpaRepository<Rank, Long> {

    List<Rank> findAllByOrderByOrderIndexAsc();

    Optional<Rank> findFirstByMinExpRequiredLessThanEqualOrderByOrderIndexDesc(Integer minExpRequired);
}
