package in.dataman.donation.service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import in.dataman.donation.enums.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import in.dataman.donation.comentity.UserMast;
import in.dataman.donation.comrepository.UserMastRepository;
import in.dataman.donation.dto.DonationDTO;
import in.dataman.donation.dto.PatientDTO;
import in.dataman.donation.dto.PaymentGatewayDTO;
import in.dataman.donation.enums.PaymentMode;
import in.dataman.donation.enums.PaymentStatus;
import in.dataman.donation.exception.DonationProcessingException;
import in.dataman.donation.transentity.Donation;
import in.dataman.donation.transentity.PaymentDetail;
import in.dataman.donation.transentity.Registration;
import in.dataman.donation.transrepository.CityRepository;
import in.dataman.donation.transrepository.CountryRepository;
import in.dataman.donation.transrepository.DonationRepository;
import in.dataman.donation.transrepository.PaymentDetailRepository;
import in.dataman.donation.transrepository.RegistrationRepository;
import in.dataman.donation.transrepository.StateRepository;
import in.dataman.donation.uitl.DmBaseService;
import in.dataman.donation.uitl.MD5Util;
import in.dataman.donation.uitl.RecId;
import in.dataman.donation.uitl.TimeConverter;

@Service
public class DonationService {

	private final UserMastRepository userMastRepository;
	private final MD5Util md5Util;
	private final RegistrationRepository registrationRepository;
	private final DonationRepository donationRepository;
	private final DmBaseService dmBaseService;
	private final PaymentDetailRepository paymentDetailRepository;

	private final CountryRepository countryRepository;
	private final StateRepository stateRepository;
	private final CityRepository cityRepository;
	private final RazorpayService razorpayService;

	@Autowired
	@Qualifier("TransactionJdbcTemplate")
	private JdbcTemplate jdbcTemplate;

	public DonationService(UserMastRepository userMastRepository, MD5Util md5Util,
			RegistrationRepository registrationRepository, DonationRepository donationRepository,
			DmBaseService dmBaseService, PaymentDetailRepository paymentDetailRepository,
			CountryRepository countryRepository, StateRepository stateRepository, CityRepository cityRepository,RazorpayService razorpayService) {
		super();
		this.userMastRepository = userMastRepository;
		this.md5Util = md5Util;
		this.registrationRepository = registrationRepository;
		this.donationRepository = donationRepository;
		this.dmBaseService = dmBaseService;
		this.paymentDetailRepository = paymentDetailRepository;
		this.countryRepository = countryRepository;
		this.stateRepository = stateRepository;
		this.cityRepository = cityRepository;
		this.razorpayService = razorpayService;
	}

