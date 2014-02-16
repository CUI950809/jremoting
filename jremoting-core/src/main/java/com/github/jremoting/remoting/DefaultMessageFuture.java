package com.github.jremoting.remoting;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.github.jremoting.core.Invoke;
import com.github.jremoting.core.MessageFuture;
import com.github.jremoting.exception.RemotingException;

public class DefaultMessageFuture implements MessageFuture {
	
	private final Invoke invoke;
	private volatile Object result;
	
	private static final Object CANCEL = new Object();
	private static final Object NULL = new Object();
	private final CountDownLatch latch = new CountDownLatch(1);

	private final long startTime = System.currentTimeMillis();
	
	private volatile Runnable listener;
	private volatile Executor executor;
	
	

	public DefaultMessageFuture(Invoke invoke) {
		this.invoke = invoke;
		this.invoke.setResultFuture(this);
	}
	
	public boolean isTimeout() {
		long now = System.currentTimeMillis();
		if(now - startTime > invoke.getTimeout()) {
			return true;
		}
		return false;
	}

	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		result = CANCEL;
		latch.countDown();
		return true;
	}

	@Override
	public Object get() throws InterruptedException, ExecutionException {
		
		latch.await();
	
		if(result instanceof RuntimeException) {
			throw (RuntimeException)result;
		}
		else if(result instanceof Throwable) {
			throw new RemotingException((Throwable)result);
		}
		return result;
	}

	@Override
	public Object get(long timeout, TimeUnit unit) throws InterruptedException,
			ExecutionException, TimeoutException {
		boolean success = latch.await(timeout, unit);
		if(!success) {
			throw new TimeoutException();
		}
		
		if(result instanceof RuntimeException) {
			throw (RuntimeException)result;
		}
		else if(result instanceof Throwable) {
			throw new RemotingException((Throwable)result);
		}
		
		return result;
	}

	@Override
	public boolean isCancelled() {
		return result == CANCEL;
	}

	@Override
	public boolean isDone() {
		return result != null;
	}

	@Override
	public void setListener(Runnable listener, Executor executor) {
		this.executor = executor;
		this.listener = listener;
	}

	public void setResult(Object result) {
		this.result = (result == null? NULL : result);
		latch.countDown();
	}
	
	public void onResult(final Object result) {
		if(invoke.isAsync()) {
			invoke.getCallbackExecutor().execute(new Runnable() {
				@Override
				public void run() {
					Invoke invoke = DefaultMessageFuture.this.invoke;
					invoke.getTailInvokeFilter().endInvoke(invoke, result);
					if(listener != null) {
						listener.run();
					}
				}
			});
		}
		else {
			this.setResult(result);
			if(listener != null) {
				this.executor.execute(listener);
			}
		}

	}

	public Invoke getInvoke() {
		return invoke;
	}

}
