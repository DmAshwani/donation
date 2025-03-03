package in.dataman.donation.dto;

import lombok.Data;

import java.util.Map;

@Data
public class AdminDonationTransactionHistroryDTO {
	private String transactionID;
	private String TransactionDate;
	private String DonorName;
	private String Status;
	private String amount;
	private String docId;
	private String paymentId;
	private String panNumber;
	private String address1;
	private String address2;
	private Map<String,String> selectedCountry;
	private Map<String,String> selectedState;
	private Map<String,String> selectedDistrict;
	private String pincode;
	private String email;
	private String mobile;
	private String isTaxExemption;
}
