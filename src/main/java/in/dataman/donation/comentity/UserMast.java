package in.dataman.donation.comentity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="userMast")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserMast {
	@Id
	private Integer code; 
	private String user_Name;
	private String eMail;
	private String mobile;
	private String isdCode;
	private String passWd;
	private String description;
	private String user_Role;
	private String preparedBy;
	private String preparedDt;
	
	
}
