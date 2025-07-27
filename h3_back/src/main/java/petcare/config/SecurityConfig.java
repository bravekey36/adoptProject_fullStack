package petcare.config;

import petcare.security.jwt.JwtFilter;
import petcare.security.jwt.JwtProvider;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequestEntityConverter;
import petcare.security.oauth.CustomOAuth2SuccessHandler;
import petcare.service.CustomOAuthUserService;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import jakarta.servlet.http.HttpServletResponse;
import java.net.URI;
import java.util.Collections;

import static org.springframework.security.config.Customizer.withDefaults;
import static org.springframework.security.config.http.SessionCreationPolicy.*;
import static org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.*;

@Configuration
public class SecurityConfig {

    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private CustomOAuth2SuccessHandler oAuth2SuccessHandler;

    @Autowired
    private CustomOAuthUserService customOAuthUserService;

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> oAuth2AccessTokenResponseClient() {
        DefaultAuthorizationCodeTokenResponseClient client = new DefaultAuthorizationCodeTokenResponseClient();
        client.setRequestEntityConverter(new OAuth2AuthorizationCodeGrantRequestEntityConverter() {
            @Override
            public RequestEntity<?> convert(OAuth2AuthorizationCodeGrantRequest request) {
                RequestEntity<?> entity = super.convert(request);
                if ("kakao".equals(request.getClientRegistration().getRegistrationId())) {
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
                    headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
                    MultiValueMap<String, String> formParameters = new LinkedMultiValueMap<>();
                    formParameters.add("grant_type", "authorization_code");
                    formParameters.add("client_id", request.getClientRegistration().getClientId());
                    formParameters.add("client_secret", request.getClientRegistration().getClientSecret());
                    formParameters.add("redirect_uri", request.getClientRegistration().getRedirectUri());
                    formParameters.add("code", request.getAuthorizationExchange().getAuthorizationResponse().getCode());
                    return new RequestEntity<>(formParameters, headers, HttpMethod.POST, URI.create(request.getClientRegistration().getProviderDetails().getTokenUri()));
                }
                return entity;
            }
        });
        return client;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(withDefaults()) // CORS 설정 추가 (withDefaults()로 명확하게)
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // 🔓 인증 없이 접근 가능한 엔드포인트
                .requestMatchers("/api/user/register", "/api/user/login", "/api/user/check-duplicate/**", "/api/user/check-duplicate-email/**").permitAll()
                .requestMatchers("/api/social/**").permitAll()  // 소셜 로그인 관련 API
                .requestMatchers("/h2-console/**", "/error").permitAll()
                .requestMatchers("/test/**", "/dbtest/**").hasRole("ADMIN")  // 🔒 테스트 엔드포인트 - 관리자만
                
                // � 메인화면용 공지사항 및 보호견 발견신고 조회 (비회원도 접근 가능) - 구체적인 패턴 먼저!
                .requestMatchers("/admin/notice/select", "/admin/notice/petfindinfo").permitAll()
                
                // �👑 관리자 권한 필요 (ROLE_ADMIN)
                .requestMatchers("/api/user/list", "/api/user/update-role").hasRole("ADMIN")
                .requestMatchers("/api/user/admin-test").hasRole("ADMIN")  // 테스트용
                .requestMatchers("/admin/**").hasRole("ADMIN")  // 위의 구체적 패턴 이후에 배치
                .requestMatchers("/api/common/code/**", "/api/common/code", "/api/common/code-group").hasRole("ADMIN")  // 공통코드 관리
                
                // 🔓 공통코드 조회는 PUBLIC (데이터 참조용)
                .requestMatchers("/api/common/codes", "/api/common/shelters").permitAll()
                
                // 👤 인증된 사용자 (USER + ADMIN 모두)
                .requestMatchers("/api/user/me", "/api/user/info", "/api/user/phone", "/api/user/password").authenticated()
                .requestMatchers("/api/user/{userId}").authenticated()
                .requestMatchers("/api/supabase/storage/**").authenticated()
                .requestMatchers("/api/animals/**").authenticated()
                .requestMatchers("/api/gemini/**").authenticated()
                .requestMatchers("/manager/**").authenticated()
                
                // 🤖 AI 관련 서비스 (모든 채팅/진단 API - 인증 필요)
                .requestMatchers("/adopt/**").authenticated()  // 입양 상담 AI
                .requestMatchers("/aivet/**").authenticated()  // AI 수의사 피부진단
                .requestMatchers("/petcare/chat/**", "/petcare/close_chatsession").permitAll() // 보호견 검색 채팅 AI
                .requestMatchers("/petcare/chatlog/**").permitAll() // 채팅 로그
                .requestMatchers("/petcare/saveMissingPet").authenticated()  // 실종 신고 등록
                .requestMatchers("/petcare/query").hasRole("ADMIN")  // 🚨 SQL 쿼리 실행 - 관리자만!
                
                // 🔓 보호견/보호소 정보 조회 (공개)
                .requestMatchers("/petcare/shelterList", "/petcare/searchArea").permitAll()
                .requestMatchers("/petcare/petlist/**").permitAll()  // 보호견 목록 조회
                .requestMatchers("/petcare/test", "/petcare/fastapi", "/petcare/").permitAll()  // 테스트/개발용
                
                // 🔒 그 외 모든 요청은 인증 필요
                .anyRequest().authenticated()
            )
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable())
            .oauth2Login(oauth2 -> oauth2
                .tokenEndpoint(tokenEndpoint -> tokenEndpoint
                    .accessTokenResponseClient(oAuth2AccessTokenResponseClient())
                )
                .userInfoEndpoint(userInfo -> userInfo.userService(customOAuthUserService))
                .successHandler(oAuth2SuccessHandler)
            )
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint((request, response, authException) -> {
                    String uri = request.getRequestURI();
                    if (uri.equals("/login") || uri.equals("/login?error")) {
                        response.sendRedirect("/");
                    } else {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    }
                })
            )
            .addFilterBefore(new JwtFilter(jwtProvider), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}