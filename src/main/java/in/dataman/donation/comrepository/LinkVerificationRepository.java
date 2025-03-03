package in.dataman.donation.comrepository;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import in.dataman.donation.comentity.LinkVerification;
import jakarta.transaction.Transactional;

@Repository
public interface LinkVerificationRepository extends JpaRepository<LinkVerification, String> {

    @Query(value = "SELECT * FROM linkVerification WHERE mobile = ?1 AND otp = ?2", nativeQuery = true)
    LinkVerification findByMobileAndOtp(String mobile, String otp);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM linkVerification WHERE mobile = ?1 AND keyField = ?2", nativeQuery = true)
    void deleteByMobileAndKeyField(String mobile, String keyField);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM linkVerification WHERE preparedDt < ?1", nativeQuery = true)
    void deleteExpiredOtps(LocalDateTime expirationTime);
    
    
}
