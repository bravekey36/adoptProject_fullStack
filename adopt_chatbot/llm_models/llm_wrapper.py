import vertexai
from vertexai.preview.generative_models import GenerativeModel
from dotenv import load_dotenv
import os
from pathlib import Path

load_dotenv()  # .env 파일을 반드시 로드

KEY_PATH = os.getenv("GOOGLE_APPLICATION_CREDENTIALS")
if not KEY_PATH or not isinstance(KEY_PATH, str):
    raise ValueError(
        "GOOGLE_APPLICATION_CREDENTIALS 환경변수가 비어 있거나 문자열이 아닙니다. .env 파일과 경로를 확인하세요."
    )
os.environ["GOOGLE_APPLICATION_CREDENTIALS"] = KEY_PATH

# .env 경로 지정
dotenv_path = Path(__file__).resolve().parents[2] / ".env"
load_dotenv(dotenv_path)

GEMINI_ID = os.getenv("GEMINI_ID")

# Vertex AI 초기화 (프로젝트, 리전)
vertexai.init(project=GEMINI_ID, location="us-central1")

# Gemini 모델 인스턴스 미리 생성 (재사용)
# gemini-2.0-flash-lite-001:
# 이 모델은 Vertex AI의 Gemini 2.0 버전 중 경량화된 플래시 모델로,
# 빠른 응답과 낮은 리소스 사용을 목표로 설계되었습니다.
# 사용 사례: 간단한 텍스트 생성, 빠른 프로토타입 제작 등
# 자세한 내용은 Vertex AI 공식 문서를 참조하세요.
model = GenerativeModel("gemini-2.0-flash-lite-001")

def call_gemini_api(prompt_text: str) -> str:
    """
    Gemini API를 호출하여 프롬프트에 대한 응답을 문자열로 반환합니다.
    """
    response = model.generate_content(prompt_text)
    return response.text