package in.dataman.donation.uitl;

import java.sql.Types;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Service;


@Service
public class DmBaseServiceImpl implements DmBaseService {

	@Autowired
    @Qualifier("TransactionJdbcTemplate")
	private JdbcTemplate jdbcTemplate;
	
	@Autowired
    @Qualifier("TransactionJdbcTemplate")
	private SimpleJdbcCall simpleJdbcCall;

	


	@Override
	public String getSerialNoInteger(String tableName, String siteCode, Integer prefix) {
		String sqlQuery = "exec getCode ?, ?, ?";
		String serialNumber;

		try {
			@SuppressWarnings("deprecation")
			Integer serialNo = jdbcTemplate.queryForObject(sqlQuery, new Object[] { tableName, siteCode, prefix },
					Integer.class);
			serialNumber = "1" + padLeftZeros(siteCode, 4) + padLeftZeros(String.valueOf(serialNo), 4);
		} catch (Exception e) {
			e.printStackTrace(); // Log the full stack trace
			throw new RuntimeException("Error fetching serial number: " + e.getMessage());
		}
		return serialNumber;
	}

	
	
	
	@Override
	public String getSerialNoBigInt(String siteCode, String tableName) {
		Map<String, Object> params = new HashMap<>();
		params.put("vTableName", tableName);
		params.put("vSiteCode", siteCode);
		params.put("vPrefix", 0); // Assuming the prefix value is 0 for this method

		Map<String, Object> result = simpleJdbcCall.withProcedureName("getCode").execute(params);
		 // Retrieve the first result from the result set and get v_No
	    @SuppressWarnings("unchecked")
		Long serialNo = (Long) ((List<Map<String, Object>>) result.get("#result-set-1")).get(0).get("v_No");
	    
	    // Check if serialNo is null and throw exception if so
		if (serialNo == null) {
			throw new RuntimeException("No serial number returned from stored procedure.");
		}

		return formatSerialNumber(siteCode, serialNo, 14);
	}
	
	private String formatSerialNumber(String siteCode, Number serialNo, int serialNoLength) {
		String siteCodePadded = padLeftZeros(siteCode, 4);
		String serialNoPadded = padLeftZeros(serialNo.toString(), serialNoLength);
		return "1" + siteCodePadded + serialNoPadded;
	}


	@Override
	public String getSerialNoBigInt(String siteCode, String vPrefix, String tableName) {
		Map<String, Object> params = new HashMap<>();
		params.put("tableName", tableName);
		params.put("siteCode", siteCode);
		params.put("vPrefix", vPrefix);

		Map<String, Object> result = simpleJdbcCall.execute(params);
		Long serialNo = (Long) result.get("serialNo");
		if (serialNo == null) {
			throw new RuntimeException("No serial number returned from stored procedure.");
		}

		return formatSerialNumber(siteCode, vPrefix, serialNo, 14);
	}

	
	private String formatSerialNumber(String siteCode, String vPrefix, Number serialNo, int serialNoLength) {
		String siteCodePadded = padLeftZeros(siteCode, 4);
		String vPrefixPadded = padLeftZeros(vPrefix, 2);
		String serialNoPadded = padLeftZeros(serialNo.toString(), serialNoLength);
		return "1" + siteCodePadded + vPrefixPadded + serialNoPadded;
	}

	@Override
	public String getDocId(String voucherType, String voucherPrefix, String siteCode) throws Exception {
		// TODO Auto-generated method stub
		SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate).withProcedureName("getDocId").declareParameters(
				new SqlParameter("vVoucherType", Types.VARCHAR), // Update to match stored procedure parameter name
				new SqlParameter("vVoucherPrefix", Types.VARCHAR), // Update to match stored procedure parameter name
				new SqlParameter("vSiteCode", Types.VARCHAR), // Update to match stored procedure parameter name
				new SqlOutParameter("voucherNo", Types.INTEGER));

