package com.sipomeokjo.commitme.api.response;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode implements ResponseCode {
 
	// 유저
	USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "존재하지 않는 회원입니다."),
	
	// 채팅
	CHAT_MESSAGE_INVALID(HttpStatus.BAD_REQUEST, "CHAT_MESSAGE_INVALID", "메시지 또는 첨부파일이 필요합니다."),
	CHATROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "CHATROOM_NOT_FOUND", "채팅방을 찾을 수 없습니다."),
	
	// 업로드
	UPLOAD_STATUS_INVALID(HttpStatus.CONFLICT, "UPLOAD_STATUS_INVALID", "업로드 상태가 올바르지 않습니다."),
	UPLOAD_NOT_FOUND(HttpStatus.NOT_FOUND, "UPLOAD_NOT_FOUND", "업로드 정보를 찾을 수 없습니다."),
	UPLOAD_OBJECT_NOT_FOUND(
			HttpStatus.NOT_FOUND, "UPLOAD_OBJECT_NOT_FOUND", "S3에 업로드된 객체를 찾을 수 없습니다."),
	UPLOAD_PURPOSE_INVALID(HttpStatus.BAD_REQUEST, "UPLOAD_PURPOSE_INVALID", "업로드 목적이 올바르지 않습니다."),
	UPLOAD_CONTENT_TYPE_NOT_ALLOWED(
			HttpStatus.BAD_REQUEST, "UPLOAD_CONTENT_TYPE_NOT_ALLOWED", "허용되지 않은 Content-Type입니다."),
	UPLOAD_EXTENSION_NOT_ALLOWED(
			HttpStatus.BAD_REQUEST, "UPLOAD_EXTENSION_NOT_ALLOWED", "허용되지 않은 파일 확장자입니다."),
	UPLOAD_FILE_SIZE_EXCEEDED(
			HttpStatus.BAD_REQUEST, "UPLOAD_FILE_SIZE_EXCEEDED", "파일 크기 제한을 초과했습니다."),
	UPLOAD_FILE_SIZE_MISMATCH(
			HttpStatus.BAD_REQUEST,
			"UPLOAD_FILE_SIZE_MISMATCH",
			"요청한 파일 크기와 업로드된 파일 크기가 일치하지 않습니다."),
	UPLOAD_ETAG_MISMATCH(HttpStatus.BAD_REQUEST, "UPLOAD_ETAG_MISMATCH", "ETag가 일치하지 않습니다."),
	UPLOAD_FORBIDDEN(HttpStatus.FORBIDDEN, "UPLOAD_FORBIDDEN", "해당 업로드에 접근할 권한이 없습니다."),
	UPLOAD_S3_ERROR(HttpStatus.SERVICE_UNAVAILABLE, "UPLOAD_S3_ERROR", "S3 처리 중 오류가 발생했습니다."),
	
	// 페이지네이션
    INVALID_CURSOR_VALUE(HttpStatus.BAD_REQUEST, "INVALID_CURSOR_VALUE", "커서 값이 유효하지 않거나 손상되었습니다."),
    INVALID_KEYWORD_VALUE(
            HttpStatus.BAD_REQUEST, "INVALID_KEYWORD_VALUE", "검색어 값이 유효하지 않거나 손상되었습니다."),
    
	//전역
	BAD_REQUEST(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "잘못된 요청입니다."),
	METHOD_NOT_ALLOWED(
			HttpStatus.METHOD_NOT_ALLOWED, "METHOD_NOT_ALLOWED", "허용되지 않은 HTTP METHOD입니다."),
	UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "AUTH_UNAUTHORIZED", "인증이 필요합니다."),
	REFRESH_TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "REFRESH_TOKEN_INVALID", "리프레시 토큰이 유효하지 않습니다."),
	FORBIDDEN(HttpStatus.FORBIDDEN, "AUTH_FORBIDDEN", "접근 권한이 없습니다."),
	INTERNAL_SERVER_ERROR(
			HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다."),
	SERVICE_UNAVAILABLE(
            HttpStatus.SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", "일시적으로 서비스를 사용할 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus httpStatus, String code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }
}
