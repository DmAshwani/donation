package in.dataman.donation.dto;

import in.dataman.donation.comentity.UserMast;
import in.dataman.donation.enums.UserRole;
import in.dataman.donation.transentity.Donation;
import in.dataman.donation.transentity.Registration;
import in.dataman.donation.uitl.TimeConverter;
import lombok.Data;

@Data
public class PatientDTO {

	private String amount;
    private String email;
    private String mobile;
    private String preparedDt;
    private String patientName;
    private String patientEMRNo;
    private String surgeryDate;
    private String patientEmail;
    
    public UserMast toUserMast() {
    	UserMast userMast = new UserMast();
    	userMast.setDescription(this.patientName);
    	userMast.setEMail(this.email);
    	userMast.setIsdCode("+91");
    	userMast.setMobile(this.mobile);
    	userMast.setUser_Role(UserRole.External.getDescription());
    	userMast.setPreparedDt(TimeConverter.formatUnixTimestamp(this.preparedDt));
    	return userMast;
    }
    
    public Registration toRegistration() {
    	Registration registration = new Registration();
    	registration.setName(this.patientName);
    	registration.setIsdCode("+91");
    	registration.setEmail(this.email);
    	registration.setPreparedDt(TimeConverter.formatUnixTimestamp(this.preparedDt));
    	return registration;
    }
    
    public Donation toDonation() {
		Donation donation = new Donation();
		donation.setAmount(this.amount);
		donation.setName(this.patientName);
		donation.setMobile(this.mobile);
		donation.setEmail(this.email);
		donation.setPatientEMRNo(this.patientEMRNo);
		donation.setSurgeryDate(TimeConverter.formatUnixTimestamp(this.surgeryDate));
		donation.setIsdCode("+91");
		return donation;
	}
}
