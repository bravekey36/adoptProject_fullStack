package petcare.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import petcare.mapper.AdoptMapper;
import petcare.dto.AdoptDTO;

import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class AdoptService {
    private static final Logger logger = LoggerFactory.getLogger(AdoptService.class);

    @Autowired
    private AdoptMapper adoptMapper;

    /**
     * LLM에서 받은 query 조건에 맞는 보호동물 리스트를 조회합니다.
     * 신뢰성 있는 데이터(pet_uid 1~1377)만 추천합니다.
     * @param query LLM에서 받은 추천 조건(JSON)
     * @return 보호동물 리스트
     */
    public List<AdoptDTO> findPetsByQuery(Map<String, Object> query) {
        // 기존 조건 파싱
        String breed = (String) query.get("breed_cd");
        List<String> weightList = parseWeightList(query.get("weight_kg"));
        String gender = (String) query.get("gender_cd");
        String status = "APPLY_AVAILABLE";
        String neutered = (String) query.get("neutered_cd");
        List<String> colorList = parseColorList(query.get("color"));
        Long shelterId = parseShelterId(query.get("shelter_id"));
        List<String> features = parseFeatures(query.get("feature"));
        
        // 나이 그룹 → 생년월일 범위 변환
        String[] birthRange = parseBirthRange(query.get("age_group"));
        String birthMin = birthRange[0];
        String birthMax = birthRange[1];
        
        int limit = 3;

        // 조건에 맞는 보호동물 조회 (DB에서 이미 신뢰성 범위 적용)
        List<AdoptDTO> result = adoptMapper.selectByConditions(
            breed, weightList, gender, status, neutered, features, colorList, shelterId, birthMin, birthMax, limit
        );
        
        logger.info("[AdoptService] 조건 검색 결과: {}마리", result.size());
        
        // 조건에 맞는 강아지가 부족한 경우에만 랜덤 추가
        if (result.size() < limit) {
            int needed = limit - result.size();
            logger.info("[AdoptService] 부족한 개수: {}마리, 랜덤 추가 진행", needed);
            List<AdoptDTO> randomPets = getRandomTrustedPets(needed, result);
            
            // 랜덤 추천 강아지에만 다양한 코멘트 추가
            addRandomComments(randomPets);
            
            result.addAll(randomPets);
            logger.info("[AdoptService] 랜덤 추가 완료, 최종 결과: {}마리", result.size());
        }
        
        return result;
    }

    /**
     * 가중치 리스트 파싱 - 배열 형태 처리
     */
    private List<String> parseWeightList(Object weightObj) {
        List<String> weightList = new java.util.ArrayList<>();
        
        if (weightObj instanceof String str && !str.isBlank()) {
            String w = str.trim().toLowerCase();
            if (w.equals("s") || w.equals("m") || w.equals("l")) {
                weightList.add(w);
            }
        } else if (weightObj instanceof List<?> list) {
            for (Object item : list) {
                if (item instanceof String str && !str.isBlank()) {
                    String w = str.trim().toLowerCase();
                    if (w.equals("s") || w.equals("m") || w.equals("l")) {
                        weightList.add(w);
                    }
                }
            }
        }
        
        return weightList.isEmpty() ? null : weightList;
    }

    /**
     * 색상 리스트 파싱
     */
    private List<String> parseColorList(Object colorObj) {
        if (!(colorObj instanceof String str) || str.isBlank()) {
            return null;
        }
        
        List<String> colorList = new java.util.ArrayList<>();
        String trimmed = str.trim();
        
        switch (trimmed) {
            case "흰색" -> {
                colorList.add("흰색");
                colorList.add("크림색");
            }
            case "갈색" -> {
                colorList.add("갈색");
                colorList.add("갈색&흰색");
                colorList.add("황갈색");
                colorList.add("흰색&황갈색");
            }
            case "검정" -> {
                colorList.add("검정");
                colorList.add("검정&흰색");
                colorList.add("갈색&검정");
                colorList.add("검정색");
            }
            default -> colorList.add(trimmed);
        }
        
        return colorList;
    }

    /**
     * 보호소 ID 파싱
     */
    private Long parseShelterId(Object shelterObj) {
        if (shelterObj instanceof Number num) {
            return num.longValue();
        }
        return null;
    }

    /**
     * 특징 리스트 파싱
     */
    private List<String> parseFeatures(Object featuresObj) {
        if (featuresObj instanceof List<?>) {
            return ((List<?>) featuresObj).stream()
                .map(Object::toString)
                .toList();
        } else if (featuresObj instanceof String str && !str.isBlank()) {
            return List.of(str);
        }
        return null;
    }

    /**
     * 나이 그룹 → 생년월일 범위 변환
     */
    private String[] parseBirthRange(Object ageGroupObj) {
        String birthMin = null;
        String birthMax = null;
        
        if (ageGroupObj instanceof String ag && !ag.isBlank()) {
            java.time.LocalDate today = java.time.LocalDate.now();
            switch (ag) {
                case "유견" -> {
                    birthMax = today.toString().substring(0, 7);
                    birthMin = today.minusYears(1).toString().substring(0, 7);
                }
                case "성견" -> {
                    birthMax = today.minusYears(2).toString().substring(0, 7);
                    birthMin = today.minusYears(7).toString().substring(0, 7);
                }
                case "노견" -> {
                    birthMax = today.minusYears(8).toString().substring(0, 7);
                    birthMin = "1970-01";
                }
            }
        }
        
        return new String[]{birthMin, birthMax};
    }

    /**
     * 랜덤 추천 코멘트 추가 (조건에 맞지 않는 추가 추천 시에만 사용)
     */
    private void addRandomComments(List<AdoptDTO> randomPets) {
        String[] comments = {
            "이런 개는 어때요?",
            "추가로 고려해볼 만한 아이예요!",
            "이 아이도 좋은 선택이 될 것 같아요!",
            "다른 아이도 추천드려요.",
            "이런 친구는 어떠세요?"
        };
        
        for (int i = 0; i < randomPets.size(); i++) {
            AdoptDTO pet = randomPets.get(i);
            String comment = comments[i % comments.length];
            pet.setRecommendation_comment(comment);
        }
    }

    /**
     * 랜덤 보호동물 추천 (이미 추천된 동물은 제외)
     * @param count 필요한 개수
     * @param excludePets 제외할 동물 리스트
     * @return 랜덤 보호동물 리스트
     */
    private List<AdoptDTO> getRandomTrustedPets(int count, List<AdoptDTO> excludePets) {
        if (count <= 0) {
            return new java.util.ArrayList<>();
        }
        
        logger.info("[AdoptService] 랜덤 펫 요청: {}마리", count);
        
        // 이미 추천된 동물의 pet_uid 수집
        java.util.Set<Long> excludeIds = excludePets.stream()
            .map(AdoptDTO::getPet_uid)
            .collect(java.util.stream.Collectors.toSet());
        
        logger.info("[AdoptService] 제외할 펫 ID: {}", excludeIds);
        
        // 넉넉하게 조회하여 중복 제거 후 충분한 개수 확보
        int queryLimit = Math.max(count * 2, 20);
        
        // 랜덤 동물 조회 (기본 조건만 적용, DB에서 이미 신뢰성 범위 적용)
        List<AdoptDTO> randomPets = adoptMapper.selectByConditions(
            null, // breed
            null, // weightList  
            null, // gender
            "APPLY_AVAILABLE", // status
            null, // neutered
            null, // features
            null, // colorList
            null, // shelterId
            null, // birthMin
            null, // birthMax
            queryLimit
        );
        
        logger.info("[AdoptService] 랜덤 조회 결과: {}마리", randomPets.size());
        
        // 이미 추천된 동물 제외
        List<AdoptDTO> filtered = randomPets.stream()
            .filter(pet -> !excludeIds.contains(pet.getPet_uid()))
            .limit(count)
            .collect(java.util.stream.Collectors.toList());
        
        logger.info("[AdoptService] 중복 제거 후 반환: {}마리", filtered.size());
        
        return filtered;
    }
}