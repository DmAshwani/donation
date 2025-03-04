package in.dataman.donation.service;

import java.text.SimpleDateFormat;
import java.util.*;

import in.dataman.donation.transrepository.CityRepository;
import in.dataman.donation.transrepository.CountryRepository;
import in.dataman.donation.transrepository.StateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import in.dataman.donation.comentity.UserMast;
import in.dataman.donation.comrepository.UserMastRepository;
import in.dataman.donation.dto.AdminDonationTransactionHistroryDTO;
import in.dataman.donation.dto.AdminPatientTransactionHistroyDTO;
import in.dataman.donation.dto.DonationTransactionHistroyDTO;
import in.dataman.donation.dto.PatientTransactionHistroyDTO;
import in.dataman.donation.enums.PaymentStatus;

@Service
public class TransactionHistorySrv {

    private static final Logger logger = LoggerFactory.getLogger(TransactionHistorySrv.class);

    @Autowired
    @Qualifier("TransactionJdbcTemplate")
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private UserMastRepository userMastRepository;

    @Autowired
    private CityRepository cityRepository;

    @Autowired
    private StateRepository stateRepository;

    @Autowired
    private CountryRepository countryRepository;

    @SuppressWarnings("deprecation")
    public List<DonationTransactionHistroyDTO> getTransactionHistory(String data, String nature) {

        UserMast user = userMastRepository.findByEmailOrMobile(data, data);

        if (user == null) {
            return Collections.emptyList();
        }

        String sql = "SELECT d.recId, d.v_Date, d.name, d.amount, d.status,d.docId FROM donation d LEFT JOIN voucher_Type vt ON vt.v_Type = d.v_Type WHERE d.preparedBy = ? AND vt.category = ?";

        return jdbcTemplate.query(sql, new Object[]{user.getUser_Name(), "DO"}, transactionRowMapper());
    }

    // RowMapper to map query result to DTO
    private RowMapper<DonationTransactionHistroyDTO> transactionRowMapper() {
        return (rs, rowNum) -> {
            DonationTransactionHistroyDTO dto = new DonationTransactionHistroyDTO();
            dto.setTransactionID(rs.getString("recId"));
            dto.setTransactionDate(formatDate(rs.getString("v_Date")));
            dto.setDonorName(rs.getString("name"));
            dto.setAmount(rs.getString("amount"));
            dto.setDocId(rs.getString("docId"));
            // Map integer status to PaymentStatus enum
            int statusValue = rs.getInt("status");
            PaymentStatus paymentStatus = getPaymentStatusByCode(statusValue);
            dto.setStatus(paymentStatus.getDescription());

            return dto;
        };
    }

    // Admin panel
    @SuppressWarnings("deprecation")
    public List<AdminDonationTransactionHistroryDTO> getAdminDonationTransactionHistory(String toDate,
                                                                                        String fromDate) {
        String sql = "SELECT d.recId, d.v_Date, d.name, d.amount, d.status, d.docId, pd.resBankTransrefNo, "
                + "d.name,d.add1,d.add2,d.countryCode,d.stateCode,d.cityCode,d.mobile,d.email,d.pin,d.isTaxExemption,d.pan "
                + "FROM donation d " + "LEFT JOIN voucher_Type vt ON vt.v_Type = d.v_Type "
                + "LEFT JOIN paymentDetail pd ON pd.id = d.paymentId "
                + "WHERE vt.category = ? AND d.v_Date >= ? AND d.v_Date <= ? "
                + "ORDER BY d.v_Date DESC";

        return jdbcTemplate.query(sql, new Object[]{"DO", fromDate, toDate}, adiminTransactionRowMapper());
    }

    // RowMapper to map query result to DTO
    private RowMapper<AdminDonationTransactionHistroryDTO> adiminTransactionRowMapper() {
        return (rs, rowNum) -> {
            AdminDonationTransactionHistroryDTO dto = new AdminDonationTransactionHistroryDTO();
            dto.setTransactionID(rs.getString("recId"));
            dto.setTransactionDate(formatDate(rs.getString("v_Date")));
            dto.setDonorName(rs.getString("name"));
            dto.setAmount(rs.getString("amount"));
            dto.setDocId(rs.getString("docId"));
            dto.setPaymentId(rs.getString("resBankTransrefNo"));
            dto.setEmail(rs.getString("email"));
            dto.setMobile(rs.getString("mobile"));
            dto.setAddress1(rs.getString("add1"));
            dto.setAddress2(rs.getString("add2"));
            dto.setPincode(rs.getString("pin"));
            dto.setSelectedCountry(getCodeAndNameByCode(rs.getString("countryCode")));
            dto.setSelectedState(getStateCodeAndName(rs.getString("stateCode")));
            dto.setSelectedDistrict(getCodeAndNameBycityCode(rs.getString("cityCode")));
            dto.setIsTaxExemption(rs.getString("isTaxExemption"));
            dto.setPanNumber(rs.getString("pan"));
            // Map integer status to PaymentStatus enum
            int statusValue = rs.getInt("status");
            PaymentStatus paymentStatus = getPaymentStatusByCode(statusValue);
            dto.setStatus(paymentStatus.getDescription());

            return dto;
        };
    }

