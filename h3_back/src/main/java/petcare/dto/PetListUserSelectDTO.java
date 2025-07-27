package petcare.dto;

import lombok.Data;

@Data
public class PetListUserSelectDTO {

    private String breed;
    private String shelter;
    private Boolean hasProfile;
    
}
