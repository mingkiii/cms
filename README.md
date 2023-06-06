## 개요
간단한 커머스 프로젝트

Use : Spring, Jpa, Mysql, Redis, Docker, AWS

목표 : 판매자와 구매자 사이를 중개하는 커머스 서버를 구축한다.

## 회원 api
### 공통
- [x] 이메일을 통해 인증번호로 회원가입

### 고객
- [x] 회원 가입
- [x] 인증 (이메일)
- [x] 로그인 토큰 발행
- [x] 로그인 토큰을 통한 제어 확인 (JWT, Filter를 사용)
- [x] 예치금 관리

### 판매자
- [x] 회원 가입
- [x] 인증 (이메일)
- [x] 로그인 토큰 발행
- [x] 로그인 토큰을 통한 제어 확인 (JWT, Filter를 사용)

## 주문 api

### 고객
- [ ] 장바구니를 위한 Redis 연동
- [ ] 상품 겅색 & 상세 페이지
- [ ] 장바구니에 상품 추가
- [ ] 장바구니 목록 조회
- [ ] 주문하기
- [ ] 주문내역 이메일로 발송하기

### 판매자
- [x] 상품 등록, 수정
- [x] 상품 삭제