    @SuppressWarnings("deprecation")
    public List<AdminPatientTransactionHistroyDTO> getAdminPatientTransactionHistory(String toDate, String fromDate) {
        String sql = "SELECT d.recId, d.v_Date, d.mobile, d.patientName, d.patientEMRNo, d.surgeryDate, d.amount, d.status, d.docId, pd.resBankTransrefNo, "
                + "d.email,d.patientEmail "
                + "FROM donation d "
                + "LEFT JOIN voucher_Type vt ON vt.v_Type = d.v_Type "
                + "LEFT JOIN paymentDetail pd ON pd.id = d.paymentId "
                + "WHERE vt.category = ? AND d.v_Date >= ? AND d.v_Date <= ?"
                + "ORDER BY d.v_Date DESC";
        return jdbcTemplate.query(sql, new Object[]{"pp", fromDate, toDate}, adiminPatientTransactionRowMapper());
    }

    // RowMapper to map query result to DTO
    private RowMapper<AdminPatientTransactionHistroyDTO> adiminPatientTransactionRowMapper() {
        return (rs, rowNum) -> {
            AdminPatientTransactionHistroyDTO dto = new AdminPatientTransactionHistroyDTO();
            dto.setTransactionID(rs.getString("recId"));
            dto.setTransactionDate(formatDate(rs.getString("v_Date")));
            dto.setCustomerMobile(rs.getString("mobile"));
            dto.setPatientName(rs.getString("patientName"));
            dto.setPatientEMRNo(rs.getString("patientEMRNo"));
            dto.setSurgeryDate(formatDate(rs.getString("surgeryDate")));
            dto.setAmount(rs.getString("amount"));
            dto.setDocId(rs.getString("docId"));
            dto.setPaymentId(rs.getString("resBankTransrefNo"));
            dto.setEmail(rs.getString("email"));
            dto.setPatientEmail(rs.getString("patientEmail"));
            // Map integer status to PaymentStatus enum
            int statusValue = rs.getInt("status");
            PaymentStatus paymentStatus = getPaymentStatusByCode(statusValue);
            dto.setStatus(paymentStatus.getDescription());

            return dto;
        };
    }

    @SuppressWarnings("deprecation")
    public List<PatientTransactionHistroyDTO> getPatientTransactionHistory(String data, String nature) {

        UserMast user = userMastRepository.findByEmailOrMobile(data, data);

        if (user == null) {
            logger.warn("No user found with email or mobile: {}", data);
            return Collections.emptyList();
        }

        String sql = "SELECT d.recId,d.v_Date,d.mobile,d.patientName,d.patientEMRNo,d.surgeryDate,d.amount,d.status,d.docId FROM donation d LEFT JOIN voucher_Type vt ON vt.v_Type = d.v_Type WHERE d.preparedBy = ? AND vt.category = ?";

        List<PatientTransactionHistroyDTO> transactions = jdbcTemplate.query(sql,
                new Object[]{user.getUser_Name(), "PP"}, patientTransactionRowMapper());
        Collections.reverse(transactions);
        return transactions;
    }

    // RowMapper to map query result to DTO
    private RowMapper<PatientTransactionHistroyDTO> patientTransactionRowMapper() {
        return (rs, rowNum) -> {
            PatientTransactionHistroyDTO dto = new PatientTransactionHistroyDTO();
            dto.setTransactionID(rs.getString("recId"));
            dto.setTransactionDate(formatDate(rs.getString("v_Date")));
            dto.setCustomerMobile(rs.getString("mobile"));
            dto.setPatientName(rs.getString("patientName"));
            dto.setPatientEMRNo(rs.getString("patientEMRNo"));
            dto.setSurgeryDate(formatDate(rs.getString("surgeryDate")));
            dto.setAmount(rs.getString("amount"));
            dto.setDocId(rs.getString("docId"));
            // Map integer status to PaymentStatus enum
            int statusValue = rs.getInt("status");
            PaymentStatus paymentStatus = getPaymentStatusByCode(statusValue);
            dto.setStatus(paymentStatus.getDescription());

            return dto;
        };
    }

    @SuppressWarnings("unused")
    private Integer fetchVoucherType(String vtCategory) {
        String sql = "SELECT TOP 1 vt.v_Type FROM voucher_Type vt WHERE vt.isActive = 1 AND vt.category = ?";

        return Optional.ofNullable(jdbcTemplate.queryForObject(sql, Integer.class, vtCategory))
                .orElseThrow(() -> new RuntimeException("Voucher Type not found"));
    }

    // Method to get PaymentStatus by code
    private PaymentStatus getPaymentStatusByCode(int code) {
        for (PaymentStatus status : PaymentStatus.values()) {
            if (status.getCode() == code) {
                return status;
            }
        }
        return PaymentStatus.NoRecordsFound; // Default value if not found
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


    public Map<String, String> getCodeAndNameBycityCode(String code) {
        return cityRepository.findCodeAndNameByCode(code)
                .map(result -> Map.of(
                        "code", result.getCityCode() != null ? result.getCityCode() : "",
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


}
