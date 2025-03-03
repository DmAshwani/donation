package in.dataman.donation.transcontroller;

import java.util.List;
import java.util.Map;


import dataman.dmbase.encryptiondecryptionutil.EncryptionDecryptionUtilNew;
import dataman.dmbase.encryptiondecryptionutil.PayloadEncryptionDecryptionUtil;
import in.dataman.donation.uitl.AuthKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import in.dataman.donation.service.LocationService;
import in.dataman.donation.transentity.City;
import in.dataman.donation.transentity.Country;
import in.dataman.donation.transentity.State;

@RestController
@CrossOrigin(origins = { "http://192.168.7.57:3000" }, originPatterns = "**", allowCredentials = "true",  exposedHeaders = {"authKey", "token"})
@RequestMapping("/api/v1")
public class LocationController {
	@Autowired
	private LocationService locationService;

	@Autowired
	private EncryptionDecryptionUtilNew encryptionDecryptionUtil;

	@Autowired
	private AuthKeyUtil authKeyUtil;

	@GetMapping("/countries")
	public ResponseEntity<?> getCountries(@RequestHeader("authKey") String authKey) {

		//====================================================================================================================
		try{
			authKey = encryptionDecryptionUtil.decrypt(authKey);
		}catch (Exception e){
			e.printStackTrace();
		}
		if (authKeyUtil.getAuthKey(authKey) == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized access");
		}

		authKeyUtil.deleteAuthKey(authKey);
		HttpHeaders headers = new HttpHeaders();
		String id = authKeyUtil.generateAuthKey();
		authKeyUtil.storeAuthKey(id, 60*60*1000);
		String encryptedAuthKey = null;
		try{
			encryptedAuthKey = encryptionDecryptionUtil.encrypt(id);
		}catch (Exception e){
			e.printStackTrace();
		}
		headers.add("authKey", encryptedAuthKey);
		//====================================================================================================================
		List<Country> countries = locationService.getAllCountries();
		//return PayloadEncryptionDecryptionUtil.encryptResponse(countries,encryptionDecryptionUtil);

		Map<String, String> result = PayloadEncryptionDecryptionUtil.encryptResponse(countries,encryptionDecryptionUtil);

		return ResponseEntity.ok().headers(headers).body(result);
	}

	@GetMapping("/states")
	public ResponseEntity<?> getStates(@RequestHeader("authKey") String authKey) {


		//====================================================================================================================
		try{
			authKey = encryptionDecryptionUtil.decrypt(authKey);
		}catch (Exception e){
			e.printStackTrace();
		}
		if (authKeyUtil.getAuthKey(authKey) == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized access");
		}

		authKeyUtil.deleteAuthKey(authKey);
		HttpHeaders headers = new HttpHeaders();
		String id = authKeyUtil.generateAuthKey();
		authKeyUtil.storeAuthKey(id, 60*60*1000);
		String encryptedAuthKey = null;
		try{
			encryptedAuthKey = encryptionDecryptionUtil.encrypt(id);
		}catch (Exception e){
			e.printStackTrace();
		}
		headers.add("authKey", encryptedAuthKey);
		//====================================================================================================================



		List<State> states = locationService.getStatesByCountry();
        //return PayloadEncryptionDecryptionUtil.encryptResponse(states,encryptionDecryptionUtil);

		Map<String, String> result = PayloadEncryptionDecryptionUtil.encryptResponse(states,encryptionDecryptionUtil);

		return ResponseEntity.ok().headers(headers).body(result);

    }

	@GetMapping("/cities")
	public ResponseEntity<?> getCities(@RequestParam String stateCode, @RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "25") int size, @RequestHeader("authKey") String authKey) {

		//====================================================================================================================
		try{
			authKey = encryptionDecryptionUtil.decrypt(authKey);
		}catch (Exception e){
			e.printStackTrace();
		}
		if (authKeyUtil.getAuthKey(authKey) == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized access");
		}

		authKeyUtil.deleteAuthKey(authKey);
		HttpHeaders headers = new HttpHeaders();
		String id = authKeyUtil.generateAuthKey();
		authKeyUtil.storeAuthKey(id, 60*60*1000);
		String encryptedAuthKey = null;
		try{
			encryptedAuthKey = encryptionDecryptionUtil.encrypt(id);
		}catch (Exception e){
			e.printStackTrace();
		}
		headers.add("authKey", encryptedAuthKey);
		//====================================================================================================================
	
		//return PayloadEncryptionDecryptionUtil.encryptResponse(locationService.getCitiesByState(stateCode, page, size), encryptionDecryptionUtil);
		Map<String, String> result = PayloadEncryptionDecryptionUtil.encryptResponse(locationService.getCitiesByState(stateCode, page, size),encryptionDecryptionUtil);

		return ResponseEntity.ok().headers(headers).body(result);
	}
	
	@GetMapping("/cities-name")
	public ResponseEntity<?> getCities(
	        @RequestParam String stateCode, @RequestHeader("authKey") String authKey,
	        @RequestParam(required = false) String namePrefix,
	        @RequestParam(defaultValue = "0") int page,
	        @RequestParam(defaultValue = "25") int size) {


		//====================================================================================================================
		try{
			authKey = encryptionDecryptionUtil.decrypt(authKey);
		}catch (Exception e){
			e.printStackTrace();
		}
		if (authKeyUtil.getAuthKey(authKey) == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized access");
		}

		authKeyUtil.deleteAuthKey(authKey);
		HttpHeaders headers = new HttpHeaders();
		String id = authKeyUtil.generateAuthKey();
		authKeyUtil.storeAuthKey(id, 60*60*1000);
		String encryptedAuthKey = null;
		try{
			encryptedAuthKey = encryptionDecryptionUtil.encrypt(id);
		}catch (Exception e){
			e.printStackTrace();
		}
		headers.add("authKey", encryptedAuthKey);
		//====================================================================================================================


		Page<City> cities = locationService.getCitiesByStateS(stateCode, namePrefix, page, size);



	    //return PayloadEncryptionDecryptionUtil.encryptResponse(cities,encryptionDecryptionUtil);

		Map<String, String> result = PayloadEncryptionDecryptionUtil.encryptResponse(cities,encryptionDecryptionUtil);

		return ResponseEntity.ok().headers(headers).body(result);
	}

}
