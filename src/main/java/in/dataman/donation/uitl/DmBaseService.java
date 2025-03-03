package in.dataman.donation.uitl;

import java.util.HashMap;

public interface DmBaseService {
	String getSerialNoInteger(String siteCode, String tableName, Integer prefix);

	String getSerialNoBigInt(String siteCode, String tableName);

	String getSerialNoBigInt(String siteCode, String vPrefix, String tableName);

	String getDocId(String voucherType, String voucherPrefix, String siteCode) throws Exception;

	public RecId getRecId(String tableName, String keyFieldName, String keyValue, String recIdFieldName, RecId recId,
			String dateValue, String voucherTypeFieldName, String voucherType, String voucherPrefix, String siteCode,
			String siteShortName, String companyCode, Boolean isCheckDate, HashMap<String, String> paramFormat)
			throws Exception;


	String fetchVoucherPrefix(String dateStr);
}
