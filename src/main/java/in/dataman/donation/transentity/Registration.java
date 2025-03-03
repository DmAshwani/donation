package in.dataman.donation.transentity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "registration")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Registration {
	@Id
	private Long code;
	private String userId;
	private String name;
	private String add1;
	private String add2;
	private Integer countryCode;
	private Integer stateCode;
	private Integer cityCode;
	private String mobile;
	private String email;
	private String pin;
	private String isdCode;
	private String pan;
	private String site_Code;
	private String preparedBy;
	private String preparedDt;  // this is sql table dataTime 
}
