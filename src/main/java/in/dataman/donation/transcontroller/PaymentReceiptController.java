package in.dataman.donation.transcontroller;


import dataman.dmbase.encryptiondecryptionutil.EncryptionDecryptionUtilNew;
import dataman.dmbase.encryptiondecryptionutil.PayloadEncryptionDecryptionUtil;
import in.dataman.donation.uitl.AuthKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import in.dataman.donation.dto.PatientPaymentReceiptDTO;
import in.dataman.donation.dto.PaymentReceiptDTO;
import in.dataman.donation.service.PaymentReceiptService;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = { "http://192.168.7.57:3000" }, originPatterns = "**", exposedHeaders = {"authKey", "token"})
public class PaymentReceiptController {
    private final PaymentReceiptService paymentReceiptService;

    public PaymentReceiptController(PaymentReceiptService paymentReceiptService) {
        this.paymentReceiptService = paymentReceiptService;
    }

    @Autowired
    private EncryptionDecryptionUtilNew encryptionDecryptionUtil;

    @Autowired
    private AuthKeyUtil authKeyUtil;

    @GetMapping("/payment-receipts")
    public ResponseEntity<?> getPaymentReceipt(
            @RequestParam String docId, @RequestHeader("authKey") String authKey) {


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


        Long docIdL = Long.parseLong(docId);
        PaymentReceiptDTO prdto = paymentReceiptService.getPaymentReceipt(docIdL);
        //return PayloadEncryptionDecryptionUtil.encryptResponse(prdto,encryptionDecryptionUtil);
        Map<String, String> result = PayloadEncryptionDecryptionUtil.encryptResponse(prdto,encryptionDecryptionUtil);

        return ResponseEntity.ok().headers(headers).body(result);
    }
    
    @GetMapping("/patient-receipts")
    public ResponseEntity<?> getReceipts(@RequestParam String docId, @RequestHeader("authKey") String authKey) {

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


    	Long docIdL = Long.parseLong(docId);
        PatientPaymentReceiptDTO patientPaymentReceiptDTO = paymentReceiptService.getReceiptsByDocId(docIdL);
        //return PayloadEncryptionDecryptionUtil.encryptResponse(patientPaymentReceiptDTO,encryptionDecryptionUtil);

        Map<String, String> result = PayloadEncryptionDecryptionUtil.encryptResponse(patientPaymentReceiptDTO,encryptionDecryptionUtil);

        return ResponseEntity.ok().headers(headers).body(result);
    }
    
    
    
    
}

