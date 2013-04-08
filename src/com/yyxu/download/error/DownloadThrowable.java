package com.yyxu.download.error;
public class DownloadThrowable extends Throwable{
	private ErrorInfo errorInfo;
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public DownloadThrowable(ErrorInfo message) {
		super(message.getErrorCode()+message.getErrorName()+message.getErrorMsg());
		this.errorInfo = message;
	}
	public ErrorInfo getErrorInfo() {
		return errorInfo;
	}
	
	
}