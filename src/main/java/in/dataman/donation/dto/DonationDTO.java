package in.dataman.donation.dto;

import java.util.Map;

import in.dataman.donation.comentity.UserMast;
import in.dataman.donation.enums.UserRole;
import in.dataman.donation.transentity.Donation;
import in.dataman.donation.transentity.Registration;
import in.dataman.donation.uitl.TimeConverter;
import lombok.Data;

@Data
public class DonationDTO {
	private String  fullName;
    private String panNumber;
    private String donationAmount;
    private String address1;
    private String address2;
    private Map<String,String> selectedCountry;
    private Map<String,String> selectedState;
    private Map<String,String> selectedDistrict;
    private String pincode;
    private String email;
    private String mobile;
    private String preparedDt;
    private Integer isTaxExemption;
    
    public UserMast toUserMast() {
    	UserMast userMast = new UserMast();
    	userMast.setDescription(this.fullName);
    	userMast.setEMail(this.email);
    	userMast.setIsdCode("+91");
    	userMast.setMobile(this.mobile);
    	userMast.setUser_Role(UserRole.External.getDescription());
    	userMast.setPreparedDt(TimeConverter.formatUnixTimestamp(this.preparedDt));
    	return userMast;
    }
    
    public Registration toRegistration() {
    	Registration registration = new Registration();
    	registration.setName(this.fullName);
    	registration.setAdd1(this.address1);
    	registration.setAdd2(this.address2);
    	registration.setCountryCode(Integer.parseInt(this.selectedCountry.get("code")));
    	registration.setStateCode(Integer.parseInt(this.selectedState.get("code")));
    	registration.setCityCode(Integer.parseInt(this.selectedDistrict.get("code")));
    	registration.setPan(this.panNumber);
    	registration.setPin(this.pincode);
    	registration.setIsdCode("+91");
    	registration.setPreparedDt(TimeConverter.formatUnixTimestamp(this.preparedDt));
    	return registration;
    }
    
    public Donation toDonation() {
		Donation donation = new Donation();
		donation.setAmount(this.donationAmount);
		donation.setName(this.fullName);
		donation.setAdd1(this.address1);
		donation.setAdd2(this.address2);
		donation.setCountryCode(Integer.parseInt(this.selectedCountry.get("code")));
		donation.setStateCode(Integer.parseInt(this.selectedState.get("code")));
		donation.setCityCode(Integer.parseInt(this.selectedDistrict.get("code")));
		donation.setMobile(this.mobile);
		donation.setEmail(this.email);
		donation.setPin(this.pincode);
		donation.setIsdCode("+91");
		donation.setPan(this.panNumber);
		return donation;
	}
     
    
}
