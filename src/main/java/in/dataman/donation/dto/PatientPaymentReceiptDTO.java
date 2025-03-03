package in.dataman.donation.dto;

import lombok.Data;

@Data
public class PatientPaymentReceiptDTO {
	private String transactionID;
	private String TransactionDate;
	private String CustamerMobile;
	private String PatientName;
	private String PatientEMRNo;
	private String surgeryDate;
	private String amount;
	private String paymentMode;
	private String amountInWords;  // Converted from numeric amount
	
	
	
}

