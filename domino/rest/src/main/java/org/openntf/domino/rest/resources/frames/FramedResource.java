package org.openntf.domino.rest.resources.frames;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.openntf.domino.big.NoteCoordinate;
import org.openntf.domino.big.ViewEntryCoordinate;
import org.openntf.domino.graph2.DGraphUtils;
import org.openntf.domino.graph2.DKeyResolver;
import org.openntf.domino.graph2.impl.DFramedTransactionalGraph;
import org.openntf.domino.rest.json.JsonGraphFactory;
import org.openntf.domino.rest.json.JsonGraphWriter;
import org.openntf.domino.rest.resources.AbstractResource;
import org.openntf.domino.rest.service.Headers;
import org.openntf.domino.rest.service.ODAGraphService;
import org.openntf.domino.rest.service.Parameters;
import org.openntf.domino.rest.service.Parameters.ParamMap;
import org.openntf.domino.rest.service.Routes;
import org.openntf.domino.types.CaseInsensitiveString;
import org.openntf.domino.utils.Factory;

import com.ibm.commons.util.io.json.JsonException;
import com.ibm.commons.util.io.json.JsonJavaObject;
import com.ibm.commons.util.io.json.JsonParser;
import com.ibm.domino.httpmethod.PATCH;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.EdgeFrame;
import com.tinkerpop.frames.VertexFrame;

@Path(Routes.ROOT + "/" + Routes.FRAMED + "/" + Routes.NAMESPACE_PATH_PARAM)
public class FramedResource extends AbstractResource {

	public FramedResource(final ODAGraphService service) {
		super(service);
	}

