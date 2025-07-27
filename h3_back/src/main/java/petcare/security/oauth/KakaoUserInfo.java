package petcare.security.oauth;

import java.util.Map;

public class KakaoUserInfo extends OAuthUserInfo {
    public KakaoUserInfo(Map<String, Object> attributes) {
        super(attributes);
        System.out.println("[KakaoUserInfo] attributes: " + attributes);
        if (attributes != null) {
            Object kakaoAccount = attributes.get("kakao_account");
            System.out.println("[KakaoUserInfo] kakao_account: " + kakaoAccount);
            Object properties = attributes.get("properties");
            System.out.println("[KakaoUserInfo] properties: " + properties);
        }
    }
    @Override
    public String getId() {
        return String.valueOf(attributes.get("id"));
    }
    @SuppressWarnings("unchecked")
    @Override
    public String getName() {
        // 1순위: kakao_account.profile.nickname
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        if (kakaoAccount != null) {
            Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
            if (profile != null && profile.get("nickname") != null) {
                return (String) profile.get("nickname");
            }
        }
        // 2순위: properties.nickname
        Map<String, Object> properties = (Map<String, Object>) attributes.get("properties");
        if (properties != null && properties.get("nickname") != null) {
            return (String) properties.get("nickname");
        }
        // 3순위: email 앞부분
        String email = getEmail();
        if (email != null && email.contains("@")) {
            return email.split("@")[0];
        }
        return "카카오사용자";
    }
    @SuppressWarnings("unchecked")
    @Override
    public String getEmail() {
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        return kakaoAccount != null ? (String) kakaoAccount.get("email") : null;
    }
    @SuppressWarnings("unchecked")
    @Override
    public String getImageUrl() {
        // 1순위: kakao_account.profile.profile_image_url
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        if (kakaoAccount != null) {
            Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
            if (profile != null && profile.get("profile_image_url") != null) {
                return (String) profile.get("profile_image_url");
            }
        }
        // 2순위: properties.profile_image
        Map<String, Object> properties = (Map<String, Object>) attributes.get("properties");
        if (properties != null && properties.get("profile_image") != null) {
            return (String) properties.get("profile_image");
        }
        return null;
    }
}
