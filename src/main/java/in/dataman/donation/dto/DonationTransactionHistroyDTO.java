package in.dataman.donation.dto;

import lombok.Data;

@Data
public class DonationTransactionHistroyDTO {
	private String TransactionID;
	private String TransactionDate;
	private String DonorName;
	private String Status;
	private String amount;
	private String docId;
}
