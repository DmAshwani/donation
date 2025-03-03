package in.dataman.donation.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PaymentMode {

    ONLINE(1, "Online"),
    OFFLINE(2, "Offline");

    private final int code;
    private final String description;
}