	@Transactional(isolation = Isolation.SERIALIZABLE, timeout = 10)
	public Map<String,String> donation(DonationDTO donationDTO) throws Exception {
		try {

			boolean mobileExists = userMastRepository.existsByMobile(donationDTO.getMobile());
			boolean emailExists = userMastRepository.existsByeMail(donationDTO.getEmail());

			System.out.println("//////////////////////////////////////////////");
			System.out.println("Mobile exists "+mobileExists);
			System.out.println("Email Exists "+emailExists);
			System.out.println("//////////////////////////////////////////////");
			String uNamePrefix;
			Registration registration;
			Long codereg;
			if (!mobileExists || !emailExists) {
				Integer codes = userMastRepository.findMaxCode();
				uNamePrefix = "DM" + codes;
				String encodedPassword = md5Util.encodeToHex(donationDTO.getFullName(), codes.toString());
				// **New User Case** → Create UserMast and Registration
				UserMast userMast = donationDTO.toUserMast();
				userMast.setCode(codes);
				userMast.setUser_Name(uNamePrefix);
				userMast.setPassWd(encodedPassword);
				userMast.setPreparedBy(uNamePrefix);
				userMast.setUser_Role(UserRole.External.getDescription());
				userMastRepository.save(userMast);

				codereg = Long.parseLong(dmBaseService.getSerialNoBigInt("1", "registration"));
				registration = donationDTO.toRegistration();
				registration.setCode(codereg);
				registration.setUserId(uNamePrefix);
				registration.setEmail(donationDTO.getEmail()); // Set Email
				registration.setMobile(donationDTO.getMobile()); // Set Mobile
				registration.setPreparedBy(uNamePrefix);
				registrationRepository.save(registration);

			} else {
			
				// **Edit Case** → Only update Registration (Mobile & Email are not editable)
				registration = registrationRepository
						.findByEmailAndMobile(donationDTO.getEmail(), donationDTO.getMobile())
						.orElseThrow(
								() -> new DonationProcessingException("No registration found for this email/mobile"));

				// Preserve existing 'code' and 'id'
				codereg = registration.getCode(); // Keep the existing 'code'
				uNamePrefix = registration.getUserId();
				// Update only the necessary fields
				registration.setPreparedBy(uNamePrefix);
				registration.setName(donationDTO.getFullName()); // Example: Update full name
				registration.setAdd1(donationDTO.getAddress1()); // Update address1
				registration.setAdd2(donationDTO.getAddress2()); // Update address2
				registration.setCountryCode(Integer.parseInt(donationDTO.getSelectedCountry().get("code")));
				registration.setStateCode(Integer.parseInt(donationDTO.getSelectedState().get("code")));
				registration.setCityCode(Integer.parseInt(donationDTO.getSelectedDistrict().get("code")));
				registration.setPan(donationDTO.getPanNumber()); // Update pan number
				registration.setPin(donationDTO.getPincode()); // Update pincode
				registration.setIsdCode("+91"); // Update isd code if necessary
				registration.setPreparedDt(TimeConverter.formatUnixTimestamp(donationDTO.getPreparedDt())); // Update
																											// prepared
																											// date

				// Save the updated registration without changing the 'id' or 'code'
				registrationRepository.save(registration);
			}

			Integer voucherType = fetchVoucherType("DO");
			String v_Prefix = dmBaseService.fetchVoucherPrefix(donationDTO.getPreparedDt());
			Long docId = Long.parseLong(dmBaseService.getDocId(voucherType.toString(), v_Prefix, "1"));

			RecId recId = dmBaseService.getRecId("donation", "docId", docId.toString(), "recId", new RecId(),
					convertUnixTimestampToDate(donationDTO.getPreparedDt()), "v_Type", voucherType.toString(), v_Prefix,
					"1", "HO", "1", true, null);

			Donation donation = donationDTO.toDonation();
			donation.setDocId(docId);
			donation.setV_Type(voucherType);
			donation.setV_No(recId.getCounter().intValue());
			donation.setRecIdPrefix(recId.getPrefix());
			donation.setRecId(recId.getRecIdValue());
			donation.setV_Prefix(Integer.parseInt(v_Prefix));
			donation.setV_Time(Double.parseDouble(convertUnixTimestampToTime(donationDTO.getPreparedDt())));
			donation.setSite_Code(1);
			donation.setPreparedBy(uNamePrefix);
			donation.setPaymentMode(PaymentMode.ONLINE.getCode());
			donation.setRegistrationCode(codereg);
			donation.setV_Date(convertUnixTimestampToDate(donationDTO.getPreparedDt()));
			donation.setAmount(donationDTO.getDonationAmount());
			donation.setStatus(PaymentStatus.AppInitiated.getCode());
			donation.setIsTaxExemption(donationDTO.getIsTaxExemption());
			donationRepository.save(donation);

			String onlineTransId = UUID.randomUUID().toString().replace("-", "");
			Double amount = Double.parseDouble(donationDTO.getDonationAmount());
			String razorpayOderId = razorpayService.createOrder(amount, "INR", onlineTransId);
			PaymentDetail paymentDetail = new PaymentDetail();
			paymentDetail.setV_Type(voucherType);
			paymentDetail.setRecId(recId.getRecIdValue());
			paymentDetail.setV_Date(convertUnixTimestampToDate(donationDTO.getPreparedDt()));
			paymentDetail.setV_Time(Double.parseDouble(convertUnixTimestampToTime(donationDTO.getPreparedDt())));
			paymentDetail.setDocId(docId);
			paymentDetail.setSite_Code((short) 1);
			paymentDetail.setPaymentOption(PaymentMode.ONLINE.getCode());
			paymentDetail.setOnlineTransId(onlineTransId);
			paymentDetail.setResTransRefId(razorpayOderId);
			paymentDetail.setAmount(Double.parseDouble(donationDTO.getDonationAmount()));
			paymentDetail.setStatus(PaymentStatus.AppInitiated.getCode());
			paymentDetailRepository.save(paymentDetail);
			
			Optional<PaymentDetail> pdoptional = paymentDetailRepository.findByResTransRefId(razorpayOderId);

			pdoptional.ifPresent(pd -> {
			    donation.setPaymentId(pd.getId());  // Set payment ID
			    donationRepository.save(donation);  // Re-save donation with updated paymentId
			});

			
			Map<String, String> resTrId = new HashMap<>();
			resTrId.put("OrderId", razorpayOderId);
			return resTrId;
		} catch (Exception ex) {
			// Log the error and throw a custom exception to trigger rollback
			throw new DonationProcessingException("Error processing donation: " + ex.getMessage(), ex);
		}
	}

	
	@Transactional(isolation = Isolation.SERIALIZABLE, timeout = 10)
	public Map<String, String> patient(PatientDTO patientDTO) throws Exception {
	    try {
	        boolean mobileExists = userMastRepository.existsByMobile(patientDTO.getMobile());
	        boolean emailExists = userMastRepository.existsByeMail(patientDTO.getEmail());

	        String uNamePrefix;
	        Registration registration;
	        Long codereg;

	        if (!mobileExists || !emailExists) {
	            Integer codes = userMastRepository.findMaxCode();
	            uNamePrefix = "DM" + codes;
	            String encodedPassword = md5Util.encodeToHex(patientDTO.getPatientName(), codes.toString());
	            
	            UserMast userMast = patientDTO.toUserMast();
	            userMast.setCode(codes);
	            userMast.setUser_Name(uNamePrefix);
	            userMast.setPassWd(encodedPassword);
	            userMast.setPreparedBy(uNamePrefix);
	            userMastRepository.save(userMast);

	            codereg = Long.parseLong(dmBaseService.getSerialNoBigInt("1", "registration"));
	            registration = patientDTO.toRegistration();
	            registration.setCode(codereg);
	            registration.setUserId(uNamePrefix);
	            registration.setEmail(patientDTO.getEmail());
	            registration.setMobile(patientDTO.getMobile());
	            registration.setPreparedBy(uNamePrefix);
	            registrationRepository.save(registration);
	        } else {

	           registration = registrationRepository.findByEmailAndMobile(patientDTO.getEmail(), patientDTO.getMobile())
	                    .orElseThrow(() -> new DonationProcessingException("No registration found for this email/mobile"));
	            
	            codereg = registration.getCode();
	            uNamePrefix = registration.getUserId();
	            
	            registration.setPreparedBy(uNamePrefix);
	            registration.setName(patientDTO.getPatientName());
	            registrationRepository.save(registration);
	        }

	        Integer voucherType = fetchVoucherType("PP");
	        String v_Prefix = dmBaseService.fetchVoucherPrefix(patientDTO.getPreparedDt());
	        Long docId = Long.parseLong(dmBaseService.getDocId(voucherType.toString(), v_Prefix, "1"));

	        RecId recId = dmBaseService.getRecId("donation", "docId", docId.toString(), "recId", new RecId(),
	                convertUnixTimestampToDate(patientDTO.getPreparedDt()), "v_Type", voucherType.toString(), v_Prefix,
	                "1", "HO", "1", true, null);

	        Donation donation = patientDTO.toDonation();
	        donation.setPatientName(patientDTO.getPatientName());
	        donation.setSurgeryDate(convertUnixTimestampToDate(patientDTO.getSurgeryDate()));
	        donation.setPatientEmail(patientDTO.getPatientEmail());
	        donation.setDocId(docId);
	        donation.setV_Type(voucherType);
	        donation.setV_No(recId.getCounter().intValue());
	        donation.setRecIdPrefix(recId.getPrefix());
	        donation.setRecId(recId.getRecIdValue());
	        donation.setV_Prefix(Integer.parseInt(v_Prefix));
	        donation.setV_Time(Double.parseDouble(convertUnixTimestampToTime(patientDTO.getPreparedDt())));
	        donation.setSite_Code(1);
	        donation.setPreparedBy(uNamePrefix);
	        donation.setPaymentMode(PaymentMode.ONLINE.getCode());
	        donation.setRegistrationCode(codereg);
	        donation.setV_Date(convertUnixTimestampToDate(patientDTO.getPreparedDt()));
	        donation.setAmount(patientDTO.getAmount());
	        donation.setStatus(PaymentStatus.AppInitiated.getCode());
	        donationRepository.save(donation);

	        String onlineTransId = UUID.randomUUID().toString().replace("-", "");
	        Double amount = Double.parseDouble(patientDTO.getAmount());
	        String razorpayOrderId = razorpayService.createOrder(amount, "INR", onlineTransId);

	        PaymentDetail paymentDetail = new PaymentDetail();
	        paymentDetail.setV_Type(voucherType);
	        paymentDetail.setRecId(recId.getRecIdValue());
	        paymentDetail.setV_Date(convertUnixTimestampToDate(patientDTO.getPreparedDt()));
	        paymentDetail.setV_Time(Double.parseDouble(convertUnixTimestampToTime(patientDTO.getPreparedDt())));
	        paymentDetail.setDocId(docId);
	        paymentDetail.setSite_Code((short) 1);
	        paymentDetail.setPaymentOption(PaymentMode.ONLINE.getCode());
	        paymentDetail.setOnlineTransId(onlineTransId);
	        paymentDetail.setResTransRefId(razorpayOrderId);
	        paymentDetail.setAmount(amount);
	        paymentDetail.setStatus(PaymentStatus.AppInitiated.getCode());
	        paymentDetailRepository.save(paymentDetail);

			Optional<PaymentDetail> pdoptional = paymentDetailRepository.findByResTransRefId(razorpayOrderId);

			pdoptional.ifPresent(pd -> {
				donation.setPaymentId(pd.getId());  // Set payment ID
				donationRepository.save(donation);  // Re-save donation with updated paymentId
			});



			Map<String, String> resTrId = new HashMap<>();
	        resTrId.put("OrderId", razorpayOrderId);
	        return resTrId;
	    } catch (Exception ex) {
	        throw new DonationProcessingException("Error processing patient donation: " + ex.getMessage(), ex);
	    }
	}

	
	
