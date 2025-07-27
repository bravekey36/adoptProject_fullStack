package petcare.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

public class JwtFilter extends OncePerRequestFilter {
    private final JwtProvider jwtProvider;

    public JwtFilter(JwtProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String uri = request.getRequestURI();
        String bearerToken = request.getHeader("Authorization");
        String token = null;
        System.out.println("[JwtFilter] 요청 URI: " + uri);
        System.out.println("[JwtFilter] Authorization 헤더: " + bearerToken);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            token = bearerToken.substring(7);
        }

        if (token != null) {
            System.out.println("[JwtFilter] 추출된 토큰: " + token);
        } else {
            System.out.println("[JwtFilter] 토큰 추출 실패 (Authorization 헤더 없음 또는 형식 오류)");
        }

        if (token != null && jwtProvider.validateToken(token)) {
            System.out.println("[JwtFilter] 토큰 유효: 인증 객체 세팅");
            String username = jwtProvider.getUsernameFromToken(token);
            String role = jwtProvider.getRole(token);
            System.out.println("[JwtFilter] 토큰에서 추출한 username: " + username);
            System.out.println("[JwtFilter] 토큰에서 추출한 role: " + role);
            Authentication auth = new UsernamePasswordAuthenticationToken(username, null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role)));
            SecurityContextHolder.getContext().setAuthentication(auth);
        } else if (requiresAuthentication(request)) {
            if (token != null) {
                System.out.println("[JwtFilter] 토큰 유효성 검사 실패: " + token);
            }
            System.out.println("[JwtFilter] 토큰 없음 또는 유효하지 않음: 401 반환");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or missing JWT token");
            return;
        }
        filterChain.doFilter(request, response);
    }

    // 인증이 필요한 경로 체크 - 이제 SecurityConfig에서 대부분 처리하므로 단순화
    private boolean requiresAuthentication(HttpServletRequest request) {
        String uri = request.getRequestURI();
        // 공개 API가 아닌 경우 인증 필요
        return !uri.startsWith("/api/user/register") 
            && !uri.startsWith("/api/user/login") 
            && !uri.startsWith("/api/user/check-duplicate")
            && !uri.startsWith("/api/common/codes")
            && !uri.startsWith("/api/common/shelters")
            && !uri.startsWith("/h2-console")
            && !uri.startsWith("/error")
            && !uri.startsWith("/test")
            && !uri.startsWith("/dbtest")
            && !uri.startsWith("/aivet")
            && !uri.equals("/admin/notice/select")  // 공지사항 조회는 비회원 허용
            && !uri.equals("/admin/notice/petfindinfo")  // 발견신고 조회는 비회원 허용
            && (uri.startsWith("/api/") || uri.startsWith("/adopt/AIprocess") || uri.startsWith("/admin/"));
    }
}