	@SuppressWarnings("unchecked")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getFramedObject(@Context final UriInfo uriInfo, @PathParam(Routes.NAMESPACE) final String namespace)
			throws JsonException, IOException {
		@SuppressWarnings("rawtypes")
		DFramedTransactionalGraph graph = this.getGraph(namespace);

		String jsonEntity = null;
		ResponseBuilder builder = null;
		ParamMap pm = Parameters.toParamMap(uriInfo);
		StringWriter sw = new StringWriter();
		JsonGraphWriter writer = new JsonGraphWriter(sw, graph, pm, false, true, false);

		try {
			if (pm.get(Parameters.ID) != null) {
				List<String> ids = pm.get(Parameters.ID);
				if (ids.size() == 0) {
					writer.outNull();
				} else if (ids.size() == 1) {
					String id = ids.get(0);
					NoteCoordinate nc = null;
					if (id.startsWith("E")) {
						nc = ViewEntryCoordinate.Utils.getViewEntryCoordinate(id);
					} else if (id.startsWith("V")) {
						nc = ViewEntryCoordinate.Utils.getViewEntryCoordinate(id);
					} else {
						nc = NoteCoordinate.Utils.getNoteCoordinate(id);
					}
					if (nc == null) {
						System.err.println("NoteCoordinate is null for id " + id);
					}
					if (graph == null) {
						System.err.println("Graph is null for namespace " + namespace);
					}
					Object elem = graph.getElement(nc, null);
					if (elem == null) {
						throw new IllegalStateException("Graph element is null for id " + id);
					}
					writer.outObject(elem);
				} else {
					List<Object> maps = new ArrayList<Object>();
					for (String id : ids) {
						NoteCoordinate nc = NoteCoordinate.Utils.getNoteCoordinate(id);
						maps.add(graph.getElement(nc, null));
					}
					writer.outArrayLiteral(maps);
				}
				jsonEntity = sw.toString();
			} else if (pm.getKeys() != null) {
				Class<?> type = null;
				if (pm.getTypes() != null) {
					List<CharSequence> types = pm.getTypes();
					String typename = types.get(0).toString();
					type = graph.getTypeRegistry().findClassByName(typename);
				}
				DKeyResolver resolver = graph.getKeyResolver(type);
				List<CharSequence> keys = pm.getKeys();
				if (keys.size() == 0) {
					writer.outNull();
				} else if (keys.size() == 1) {
					CharSequence id = keys.get(0);
					NoteCoordinate nc = resolver.resolveKey(type, URLDecoder.decode(id.toString(), "UTF-8"));
					if (nc == null) {
						System.err.println("NoteCoordinate is null for id " + id);
					}
					Object elem = graph.getElement(nc);
					if (elem == null) {
						elem = resolver.handleMissingKey(type, id);
						if (elem == null) {
							throw new IllegalStateException("Graph element is null for id " + id);
						}
					}
					if (elem instanceof Vertex) {
						// System.out.println("TEMP DEBUG Framing a vertex of type "
						// + elem.getClass().getName());
						Object vf = graph.frame((Vertex) elem, type);
						writer.outObject(vf);
					} else if (elem instanceof Edge) {
						Object ef = graph.frame((Edge) elem, type);
						writer.outObject(ef);
					}
				} else {
					List<Object> maps = new ArrayList<Object>();
					for (CharSequence id : keys) {
						NoteCoordinate nc = resolver.resolveKey(type, id);
						maps.add(graph.getElement(nc, null));
					}
					writer.outArrayLiteral(maps);
				}
				jsonEntity = sw.toString();

			} else {
				MultivaluedMap<String, String> mvm = uriInfo.getQueryParameters();
				for (String key : mvm.keySet()) {
					// System.out.println("TEMP DEBUG: " + key + ": " +
					// mvm.getFirst(key));
				}
				Map<String, Object> jsonMap = new LinkedHashMap<String, Object>();
				jsonMap.put("namespace", namespace);
				jsonMap.put("status", "active");
				writer.outObject(jsonMap);
			}
			builder = Response.ok();
		} catch (Exception e) {
			builder = Response.status(Status.INTERNAL_SERVER_ERROR);
			writer.outObject(e);
		}

		jsonEntity = sw.toString();
		builder.type(MediaType.APPLICATION_JSON_TYPE).entity(jsonEntity);
		Response response = builder.build();
		return response;
	}

	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response putDocumentByUnid(final String requestEntity, @Context final UriInfo uriInfo,
			@PathParam(Routes.NAMESPACE) final String namespace,
			@HeaderParam(Headers.IF_UNMODIFIED_SINCE) final String ifUnmodifiedSince) throws JsonException, IOException {
		ParamMap pm = Parameters.toParamMap(uriInfo);
		Response response = updateFrameByMetaid(requestEntity, namespace, ifUnmodifiedSince, pm, true);

		return response;
	}

	// @OPTIONS
	@PATCH
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response patchDocumentByUnid(final String requestEntity, @Context final UriInfo uriInfo,
			@PathParam(Routes.NAMESPACE) final String namespace,
			@HeaderParam(Headers.IF_UNMODIFIED_SINCE) final String ifUnmodifiedSince) throws JsonException, IOException {
		ParamMap pm = Parameters.toParamMap(uriInfo);
		Response response = updateFrameByMetaid(requestEntity, namespace, ifUnmodifiedSince, pm, false);
		return response;
	}