	public DonationDTO getDonation(String data) {
		 // Determine whether data is an email or a mobile number
	    boolean isEmail = data.contains("@");  // Basic email check
	    boolean isMobile = data.matches("\\d{10}"); // Check if it's a 10-digit number

	    if (!isEmail && !isMobile) {
	        return null; // Invalid input, return null
	    }

	    // Fetch registration data based on the correct field
	    Optional<Registration> registrationOpt = isEmail
	        ? registrationRepository.findByEmailOrMobile(data, null)
	        : registrationRepository.findByEmailOrMobile(null, data);

	    // If no data is found, return null
	    if (registrationOpt.isEmpty()) {
	        return null;
	    }

		Registration registration = registrationOpt.get();

		DonationDTO donationDTO = new DonationDTO();
//		donationDTO.setFullName(registration.getName());
//		donationDTO.setAddress1(registration.getAdd1());
//		donationDTO.setAddress2(registration.getAdd2());
//		donationDTO.setSelectedCountry(getCodeAndNameByCode(registration.getCountryCode().toString()));
//		donationDTO.setSelectedState(getStateCodeAndName(registration.getStateCode().toString()));
//		donationDTO.setSelectedDistrict(getCodeAndNameBycityCode(registration.getCityCode().toString()));
		donationDTO.setMobile(registration.getMobile());
		donationDTO.setEmail(registration.getEmail());
//		donationDTO.setPanNumber(registration.getPan());
//		donationDTO.setPincode(registration.getPin());

		return donationDTO;
	}



