from fastapi import FastAPI
# Depends, HTTPException
# from sqlalchemy.orm import Session
# from fastapi.middleware.cors import CORSMiddleware

from app.api import llm_api

# uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload

app = FastAPI()

@app.get("/")
def root():
    return {"message": "서버 정상 작동 중"}

app.include_router(llm_api.router, prefix="/api")








