package in.dataman.donation.transentity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="donation")
public class Donation {
	
	@Id
	private Long docId;
	
	private Integer v_Type;
	private Integer v_No;
	private String recIdPrefix;
	private String recId;
	private Integer v_Prefix;
	private String v_Date;
	private Double v_Time;
	private Integer site_Code;
	private String preparedDt;
	private String preparedBy;
	private String amount;
	private Integer paymentMode;
	private Integer status;
	private Long registrationCode;
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
	private Long paymentId;
	private String patientName;
	private String patientEMRNo;
	private String surgeryDate;
	private String patientEmail;
	private Integer isTaxExemption = 0;
	
}
