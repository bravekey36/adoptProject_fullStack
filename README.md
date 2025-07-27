#adoptProject_fullStack
반려동물 케어 서비스의 프론트엔드, 백엔드, LLM(챗봇) 서버를 통합 관리하는 풀스택 프로젝트입니다.

주요 기능
반려동물 정보 관리 및 입양 상담
AI 기반 피부 진단 및 이미지 검색
관리자/사용자 권한 관리, 공지사항 등 다양한 서비스 제공
기술 스택
프론트엔드: React, Vite, Tailwind CSS, Material Tailwind, ag-grid
백엔드: Spring Boot, FastAPI, OAuth2, MariaDB
LLM 서버: FastAPI 기반 챗봇
구조
h3_front: 프론트엔드 소스
h3_back: 백엔드(Spring Boot) 소스
adopt_chatbot: LLM(챗봇) 서버 소스
환경설정
각 서비스별 .env 파일로 환경변수 관리
운영/테스트/로컬 환경 전환이 용이
