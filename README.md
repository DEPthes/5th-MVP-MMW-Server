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



## 외부 API 키 설정 안내 ( TTS / STT )

### 환경 변수

| Key              | 설명 | 발급처 |
|------------------|---|---|
| `GEMINI_API_KEY` | Gemini API 키 | Google AI Studio |
| `GCP_API_KEY`    | STT/TTS 공용 키 | GCP  |


### 1. IntelliJ에 환경 변수 등록

1. 상단 실행 설정 드롭다운 → `Edit Configurations...`
2. 해당 Application 설정 선택
3. `Environment variables` 항목 (안 보이면 `Modify options` → `Environment variables`)
4. 우측 아이콘 클릭 후 추가

```
GEMINI_API_KEY=발급받은_키
GCP_API_KEY=발급받은_키
```
해당 키 디스코드에 올려놨습니다.

5. `Apply` → `OK`

> 환경 변수가 없으면 실행 시 `Could not resolve placeholder 'GEMINI_API_KEY'` 오류 발생

### 2. 연결 확인

앱 실행 후 브라우저에서 접속

```
http://localhost:8080/gemini/test
```

Gemini 응답 텍스트가 표시되면 정상! 질문을 바꾸려면 `?prompt=질문내용`을 붙이면 됨.

STT/TTS는 아직 코드로 연결되지 않았으며, 터미널에서 확인할 수 있습니다(코드 완성 시 내용 바로 수정하겠습니다)

```bash
# TTS
curl -X POST -H "Content-Type: application/json" \
  -d '{"input":{"text":"테스트"},"voice":{"languageCode":"ko-KR","ssmlGender":"FEMALE"},"audioConfig":{"audioEncoding":"MP3"}}' \
  "https://texttospeech.googleapis.com/v1/text:synthesize?key=발급받은_키"
```

`audioContent`에 base64 문자열이 오면 정상

---

## 참고 사항

### Gemini 모델

`application.yaml`의 `gemini.model`은 **`gemini-flash-latest`** 를 사용

| 모델 | 결과 |
|---|---|
| `gemini-2.0-flash` | `429` — 무료 할당량 0 |
| `gemini-2.5-flash` | `404` — 신규 사용자 사용 불가 |
| `gemini-flash-latest` | 정상 |

사용 가능한 모델 목록은 아래로 조회할 수 있습니다.

```bash
curl "https://generativelanguage.googleapis.com/v1beta/models?key=발급받은_키"
```

### STT 오디오 형식

`LINEAR16` / `16000Hz` 조합에서 정상 동작을 확인했습니다. 요청 시 `config`에 파일의 실제 형식을 정확히 명시해야 합니다.

한국어 인식 시 **고유명사 정확도가 낮다** (테스트 시 "김우성" → "김무성", confidence 0.63)

### 주의


- 로컬 실행 시 **Docker Desktop이 실행 중**이어야 작동합니다.