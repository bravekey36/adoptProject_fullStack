package petcare.security.oauth;

import java.util.Map;

public abstract class OAuthUserInfo {
    protected Map<String, Object> attributes;

    public OAuthUserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public abstract String getId();
    public abstract String getName();
    public abstract String getEmail();
    public abstract String getImageUrl();
    
    public petcare.dto.UserDTO toUserDTO(String provider) {
        petcare.dto.UserDTO userDTO = new petcare.dto.UserDTO();
        userDTO.setEmail(getEmail());
        userDTO.setName(getName());
        userDTO.setProvider(provider);
        userDTO.setProviderId(getId());
        userDTO.setProfileImage(getImageUrl());
        userDTO.setRole("USER");
        return userDTO;
    }
}
