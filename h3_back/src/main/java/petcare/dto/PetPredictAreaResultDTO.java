package petcare.dto;

import lombok.Data;

@Data
public class PetPredictAreaResultDTO {
    private int predicted_cluster;
    private float center_latitude;
    private float center_longitude;
    private double radius;
}
