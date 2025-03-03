package in.dataman.donation.transrepository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import in.dataman.donation.transentity.Donation;

@Repository
public interface DonationRepository extends JpaRepository<Donation, Long> {
	
}