	protected Response updateFrameByMetaid(final String requestEntity, final String namespace, final String ifUnmodifiedSince,
			final ParamMap pm, final boolean isPut) throws JsonException, IOException {
		Response result = null;
		DFramedTransactionalGraph<?> graph = this.getGraph(namespace);
		JsonJavaObject jsonItems = null;
		List<Object> jsonArray = null;
		ResponseBuilder builder = null;
		JsonGraphFactory factory = JsonGraphFactory.instance;
		StringWriter sw = new StringWriter();
		JsonGraphWriter writer = new JsonGraphWriter(sw, graph, pm, false, true, false);

		try {
			StringReader reader = new StringReader(requestEntity);
			try {
				Object jsonRaw = JsonParser.fromJson(factory, reader);
				if (jsonRaw instanceof JsonJavaObject) {
					jsonItems = (JsonJavaObject) jsonRaw;
				} else if (jsonRaw instanceof List) {
					jsonArray = (List) jsonRaw;
				}
				builder = Response.ok();
			} catch (Throwable t) {
				builder = Response.status(Status.INTERNAL_SERVER_ERROR);
				writer.outObject(t);
			} finally {
				reader.close();
			}
		} catch (Exception ex) {
			writer.outObject(ex);
			builder = Response.status(Status.INTERNAL_SERVER_ERROR);
		}
		if (jsonArray != null) {
			writer.startArray();
			for (Object raw : jsonArray) {
				if (raw instanceof JsonJavaObject) {
					writer.startArrayItem();
					processJsonUpdate((JsonJavaObject) raw, graph, writer, pm, isPut);
					writer.endArrayItem();
				}
			}
			writer.endArray();
		} else if (jsonItems != null) {
			processJsonUpdate(jsonItems, graph, writer, pm, isPut);
		}

		builder.type(MediaType.APPLICATION_JSON_TYPE).entity(sw.toString());

		result = builder.build();

		return result;
	}

	private void processJsonUpdate(final JsonJavaObject jsonItems, final DFramedTransactionalGraph graph, final JsonGraphWriter writer,
			final ParamMap pm, final boolean isPut) throws JsonException, IOException {
		Map<CaseInsensitiveString, Object> cisMap = new HashMap<CaseInsensitiveString, Object>();
		for (String jsonKey : jsonItems.keySet()) {
			CaseInsensitiveString cis = new CaseInsensitiveString(jsonKey);
			cisMap.put(cis, jsonItems.get(jsonKey));
		}
		List<String> ids = pm.get(Parameters.ID);
		if (ids.size() == 0) {
			// TODO no id
		} else {
			JsonFrameAdapter adapter = null;
			for (String id : ids) {
				NoteCoordinate nc = NoteCoordinate.Utils.getNoteCoordinate(id);
				Object element = graph.getElement(nc, null);
				if (element instanceof EdgeFrame) {
					adapter = new JsonFrameAdapter(graph, (EdgeFrame) element, null, false);
				} else if (element instanceof VertexFrame) {
					adapter = new JsonFrameAdapter(graph, (VertexFrame) element, null, false);
				} else if (element == null) {
					throw new RuntimeException("Cannot force a metaversalid through REST API: " + id);
				} else {
					throw new RuntimeException("TODO"); // TODO
				}
				Iterator<String> frameProperties = adapter.getJsonProperties();
				while (frameProperties.hasNext()) {
					CaseInsensitiveString key = new CaseInsensitiveString(frameProperties.next());
					if (!key.startsWith("@")) {
						Object value = cisMap.get(key);
						if (value != null) {
							// if ("fullname".equals(key)) {
							// System.out.println("TEMP DEBUG fullname: " +
							// value);
							// }
							adapter.putJsonProperty(key.toString(), value);
							cisMap.remove(key);
						} else if (isPut) {
							adapter.putJsonProperty(key.toString(), value);
						}
					}
				}
				for (CaseInsensitiveString cis : cisMap.keySet()) {
					if (!cis.startsWith("@")) {
						Object value = cisMap.get(cis);
						if (value != null) {
							adapter.putJsonProperty(cis.toString(), value);
						}
					}
				}
				writer.outObject(element);
			}
			graph.commit();
		}
	}

