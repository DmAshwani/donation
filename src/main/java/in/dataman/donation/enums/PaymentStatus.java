package in.dataman.donation.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PaymentStatus {

    AppInitiated(-1, "App Initiated"),
    Confirmed(0, "Confirmed"),
    PaymentInitiated(1, "Payment Initiated"),
    Incomplete(2, "Incomplete"),
    Submitted(3, "Submitted"),
    Success(4, "SUCCESS"),
    Fail(5, "FAIL"),
    Abort(6, "ABORT"),
    NoRecordsFound(7, "No Records Found"),
    Booked(8, "BOOKED"),
    Refund(9, "REFUND"),
    Pending(10, "PENDING"),
    Expired(11, "EXPIRED"),
    Closed(12, "CLOSED"),
    Reject(13, "REJECT");
	
    private final int code;
    private final String description;
}

