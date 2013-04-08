package com.yyxu.download.error;
public class ErrorInfo {
private String ErrorCode;
private String ErrorName;
private String ErrorMsg;
public ErrorInfo(String ErrorCode,String ErrorName,String ErrorMsg){
this.ErrorCode = ErrorCode;
this.ErrorName = ErrorName;
this.ErrorMsg = ErrorMsg;
}
public String getErrorCode() {
	return ErrorCode;
}
public String getErrorName() {
	return ErrorName;
}
public String getErrorMsg() {
	return ErrorMsg;
}

}