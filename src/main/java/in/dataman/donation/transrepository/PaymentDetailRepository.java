package in.dataman.donation.transrepository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import in.dataman.donation.transentity.PaymentDetail;

@Repository
public interface PaymentDetailRepository extends JpaRepository<PaymentDetail, Long> {

	Optional<PaymentDetail> findByResTransRefId(String resTransRefId);

}