	public PatientDTO getPatient(String data) {
        // Determine whether data is an email or a mobile number
        boolean isEmail = data.contains("@");
        boolean isMobile = data.matches("\\d{10}");

        if (!isEmail && !isMobile) {
            return null; // Invalid input, return null
        }

     // Fetch registration data based on the correct field
	    Optional<Registration> registrationOpt = isEmail
	        ? registrationRepository.findByEmailOrMobile(data, null)
	        : registrationRepository.findByEmailOrMobile(null, data);

	    // If no data is found, return null
	    if (registrationOpt.isEmpty()) {
	        return null;
	    }
	    
	    Registration registration = registrationOpt.get(); // Optional से actual object लो
	    
	    PatientDTO patientDTO = new PatientDTO();
	    patientDTO.setEmail(registration.getEmail());
	    patientDTO.setMobile(registration.getMobile());
	    return patientDTO;
    }

	
	private Integer fetchVoucherType(String vtCategory) {
	    String sql = "SELECT TOP 1 vt.v_Type FROM voucher_Type vt WHERE vt.isActive = 1 AND vt.category = ?";

	    return Optional.ofNullable(jdbcTemplate.queryForObject(sql, Integer.class, vtCategory))
	            .orElseThrow(() -> new RuntimeException("Voucher Type not found"));
	}


