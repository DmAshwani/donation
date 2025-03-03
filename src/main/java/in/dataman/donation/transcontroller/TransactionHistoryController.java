package in.dataman.donation.transcontroller;

import dataman.dmbase.encryptiondecryptionutil.EncryptionDecryptionUtil;
import dataman.dmbase.encryptiondecryptionutil.EncryptionDecryptionUtilNew;
import dataman.dmbase.encryptiondecryptionutil.PayloadEncryptionDecryptionUtil;
import in.dataman.donation.uitl.AuthKeyUtil;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import in.dataman.donation.dto.AdminDonationTransactionHistroryDTO;
import in.dataman.donation.dto.AdminPatientTransactionHistroyDTO;
import in.dataman.donation.dto.DonationTransactionHistroyDTO;
import in.dataman.donation.dto.PatientTransactionHistroyDTO;
import in.dataman.donation.service.TransactionHistorySrv;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = { "http://192.168.7.57:3000" }, originPatterns = "**", allowCredentials = "true", exposedHeaders = {"authKey", "token"})
public class TransactionHistoryController {

	@Autowired
	private TransactionHistorySrv transactionHistorySrv;

	@Autowired
	private EncryptionDecryptionUtilNew encryptionDecryptionUtil;

	@Autowired
	private AuthKeyUtil authKeyUtil;

	@GetMapping("/transactions")
	public ResponseEntity<?> getTransactionHistory(@RequestParam String data,
			@RequestParam String nature, @RequestHeader("authKey") String authKey) {


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

		List<DonationTransactionHistroyDTO> history = transactionHistorySrv.getTransactionHistory(data, nature);

		if (history.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NO_CONTENT).body(history);
		}

		Map<String, String> result = PayloadEncryptionDecryptionUtil.encryptResponse(history,encryptionDecryptionUtil);

		return ResponseEntity.ok().headers(headers).body(result);
	}

	@GetMapping("/PatientTransactions")
	public ResponseEntity<?> getPatientTransaction(@RequestParam String data,
			@RequestParam String nature, @RequestHeader("authKey") String authKey) {

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


		List<PatientTransactionHistroyDTO> history = transactionHistorySrv.getPatientTransactionHistory(data, nature);
		if (history.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NO_CONTENT).body(history);
		}


		Map<String, String> result = PayloadEncryptionDecryptionUtil.encryptResponse(history,encryptionDecryptionUtil);

		return ResponseEntity.ok().headers(headers).body(result);
	}

	@GetMapping("/admin-donation-transactions")
	public ResponseEntity<?> getAdminDonationTransactionHistory(
			@RequestParam String fromDate, @RequestParam String toDate,@RequestHeader("authKey") String authKey) {



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

		List<AdminDonationTransactionHistroryDTO> transactions = transactionHistorySrv
				.getAdminDonationTransactionHistory(toDate, fromDate);

		if (transactions.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NO_CONTENT).body(transactions);
		}

		//return PayloadEncryptionDecryptionUtil.encryptResponse(transactions,encryptionDecryptionUtil);

		Map<String, String> result = PayloadEncryptionDecryptionUtil.encryptResponse(transactions,encryptionDecryptionUtil);

		return ResponseEntity.ok().headers(headers).body(result);
	}

	@GetMapping("/admin-patient-transactions")
	public ResponseEntity<?> getPatientTransactionHistory(
			@RequestParam String fromDate, @RequestParam String toDate, @RequestHeader("authKey") String authKey) {


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


		List<AdminPatientTransactionHistroyDTO> transactions = transactionHistorySrv
				.getAdminPatientTransactionHistory(toDate, fromDate);
		
		if (transactions.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NO_CONTENT).body(transactions);
		}

		//return PayloadEncryptionDecryptionUtil.encryptResponse(transactions,encryptionDecryptionUtil);

		Map<String, String> result = PayloadEncryptionDecryptionUtil.encryptResponse(transactions,encryptionDecryptionUtil);

		return ResponseEntity.ok().headers(headers).body(result);


	}

}