		Map<String, Object> params = Map.of("vVoucherType", voucherType, // Update to match stored procedure parameter
																			// name
				"vVoucherPrefix", voucherPrefix, // Update to match stored procedure parameter name
				"vSiteCode", siteCode // Update to match stored procedure parameter name
		);

		Map<String, Object> result = jdbcCall.execute(params);
		System.out.println("Stored Procedure Result: " + result);

		// Extract voucherNo from the result set
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> resultSet = (List<Map<String, Object>>) result.get("#result-set-1");
		Integer voucherNo = null;
		if (resultSet != null && !resultSet.isEmpty()) {
			Object voucherNoObj = resultSet.get(0).get("v_No");
			if (voucherNoObj instanceof Number) {
				voucherNo = ((Number) voucherNoObj).intValue(); // Cast to Integer
			}
		}

		// Check if voucherNo is valid
		if (voucherNo == null || voucherNo <= 0) {
			throw new UnsupportedOperationException("Unable to generate docId due to invalid voucherNo.");
		}

		return buildDocId(voucherType, voucherPrefix, siteCode, voucherNo);
	}

	private String buildDocId(String voucherType, String voucherPrefix, String siteCode, Integer voucherNo) {
		return "1" + padLeftZeros(siteCode, 4) + padLeftZeros(voucherType, 3) + padLeftZeros(voucherPrefix, 2)
				+ padLeftZeros(voucherNo.toString(), 8);
	}

	// Helper method to handle padding
	private String padLeftZeros(String inputString, int length) {
		if (inputString == null) {
			return "0".repeat(length);
		}
		return String.format("%1$" + length + "s", inputString).replace(' ', '0');
	}

	@Override
	public RecId getRecId(String tableName, String keyFieldName, String keyValue, String recIdFieldName, RecId recId,
			String dateValue, String voucherTypeFieldName, String voucherType, String voucherPrefix, String siteCode,
			String siteShortName, String companyCode, Boolean isCheckDate, HashMap<String, String> paramFormat)
			throws Exception {

		if (recId == null) {
			throw new IllegalArgumentException("Please mention recId.");
		}

		// Initialize recId value
		recId.setRecIdValue(recId.getRecIdValue() != null ? recId.getRecIdValue() : "");

		// Validate inputs
		if (voucherType == null || voucherType.trim().isEmpty()) {
			throw new IllegalArgumentException("Please mention voucher type.");
		}
		if (dateValue == null || dateValue.trim().isEmpty()) {
			throw new IllegalArgumentException("Please mention date.");
		}

		String v_SN = fetchVoucherType(voucherType);

		// Initialize variables for the number system
		String nsIncludeV_Type = "", nsFormat = "", nsCounterFormat = "0", companySName = "", voucherSName = v_SN;
		boolean isYearWise = true, isSiteWise = true;

		// SQL query to get number system format
		String sqlQuery = "SELECT TOP 1 nsf.nsFormat, nsf.counterFormat, nsf.isYearWise, nsf.isSiteWise, ns.includeV_Type "
				+ "FROM numberSystem ns JOIN numberSystemFormat nsf ON ns.nsGroup = nsf.nsGroup "
				+ "WHERE ns.v_Type = ? AND ISNULL(nsf.site_Code, ?) = ? " + "ORDER BY nsf.site_Code DESC;";

		try {
			// Get number system format
			Map<String, Object> nsResult = jdbcTemplate.queryForMap(sqlQuery, voucherType, siteCode, siteCode);

			// Extract results
			nsIncludeV_Type = (String) nsResult.get("includeV_Type");
			nsFormat = (String) nsResult.get("nsFormat");
			nsCounterFormat = (String) nsResult.get("counterFormat");

			// Retrieve as Short and convert to boolean
			Short isYearWiseShort = (Short) nsResult.get("isYearWise");
			isYearWise = (isYearWiseShort != null && isYearWiseShort != 0);

			Short isSiteWiseShort = (Short) nsResult.get("isSiteWise");
			isSiteWise = (isSiteWiseShort != null && isSiteWiseShort != 0);
		} catch (EmptyResultDataAccessException e) {
			throw new IllegalArgumentException(
					"No number system format found for the given voucher type and site code.");
		}

		if (nsFormat.trim().isEmpty()) {
			throw new IllegalArgumentException("Please define number format.");
		}
		if (nsCounterFormat.trim().isEmpty()) {
			throw new IllegalArgumentException("Please define counter format.");
		}

		if (dateValue == null || dateValue.trim().isEmpty()) {
			throw new IllegalArgumentException("Please mention date.");
		}

		// Parse dateValue to java.sql.Date
		java.sql.Date sqlDate;
		try {
			SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy"); // Only MM/dd/yyyy format
			java.util.Date parsedDate = formatter.parse(dateValue);
			sqlDate = new java.sql.Date(parsedDate.getTime());
			System.out.println("SQL Date: " + sqlDate.toString());
		} catch (ParseException e) {
			throw new IllegalArgumentException("Invalid date format. Expected format: MM/dd/yyyy", e);
		}

		// Set the date components
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(sqlDate);
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		int month = calendar.get(Calendar.MONTH) + 1; // Calendar months are 0-based, so we add 1
		int yearFull = calendar.get(Calendar.YEAR);
		int year = yearFull % 100; // Last two digits of the year

		if (recId.getRecIdValue().trim().isEmpty()) {
			nsFormat = nsFormat.replace("{sitesname}", siteShortName)
					.replace("{site}", String.format("%03d", Integer.parseInt(siteCode)))
					.replace("{voucherprefix}", String.format("%02d", Integer.parseInt(voucherPrefix)))
					.replace("{company}", companySName).replace("{year}", String.format("%02d", year))
					.replace("{yearfull}", String.format("%04d", yearFull))
					.replace("{month}", String.format("%02d", month)).replace("{day}", String.format("%02d", day))
					.replace("{voucher}", String.format("%03d", Integer.parseInt(voucherType)))
					.replace("{vouchersname}", voucherSName);

			if (paramFormat != null) {
				for (String keySet : paramFormat.keySet()) {
					nsFormat = nsFormat.replace("{" + keySet + "}", paramFormat.get(keySet).trim());
				}
			}

			recId.setPrefix(nsFormat.replace("{counter}", ""));

			if (recId.getCounter() == null) {
				// Initialize the counter to 0 if it's null
				recId.setCounter(0L); // Ensure that it's Long
			}

			// Increment or retrieve counter
			if (recId.getCounter() == 0) {
				String counterSql = "EXEC getRecId ?, ?, ?, ?";
				// Assuming the counter is returned as Long
				Long counterValue = jdbcTemplate.queryForObject(counterSql, Long.class, voucherType,
						isYearWise ? voucherPrefix : null, isSiteWise ? siteCode : null, recId.getPrefix());

				// Check if counterValue is null and handle accordingly
				if (counterValue != null) {
					recId.setCounter(counterValue);
				} else {
					// Handle the case where counterValue is null (e.g., log or throw an exception)
					throw new IllegalStateException("Counter value returned by stored procedure is null.");
				}
			} else {
				// Check for existing counters
				String checkCounterSql = "SELECT [counter] FROM numberSystemCounter WHERE nsGroup = ? AND "
						+ "ISNULL(v_Prefix, 0) = ISNULL(?, 0) AND ISNULL(site_Code, 0) = ISNULL(?, 0) AND "
						+ "ISNULL(nsFormatValue, '') = ISNULL(?, '') AND ISNULL([counter], 0) > ?";
				Short existingCount = jdbcTemplate.queryForObject(checkCounterSql, Short.class, voucherType,
						voucherPrefix, siteCode, recId.getPrefix(), recId.getCounter());

				if (existingCount != null && existingCount > 0) {
					throw new IllegalArgumentException("Given counter cannot be greater than existing counter.");
				}
			}

			// Set final record ID value
			nsFormat = nsFormat.replace("{counter}",
					String.format("%0" + nsCounterFormat.length() + "d", recId.getCounter()));
			recId.setRecIdValue(nsFormat);
		}

		// Check for duplicates
		if (!recId.getRecIdValue().trim().isEmpty()) {
			if (voucherTypeFieldName.trim().isEmpty()) {
				voucherTypeFieldName = "v_Type";
			}
			String duplicateCheckSql = "SELECT COUNT(" + recIdFieldName + ") AS Cnt FROM " + tableName + " WHERE "
					+ recIdFieldName + " = ? AND " + keyFieldName + " <> ? AND " + voucherTypeFieldName + " IN ("
					+ getParamList(nsIncludeV_Type) + ")";

			if (isYearWise) {
				duplicateCheckSql += " AND v_Prefix = ?";
			}
			if (isSiteWise) {
				duplicateCheckSql += " AND site_Code = ?";
			}

			// Prepare parameters for duplicate check
			List<Object> params = new ArrayList<>();
			params.add(recId.getRecIdValue());
			params.add(keyValue);
			Collections.addAll(params, nsIncludeV_Type.split(","));
			if (isYearWise) {
				params.add(voucherPrefix);
			}
			if (isSiteWise) {
				params.add(siteCode);
			}

			int count = jdbcTemplate.queryForObject(duplicateCheckSql, Integer.class, params.toArray());
			if (count > 0) {
				throw new IllegalArgumentException("Duplicate voucher no.");
			}
		}

		if (recId.getRecIdValue().trim().isEmpty()) {
			throw new IllegalArgumentException("Voucher no. is mandatory.");
		}

		return recId;
	}

	private String fetchVoucherType(String voucherType) throws Exception {
		String sql = "SELECT TOP 1 vt.short_Name FROM voucher_Type vt WHERE vt.isActive = 1 AND vt.v_Type = ?";
		try {
			// Use parameterized query to prevent SQL injection
			@SuppressWarnings("deprecation")
			String voucherName = jdbcTemplate.queryForObject(sql, new Object[] { voucherType }, String.class);
			return voucherName; // Return the result directly if found
		} catch (EmptyResultDataAccessException e) {
			// Handle the case where no result is found
			throw new Exception("Voucher Type not found for type: " + voucherType, e);
		} catch (DataAccessException e) {
			// Handle other database-related exceptions
			throw new Exception("Database error occurred while fetching voucher type: " + voucherType, e);
		}
	}

	private String getParamList(String nsIncludeV_Type) {
		String[] types = nsIncludeV_Type.split(",");
		StringBuilder paramList = new StringBuilder();
		for (int i = 0; i < types.length; i++) {
			if (i > 0) {
				paramList.append(",");
			}
			paramList.append("?");
		}
		return paramList.toString();
	} 

	@Override
	public String fetchVoucherPrefix(String unixTimestamp) {
	    // Convert the string Unix timestamp to a long
	    long timestamp = Long.parseLong(unixTimestamp);
	    
	    // Convert Unix timestamp to LocalDate (assuming the timestamp is in seconds)
	    Instant instant = Instant.ofEpochSecond(timestamp);
	    LocalDate date = instant.atZone(ZoneId.systemDefault()).toLocalDate();
	    
	    // Get the year and month from the date
	    int year = date.getYear();
	    int month = date.getMonthValue();
	    
	    // Determine the financial year prefix
	    if (month >= 4) { // April to December
	        return String.valueOf(year).substring(2); // Last two digits of the current year
	    } else { // January to March
	        return String.valueOf(year - 1).substring(2); // Last two digits of the previous year
	    }
	}
	
}
