package in.dataman.donation.test;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestDTO {

    private String userId;
    private String name;
    private Integer age;
    private String gender;

}
