package petcare.dto;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Data // Getter/Setter, ToString, Equals 자동 생성
public class PetProfileDTO {
    private Long petUid;                // 고유ID (PK)
    private String name;                // 이름
    private String genderCd;            // 성별 [PET_GENDER]
    private String genderNm;            // 성별 명
    private String breedCd;             // 품종 [DOG_BREED]
    private String breedNm;             // 품종 명
    private String adoptionStatusCd;    // 입양 상태 [ADOPTION_STATUS]
    private String adoptionStatusNm;    // 입양 상태 명
    private String birthYyyyMm;         // 출생년월 (YYYY-MM)
    private String neuteredCd;          // 중성화 여부 [NEUTER_STATUS]
    private String neuteredNm;          // 중성화 여부 명
    private BigDecimal weightKg;        // 체중 (kg)
    private String color;               // 주 색상
    private String feature;             // 특징
    private String foundLocation;       // 발견장소
    private LocalDate receptionDate;    // 접수일자(신고일자)
    private Integer imageCount;         // 등록 사진 개수
    
    private String shelterId;           // 보호처ID
    private String shelterName;         // 보호처명
    private String shelterPhone;        // 보호처 전화번호
    private String cityName;            // 시도명
    private String jurisdictionOrg;     // 관할기관
    private String shelterRoadAddress;  // 보호처 도로명주소
    private String noticeId;            // 공고ID(해당 센터의 고유ID)

    private LocalDateTime createdAt;   // 생성일시
    private String creatorId;          // 생성자ID
    private String creatorNm;          // 생성자명
    private LocalDateTime updatedAt;   // 수정일시
    private String updatorId;          // 수정자ID
    private String updatorNm;          // 수정자명
    private String profileHtml;        // 입양프로필 HTML

    public Map<String, Object> toPetProfileMap() {
        Map<String, Object> petProfileMap = new HashMap<>();
        petProfileMap.put("noticeId", noticeId);
        petProfileMap.put("이름", name);
        petProfileMap.put("성별", genderNm);
        petProfileMap.put("무게(kg)", weightKg != null ? weightKg.toString() : "");
        petProfileMap.put("색상", color);
        petProfileMap.put("중성화 여부", neuteredNm);
        petProfileMap.put("입양 상태", adoptionStatusNm);
        petProfileMap.put("보호센터", shelterName);
        petProfileMap.put("보호센터 연락처", shelterPhone);
        return petProfileMap;
    }
}