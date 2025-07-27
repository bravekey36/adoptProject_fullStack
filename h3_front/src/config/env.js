// Vite 환경 변수 래퍼
// 사용법: import해서 상수처럼 사용
// import { API_SERVER_URL, WEBAPP_URL } from '@/config/env';

// 실행환경 설정 (local, development, production)
export const NODE_ENV = 'local';

// 서버 주소
// export const API_SERVER_URL = "http://localhost:8080"; // import.meta.env.VITE_API_SERVER_URL;
// export const WEBAPP_URL = "http://localhost:5173";


export const API_SERVER_URL = "http://10.118.45.154:8080"; // 백엔드 서버의 실제 IP:포트로 변경
export const WEBAPP_URL = "http://10.118.45.154:5173"; // 프론트엔드 실제 주소

// AI 이미지 서버 주소 (운영/개발 환경에 맞게 .env에서 설정)
export const AI_IMAGE_SERVER_URL = import.meta.env.VITE_AI_IMAGE_SERVER_URL || "http://localhost:8001";