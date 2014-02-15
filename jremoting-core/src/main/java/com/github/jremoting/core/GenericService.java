package com.github.jremoting.core;

import java.util.concurrent.Executor;
import java.util.concurrent.Future;

import com.github.jremoting.core.ServiceParticipantInfo.ParticipantType;
import com.github.jremoting.util.NetUtil;

public class GenericService  {
	 
	private final String interfaceName;
	private final String version;
	private final RpcClient rpcClient;
	
	private long timeout;
	private Serializer serializer;
	private String address;
	private Executor callbackExecutor;

	public GenericService(String interfaceName, String version, RpcClient rpcClient) {
		this.interfaceName = interfaceName;
		this.version = version;
		this.rpcClient = rpcClient;
	}
	public Object invoke(String methodName, 
			String[] parameterTypeNames, Object[] args) {
		Invoke invoke = createInvoke(methodName, parameterTypeNames, args);
		return rpcClient.invoke(invoke);
	}

	
	public Future<?> $invoke(String methodName, String[] parameterTypeNames, Object[] args) {
		Invoke invoke = createInvoke(methodName, parameterTypeNames, args);
		invoke.setAsync(true);
		return (Future<?>)rpcClient.invoke(invoke);
	}
	
	public Future<?> $invoke(String methodName, String[] parameterTypeNames, Object[] args,
			Runnable callback) {
		
		Invoke invoke = createInvoke(methodName, parameterTypeNames, args);
		invoke.setAsync(true);
		invoke.setCallback(callback);
		invoke.setCallbackExecutor(this.callbackExecutor);
		
		return (Future<?>)rpcClient.invoke(invoke);
	}
	
	public Future<?> $invoke(String methodName, String[] parameterTypeNames, Object[] args
			,Executor executor ,Runnable callback) {
		Invoke invoke = createInvoke(methodName, parameterTypeNames, args);
		invoke.setAsync(true);
		invoke.setCallback(callback);
		invoke.setCallbackExecutor(executor);
		
		return (Future<?>)rpcClient.invoke(invoke);
	}
	
	public GenericService start() {
		this.rpcClient.register(new ServiceParticipantInfo(this.interfaceName + ":" + this.version , NetUtil.getLocalHost(), ParticipantType.CONSUMER));
		return this;
	}
	
	private Invoke createInvoke(String methodName, String[] parameterTypeNames,
			Object[] args) {
		Invoke invoke = new Invoke(interfaceName, version, methodName, serializer, args, parameterTypeNames);
		invoke.setTimeout(timeout);
		invoke.setRemoteAddress(address);
		return invoke;
	}

	public long getTimeout() {
		return timeout;
	}

	public GenericService setTimeout(long timeout) {
		this.timeout = timeout;
		return this;
	}
	public String getVersion() {
		return version;
	}


	public RpcClient getRpcClient() {
		return rpcClient;
	}
	public Serializer getSerializer() {
		return serializer;
	}

	public void setSerializer(Serializer serializer) {
		this.serializer = serializer;
	}

	public String getAddress() {
		return address;
	}

	public GenericService setAddress(String address) {
		this.address = address;
		return this;
	}

	public String getInterfaceName() {
		return interfaceName;
	}
	public Executor getCallbackExecutor() {
		return callbackExecutor;
	}
	public GenericService setCallbackExecutor(Executor callbackExecutor) {
		this.callbackExecutor = callbackExecutor;
		return this;
	}

}
