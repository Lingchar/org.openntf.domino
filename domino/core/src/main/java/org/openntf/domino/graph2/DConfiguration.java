package org.openntf.domino.graph2;

import java.io.Externalizable;
import java.util.Map;

import org.openntf.domino.graph2.impl.DGraph;

import com.tinkerpop.frames.EdgeFrame;
import com.tinkerpop.frames.VertexFrame;
import com.tinkerpop.frames.modules.Module;
import com.tinkerpop.frames.modules.typedgraph.TypeManager;
import com.tinkerpop.frames.modules.typedgraph.TypeRegistry;

public interface DConfiguration extends Externalizable {

	public static interface IExtConfiguration {
		public void extendConfiguration(DConfiguration config);
	}

	public Map<Class<?>, Long> getTypeMap();

	public Map<Long, DElementStore> getElementStores();

	public DElementStore addElementStore(DElementStore store);

	public void addKeyResolver(DKeyResolver resolver);

	public DKeyResolver getKeyResolver(Class<?> type);

	public DGraph getGraph();

	public Module getModule();

	public TypeRegistry getTypeRegistry();

	public TypeManager getTypeManager();

	public DGraph setGraph(DGraph graph);

	public DElementStore getDefaultElementStore();

	public void setDefaultElementStore(Long key);

	public void setDefaultElementStore(DElementStore store);

	public DElementStore getDefaultProxyStore();

	public void setDefaultProxyStore(Long key);

	public void setDefaultProxyStore(DElementStore store);

	public boolean isSuppressSingleValueCategories();

	public void setSuppressSingleValueCategories(boolean value);

	public Class<?> getDefaultVertexFrameType();

	public void setDefaultVertexFrameType(Class<? extends VertexFrame> clazz);

	public Class<?> getDefaultEdgeFrameType();

	public void setDefaultEdgeFrameType(Class<? extends EdgeFrame> clazz);

	void setDefaultReverseProxyStore(Long key);

	void setDefaultReverseProxyStore(DElementStore store);

	DElementStore getDefaultReverseProxyStore();

}
