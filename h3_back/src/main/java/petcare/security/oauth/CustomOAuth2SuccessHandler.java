package petcare.security.oauth;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import petcare.dto.UserDTO;
import petcare.security.jwt.JwtProvider;
import java.io.IOException;
import java.net.URLEncoder;

@Component
@RequiredArgsConstructor
public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {
    private final JwtProvider jwtTokenProvider;

    // 프론트엔드 콜백 URL (환경에 따라 분리 권장)
    private static final String REDIRECT_URL = "http://192.168.0.13:5173/social/callback";

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        Object principal = authentication.getPrincipal();
        System.out.println("[DEBUG] principal class: " + principal.getClass().getName());
        if (principal instanceof CustomOAuthUser oAuth2User) {
            UserDTO user = oAuth2User.getUserDTO();
            System.out.println("[DEBUG] userDTO: " + user);
            if (user == null) {
                System.out.println("[ERROR] userDTO is null!");
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "userDTO가 null입니다.");
                return;
            }
            if (user.getUserId() == null || user.getUserId().isEmpty()) {
                System.out.println("[ERROR] userId is null or empty! userDTO: " + user);
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "userId가 null입니다.");
                return;
            }
            String token;
            try {
                token = jwtTokenProvider.create(user.getUserId(), user.getRole());
            } catch (Exception e) {
                System.out.println("[ERROR] JWT 생성 실패: " + e.getMessage());
                e.printStackTrace();
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "JWT 생성 실패");
                return;
            }
            System.out.println("[DEBUG] token: " + token);
            String redirectUrl = REDIRECT_URL
                + "?token=" + URLEncoder.encode(token, "UTF-8")
                + "&userId=" + URLEncoder.encode(user.getUserId(), "UTF-8")
                + "&name=" + URLEncoder.encode(user.getName(), "UTF-8")
                + "&role=" + URLEncoder.encode(user.getRole(), "UTF-8");
            response.sendRedirect(redirectUrl);
        } else {
            System.out.println("[ERROR] principal is not CustomOAuthUser: " + principal);
            System.out.println("[ERROR] principal toString: " + principal.toString());
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "OAuth2 인증 객체 타입 오류");
        }
    }
}
