✨ FITTIM = “남들 말고, 진짜 나를 입는 시간 10초”

Front + Back 모두 함께 동작할 때 완전한 서비스가 됩니다.

──────────────────────────────

🟥 FITTIM – Backend (Spring Boot) README 소개글

──────────────────────────────

🔧 FITTIM Backend (Spring Boot)

FITTIM은 미니멀/인스타 감성을 좋아하는 MZ세대를 위한
“내 옷장 기반 코디 추천 서비스”입니다.

이 레포는 해당 서비스의 **Spring Boot 기반 REST API 서버(Backend)**입니다.

🚀 주요 기능 (Backend)
🔐 1. 이메일 기반 회원가입 / 로그인

- Step 1: 이름 + 이메일 등록
- Step 2: 이메일 인증코드 발송 + 검증
- Step 3: 비밀번호 설정 → 회원가입 완료
- 로그인(JWT Access Token 발급)
- Spring Security + JWT 기반 인증

👚 2. 옷장(Wardrobe) 기능

- 옷 추가 (카테고리/색상/계절/스타일)
- 이미지 업로드 (Multipart → 로컬 저장 or Static 폴더)
- 옷 리스트 조회 / 삭제
- 옷 정보 관리

👔 3. FIT 추천 기능

- 장소/기분/계절 기반 코디 추천 API
- 옷장 아이템 조합으로 3개의 FIT 생성
- FIT 히스토리 저장 및 조회

🛍 4. 쇼핑 추천 (확장)
- 태그 기반 추천 엔진(미래 업데이트)

🧩 Backend 기술 스택
Language & Framework

- Java 17
- Spring Boot 3.2.0
- Spring Web
- Spring Security
- Spring Data JPA
- Authentication
- JWT 기반 인증/인가
- 이메일 인증 코드 시스템

Database

- H2 Database (개발용)
- PostgreSQL (배포용 예정)

Storage

- 로컬 디렉토리 이미지 저장
- Static Resource Handler로 URL 변환

Build & Packaging

- Gradle
- Dockerfile 포함 (개발/배포용)

🗂 Backend 패키지 구조
```
com.fittim.backend
 ├── auth
 │     ├── controller
 │     ├── service
 │     ├── dto
 │     ├── entity
 ├── user
 ├── wardrobe
 ├── fit
 ├── shop
 ├── common
 │     ├── ApiResponse
 │     ├── exception
 └── config
       ├── SecurityConfig
       ├── JwtProvider
       ├── CorsConfig
```

🔗 Frontend 연동

이 서버는
👉 Flutter 기반 FITTIM Frontend
에서 호출되며,

- 회원가입/로그인
- 옷장 관리

FIT 추천
모든 API를 제공하는 핵심 서비스입니다.

✨ FITTIM Backend = MZ 패션 코디 추천 엔진의 심장부

안정적인 서버가 있어야 감성 코디 추천이 완성됩니다.

──────────────────────────────
