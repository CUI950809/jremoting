package com.github.jremoting.core;

public interface Invocation {
	Object[] getArgs();
	Class<?> getReturnType();
	String getServiceName();
	String getServiceVersion();
	String getMethodName();
	String getRemoteAddress();
	void setRemoteAddress(String address);
	long getInvocationId();
	void setInvocationId(long id);
	Protocal getProtocal();
	int getSerializerId();
	
}
