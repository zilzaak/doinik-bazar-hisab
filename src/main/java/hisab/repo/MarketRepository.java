package hisab.repo;

import hisab.entity.Market;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface MarketRepository extends JpaRepository<Market,Long> {

    boolean existsByDate(LocalDate now);

    List<Market> findByDate(LocalDate now);

    @Query("select x from Market x where " +
            " ( cast( :fd as date ) is null or cast(x.date as date) >=  cast(:fd as date ) ) " +
            " and ( cast( :td as date ) is null or cast(x.date as date) <=  cast(:td as date ) )  " +
            " and ( :itemName is null or x.itemName like concat('%' , :itemName , '%')   ) " +
            " order by x.date asc  ")
    Page<Market> allShoppingList(@Param("fd") LocalDate fd, @Param("td") LocalDate td,
                                 @Param("itemName") String itemName, Pageable pageable);

    @Query("select sum(x.itemPrice) from Market x where " +
            " ( cast( :fd as date ) is null or cast(x.date as date) >=  cast(:fd as date ) ) " +
            " and ( cast( :td as date ) is null or cast(x.date as date) <=  cast(:td as date ) )  " +
            " and ( :itemName is null or x.itemName like concat('%' , :itemName , '%')   ) " +
            " order by x.date asc  ")
    Double totalPrice(@Param("fd") LocalDate fd, @Param("td") LocalDate td,
                                 @Param("itemName") String itemName);

    boolean existsByItemNameAndDate(String itemName, LocalDate date);

    boolean existsByItemNameAndDateAndIdNotIn(String itemName, LocalDate date, List<Long> asList);
}