	public String convertUnixTimestampToDate(String unixTimestamp) {
		// Convert the string Unix timestamp to a long
		long timestamp = Long.parseLong(unixTimestamp);

		// Convert Unix timestamp to Instant
		Instant instant = Instant.ofEpochSecond(timestamp);

		// Convert Instant to ZonedDateTime with system default timezone
		ZonedDateTime zonedDateTime = instant.atZone(ZoneId.systemDefault());

		// Format the date in "dd/MM/yyyy" format
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
		return zonedDateTime.format(formatter);
	}

	public String convertUnixTimestampToTime(String unixTimestamp) {
		// Convert the string Unix timestamp to a long
		long timestamp = Long.parseLong(unixTimestamp);

		// Convert Unix timestamp to Instant
		Instant instant = Instant.ofEpochSecond(timestamp);

		// Convert Instant to ZonedDateTime with system default timezone
		ZonedDateTime zonedDateTime = instant.atZone(ZoneId.systemDefault());

		// Get the hours and minutes
		int hours = zonedDateTime.getHour();
		int minutes = zonedDateTime.getMinute();

		// Format time as "HH.mm"
		return String.format("%02d.%02d", hours, minutes);
	}

	public Map<String, String> getCodeAndNameBycityCode(String code) {
	    return cityRepository.findCodeAndNameByCode(code)
	            .map(result -> Map.of(
	                "code", result.getCityCode() != null ? result.getCityCode() :"",
	                "name", result.getCityName() != null ? result.getCityName() : ""
	            ))
	            .orElse(null); // return null if no result found or if data is incomplete
	}

	public Map<String, String> getStateCodeAndName(String code) {
	    return stateRepository.findCodeAndNameByCode(code)
	            .map(result -> Map.of(
	                "code", result.getCode() != null ? result.getCode() : "",
	                "name", result.getName() != null ? result.getName() : ""
	            ))
	            .orElse(null); // return null if no result found or if data is incomplete
	}

	public Map<String, String> getCodeAndNameByCode(String code) {
	    return countryRepository.findByCode(code)
	            .map(country -> {
	                // Return a map with code and name
	                return Map.of(
	                        "code", country.getCode() != null ? country.getCode() : "",
	                        "name", country.getName() != null ? country.getName() : ""
	                );
	            })
	            .orElse(null); // Return null if no result found
	}





	public List<PaymentGatewayDTO> getAllPaymentGateways() {
		String sql = "SELECT pg.code, pg.name, pg.shortName FROM paymentGateway pg WHERE pg.isActive = 1";
		return jdbcTemplate.query(sql, (rs, rowNum) -> new PaymentGatewayDTO(rs.getInt("code"), rs.getString("name"),
				rs.getString("shortName")));
	}
	
	
}
