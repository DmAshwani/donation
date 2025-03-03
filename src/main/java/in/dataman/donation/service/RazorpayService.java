package in.dataman.donation.service;

import java.util.Optional;

import org.json.JSONObject;
import org.springframework.stereotype.Service;

import com.razorpay.Order;
import com.razorpay.Payment;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;

import in.dataman.donation.enums.PaymentStatus;
import in.dataman.donation.transentity.Donation;
import in.dataman.donation.transentity.PaymentDetail;
import in.dataman.donation.transrepository.DonationRepository;
import in.dataman.donation.transrepository.PaymentDetailRepository;

@Service
public class RazorpayService {

    private final RazorpayClient razorpayClient;
    private final PaymentDetailRepository paymentDetailRepository;
    private final DonationRepository donationRepository;

    private static final String RAZORPAY_KEY_SECRET = "LBD4KkcvlMVh4lgFnvmrGq2b";

    public RazorpayService(RazorpayClient razorpayClient, 
                           PaymentDetailRepository paymentDetailRepository, 
                           DonationRepository donationRepository) {
        this.razorpayClient = razorpayClient;
        this.paymentDetailRepository = paymentDetailRepository;
        this.donationRepository = donationRepository;
    }

    // Create Razorpay Order
    public String createOrder(Double amount, String currency, String receipt) throws RazorpayException {
        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", amount * 100); // Amount in paise
        orderRequest.put("currency", currency);
        orderRequest.put("receipt", receipt);
        orderRequest.put("payment_capture", 1);

        Order order = razorpayClient.orders.create(orderRequest);
        System.out.println(order.toString());
        return order.get("id").toString();
    }


    public String verifyPayment(String paymentId, String orderId, String signature) { 
        try {
            // ✅ Validate payment signature
            JSONObject options = new JSONObject();
            options.put("razorpay_payment_id", paymentId);
            options.put("razorpay_order_id", orderId);
            options.put("razorpay_signature", signature);

            boolean isValid = Utils.verifyPaymentSignature(options, RAZORPAY_KEY_SECRET);
            if (!isValid) {
                return "Invalid Signature Verification";
            }

//            System.out.println(paymentId);
            // ✅ Fetch actual payment status from Razorpay
            Payment payment = razorpayClient.payments.fetch(paymentId);

            String status = payment.get("status");
            String payment_Method = payment.get("method");
            
            // ✅ Fetch the PaymentDetail using paymentId
            Optional<PaymentDetail> paymentDetailOpt = paymentDetailRepository.findByResTransRefId(orderId);
       
            System.out.println(paymentDetailOpt.toString());
            
            if (paymentDetailOpt.isEmpty()) {
                return "No payment details found for ID: " + paymentId;
            }

            PaymentDetail paymentDetail = paymentDetailOpt.get();
            Optional<Donation> donationOpt = donationRepository.findById(paymentDetail.getDocId());
            if (donationOpt.isEmpty()) {
                return "No donation record found for docId: " + paymentDetail.getDocId();
            }

            Donation donation = donationOpt.get();

            // ✅ Update statuses based on Razorpay response
            if ("captured".equals(status)) {
                paymentDetail.setStatus(PaymentStatus.Success.getCode());
                paymentDetail.setResPayMode(payment_Method);
                paymentDetail.setResBankTransrefNo(paymentId);
                donation.setStatus(PaymentStatus.Success.getCode());
            } else if ("failed".equals(status)) {
                paymentDetail.setStatus(PaymentStatus.Fail.getCode());
                paymentDetail.setResPayMode(payment_Method);
                paymentDetail.setResBankTransrefNo(paymentId);
                donation.setStatus(PaymentStatus.Fail.getCode());
            } else {
                paymentDetail.setStatus(PaymentStatus.Pending.getCode());
                paymentDetail.setResPayMode(payment_Method);
                paymentDetail.setResBankTransrefNo(paymentId);
                donation.setStatus(PaymentStatus.Pending.getCode());
            }

            // ✅ Save updated records
            paymentDetailRepository.save(paymentDetail);
            donationRepository.save(donation);

            return "Payment status updated to: " + status;
        } catch (Exception e) {
            return "Payment Verification Failed: " + e.getMessage();
        }
    }
    
//    @Transactional
//    public String verifyPayment(String paymentId) throws RazorpayException {
//        RazorpayClient razorpay = new RazorpayClient(RAZORPAY_KEY_ID, RAZORPAY_KEY_SECRET);
//        Payment payment = razorpay.payments.fetch(paymentId); 
//        String status = payment.get("status");
//        String paymentMethod = payment.get("method");
//        
//       System.out.println("Payment obj: "+payment);
//       System.out.println(paymentMethod);
//        return "Payment status updated to: " + status;
//    }
   
    
}
