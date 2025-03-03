package in.dataman.donation.transrepository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import in.dataman.donation.transentity.State;

public interface StateRepository extends JpaRepository<State, String> {

	@Query(value = "SELECT s.code, s.name FROM stateMast s WHERE s.code = :code",nativeQuery = true)
    Optional<State> findCodeAndNameByCode(@Param("code") String code);
	
}
