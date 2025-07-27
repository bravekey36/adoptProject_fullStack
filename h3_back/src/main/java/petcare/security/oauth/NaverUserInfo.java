package petcare.security.oauth;

import java.util.Map;

public class NaverUserInfo extends OAuthUserInfo {
    public NaverUserInfo(Map<String, Object> attributes) {
        super(attributes);
    }
    @Override
    public String getId() {
        Map<String, Object> response = getResponse();
        return response != null ? (String) response.get("id") : null;
    }
    @Override
    public String getName() {
        Map<String, Object> response = getResponse();
        return response != null ? (String) response.get("name") : null;
    }
    @Override
    public String getEmail() {
        Map<String, Object> response = getResponse();
        return response != null ? (String) response.get("email") : null;
    }
    @Override
    public String getImageUrl() {
        Map<String, Object> response = getResponse();
        return response != null ? (String) response.get("profile_image") : null;
    }
    @SuppressWarnings("unchecked")
    private Map<String, Object> getResponse() {
        Object resp = attributes.get("response");
        if (resp instanceof Map) return (Map<String, Object>) resp;
        return attributes;
    }
}
