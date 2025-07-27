package petcare.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class AdoptDTO {
    private Long pet_uid;           // 펫 id
    private String name;            // 이름
    private String gender_cd;       // 성별 (F, M, Q)
    private String breed_cd;        // 품종
    private String adoption_status_cd; // 상태 (APPLY_AVAILABLE만 조회)
    private String birth_yyyy_mm;   // 2025-01 형태
    private String neutered_cd;     // 중성화 (Y, N, U)
    private Double weight_kg;       // 몸무게
    private String color;           // 털색
    private String feature;         // 특징
    private Long shelter_id;        // 쉘터 id
    private String shelter_name;    // 보호소명
    private LocalDate reception_date; // 접수일자
    private String found_location;  // 발견장소
    private String notice_id;       // 공고고유번호
    private LocalDate notice_start_date; // 공고시작일자
    private LocalDate notice_end_date;   // 공고종료일자
    private String data_source;     // 데이터 소스
    private LocalDateTime created_at; // 생성일시
    private LocalDateTime updated_at; // 수정일시
    private String creator_id;        // 생성자 id (문자열로 변경)
    private String updator_id;        // 수정자 id (문자열로 변경)
    private String public_url;      // 이미지 public url
    private String recommendation_comment; // 추천 코멘트 (랜덤 추천 시 사용)

    public AdoptDTO() {}

    public Long getPet_uid() { return pet_uid; }
    public void setPet_uid(Long pet_uid) { this.pet_uid = pet_uid; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getGender_cd() { return gender_cd; }
    public void setGender_cd(String gender_cd) { this.gender_cd = gender_cd; }

    public String getBreed_cd() { return breed_cd; }
    public void setBreed_cd(String breed_cd) { this.breed_cd = breed_cd; }

    public String getAdoption_status_cd() { return adoption_status_cd; }
    public void setAdoption_status_cd(String adoption_status_cd) { this.adoption_status_cd = adoption_status_cd; }

    public String getBirth_yyyy_mm() { return birth_yyyy_mm; }
    public void setBirth_yyyy_mm(String birth_yyyy_mm) { this.birth_yyyy_mm = birth_yyyy_mm; }

    public String getNeutered_cd() { return neutered_cd; }
    public void setNeutered_cd(String neutered_cd) { this.neutered_cd = neutered_cd; }

    public Double getWeight_kg() { return weight_kg; }
    public void setWeight_kg(Double weight_kg) { this.weight_kg = weight_kg; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getFeature() { return feature; }
    public void setFeature(String feature) { this.feature = feature; }

    public LocalDate getReception_date() { return reception_date; }
    public void setReception_date(LocalDate reception_date) { this.reception_date = reception_date; }

    public String getFound_location() { return found_location; }
    public void setFound_location(String found_location) { this.found_location = found_location; }

    public String getNotice_id() { return notice_id; }
    public void setNotice_id(String notice_id) { this.notice_id = notice_id; }

    public LocalDate getNotice_start_date() { return notice_start_date; }
    public void setNotice_start_date(LocalDate notice_start_date) { this.notice_start_date = notice_start_date; }

    public LocalDate getNotice_end_date() { return notice_end_date; }
    public void setNotice_end_date(LocalDate notice_end_date) { this.notice_end_date = notice_end_date; }

    public String getData_source() { return data_source; }
    public void setData_source(String data_source) { this.data_source = data_source; }

    public Long getShelter_id() { return shelter_id; }
    public void setShelter_id(Long shelter_id) { this.shelter_id = shelter_id; }

    public String getShelter_name() { return shelter_name; }
    public void setShelter_name(String shelter_name) { this.shelter_name = shelter_name; }

    public LocalDateTime getCreated_at() { return created_at; }
    public void setCreated_at(LocalDateTime created_at) { this.created_at = created_at; }

    public LocalDateTime getUpdated_at() { return updated_at; }
    public void setUpdated_at(LocalDateTime updated_at) { this.updated_at = updated_at; }


    public String getCreator_id() { return creator_id; }
    public void setCreator_id(String creator_id) { this.creator_id = creator_id; }

    public String getUpdator_id() { return updator_id; }
    public void setUpdator_id(String updator_id) { this.updator_id = updator_id; }

    public String getPublic_url() { return public_url; }
    public void setPublic_url(String public_url) { this.public_url = public_url; }

    public String getRecommendation_comment() { return recommendation_comment; }
    public void setRecommendation_comment(String recommendation_comment) { this.recommendation_comment = recommendation_comment; }
}