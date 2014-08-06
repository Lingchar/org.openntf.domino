package org.openntf.domino.xots;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import org.openntf.domino.helpers.TrustedDispatcher;
import org.openntf.domino.thread.DominoExecutor.ThreadCleaner;
import org.openntf.domino.thread.DominoManualRunner;
import org.openntf.domino.thread.DominoNoneRunner;
import org.openntf.domino.thread.DominoSessionType;
import org.openntf.domino.thread.DominoThread;
import org.openntf.domino.thread.DominoThreadFactory;
import org.openntf.domino.thread.model.IDominoRunnable;
import org.openntf.domino.xots.builtin.XotsNsfScanner;

import com.ibm.designer.runtime.domino.adapter.HttpService;
import com.ibm.designer.runtime.domino.adapter.LCDEnvironment;

/*
 * This class and package is intended to become the space for the XPages implementation
 * of IBM's DOTS. Except it will use modern thread management instead of acting like it was
 * written in Java 1.1
 */
public class XotsDaemon extends TrustedDispatcher implements Observer {
	private static XotsDaemon INSTANCE;
	private XotsService xotsService_;
	private Set<Class<?>> taskletClasses_;
	private Set<XotsBaseTasklet> tasklets_;

	protected class XotsExecutor extends TrustedDispatcher.TrustedExecutor {
		protected XotsExecutor(final TrustedDispatcher dispatcher) {
			super(dispatcher, new XotsThreadFactory());
		}

		@Override
		public void execute(final Runnable runnable) {
			if (!(runnable instanceof IXotsRunner)) {
				System.out.println("DEBUG: XotsExecutor has been asked to execute a " + runnable.getClass().getName());
			}
			super.execute(runnable);
		}

		@Override
		protected void beforeExecute(final Thread t, final Runnable r) {
			Runnable run = r;
			if (r instanceof TrustedRunnable) {
				run = ((TrustedRunnable) r).getRunnable();
			}
			if (run instanceof IXotsRunner) {
				ClassLoader loader = ((IXotsRunner) run).getContextClassLoader();
				if (loader != null) {
					t.setContextClassLoader(loader);
				}
			} else {
				System.out.println("DEBUG: XotsDaemon is running a " + run.getClass().getName());
			}
		}

		@Override
		protected void afterExecute(final Runnable r, final Throwable t) {

		}
	}

	protected class XotsThreadFactory extends DominoThreadFactory {
		public XotsThreadFactory() {

		}

		@Override
		protected DominoThread makeThread(final Runnable runnable) {
			return new XotsThread(runnable);
		}
	}

	protected class XotsThread extends DominoThread {
		public XotsThread(final Runnable runnable) {
			super(runnable);
		}
	}

	private XotsDaemon() {
		super();
		LCDEnvironment env = LCDEnvironment.getInstance();
		List<HttpService> services = env.getServices();
		xotsService_ = new XotsService(env);
		services.add(xotsService_);

		XotsNsfScanner scanner = new XotsNsfScanner("");
		scanner.addObserver(this);
		Thread t = new lotus.domino.NotesThread(scanner);
		t.start();
	}

	public synchronized static void stop() {
		ThreadCleaner.INSTANCE.clean();
	}

	public synchronized static void addToQueue(final Runnable runnable) {
		try {
			AccessController.doPrivileged(new PrivilegedAction<Object>() {
				@Override
				public Object run() {
					getInstance().queue(runnable, runnable.getClass().getClassLoader());
					return null;
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public synchronized static XotsDaemon getInstance() {
		if (null == INSTANCE) {
			INSTANCE = new XotsDaemon();
		}
		return INSTANCE;
	}

	@Override
	protected TrustedExecutor getExecutor() {
		if (intimidator_ == null) {
			intimidator_ = new XotsExecutor(this);
		}
		return intimidator_;
	}

	public void scan(final String serverName) {
		XotsNsfScanner scanner = new XotsNsfScanner(serverName);
		scanner.addObserver(this);
		scanner.scan();
	}

	public void queue(final Runnable runnable) {
		queue(runnable, runnable.getClass().getClassLoader());
	}

	public void queue(final Runnable runnable, final ClassLoader loader) {
		ClassLoader localLoader = loader == null ? runnable.getClass().getClassLoader() : loader;
		if (runnable instanceof IDominoRunnable) {
			DominoSessionType type = ((IDominoRunnable) runnable).getSessionType();
			if (type == DominoSessionType.NAMED) {
				XotsNamedRunner runner = new XotsNamedRunner(runnable, localLoader);
				super.process(runner);
			} else if (type == DominoSessionType.NATIVE) {
				XotsNativeRunner runner = new XotsNativeRunner(runnable, localLoader);
				super.process(runner);
			} else if (type == DominoSessionType.MANUAL) {
				DominoManualRunner runner = new DominoManualRunner(runnable, localLoader);
				super.process(runner);
			} else if (type == DominoSessionType.NONE) {
				DominoNoneRunner runner = new DominoNoneRunner(runnable, localLoader);
				super.process(runner);
			}
		} else {
			DominoNoneRunner runner = new DominoNoneRunner(runnable, loader);
			super.process(runner);
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void update(final Observable arg0, final Object arg1) {
		if (arg0 instanceof XotsNsfScanner) {
			if (arg1 instanceof Set) {
				taskletClasses_ = (Set) arg1;
			}
		}
	}

}
