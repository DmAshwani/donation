package in.dataman.donation.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserRole {
	External(1,"External"),
	Internal(2,"Internal");
	
	private final int code;
    private final String description;
}
