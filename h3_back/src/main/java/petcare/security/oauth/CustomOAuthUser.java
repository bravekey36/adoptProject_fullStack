package petcare.security.oauth;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;
import petcare.dto.UserDTO;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class CustomOAuthUser implements OAuth2User {
    private OAuth2User oauth2User;
    private UserDTO userDTO;

    public CustomOAuthUser(OAuth2User oauth2User, UserDTO userDTO) {
        this.oauth2User = oauth2User;
        this.userDTO = userDTO;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return oauth2User.getAttributes();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + userDTO.getRole()));
    }

    @Override
    public String getName() {
        return userDTO.getName();
    }

    public UserDTO getUserDTO() {
        return userDTO;
    }

    public String getUserId() {
        return userDTO.getUserId();
    }

    public String getEmail() {
        return userDTO.getEmail();
    }

    public String getRole() {
        return userDTO.getRole();
    }
}
