package petcare.dto;

import lombok.Data;

@Data
public class ShelterDTO {

    String breed;
    String shelter;
    String shelter_call_number;
    String shelter_address;
    double WGS84_latitude;
    double WGS84_longitude;
    
}
