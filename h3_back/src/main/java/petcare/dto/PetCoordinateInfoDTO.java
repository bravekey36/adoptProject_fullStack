package petcare.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class PetCoordinateInfoDTO {

    @JsonProperty("id")
    private int id;

    @JsonProperty("place_latitude")
    private Double place_latitude;

    @JsonProperty("place_longitude")
    private Double place_longitude;
}