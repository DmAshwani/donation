package in.dataman.donation.transentity;

import java.util.Date;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name="paymentDetail")
public class PaymentDetail {
	
	@Id
	 @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	private Integer v_Type = null;
    private String recId = null;
    private String v_Date = null;
    private Double v_Time = null;
    private Long docId = null;
    private Short site_Code = null;
    private Integer paymentOption = null;
    private String onlineTransId = null;
    private Double amount = null;
    private String receiptNo = null;
    private Date receiptDate = null;
    private String bankName = null;
    private String preparedBy = null;
    private Date preparedDt = null;
    private String u_Name = null;
    private Date u_EntDt = null;
    private String instrumentType = null;
    private String instrumentNumber = null;
    private String branchName = null;
    private String payableAt = null;
    private String city = null;
    private String state = null;
    private String country = null;
    private Integer status = null;
    private String aggRefNo = null;
    private String resCountry = null;
    private String resCurrency = null;
    private String resPayMode = null;
    private Double resAmount = null;
    private String resBankCode = null;
    private String resBankTransrefNo = null;
    private String resBankTransdate = null;
    private String resErrReason = null;
    private Long lineId = null;
    private Short paymentGateway = null;
    private String resTransRefId = null;
    private String resStatusDesc = null;
    private String resFeeGst = null;
}
