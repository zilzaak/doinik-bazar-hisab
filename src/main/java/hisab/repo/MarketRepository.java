package hisab.repo;

import hisab.entity.Market;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface MarketRepository extends JpaRepository<Market,Long> {

    boolean existsByDate(LocalDate now);

    List<Market> findByDate(LocalDate now);
}
