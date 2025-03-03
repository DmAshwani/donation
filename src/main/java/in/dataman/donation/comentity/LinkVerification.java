package in.dataman.donation.comentity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity
@Table(name = "linkVerification")
public class LinkVerification {

    @Id
    private String code;  // Primary key

    @Column(name = "keyField")
    private String keyField;

    @Column(name = "mobile")
    private String mobile;

    @Column(name = "otp")
    private String otp;
    
    @Column(name = "preparedDt")
    private LocalDateTime preparedDt;

}

