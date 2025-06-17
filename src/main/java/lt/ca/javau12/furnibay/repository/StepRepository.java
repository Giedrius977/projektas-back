package lt.ca.javau12.furnibay.repository;

import lt.ca.javau12.furnibay.Step;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.transaction.Transactional;

public interface StepRepository extends JpaRepository<Step, Long> {
    
    @Modifying
    @Transactional
    @Query("UPDATE Step s SET s.order = :order WHERE s.id = :id")
    int updateStepOrder(@Param("id") Long id, @Param("order") Integer order);
    
    @Modifying
    @Transactional
    @Query("UPDATE Step s SET s.done = :done WHERE s.id = :id")
    int updateDoneStatus(@Param("id") Long id, @Param("done") boolean done);
}