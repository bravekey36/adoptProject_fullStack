package petcare.dto;

import java.util.Map;

import lombok.Data;

@Data // Getter/Setter, ToString, Equals 자동 생성
public class PetImageDTO {
    private Long petImageId;       // 이미지 고유ID (PK)
    private Long petUid;           // 동물 고유ID (FK)
    private String fileName;       // 이미지 파일명
    private String imageUrl;       // 이미지 publicURL
    private Long fileSize;         // 파일 크기 (bytes)
    private String imageVector;    // 이미지벡터 JSON (LONGTEXT)
    private String isBestImage;  // 대표이미지 여부 (Y/N)

    public PetImageDTO() {
    }

    public PetImageDTO(Long petUid,Map<String, Object> map) {
        this.petImageId = (Long) map.get("petImageId");
        this.petUid = petUid;
        this.fileName = (String) map.get("fileName");
        this.imageUrl = (String) map.get("publicUrl");
        this.fileSize = (Long) map.get("fileSize");
        this.isBestImage = "N";
    }
}
