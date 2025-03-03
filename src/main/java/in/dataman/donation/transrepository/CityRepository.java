package in.dataman.donation.transrepository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import in.dataman.donation.dto.CityDTO;
import in.dataman.donation.transentity.City;

@Repository
public interface CityRepository extends JpaRepository<City, String> {

	Page<City> findByStateCode(String stateCode, Pageable pageable);

	@Query(value = "SELECT * FROM city WHERE stateCode = :stateCode AND cityName LIKE :cityNamePrefix%", nativeQuery = true)
    Page<City> findCitiesByStateAndCityName(@Param("stateCode") int stateCode, @Param("cityNamePrefix") String cityNamePrefix, Pageable pageable);



	@Query(value = "SELECT new in.dataman.donation.dto.CityDTO(c.cityCode, c.cityName) FROM City c WHERE c.cityCode = :code")
	Optional<CityDTO> findCodeAndNameByCode(@Param("code") String code);
}
