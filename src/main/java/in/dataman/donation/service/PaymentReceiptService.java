package in.dataman.donation.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import in.dataman.donation.dto.PatientPaymentReceiptDTO;
import in.dataman.donation.dto.PaymentReceiptDTO;
import in.dataman.donation.uitl.NumberToWordsConverter;

@Service
public class PaymentReceiptService {

	@Autowired
	@Qualifier("TransactionJdbcTemplate") // If using multiple data sources
	private JdbcTemplate jdbcTemplate;

	@SuppressWarnings("deprecation")
	public PaymentReceiptDTO getPaymentReceipt(long docId) {
		String sql = """
				SELECT d.recId,
				       d.name,
				       ISNULL(d.add1, '') + ' ' + ISNULL(d.add2, '') + ' ' + ISNULL(c.cityName, '') + ' ' + ISNULL(s.name, '') AS address,
				       ISNULL(d.pan, '') AS pan,
				       d.mobile,
				       d.email,
				       d.v_Date AS dateOfDonation,
				       d.amount,
				       ISNULL(pd.resPayMode, ' ') AS resPayMode
				FROM donation d
				LEFT JOIN city c ON c.cityCode = d.cityCode
				LEFT JOIN stateMast s ON s.code = d.stateCode
				LEFT JOIN paymentDetail pd ON d.docId = pd.docId
				WHERE d.docId = ?""";

		return jdbcTemplate.queryForObject(sql, new Object[] { docId }, new PaymentReceiptRowMapper());
	}

	private static class PaymentReceiptRowMapper implements RowMapper<PaymentReceiptDTO> {
		@Override
		public PaymentReceiptDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
			PaymentReceiptDTO receipt = new PaymentReceiptDTO();
			receipt.setRecId(rs.getString("recId"));
			receipt.setName(rs.getString("name"));
			receipt.setAddress(rs.getString("address"));
			receipt.setPan(rs.getString("pan"));
			receipt.setMobile(rs.getString("mobile"));
			receipt.setEmail(rs.getString("email"));
			receipt.setDateOfDonation(rs.getDate("dateOfDonation"));
			receipt.setAmount(rs.getDouble("amount"));
			receipt.setPaymentMode(rs.getString("resPayMode"));
			// Convert amount to words
			receipt.setAmountInWords(NumberToWordsConverter.convert((int) rs.getDouble("amount")));

			return receipt;
		}
	}

	@SuppressWarnings("deprecation")
	public PatientPaymentReceiptDTO getReceiptsByDocId(Long docId) {
		String sql = """
					SELECT d.recId, d.v_Date,d.mobile, d.patientName,d.patientEMRNo, d.surgeryDate,d.amount, ISNULL(pd.resPayMode, ' ') AS resPayMode FROM donation d LEFT JOIN paymentDetail pd ON d.docId = pd.docId WHERE d.docId = ?
				""";

		return jdbcTemplate.queryForObject(sql, new Object[] { docId }, new PatientPaymentReceiptRowMapper());
	}

	private static class PatientPaymentReceiptRowMapper implements RowMapper<PatientPaymentReceiptDTO> {

		@Override
		public PatientPaymentReceiptDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
			// TODO Auto-generated method stub
			PatientPaymentReceiptDTO dto = new PatientPaymentReceiptDTO();
			dto.setTransactionID(rs.getString("recId"));
			dto.setTransactionDate(formatDate(rs.getString("v_Date")));
			dto.setCustamerMobile(rs.getString("mobile"));
			dto.setPatientName(rs.getString("patientName"));
			dto.setPatientEMRNo(rs.getString("patientEMRNo"));
			dto.setSurgeryDate(formatDate(rs.getString("surgeryDate"))); // 2025-01-30 but we want to dd-MMM-yyyy
			dto.setAmount(rs.getString("amount"));
			dto.setPaymentMode(rs.getString("resPayMode"));
			dto.setAmountInWords(NumberToWordsConverter.convert((int) rs.getDouble("amount")) + " Rupees only");
			
			return dto;
		}

	}

	// Helper method to format the date
	private static String formatDate(String dateStr) {
		try {
			SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
			SimpleDateFormat outputFormat = new SimpleDateFormat("dd-MMM-yyyy");
			Date date = inputFormat.parse(dateStr);
			return outputFormat.format(date);
		} catch (Exception e) {
			return dateStr; // Return as is if parsing fails
		}
	}

}
