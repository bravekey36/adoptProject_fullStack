package petcare.security.oauth;

import java.util.Map;

public class OAuthUserInfoFactory {
    public static OAuthUserInfo get(String registrationId, Map<String, Object> attributes) {
        if (registrationId.equalsIgnoreCase("google")) {
            return new GoogleUserInfo(attributes);
        } else if (registrationId.equalsIgnoreCase("kakao")) {
            return new KakaoUserInfo(attributes);
        } else if (registrationId.equalsIgnoreCase("naver")) {
            return new NaverUserInfo(attributes);
        } else {
            throw new IllegalArgumentException("지원하지 않는 소셜 로그인입니다: " + registrationId);
        }
    }
}
