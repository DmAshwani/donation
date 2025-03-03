package in.dataman.donation.transcontroller;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import dataman.dmbase.encryptiondecryptionutil.EncryptionDecryptionUtilNew;
import dataman.dmbase.encryptiondecryptionutil.PayloadEncryptionDecryptionUtil;
import in.dataman.donation.uitl.AuthKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.CrossOrigin;

import com.razorpay.RazorpayException;

import in.dataman.donation.dto.DonationDTO;
import in.dataman.donation.dto.PatientDTO;
import in.dataman.donation.dto.PaymentGatewayDTO;
import in.dataman.donation.service.DonationService;
import in.dataman.donation.service.RazorpayService;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Donation Controller", description = "Handles donation operations")
@CrossOrigin(origins = {"http://192.168.7.57:3000"}, originPatterns = "**", allowCredentials = "true", exposedHeaders = {"authKey", "token"})
public class DonationController {

    @Autowired
    private DonationService donationService;

    @Autowired
    private RazorpayService razorpayService;

    @Autowired
    EncryptionDecryptionUtilNew encryptionDecryptionUtil;

    @Autowired
    private AuthKeyUtil authKeyUtil;

    @PostMapping("/submit")
    @Operation(summary = "Submit a donation", description = "Processes a donation request")
    public ResponseEntity<?> submitDonation(@RequestBody JsonNode payload, @RequestHeader("authKey") String authKey) throws NumberFormatException, Exception {


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

        DonationDTO donationDTO = PayloadEncryptionDecryptionUtil.decryptAndConvertToDTO(payload, encryptionDecryptionUtil, DonationDTO.class);

        Map<String, String> orderId = donationService.donation(donationDTO); //Unhandled exception type Exception

        //return PayloadEncryptionDecryptionUtil.encryptResponse(orderId, encryptionDecryptionUtil);

        Map<String, String> result = PayloadEncryptionDecryptionUtil.encryptResponse(orderId,encryptionDecryptionUtil);

        return ResponseEntity.ok().headers(headers).body(result);

    }

    @PostMapping("/patient")
    @Operation(summary = "Patient Submit", description = "Patient submit in donation table")
    public ResponseEntity<?> submitPatient(@RequestBody JsonNode payload, @RequestHeader("authKey") String authKey) throws NumberFormatException, Exception {

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

        PatientDTO patientDTO = PayloadEncryptionDecryptionUtil.decryptAndConvertToDTO(payload, encryptionDecryptionUtil, PatientDTO.class);
        Map<String, String> orderId = donationService.patient(patientDTO);

        //return PayloadEncryptionDecryptionUtil.encryptResponse(orderId, encryptionDecryptionUtil);

        Map<String, String> result = PayloadEncryptionDecryptionUtil.encryptResponse(orderId,encryptionDecryptionUtil);

        return ResponseEntity.ok().headers(headers).body(result);


    }


    @PostMapping("/order")
    public ResponseEntity<?> createOrder(@RequestParam Double amount, @RequestParam String currency, @RequestParam String receipt, @RequestHeader("authKey") String authKey) {


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
        String orderId = null;
        try {
            orderId = razorpayService.createOrder(amount, currency, receipt);
            return ResponseEntity.ok(orderId);
        } catch (RazorpayException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        //return ResponseEntity.ok(orderId);

        Map<String, String> result = PayloadEncryptionDecryptionUtil.encryptResponse(orderId,encryptionDecryptionUtil);

        return ResponseEntity.ok().headers(headers).body(result);
    }



    @PostMapping("/verify")
    public ResponseEntity<?> verifyPayment(@RequestParam String paymentId, @RequestParam String orderId, @RequestParam String signature, @RequestHeader("authKey") String authKey) {


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


        String response = razorpayService.verifyPayment(paymentId, orderId, signature);

        if (response.contains("captured")) {
            //return PayloadEncryptionDecryptionUtil.encryptResponse(response, encryptionDecryptionUtil);
            Map<String, String> result = PayloadEncryptionDecryptionUtil.encryptResponse(response,encryptionDecryptionUtil);

            return ResponseEntity.ok().headers(headers).body(result);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @GetMapping("/payment-gateway")
    @Operation(summary = "payment gateway", description = "Get payment gateways details")
    public ResponseEntity<?> getAllPaymentGateways(@RequestHeader("authKey") String authKey) {

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

        List<PaymentGatewayDTO> gateways = donationService.getAllPaymentGateways();
        //return PayloadEncryptionDecryptionUtil.encryptResponse(gateways, encryptionDecryptionUtil);

        Map<String, String> result = PayloadEncryptionDecryptionUtil.encryptResponse(gateways,encryptionDecryptionUtil);

        return ResponseEntity.ok().headers(headers).body(result);
    }

    @GetMapping("/getDonation")
    public ResponseEntity<?> testAPI(@RequestParam String data, @RequestHeader("authKey") String authKey) {

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

        DonationDTO donationDTO = donationService.getDonation(data);
        //return PayloadEncryptionDecryptionUtil.encryptResponse(donationDTO, encryptionDecryptionUtil);

        Map<String, String> result = PayloadEncryptionDecryptionUtil.encryptResponse(donationDTO,encryptionDecryptionUtil);

        return ResponseEntity.ok().headers(headers).body(result);

    }

    @GetMapping("/getPatient")
    public ResponseEntity<?> getPatient(@RequestParam String data, @RequestHeader("authKey") String authKey) {

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

        PatientDTO patientDTO = donationService.getPatient(data);
        //return PayloadEncryptionDecryptionUtil.encryptResponse(patientDTO, encryptionDecryptionUtil);
        Map<String, String> result = PayloadEncryptionDecryptionUtil.encryptResponse(patientDTO,encryptionDecryptionUtil);

        return ResponseEntity.ok().headers(headers).body(result);
    }
}

