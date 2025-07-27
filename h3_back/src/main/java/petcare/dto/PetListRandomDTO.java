package petcare.dto;

import lombok.Data;

@Data
public class PetListRandomDTO {
    private String name;
    private String breedName;
    private String birthYyyyMm;
    private String genderCd;
    private String foundLocation;
    private String receptionDate;
    private String shelterName;
    private String publicUrl; // 대표 이미지 URL
    private String profilehtml; // 입양 프로필
}
