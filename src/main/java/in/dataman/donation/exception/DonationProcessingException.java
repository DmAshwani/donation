package in.dataman.donation.exception;

public class DonationProcessingException extends RuntimeException {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public DonationProcessingException(String message) {
        super(message);
    }

    public DonationProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
