package petcare.dto;

import lombok.Data;

@Data
public class PetPredictAreaDTO {
    private String place;
    private String breed; 
    private String age;
    private double weight; 
    private String shelter; 
    private String shelter_address; 
    private double WGS84_latitude;
    private double WGS84_longitude;
    private double place_latitude;
    private double place_longitude;
    private String pet_received_date;
}
