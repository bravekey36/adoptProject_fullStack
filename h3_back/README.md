프로젝트 소개
Spring Boot 기반 보호견 입양·상담 백엔드 서버입니다.
RESTful API, DB 관리, 소셜 로그인, 파일 업로드, AI 연동 등 보호견 서비스에 필요한 핵심 기능을 제공합니다.

아키텍처
Spring Boot + MariaDB + MyBatis로 데이터 및 비즈니스 로직 처리
OAuth2(Google, Kakao, Naver) 및 JWT 기반 인증
Supabase 연동 파일 업로드/관리
FastAPI 기반 AI 서버와 연동하여 보호견 진단·추천 등 AI 기능 제공
환경변수(.env)와 application.properties로 운영/개발 환경 분리
CORS, 보안, 로깅 등 운영 환경에 필요한 설정 포함

주요 기능
보호견 정보 및 입양 상담 API
소셜 로그인 및 JWT 인증
이미지 업로드 및 외부 스토리지 연동
AI 서버 연동(진단, 추천 등)

실행 방법
환경변수(.env) 및 DB 설정 확인
빌드: ./gradlew build
실행: java -jar build/libs/생성된파일.jar

포트 및 환경
기본 포트: 8080
환경변수 및 DB 정보는 .env / application.properties에서 관리
