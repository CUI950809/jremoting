package com.github.jremoting.dispatcher;

import com.github.jremoting.core.ChannelBuffer;
import com.github.jremoting.core.Invocation;
import com.github.jremoting.core.InvocationHolder;
import com.github.jremoting.core.InvocationResult;
import com.github.jremoting.core.Protocal;

public class Protocals implements Protocal {
	private final Protocal[]  protocals;
	public Protocals(Protocal[]  protocals) {
		this.protocals = protocals;
	}
	@Override
	public boolean writeRequest(Invocation invocation, ChannelBuffer buffer) {
		for (Protocal protocal : protocals) {
			if(protocal.writeRequest(invocation, buffer)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public InvocationResult readResponse(InvocationHolder holder,
			ChannelBuffer buffer) {
		for (Protocal protocal : protocals) {
			InvocationResult result = protocal.readResponse(holder, buffer);
			if(result != null) {
				return result;
			}
		}
		
		return null;
	}

	@Override
	public Invocation readRequest(ChannelBuffer buffer) {
		for (Protocal protocal : protocals) {
			Invocation invocation = protocal.readRequest(buffer);
			if(invocation != null) {
				return invocation;
			}
		}
		return null;
	}

	@Override
	public boolean writeResponse(InvocationResult invocationResult,
			ChannelBuffer buffer) {
		for (Protocal protocal : protocals) {
			if(protocal.writeResponse(invocationResult, buffer)) {
				return true;
			}
		}
		return false;
	}

}
