package in.dataman.donation.comrepository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import in.dataman.donation.comentity.UserMast;

@Repository
public interface UserMastRepository extends JpaRepository<UserMast, Integer> {

	@Query("SELECT COALESCE(MAX(u.code), 0) + 1 FROM UserMast u")
	Integer findMaxCode();

	boolean existsByMobile(String mobile);

	boolean existsByeMail(String eMail);

	@Query("SELECT u FROM UserMast u WHERE (:eMail IS NOT NULL AND:eMail<>'' AND u.eMail=:eMail) OR (:eMail IS NULL OR :eMail = '' AND :mobile IS NOT NULL AND :mobile <> '' AND u.mobile = :mobile)")
	Optional<UserMast> findByEMailOrMobile(@Param("eMail") String eMail, @Param("mobile") String mobile);

	@Query("SELECT u FROM UserMast u WHERE (:eMail IS NOT NULL AND :eMail <> '' AND u.eMail = :eMail) "
			+ "OR (:mobile IS NOT NULL AND :mobile <> '' AND u.mobile = :mobile)")
	UserMast findByEmailOrMobile(@Param("eMail") String eMail, @Param("mobile") String mobile);

}
