package com.github.jremoting.invoke;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.github.jremoting.core.AbstractInvokeFilter;
import com.github.jremoting.core.Invoke;
import com.github.jremoting.core.InvokeFilter;
import com.github.jremoting.core.MessageFuture;
import com.github.jremoting.core.MessageChannel;
import com.github.jremoting.exception.RemotingException;
import com.github.jremoting.util.concurrent.ListenableFuture;

public class ClientInvokeFilterChain {

	private final InvokeFilter head;
	
	private final InvokeFilter tail;
	
	public ClientInvokeFilterChain(MessageChannel messageChannel, List<InvokeFilter> invokeFilters) {
		
		List<InvokeFilter> filters = new ArrayList<InvokeFilter>(invokeFilters.size() + 2);
		this.head = new ClientHeadInvokeFilter();
		filters.add(head);
		
		for (InvokeFilter invokeFilter : invokeFilters) {
			filters.add(invokeFilter);
		}
		this.tail = new ClientTailInvokeFilter(messageChannel);
		filters.add(this.tail);
		
		InvokeFilterUtil.link(filters);
	}

	public Object invoke(Invoke invoke) {
		return this.head.invoke(invoke);
	}
	
	
	public ListenableFuture<Object> beginInvoke(Invoke invoke) {
		invoke.setTailInvokeFilter(this.tail);
		return this.head.beginInvoke(invoke);
	}

	
	public void endInvoke(Invoke invoke, Object result) {
		this.tail.endInvoke(invoke, result);
	}
	
	public static class ClientHeadInvokeFilter extends AbstractInvokeFilter {
		@Override
		public void endInvoke(Invoke invoke, Object result) {
			invoke.getResultFuture().setResult(result);
		}
	}
	private static class ClientTailInvokeFilter extends AbstractInvokeFilter {
		
		private static final long DEFAULT_TIMEOUT = 60*1000*5; //default timeout 5 mins
		
		public ClientTailInvokeFilter(MessageChannel messageChannel) {
			this.messageChannel = messageChannel;
		}
		private final MessageChannel messageChannel;
		@Override
		public Object invoke(Invoke invoke) {
			
			if(invoke.getTimeout() <= 0) {
				invoke.setTimeout(DEFAULT_TIMEOUT);
			}
			
			MessageFuture future = messageChannel.send(invoke);
			
			try {
				
				return future.get(invoke.getTimeout(), TimeUnit.MILLISECONDS);
				
			} catch (InterruptedException e) {
				throw new RemotingException(e);
			} catch (ExecutionException e) {
				throw new RemotingException(e);
			} catch (TimeoutException e) {
				throw new com.github.jremoting.exception.TimeoutException("invoke time out timeout:" + invoke.getTimeout());
			}
		}
		
		@Override
		public ListenableFuture<Object> beginInvoke(Invoke invoke) {
			
			if(invoke.getTimeout() <= 0) {
				invoke.setTimeout(DEFAULT_TIMEOUT);
			}
			
			MessageFuture future = messageChannel.send(invoke);
			//one way message return null
			if(future == null) {
				return null;
			}
			
			
			future.setListener(invoke.getCallback(),invoke.getCallbackExecutor());

			return future;

		}
	}
}
