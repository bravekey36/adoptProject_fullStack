package petcare.dto;

public class PetcareDTO {
    private int id;          // 테이블에 있는 컬럼명에 맞게 작성
    private String sigunName;   // 시군명
    private String foundPlace;  // 발견장소

    public PetcareDTO() {}
    
    // Getter/Setter
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getSigunName() { return sigunName; }
    public void setSigunName(String sigunName) { this.sigunName = sigunName; }

    public String getFoundPlace() { return foundPlace; }
    public void setFoundPlace(String foundPlace) { this.foundPlace = foundPlace; }

}
