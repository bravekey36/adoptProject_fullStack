# adopt_chatbot

# adopt_chatbot

## LLM 구조 요약

1. **.env**
   - Gemini API 키, 프로젝트 ID 등 환경 변수 설정 파일입니다.
   - 예시:
     ```
     GEMINI_ID=your-gemini-project-id
     GOOGLE_APPLICATION_CREDENTIALS=your-service-account.json
     ```

2. **llm_models/llm_wrapper.py**
   - Google Gemini LLM API와 연동하는 래퍼 파일입니다.
   - 프롬프트를 받아 Gemini로 전송하고, 응답을 반환합니다.

3. **app/api/llm_api.py**
   - FastAPI 엔드포인트에서 LLM 호출 및 응답 파싱을 담당합니다.
   - 세션별 대화 히스토리와 사용자 입력을 조합해 LLM에 전달하고, 결과를 프론트/백엔드로 반환합니다.

---

**구조 요약**
- 사용자의 질문과 대화 히스토리가 FastAPI로 전달됨
- llm_wrapper.py에서 Gemini LLM 호출
- LLM 응답을 파싱해 실제 보호견 추천에 활용