# MMW (Backend)

**WVW** 팀의 AI 모의 면접 서비스 **MMW** 백엔드.
사용자가 AI 면접관과 음성으로 질문·답변을 주고받으며 모의 면접을 연습하고,
말투·표정·시선 이탈 분석을 종합한 피드백을 받는 서비스.

## 기술 스택

| 구분 | 내용 |
| --- | --- |
| Language / Framework | Java 25, Spring Boot 4.0.7 |
| Build | Gradle (Wrapper) |
| DB | MySQL 8 (로컬은 Docker Compose) |
| Auth | JWT |
| AI / 외부 API | Gemini API, Google Cloud Speech-to-Text |

## 사전 준비

- JDK 25 (IntelliJ에 프로젝트 SDK로 설정)
- Docker / Docker Compose
- 전역 Gradle 설치 불필요 — 항상 `./gradlew` (Windows: `gradlew.bat`) 사용

## 환경 변수

로컬 DB 접속 값은 `application.yaml`에 기본값이 있어 별도 설정 없이 실행된다. 값을 바꾸려면 환경 변수로 덮어쓴다.

| Key | 설명 | 기본값 |
| --- | --- | --- |
| `DB_URL` | JDBC URL | `jdbc:mysql://localhost:3306/mmw` |
| `DB_USERNAME` | DB 사용자 | `mmw` |
| `DB_PASSWORD` | DB 비밀번호 | `mmw` |

> 인증 정보와 API Key(JWT·Gemini·Google 키)는 환경 변수로 주입한다.

## 실행

```bash
./gradlew bootRun        # Windows: gradlew.bat bootRun
curl http://localhost:8080/actuator/health   # {"status":"UP"}
```

`bootRun`은 Docker Compose 지원이 MySQL 컨테이너를 자동 기동·연결한다 (Docker Desktop 실행 필요).

빌드/테스트는 자동 기동 대상이 아니므로 DB 를 먼저 띄운다:

```bash
docker compose up -d
./gradlew build
```

## 패키지 구조

```
com.wvw.mmw
├── global/          공통 설정, 예외, 유틸
│   ├── config
│   ├── common       # 공통 컴포넌트
│   ├── exception
│   └── util
└── domain/
    └── {도메인}/     controller · service · repository · entity · dto
```

> `global` 하위 패키지와 `domain` 안의 도메인은 예시이며, 실제 패키지는 기획·도메인 확정 후 추가한다.

## Git 협업 규칙

- **GitHub Flow**: `main` + `feature/*`
- 작업은 `feature/*` 브랜치에서 → PR → 리뷰 후 `main` 병합
- `main` 직접 push 금지 (브랜치 보호 규칙 권장)