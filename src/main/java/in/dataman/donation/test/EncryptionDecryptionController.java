package in.dataman.donation.test;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dataman.dmbase.debug.Debug;
import dataman.dmbase.encryptiondecryptionutil.EncryptionDecryptionUtilNew;
import dataman.dmbase.encryptiondecryptionutil.PayloadEncryptionDecryptionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


import java.util.HashMap;
import java.util.Map;


@RestController
public class EncryptionDecryptionController {



    private final ObjectMapper objectMapper = new ObjectMapper();
    //private final EncryptionDecryptionUtil encryptionDecryptionUtil = new EncryptionDecryptionUtil();

    @Autowired
    private EncryptionDecryptionUtilNew encryptionDecryptionUtil;

    @PostMapping("/encryption-request")
    public ResponseEntity<?> processUser(@RequestBody JsonNode request) {


        try {

            String result =  objectMapper.writeValueAsString(request);
            System.out.println(result);
            HashMap<String, String> encryptedResponse = new HashMap<>();

            encryptedResponse.put("data", encryptionDecryptionUtil.encrypt(result));
            return ResponseEntity.ok(encryptedResponse);

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/decrypt-request")
    public ResponseEntity<?> decryptPayload(@RequestBody JsonNode request) throws Exception {

        String encryptedPayload = request.get("data").asText();
        String decryptedPayload = encryptionDecryptionUtil.decrypt(encryptedPayload);
        // Convert the decrypted string back to a JsonNode
        try {
            JsonNode response = objectMapper.readTree(decryptedPayload);
            System.out.println(response.toPrettyString());
            return ResponseEntity.ok(response);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }

    @PostMapping("/decrypt-and-convert-to-dto")
    public ResponseEntity<?> testDirect(@RequestBody JsonNode payload){
        try {
            Debug.printDebugBoundary("✔");
            System.out.println(payload.toPrettyString());
            Debug.printDebugBoundary("✔");
            // Use the generic method to convert to ApplicantBankDetailsDTO
            RequestDTO requestDTO = PayloadEncryptionDecryptionUtil.decryptAndConvertToDTO(payload, encryptionDecryptionUtil, RequestDTO.class);
            return ResponseEntity.status(HttpStatus.CREATED).body(requestDTO);
        } catch (JsonProcessingException error) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error while mapping JSON to DTO: " + error.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping("/encrypt-object-and-send-map")
    public ResponseEntity<?> getEncryptedResponse() {

        RequestDTO response = new RequestDTO();

        response.setAge(20);
        response.setName("Govind");
        response.setUserId("userid");
        response.setGender("male");

        // Use the utility method to encrypt response
        return PayloadEncryptionDecryptionUtil.encryptResponse(response, encryptionDecryptionUtil);
    }


}

