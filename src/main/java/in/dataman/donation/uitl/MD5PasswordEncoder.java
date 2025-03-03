package in.dataman.donation.uitl;


import org.springframework.security.crypto.password.PasswordEncoder;

public class MD5PasswordEncoder implements PasswordEncoder {

    private final MD5Util md5Util;

    public MD5PasswordEncoder(MD5Util md5Util) {
        this.md5Util = md5Util;
    }

    @Override
    public String encode(CharSequence rawPassword) {
        // Use MD5Util to encode the raw password
        return md5Util.encodeToHex("", rawPassword.toString()); 
    }

 

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
    
//        String hashedPassword = md5Util.encodeToHex(username, rawPassword.toString());
        
        return rawPassword.equals(encodedPassword);
    }



}
