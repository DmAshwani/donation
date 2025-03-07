package in.dataman.donation.transrepository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import in.dataman.donation.transentity.Registration;

@Repository
public interface RegistrationRepository extends JpaRepository<Registration, Long> {

	@Query("SELECT r FROM Registration r WHERE r.email = :eMail AND r.mobile = :mobile")
	Optional<Registration> findByEmailAndMobile(@Param("eMail") String eMail, @Param("mobile") String mobile);
	
	Registration findByMobile(String mobile);

//	@Query("SELECT r FROM Registration r WHERE (:eMail IS NOT NULL AND :eMail <> '' AND r.email = :eMail) OR (:eMail IS NULL OR :eMail = '' AND :mobile IS NOT NULL AND :mobile <> '' AND r.mobile = :mobile)")
	@Query(value = "SELECT * FROM registration r WHERE (:eMail IS NOT NULL AND :eMail <> '' AND r.email = :eMail) OR (:eMail IS NULL OR :eMail = '') AND (:mobile IS NOT NULL AND :mobile <> '' AND r.mobile = :mobile)", nativeQuery = true)
	Optional<Registration> findByEmailOrMobile(@Param("eMail") String eMail, @Param("mobile") String mobile);

}