	@DELETE
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@SuppressWarnings("rawtypes")
	public Response deleteFramedObject(final String requestEntity, @Context final UriInfo uriInfo,
			@PathParam(Routes.NAMESPACE) final String namespace) throws JsonException, IOException {
		DFramedTransactionalGraph graph = this.getGraph(namespace);
		String jsonEntity = null;
		ResponseBuilder builder = null;
		ParamMap pm = Parameters.toParamMap(uriInfo);
		StringWriter sw = new StringWriter();
		JsonGraphWriter writer = new JsonGraphWriter(sw, graph, pm, false, true, false);

		JsonGraphFactory factory = JsonGraphFactory.instance;

		Map<String, String> report = new HashMap<String, String>();

		List<String> ids = pm.get(Parameters.ID);
		if (ids.size() == 0) {
			// TODO no id
		} else {
			for (String id : ids) {
				try {
					NoteCoordinate nc = NoteCoordinate.Utils.getNoteCoordinate(id);
					Object element = graph.getElement(nc, null);
					if (element instanceof Element) {
						((Element) element).remove();
					} else if (element instanceof VertexFrame) {
						((VertexFrame) element).asVertex().remove();
					} else if (element instanceof EdgeFrame) {
						((EdgeFrame) element).asEdge().remove();
					}
					report.put(id, "deleted");
					builder = Response.ok();
				} catch (Throwable t) {
					builder = Response.status(Status.INTERNAL_SERVER_ERROR);
					writer.outObject(t);
				}
			}
			graph.commit();
		}
		try {
			writer.outObject(report);
			builder = Response.ok();
		} catch (JsonException e) {
			builder = Response.status(Status.INTERNAL_SERVER_ERROR);
			writer.outObject(e);
		} catch (IOException e) {
			builder = Response.status(Status.INTERNAL_SERVER_ERROR);
			writer.outObject(e);
		}

		builder.type(MediaType.APPLICATION_JSON_TYPE).entity(sw.toString());
		Response response = builder.build();
		return response;
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@SuppressWarnings("rawtypes")
	public Response createFramedObject(final String requestEntity, @Context final UriInfo uriInfo,
			@PathParam(Routes.NAMESPACE) final String namespace) throws JsonException, IOException {
		// Factory.println("Processing a POST for " + namespace);
		DFramedTransactionalGraph graph = this.getGraph(namespace);
		String jsonEntity = null;
		ResponseBuilder builder = null;
		ParamMap pm = Parameters.toParamMap(uriInfo);
		StringWriter sw = new StringWriter();
		JsonGraphWriter writer = new JsonGraphWriter(sw, graph, pm, false, true, false);

		JsonJavaObject jsonItems = null;
		List<Object> jsonArray = null;
		JsonGraphFactory factory = JsonGraphFactory.instance;
		try {
			StringReader reader = new StringReader(requestEntity);
			try {
				Object jsonRaw = JsonParser.fromJson(factory, reader);
				if (jsonRaw instanceof JsonJavaObject) {
					jsonItems = (JsonJavaObject) jsonRaw;
				} else if (jsonRaw instanceof List) {
					jsonArray = (List) jsonRaw;
				}
				builder = Response.ok();
			} catch (Throwable t) {
				builder = Response.status(Status.INTERNAL_SERVER_ERROR);
				writer.outObject(t);
			} finally {
				reader.close();
			}
		} catch (Exception ex) {
			builder = Response.status(Status.INTERNAL_SERVER_ERROR);
			writer.outObject(ex);
		}
		// Map<Object, Object> results = new LinkedHashMap<Object, Object>();
		if (jsonArray != null) {
			writer.startArray();
			for (Object raw : jsonArray) {
				if (raw instanceof JsonJavaObject) {
					writer.startArrayItem();
					processJsonObject((JsonJavaObject) raw, graph, writer, pm/*, results*/);
					writer.endArrayItem();
				}
			}
			writer.endArray();
		} else if (jsonItems != null) {
			processJsonObject(jsonItems, graph, writer, pm/*, results*/);
		} else {
			// System.out.println("TEMP DEBUG Nothing to POST. No JSON items found.");
		}

		builder.type(MediaType.APPLICATION_JSON_TYPE).entity(sw.toString());
		Response response = builder.build();
		return response;
	}

	private void processJsonObject(final JsonJavaObject jsonItems, final DFramedTransactionalGraph graph, final JsonGraphWriter writer,
			final ParamMap pm/*, Map<Object, Object> resultMap*/) {
		Map<CaseInsensitiveString, Object> cisMap = new HashMap<CaseInsensitiveString, Object>();
		for (String jsonKey : jsonItems.keySet()) {
			CaseInsensitiveString cis = new CaseInsensitiveString(jsonKey);
			cisMap.put(cis, jsonItems.get(jsonKey));
		}
		List<String> ids = pm.get(Parameters.ID);
		if (ids == null) {
			Map<String, String> map = new HashMap<String, String>();
			map.put("error", "Cannot POST to frame without an id parameter");
			try {
				writer.outObject(map);
			} catch (JsonException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			if (ids.size() == 0) {
				// TODO no id
			} else {
				for (String id : ids) {
					// System.out.println("TEMP DEBUG POSTing to " + id);
					NoteCoordinate nc = NoteCoordinate.Utils.getNoteCoordinate(id);
					Object element = graph.getElement(nc, null);
					if (element instanceof VertexFrame) {
						VertexFrame parVertex = (VertexFrame) element;
						Map<CaseInsensitiveString, Method> adders = graph.getTypeRegistry().getAdders(
								parVertex.getClass());
						CaseInsensitiveString rawLabel = new CaseInsensitiveString(jsonItems.getAsString("@label"));
						Method method = adders.get(rawLabel);
						if (method == null) {
							method = adders.get(rawLabel + "In");
						}
						if (method != null) {
							String rawId = jsonItems.getAsString("@id");
							NoteCoordinate othernc = NoteCoordinate.Utils.getNoteCoordinate(rawId);
							Object otherElement = graph.getElement(othernc, null);
							if (otherElement instanceof VertexFrame) {
								VertexFrame otherVertex = (VertexFrame) otherElement;
								try {
									Object result = method.invoke(parVertex, otherVertex);
									if (result == null) {
										System.out.println("Invokation of method " + method.getName()
										+ " on a vertex of type " + DGraphUtils.findInterface(parVertex)
										+ " with an argument of type " + DGraphUtils.findInterface(otherVertex)
										+ " resulted in null when we expected an Edge");
									}
									JsonFrameAdapter adapter = new JsonFrameAdapter(graph, (EdgeFrame) result, null,
											false);
									Iterator<String> frameProperties = adapter.getJsonProperties();
									while (frameProperties.hasNext()) {
										CaseInsensitiveString key = new CaseInsensitiveString(frameProperties.next());
										if (!key.startsWith("@")) {
											Object value = cisMap.get(key);
											if (value != null) {
												adapter.putJsonProperty(key.toString(), value);
												cisMap.remove(key);
											}
										}
									}
									for (CaseInsensitiveString cis : cisMap.keySet()) {
										if (!cis.startsWith("@")) {
											Object value = cisMap.get(cis);
											if (value != null) {
												adapter.putJsonProperty(cis.toString(), value);
											}
										}
									}
									writer.outObject(result);
								} catch (Exception e) {
									e.printStackTrace();
								}
							} else {
								Factory.println("otherElement is not a VertexFrame. It's a "
										+ (otherElement == null ? "null" : DGraphUtils.findInterface(otherElement)
												.getName()));
							}
						} else {
							Class[] interfaces = element.getClass().getInterfaces();
							String intList = "";
							for (Class inter : interfaces) {
								intList = intList + inter.getName() + ", ";
							}
							String methList = "";
							for (CaseInsensitiveString key : adders.keySet()) {
								methList = methList + key.toString() + ", ";
							}
							Factory.println("No method found for " + rawLabel + " on element " + intList + ": "
									+ ((VertexFrame) element).asVertex().getId() + " methods " + methList);
						}
					} else {
						org.openntf.domino.utils.Factory.println("element is not a VertexFrame. It's a "
								+ element.getClass().getName());
					}
				}
			}
			graph.commit();
		}
	}

}
