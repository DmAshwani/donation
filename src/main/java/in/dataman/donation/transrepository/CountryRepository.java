package in.dataman.donation.transrepository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import in.dataman.donation.transentity.Country;

@Repository
public interface CountryRepository extends JpaRepository<Country, String> {
	
	 @Query(value = "SELECT c.code, c.name FROM country c WHERE c.code = :code", nativeQuery = true)
	    Optional<Country> findByCode(@Param("code") String code);
	
}
