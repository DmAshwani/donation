package in.dataman.donation.dto;

import lombok.Data;

@Data
public class PatientTransactionHistroyDTO {
	
	private String transactionID;
	private String TransactionDate;
	private String CustomerMobile;
	private String PatientName;
	private String PatientEMRNo;
	private String surgeryDate;
	private String Status;
	private String amount;
	private String docId;
	
}
