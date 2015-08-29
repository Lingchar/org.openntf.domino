package org.openntf.domino.graph2.impl;

import java.util.LinkedHashMap;
import java.util.Map;

import org.openntf.domino.ViewEntry;
import org.openntf.domino.big.NoteCoordinate;
import org.openntf.domino.big.ViewEntryCoordinate;
import org.openntf.domino.graph2.DGraph;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Vertex;

public class DEntryEdge extends DEdge {
	private static final long serialVersionUID = 1L;
	private DElementStore store_;

	public DEntryEdge(final DGraph parent, final ViewEntry delegate, final ViewEntryCoordinate id, final DElementStore store) {
		super(parent, delegate);
		delegateKey_ = id;
		store_ = store;
	}

	@Override
	public Object getVertexId(final Direction direction) {
		if (Direction.OUT.equals(direction)) {
			if (outKey_ == null) {
				String mid = ((org.openntf.domino.ViewEntry) getDelegate()).getMetaversalID();
				setOutId(NoteCoordinate.Utils.getNoteCoordinate(mid));
			}
		}
		return super.getVertexId(direction);
	}

	@Override
	public Vertex getVertex(final Direction direction) throws IllegalArgumentException {
		ViewEntry entry = (org.openntf.domino.ViewEntry) getDelegate();
		if (Direction.OUT.equals(direction) && entry.isCategory()) {
			Map<String, Object> delegateMap = new LinkedHashMap<String, Object>();
			delegateMap.put("value", entry.getCategoryValue());
			delegateMap.put("position", entry.getPosition());
			delegateMap.put("noteid", entry.getNoteID());
			DCategoryVertex result = new DCategoryVertex(getParent(), delegateMap);
			result.delegateKey_ = getVertexId(Direction.OUT);
			result.setView(entry.getParentView());
			return result;
		}
		return super.getVertex(direction);
	}

	//	public String getPriorCategories() {
	//		//TODO
	//		return null;
	//	}

}
