# 인증·회원 API 구현 문서

API 명세서의 서버 기본 경로 `/api/v1`을 적용한다. 로컬 프로필의 기본 주소는
`http://localhost:8081`이다.

## 로컬 실행

로컬 프로필은 MySQL을 사용한다. 다음 환경 변수를 설정한 후 실행한다.

```powershell
$env:LOCAL_DB_USERNAME = "사용자명"
$env:LOCAL_DB_PASSWORD = "비밀번호"
$env:JWT_SECRET = "Base64로 인코딩한 32바이트 이상의 비밀키"
$env:GEMINI_API_KEY = "Gemini API 키"
.\gradlew.bat bootRun --args="--spring.profiles.active=local"
```

JWT 비밀키는 Base64 문자열이어야 하며, 운영 비밀값은 저장소에 커밋하지 않는다.

## 응답 형식

인증 API의 본문이 있는 성공 응답은 공통 Wrapper 없이 Response DTO를 직접 반환한다.
회원가입 성공과 같이 명세가 빈 본문을 지정한 경우에는 응답 본문을 반환하지 않는다.
오류 응답은 `code`, `message`와 필요한 경우 필드별 `errors`를 반환한다.

## 회원가입

`POST /api/v1/auth/signup`

```json
{
  "loginId": "member01",
  "email": "member@example.com",
  "password": "Password1!",
  "passwordConfirm": "Password1!",
  "name": "홍길동",
  "privacyAgreed": true
}
```

성공 시 HTTP `201 Created`와 빈 본문을 반환한다. 이메일은 소문자로 정규화하고,
비밀번호는 BCrypt로 해시하여 저장한다.

- 요청값 검증 실패: `400 Bad Request`
- 이메일 또는 아이디 중복: `409 Conflict`

## 로그인

`POST /api/v1/auth/login`

```json
{
  "loginId": "member01",
  "password": "Password1!"
}
```

성공 시 HTTP `200 OK`를 반환한다.

```json
{
  "accessToken": "...",
  "refreshToken": "..."
}
```

아이디가 없거나 비밀번호가 틀리면 모두 `401 Unauthorized`와
`INVALID_CREDENTIALS`를 반환한다. Refresh Token 원문은 응답으로 한 번만 전달하고,
서버에는 SHA-256 해시만 저장한다.

## 내 정보 조회

`GET /api/v1/users/me`

```http
Authorization: Bearer {accessToken}
```

성공 시 HTTP `200 OK`를 반환한다.

```json
{
  "success": true,
  "code": "SUCCESS",
  "message": "내 정보를 조회했습니다.",
  "data": {
    "id": 1,
    "loginId": "member01",
    "nickname": null,
    "desiredPosition": null
  }
}
```

서명, 만료 시각, issuer, `tokenType=ACCESS`, 사용자 존재 여부를 모두 검증한다.
토큰 누락·변조·만료, Refresh Token 사용, 존재하지 않는 사용자 토큰은 모두
`401 Unauthorized`와 `INVALID_TOKEN`을 반환한다.

## 토큰 재발급

`POST /api/v1/auth/reissue`

```json
{
  "refreshToken": "..."
}
```

서명, 만료, issuer, `tokenType=REFRESH`, DB에 저장된 SHA-256 해시와 사용자 ID를
검증한다. 성공하면 Access/Refresh Token을 모두 새로 발급하고 기존 Refresh Token은
즉시 무효화한다. 동시 재발급은 DB 잠금으로 직렬화하며 JWT의 `jti`로 매 발급 토큰의
고유성을 보장한다. 실패 시 `401`과 `INVALID_REFRESH_TOKEN`을 반환한다.

## 로그인 세션 확인

`GET /api/v1/auth/session`

유효한 Access Token이 필요하다. 성공 응답은 다음과 같다.

```json
{
  "authenticated": true,
  "userId": 1
}
```

## 로그아웃

`POST /api/v1/auth/logout`

유효한 Access Token이 필요하다. 서버에 저장된 Refresh Token을 삭제하고 `200 OK`
빈 본문을 반환한다. 현재 ERD에는 Access Token blacklist가 없으므로 클라이언트도
보유한 Access Token을 즉시 삭제해야 한다.

## 비밀번호 변경

`PATCH /api/v1/auth/password`

```json
{
  "currentPassword": "Password1!",
  "newPassword": "NewPassword2!",
  "newPasswordConfirm": "NewPassword2!"
}
```

현재 비밀번호와 새 비밀번호 확인을 검증하고 BCrypt 해시를 교체한다. 성공하면
저장된 Refresh Token을 폐기하며 `200 OK` 빈 본문을 반환한다.

## 프로필 수정

`PATCH /api/v1/users/me`

```json
{
  "nickname": "면접왕",
  "desiredPosition": "백엔드 개발자"
}
```

전달된 필드만 수정한다. 닉네임은 최대 50자, 희망 직무는 최대 100자이며 공백만
있는 값과 빈 요청은 허용하지 않는다. 현재 구현에서는 `null`로 기존 값을 삭제하는
기능을 지원하지 않는다.

## 회원 탈퇴

`DELETE /api/v1/users/me`

ERD에 따라 사용자 행을 hard delete하고 외래키 cascade로 약관, Refresh Token,
지원 프로필, 면접 및 피드백 데이터를 함께 삭제한다. 성공 시 `204 No Content`를
반환한다.

## 오류 상태

- 요청값 검증 실패: `400`
- 아이디·이메일 중복: `409`
- 인증 실패: `401`
- 권한 부족: `403`
- 없는 URL: `404`
- 지원하지 않는 메서드: `405`
- 예상하지 못한 오류: `500`

## Postman

`postman/MMW-Auth.postman_collection.json`을 Import하고 번호 순서로 실행한다.
로그인과 재발급 요청이 최신 Access/Refresh Token을 컬렉션 변수에 자동 저장한다.
