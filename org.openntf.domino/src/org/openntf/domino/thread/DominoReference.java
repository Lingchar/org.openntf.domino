/*
 * Copyright OpenNTF 2013
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at:
 * 
 * http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing 
 * permissions and limitations under the License.
 */
package org.openntf.domino.thread;

import java.lang.ref.PhantomReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openntf.domino.Base;
import org.openntf.domino.utils.Factory;

// TODO: Auto-generated Javadoc
/**
 * The Class DominoReference.
 */
public class DominoReference extends PhantomReference<org.openntf.domino.Base<?>> {
	/** The Constant log_. */
	private static final Logger log_ = Logger.getLogger(DominoReference.class.getName());

	/** The delegate_. */
	private final lotus.domino.Base delegate_;
	/** The delegate type_. */
	private final Class<?> delegateType_;
	private final long delegateId_;
	private final int referrantHash_;
	private final int referrantId_;

	private static long watchedCpp = 0l;

	/**
	 * Instantiates a new domino reference.
	 * 
	 * @param r
	 *            the r
	 * @param q
	 *            the q
	 * @param delegate
	 *            the delegate
	 */
	public DominoReference(Base<?> r, DominoReferenceQueue q, lotus.domino.Base delegate) {
		super(r, q);
		delegate_ = delegate; // Because the reference separately contains a pointer to the delegate object, it's still available even
								// though the wrapper is null
		if (r instanceof org.openntf.domino.impl.Base) {
			delegateId_ = org.openntf.domino.impl.Base.getDelegateId((org.openntf.domino.impl.Base) r);
		} else {
			delegateId_ = org.openntf.domino.impl.Base.getLotusId((lotus.domino.local.NotesBase) delegate);
		}
		if (log_.isLoggable(Level.FINE)) {
			delegateType_ = delegate.getClass();
			referrantHash_ = r.hashCode();
			referrantId_ = System.identityHashCode(r);
		} else {
			delegateType_ = null;
			referrantHash_ = 0;
			referrantId_ = 0;
		}
	}

	public boolean isTraceTarget() {
		return delegateId_ == watchedCpp;
	}

	public Long getDelegateId() {
		return delegateId_;
	}

	public int _getReferrantHash() {
		return referrantHash_;
	}

	public int _getReferrantId() {
		return referrantId_;
	}

	/**
	 * Gets the type.
	 * 
	 * @return the type
	 */
	public Class<?> _getType() {
		return delegateType_;
	}

	/**
	 * Recycle.
	 */
	public void recycle() {
		org.openntf.domino.impl.Base.recycle(delegate_);
		int total = Factory.countAutoRecycle();
		if (log_.isLoggable(Level.FINE)) {
			if (total % 5000 == 0) {
				log_.log(Level.FINE, "Auto-recycled " + total + " references");
			}
		}
	}

}
