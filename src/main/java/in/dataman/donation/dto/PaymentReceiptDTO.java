package in.dataman.donation.dto;

import java.util.Date;

import lombok.Data;

@Data
public class PaymentReceiptDTO {
	private String recId;
    private String name;
    private String address;
    private String pan;
    private String mobile;
    private String email;
    private Date dateOfDonation;
    private double amount;
    private String paymentMode;
    private String amountInWords;  // Converted from numeric amount
}
