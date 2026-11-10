package soot.jimple.toolkits.callgraph;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2003 Ondrej Lhotak
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import soot.ArrayType;
import soot.Body;
import soot.FastHierarchy;
import soot.Kind;
import soot.MethodSubSignature;
import soot.ModuleUtil;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.SootMethodRef;
import soot.Type;
import soot.Value;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;
import soot.options.Options;
import soot.util.StringNumberer;

/**
 * Utility class used by {@link OnFlyCallGraphBuilder} for finding functions at which to place virtual callgraph edges.
 * Function signatures are configurable in {@link #SUMMARIESFILE}.
 *
 * @author Julius Naeumann
 */
public class VirtualEdgesSummaries {
  public static final int BASE_INDEX = -1;

  private static final String SUMMARIESFILE = "virtualedges.xml";

  private class InstanceInvokeEdges {
    private ArrayList<VirtualEdge> edges = new ArrayList<>();

    public VirtualEdge matchEdge(RefType type) {
      FastHierarchy fh = Scene.v().getOrMakeFastHierarchy();
      VirtualEdge edge = null;
      boolean isOriginal = true;
      for (VirtualEdge p : edges) {
        final InstanceinvokeSource src = (InstanceinvokeSource) p.source;
        final RefType edgeType = src.declaringType;
        if (!edgeType.hasSootClass()) {
          continue;
        }
        SootClass cl = edgeType.getSootClass();
        if (cl.resolvingLevel() >= SootClass.HIERARCHY && fh.canStoreType(type, edgeType)) {
          if (edge == null) {
            edge = p;
          } else {
            if (isOriginal) {
              //clone
              edge = new VirtualEdge(edge.getEdgeType(), src, new HashSet<>(edge.getTargets()));
              isOriginal = false;
            }
            edge.targets.addAll(p.targets);
          }
        }
      }
      return edge;
    }

    public void compact() {
      edges.trimToSize();
    }
  }

  protected final HashMap<MethodSubSignature, InstanceInvokeEdges> instanceinvokeEdges = new LinkedHashMap<>();
  protected final HashMap<String, VirtualEdge> staticinvokeEdges = new LinkedHashMap<>();

  protected final Map<Kind, RefType> requiredTypeByKind = new HashMap<>();
  private static final Logger logger = LoggerFactory.getLogger(VirtualEdgesSummaries.class);

  /**
   * Creates a default instance of the {@link VirtualEdgesSummaries} and loads the summaries from the
   * <code>virtualedges.xml</code> that comes with Soot.
   */
  public VirtualEdgesSummaries() {
    final String virtualEdgesPath = Options.v().virtualedges_path();
    Path summariesFile = null;
    if (virtualEdgesPath != null && !virtualEdgesPath.isEmpty()) {
      final Path virtualEdgesFilePath = Paths.get(virtualEdgesPath);
      if (Files.exists(virtualEdgesFilePath)) {
        summariesFile = virtualEdgesFilePath;
      } else {
        logger.error("The virtual edges path {} does not exist", virtualEdgesPath);
      }
    }
    if (summariesFile == null) {
      summariesFile = Paths.get(SUMMARIESFILE);
    }
    try (InputStream in = Files.exists(summariesFile) ? Files.newInputStream(summariesFile)
        : ModuleUtil.class.getResourceAsStream("/" + SUMMARIESFILE)) {
      if (in == null) {
        logger.error("Virtual edge summaries file not found");
      } else {
        loadSummaries(in);
      }
    } catch (IOException | ParserConfigurationException | SAXException e1) {
      logger.error("An error occurred while reading in virtual edge summaries", e1);
    }
  }

  /**
   * Creates a new instance of the {@link VirtualEdgesSummaries} class and loads the summaries from the given input file
   *
   * @param summariesFile
   *          The file from which to load the virtual edge summaries
   */
  public VirtualEdgesSummaries(File summariesFile) {
    try (InputStream in = new FileInputStream(summariesFile)) {
      loadSummaries(in);
    } catch (IOException | ParserConfigurationException | SAXException e1) {
      logger.error("An error occurred while reading in virtual edge summaries", e1);
    }
  }

  /**
   * Loads the edge summaries from the given stream
   *
   * @param in
   *          The {@link InputStream} from which to load the summaries
   * @throws SAXException
   * @throws IOException
   * @throws ParserConfigurationException
   */
  protected void loadSummaries(InputStream in) throws SAXException, IOException, ParserConfigurationException {
    Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(in);
    doc.getDocumentElement().normalize();

    NodeList edges = doc.getElementsByTagName("edge");
    for (int i = 0, e = edges.getLength(); i < e; i++) {
      if (edges.item(i).getNodeType() == Node.ELEMENT_NODE) {
        Element edge = (Element) edges.item(i);
        VirtualEdge edg = new VirtualEdge();
        switch (edge.getAttribute("type")) {
          case "THREAD":
            edg.edgeType = Kind.THREAD;
            break;
          case "EXECUTOR":
            edg.edgeType = Kind.EXECUTOR;
            break;
          case "HANDLER":
            edg.edgeType = Kind.HANDLER;
            break;
          case "ASYNCTASK":
            edg.edgeType = Kind.ASYNCTASK;
            break;
          case "PRIVILEGED":
            edg.edgeType = Kind.PRIVILEGED;
            break;
          case "GENERIC_FAKE":
          default:
            edg.edgeType = Kind.GENERIC_FAKE;
            break;
        }

        String requiredTypeAttr = edge.getAttribute("requiredType");
        if (requiredTypeAttr != null && !requiredTypeAttr.isEmpty()) {
          requiredTypeByKind.put(edg.edgeType, RefType.v(requiredTypeAttr));
        }
        edg.source = parseEdgeSource((Element) (edge.getElementsByTagName("source").item(0)));
        edg.targets = new HashSet<VirtualEdgeTarget>();
        Element targetsElement = (Element) edge.getElementsByTagName("targets").item(0);
        edg.targets.addAll(parseEdgeTargets(targetsElement));
        if (edg.source instanceof InstanceinvokeSource) {
          InstanceinvokeSource inst = (InstanceinvokeSource) edg.source;
          MethodSubSignature subsig = inst.subSignature;

          // don't overwrite existing definition
          addInstanceInvoke(edg, subsig);
        }
        if (edg.source instanceof StaticinvokeSource) {
          StaticinvokeSource stat = (StaticinvokeSource) edg.source;
          staticinvokeEdges.put(stat.signature, edg);
        }
      }
    }
    compact();
    logger.debug("Found {} instanceinvoke, {} staticinvoke edge descriptions", instanceinvokeEdges.size(),
        staticinvokeEdges.size());
  }

  protected void addInstanceInvoke(VirtualEdge edg, MethodSubSignature subsig) {
    InstanceInvokeEdges e = instanceinvokeEdges.computeIfAbsent(subsig, (x) -> new InstanceInvokeEdges());
    e.edges.add(edg);
  }

  public VirtualEdgesSummaries(Collection<VirtualEdge> edges) {
    for (VirtualEdge vi : edges) {
      if (vi.source instanceof InstanceinvokeSource) {
        InstanceinvokeSource inst = (InstanceinvokeSource) vi.source;
        addInstanceInvoke(vi, inst.subSignature);
      } else if (vi.source instanceof StaticinvokeSource) {
        StaticinvokeSource stat = (StaticinvokeSource) vi.source;
        staticinvokeEdges.put(stat.signature, vi);
      }
    }
    compact();
  }

  private void compact() {
    for (InstanceInvokeEdges edges : instanceinvokeEdges.values()) {
      edges.compact();
    }
  }

  public Document toXMLDocument() throws ParserConfigurationException {
    DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();

    DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();

    Document document = documentBuilder.newDocument();

    Element root = document.createElement("virtualedges");
    document.appendChild(root);

    for (InstanceInvokeEdges i : instanceinvokeEdges.values()) {
      for (VirtualEdge edge : i.edges) {
        Node e = edgeToXML(document, edge);
        root.appendChild(e);
      }
    }
    for (VirtualEdge edge : staticinvokeEdges.values()) {
      Node e = edgeToXML(document, edge);
      root.appendChild(e);
    }

    return document;

  }

  private static Element edgeToXML(Document doc, VirtualEdge edge) {
    Element node = doc.createElement("edge");
    node.setAttribute("type", edge.edgeType.name());
    Element source = doc.createElement("source");
    node.appendChild(source);

    if (edge.source instanceof StaticinvokeSource) {
      StaticinvokeSource inv = (StaticinvokeSource) edge.source;
      source.setAttribute("invoketype", "static");
      source.setAttribute("signature", inv.signature);

    } else if (edge.source instanceof InstanceinvokeSource) {
      InstanceinvokeSource inv = (InstanceinvokeSource) edge.source;
      source.setAttribute("invoketype", "instance");
      source.setAttribute("subsignature", inv.subSignature.toString());
      if (inv.declaringType != null) {
        source.setAttribute("declaringclass", inv.declaringType.getClassName());
      }

    } else {
      if (edge.source == null) {
        throw new IllegalArgumentException("Unsupported null source type");
      } else {
        throw new IllegalArgumentException("Unsupported source type " + edge.source.getClass());
      }
    }

    Element targets = doc.createElement("targets");
    node.appendChild(targets);
    for (VirtualEdgeTarget e : edge.targets) {
      Element target = edgeTargetToXML(doc, e);
      targets.appendChild(target);
    }
    return node;
  }

  private static Element edgeTargetToXML(Document doc, VirtualEdgeTarget e) {
    Element target;
    if (e instanceof DirectTarget) {
      target = doc.createElement("direct");
    } else if (e instanceof IndirectTarget) {
      target = doc.createElement("indirect");

      IndirectTarget id = (IndirectTarget) e;
      for (VirtualEdgeTarget i : id.targets) {
        target.appendChild(edgeTargetToXML(doc, i));
      }
    } else if (e instanceof DeferredVirtualEdgeTarget) {
      target = doc.createElement("deferred");
    } else {
      if (e == null) {
        throw new IllegalArgumentException("Unsupported null edge type");
      } else {
        throw new IllegalArgumentException("Unsupported source type " + e.getClass());
      }
    }

    if (e.targetType != null) {
      target.setAttribute("declaringclass", e.targetType.getClassName());
    }

    if (e instanceof InvocationVirtualEdgeTarget) {
      InvocationVirtualEdgeTarget it = (InvocationVirtualEdgeTarget) e;
      target.setAttribute("subsignature", it.targetMethod.toString());
      if (it.isBase()) {
        target.setAttribute("target-position", "base");
      } else {
        target.setAttribute("index", String.valueOf(it.argIndex));
        target.setAttribute("target-position", "argument");
      }
    }

    return target;
  }

  public VirtualEdge getVirtualEdgesMatchingSubSig(Type type, MethodSubSignature subsig) {
    RefType rt;
    if (type instanceof ArrayType) {
      rt = Scene.v().getObjectType();
    } else if (type instanceof RefType) {
      rt = (RefType) type;
    } else {
      //primtype
      return null;
    }
    InstanceInvokeEdges i = instanceinvokeEdges.get(subsig);
    if (i == null) {
      return null;
    }
    if (rt.hasSootClass() && rt.getSootClass().isApplicationClass()) {
      final Scene sc = Scene.v();
      SootMethodRef mr = sc.makeMethodRef(rt.getSootClass(), subsig, true);
      for (SootMethod r : sc.getOrMakeFastHierarchy().resolveAbstractDispatch(rt.getSootClass(), mr)) {
        if (!r.getDeclaringClass().isApplicationClass()) {
          //One non-application class may be called -> we should assume virtual edges
          return i.matchEdge(rt);
        }
      }
      //We have the real code, no need to use virtual edges
      return null;
    }
    return i.matchEdge(rt);
  }

  public RefType getRequiredType(Kind kind) {
    return requiredTypeByKind.get(kind);
  }

  public VirtualEdge getVirtualEdgesMatchingFunction(String signature) {
    return staticinvokeEdges.get(signature);
  }

  private static VirtualEdgeSource parseEdgeSource(Element source) {
    String invokeType = source.getAttribute("invoketype");
    switch (invokeType) {
      case "instance":
        RefType dClass = getDeclaringClassType(source);
        return new InstanceinvokeSource(dClass, source.getAttribute("subsignature"));
      case "static":
        return new StaticinvokeSource(source.getAttribute("signature"));
      default:
        throw new RuntimeException(String.format("Invalid invoke type %s", invokeType));
    }
  }

  private static RefType getDeclaringClassType(Element source) {
    String declClass = source.getAttribute("declaringclass");
    RefType dClass = null;
    if (declClass != null && !declClass.isEmpty()) {
      dClass = RefType.v(declClass);
    }
    return dClass;
  }

  private static List<VirtualEdgeTarget> parseEdgeTargets(Element targetsElement) {
    List<VirtualEdgeTarget> targets = new ArrayList<>();
    final StringNumberer nmbr = Scene.v().getSubSigNumberer();
    NodeList children = targetsElement.getChildNodes();
    for (int i = 0, e = children.getLength(); i < e; i++) {
      if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {
        Element targetElement = (Element) children.item(i);
        RefType type = getDeclaringClassType(targetElement);

        switch (targetElement.getTagName()) {
          case "direct": {
            MethodSubSignature subsignature
                = new MethodSubSignature(nmbr.findOrAdd(targetElement.getAttribute("subsignature")));

            String tpos = targetElement.getAttribute("target-position");
            DirectTarget dt;
            switch (tpos) {
              case "argument":
                int argIdx = Integer.valueOf(targetElement.getAttribute("index"));
                dt = new DirectTarget(type, subsignature, argIdx);
                break;
              case "base":
                dt = new DirectTarget(type, subsignature);
                break;
              default:
                throw new IllegalArgumentException("Unsupported target position " + tpos);

            }
            targets.add(dt);
            NodeList cd = targetElement.getChildNodes();
            for (int x = 0; x < cd.getLength(); x++) {
              Node ce = cd.item(x);
              if (ce instanceof Element) {
                Element cee = (Element) ce;
                if (cee.getTagName().equals("parameterMappings")) {
                  parseParameterMappings(dt, cee);
                }
              }
            }
            break;
          }
          case "indirect": {
            // Parse the attributes of the current target
            IndirectTarget target;
            MethodSubSignature subsignature
                = new MethodSubSignature(nmbr.findOrAdd(targetElement.getAttribute("subsignature")));
            String tpos = targetElement.getAttribute("target-position");
            switch (tpos) {
              case "argument":
                int argIdx = Integer.valueOf(targetElement.getAttribute("index"));
                target = new IndirectTarget(type, subsignature, argIdx);
                break;
              case "base":
                target = new IndirectTarget(type, subsignature);
                break;
              default:
                throw new IllegalArgumentException("Unsupported target position " + tpos);

            }
            targets.add(target);

            // Parse child targets, since we have a chain of target methods to track back to the point where the actual
            // callback
            // was originally registered
            target.addTargets(parseEdgeTargets(targetElement));

            targets.add(target);
            break;
          }
          case "deferred": {
            DeferredVirtualEdgeTarget target = new DeferredVirtualEdgeTarget(type);
            targets.add(target);
            break;
          }
        }
      }
    }
    return targets;
  }

  private static void parseParameterMappings(DirectTarget dt, Element cee) {
    NodeList cn = cee.getChildNodes();
    for (int i = 0; i < cn.getLength(); i++) {
      Node d = cn.item(i);
      if (d instanceof Element) {
        Element e = (Element) d;
        switch (e.getTagName()) {
          case "direct":
            int sourceIdx = Integer.parseInt(e.getAttribute("sourceIdx"));
            int targetIdx = Integer.parseInt(e.getAttribute("targetIdx"));
            dt.parameterMappings.add(new DirectParameterMapping(sourceIdx, targetIdx));
            break;
          default:
            throw new RuntimeException("Not supported: " + e.getTagName());
        }
      }
    }
  }

  public static abstract class VirtualEdgeSource {
  }

  public static class StaticinvokeSource extends VirtualEdgeSource {
    /**
     * The method signature at which to insert this edge.
     */
    String signature;

    public StaticinvokeSource(String signature) {
      this.signature = signature;
    }

    public String getSignature() {
      return signature;
    }

    @Override
    public String toString() {
      return signature;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((signature == null) ? 0 : signature.hashCode());
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if ((obj == null) || (getClass() != obj.getClass())) {
        return false;
      }
      StaticinvokeSource other = (StaticinvokeSource) obj;
      if (signature == null) {
        if (other.signature != null) {
          return false;
        }
      } else if (!signature.equals(other.signature)) {
        return false;
      }
      return true;
    }
  }

  public static class InstanceinvokeSource extends VirtualEdgeSource {
    /**
     * The method subsignature at which to insert this edge.
     */
    MethodSubSignature subSignature;

    RefType declaringType;

    /**
     * Creates a new instance of the {@link InstanceinvokeSource} class based on a method that is being invoked on the
     * current object instance
     *
     * @param declaringType
     *          A type where the method with the subsignature is declared.
     * @param subSignature
     *          The subsignature of the method that is invoked
     */
    public InstanceinvokeSource(RefType declaringType, String subSignature) {
      if (declaringType == null) {
        throw new IllegalArgumentException("Declaring type is null");
      }
      this.subSignature = new MethodSubSignature(Scene.v().getSubSigNumberer().findOrAdd(subSignature));
      this.declaringType = declaringType;
    }

    /**
     * Convenience constructor that extracts the subsignature of the callee from a call site statement
     *
     * @param invokeStmt
     *          The statement at the call site
     */
    public InstanceinvokeSource(Stmt invokeStmt) {
      this(invokeStmt.getInvokeExpr().getMethodRef().getDeclaringClass().getType(),
          invokeStmt.getInvokeExpr().getMethodRef().getSubSignature().getString());
    }

    @Override
    public String toString() {
      return (declaringType != null ? (declaringType + ": ") : "") + subSignature.toString();
    }

    public RefType getDeclaringType() {
      return declaringType;
    }

    public MethodSubSignature getSubSignature() {
      return subSignature;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((declaringType == null) ? 0 : declaringType.hashCode());
      result = prime * result + ((subSignature == null) ? 0 : subSignature.hashCode());
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (getClass() != obj.getClass()) {
        return false;
      }
      InstanceinvokeSource other = (InstanceinvokeSource) obj;
      if (declaringType == null) {
        if (other.declaringType != null) {
          return false;
        }
      } else if (!declaringType.equals(other.declaringType)) {
        return false;
      }
      if (subSignature == null) {
        if (other.subSignature != null) {
          return false;
        }
      } else if (!subSignature.equals(other.subSignature)) {
        return false;
      }
      return true;
    }

  }

  /**
   * Abstract base class for all virtual edge targets.
   *
   * @author Steven Arzt
   *
   */
  public static abstract class VirtualEdgeTarget {

    protected RefType targetType;

    VirtualEdgeTarget() {
      // internal use only
    }

    public abstract VirtualEdgeTarget clone();

    public VirtualEdgeTarget(RefType targetType) {
      this.targetType = targetType;
    }

    public RefType getTargetType() {
      return targetType;
    }

    @Override
    public int hashCode() {
      return Objects.hash(targetType);
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (getClass() != obj.getClass()) {
        return false;
      }
      VirtualEdgeTarget other = (VirtualEdgeTarget) obj;
      return Objects.equals(targetType, other.targetType);
    }

  }

  /**
   * <p>
   * A deferred edge target models cases in which a call does not immediately invoke the callback, but instead returns an
   * object on which a callback an be invoked later.
   * </p>
   *
   * <pre>
   * List<String> l = ...
   * Spliterator<String> split = l.spliterator();
   * split.forEachRemaining(callback);
   * </pre>
   *
   * @author arzt
   *
   */
  public static class DeferredVirtualEdgeTarget extends VirtualEdgeTarget {

    DeferredVirtualEdgeTarget() {
      // internal use only
    }

    public DeferredVirtualEdgeTarget(RefType targetType) {
      super(targetType);
    }

    @Override
    public DeferredVirtualEdgeTarget clone() {
      return new DeferredVirtualEdgeTarget(targetType);
    }

  }

  /**
   * <p>
   * The target of a PAG or callgraph edge that corresponds to the immediate execution of a method.
   * </p>
   *
   * <p>
   * The method can either be specified directly, or indirectly by following a chain obf subsequent calls, which is modeled
   * by the respective derived classes of this abstract base class.
   * </p>
   *
   */
  public static abstract class InvocationVirtualEdgeTarget extends VirtualEdgeTarget {

    protected int argIndex;
    protected MethodSubSignature targetMethod;

    InvocationVirtualEdgeTarget() {
      // internal use only
    }

    public InvocationVirtualEdgeTarget(RefType targetType, MethodSubSignature targetMethod) {
      super(targetType);
      this.argIndex = BASE_INDEX;
      this.targetMethod = targetMethod;
    }

    public InvocationVirtualEdgeTarget(RefType targetType, MethodSubSignature targetMethod, int argIndex) {
      super(targetType);
      this.argIndex = argIndex;
      this.targetMethod = targetMethod;
    }

    @Override
    public String toString() {
      return isBase() ? "base" : String.format("argument %d", argIndex);
    }

    public boolean isBase() {
      return argIndex == BASE_INDEX;
    }

    public int getArgIndex() {
      return argIndex;
    }

    public void setArgIndex(int value) {
      this.argIndex = value;
    }

    /**
     * Clones the edge, but with a potentially different arg index
     *
     * @param argIndex
     *          the arg index to set in the clone
     * @return the clone
     */
    public abstract VirtualEdgeTarget clone(int argIndex);

    public MethodSubSignature getTargetMethod() {
      return targetMethod;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = super.hashCode();
      result = prime * result + Objects.hash(argIndex, targetMethod);
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (!super.equals(obj)) {
        return false;
      }
      if (getClass() != obj.getClass()) {
        return false;
      }
      InvocationVirtualEdgeTarget other = (InvocationVirtualEdgeTarget) obj;
      return argIndex == other.argIndex && Objects.equals(targetMethod, other.targetMethod);
    }
  }

  public static class DirectTarget extends InvocationVirtualEdgeTarget {
    private List<AbstractParameterMapping> parameterMappings = new ArrayList<>();

    DirectTarget() {
      // internal use only
    }

    /**
     * Creates a new direct method invocation on an object passed to the original source as an argument. For example,
     * <code>foo.do(x)></code> could invoke <code>x.bar()</code> as a callback.
     *
     * @param targetType
     *          The declaring class of the target method
     * @param targetMethod
     *          The target method that is invoked on the argument object
     * @param argIndex
     *          The index of the argument that receives the target object
     */
    public DirectTarget(RefType targetType, MethodSubSignature targetMethod, int argIndex) {
      super(targetType, targetMethod, argIndex);
    }

    /**
     * Creates a new direct method invocation on the base object of the original source. For example, <code>foo.do()></code>
     * could invoke <code>foo.bar()</code> as a callback.
     *
     * @param targetType
     *          The declaring class of the target method
     * @param targetMethod
     *          The target method that is invoked on the base object
     */
    public DirectTarget(RefType targetType, MethodSubSignature targetMethod) {
      super(targetType, targetMethod);
    }

    public DirectTarget clone() {
      return new DirectTarget(targetType, targetMethod, argIndex);
    }

    public DirectTarget clone(int argIndex) {
      return new DirectTarget(targetType, targetMethod, argIndex);
    }

    @Override
    public String toString() {
      return String.format("Direct to %s%s on %s", targetType != null ? targetType.getClassName() + ": " : "",
          targetMethod.toString(), super.toString());
    }

    @Override
    public int hashCode() {
      return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (!super.equals(obj) || (getClass() != obj.getClass())) {
        return false;
      }
      return true;
    }

    public List<AbstractParameterMapping> getParameterMappings() {
      return parameterMappings;
    }
  }

  public static abstract class AbstractParameterMapping {
    public abstract Value getMappedSourceArgumentArg(InvokeExpr expr);

    public abstract Value getMappedTargetArgumentArg(Body body);
  }

  public static class DirectParameterMapping extends AbstractParameterMapping {
    private int sourceIndex, targetIndex;

    public DirectParameterMapping(int src, int tgt) {
      this.sourceIndex = src;
      this.targetIndex = tgt;
    }

    public int getSourceIndex() {
      return sourceIndex;
    }

    public int getTargetIndex() {
      return targetIndex;
    }

    @Override
    public Value getMappedSourceArgumentArg(InvokeExpr expr) {
      return getValueByIndex(expr, sourceIndex);
    }

    @Override
    public Value getMappedTargetArgumentArg(Body body) {
      if (targetIndex == -1) {
        return body.getThisLocal();
      }
      return body.getParameterLocal(targetIndex);
    }
  }

  private static Value getValueByIndex(InvokeExpr expr, int idx) {
    if (idx == BASE_INDEX) {
      return ((InstanceInvokeExpr) expr).getBase();
    }
    return expr.getArg(idx);
  }

  public static class IndirectTarget extends InvocationVirtualEdgeTarget {
    List<VirtualEdgeTarget> targets = new ArrayList<>();

    IndirectTarget() {
      // internal use only
    }

    /**
     * Creates a new direct method invocation. The signature of this indirect target references a method that was called
     * earlier, and which received the object on which the callback is invoked. This constructor assumes that the earlier
     * method has created an object which is passed the current method as an argument.
     *
     * @param targetType
     *          The target type which declares the target method
     * @param targetMethod
     *          The method with which the original callback was registered
     * @param argIndex
     *          The index of the argument that holds the object that holds the callback or next step of the indirect
     *          invocation
     */
    public IndirectTarget(RefType targetType, MethodSubSignature targetMethod, int argIndex) {
      super(targetType, targetMethod, argIndex);
    }

    /**
     * Creates a new indirect target as an indirection from a method that was previously considered a source
     *
     * @param source
     *          The source from which to create the indirect target
     */
    public IndirectTarget(InstanceinvokeSource source) {
      super(source.declaringType, source.subSignature);
    }

    /**
     * Creates a new direct method invocation. The signature of this indirect target references a method that was called
     * earlier, and which received the object on which the callback is invoked.
     *
     * @param targetType
     *          The target type which declares the target method
     * @param targetMethod
     *          The method with which the original callback was registered
     */
    public IndirectTarget(RefType targetType, MethodSubSignature targetMethod) {
      super(targetType, targetMethod);
    }

    public IndirectTarget clone() {
      IndirectTarget d = new IndirectTarget(targetType, targetMethod, argIndex);
      for (VirtualEdgeTarget i : getTargets()) {
        d.addTarget(i.clone());
      }
      return d;
    }

    public IndirectTarget clone(int argIndex) {
      IndirectTarget d = new IndirectTarget(targetType, targetMethod, argIndex);
      for (VirtualEdgeTarget i : getTargets()) {
        d.addTarget(i.clone());
      }
      return d;
    }

    public void addTarget(VirtualEdgeTarget target) {
      if (!targets.contains(target)) {
        targets.add(target);
      }
    }

    public void addTargets(Collection<? extends VirtualEdgeTarget> targets) {
      for (VirtualEdgeTarget target : targets) {
        addTarget(target);
      }
    }

    public List<VirtualEdgeTarget> getTargets() {
      return targets;
    }

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder();
      for (VirtualEdgeTarget t : targets) {
        sb.append('(').append(t.toString()).append(") ");
      }
      return String.format("(Instances passed to <" + (targetType != null ? targetType : "?") + ": %s> on %s => %s)",
          targetMethod.toString(), super.toString(), sb.toString());

    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = super.hashCode();
      result = prime * result + ((targets == null) ? 0 : targets.hashCode());
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (!super.equals(obj) || (getClass() != obj.getClass())) {
        return false;
      }
      IndirectTarget other = (IndirectTarget) obj;
      if (targets == null) {
        if (other.targets != null) {
          return false;
        }
      } else if (!targets.equals(other.targets)) {
        return false;
      }
      return true;
    }
  }

  public static class VirtualEdge {
    /**
     * The kind of edge to insert
     */
    Kind edgeType;
    VirtualEdgeSource source;
    Set<VirtualEdgeTarget> targets;

    VirtualEdge() {
      // internal use only
    }

    public VirtualEdge(Kind edgeType, VirtualEdgeSource source, VirtualEdgeTarget target) {
      this(edgeType, source, new ArrayList<>(Collections.singletonList(target)));
    }

    public VirtualEdge(Kind edgeType, VirtualEdgeSource source, Collection<VirtualEdgeTarget> targets) {
      this.edgeType = edgeType;
      this.source = source;
      this.targets = new HashSet<>(targets);
    }

    public Kind getEdgeType() {
      return edgeType;
    }

    public VirtualEdgeSource getSource() {
      return source;
    }

    public Set<VirtualEdgeTarget> getTargets() {
      return targets;
    }

    /**
     * Adds the given targets to this edge summary
     *
     * @param newTargets
     *          The targets to add
     */
    public void addTargets(Collection<VirtualEdgeTarget> newTargets) {
      this.targets.addAll(newTargets);
    }

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder();
      for (VirtualEdgeTarget t : targets) {
        sb.append(t.toString()).append(' ');
      }
      return String.format("%s %s => %s\n", edgeType, source.toString(), sb.toString());
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((edgeType == null) ? 0 : edgeType.hashCode());
      result = prime * result + ((source == null) ? 0 : source.hashCode());
      result = prime * result + ((targets == null) ? 0 : targets.hashCode());
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if ((obj == null) || (getClass() != obj.getClass())) {
        return false;
      }
      VirtualEdge other = (VirtualEdge) obj;
      if (edgeType == null) {
        if (other.edgeType != null) {
          return false;
        }
      } else if (!edgeType.equals(other.edgeType)) {
        return false;
      }
      if (source == null) {
        if (other.source != null) {
          return false;
        }
      } else if (!source.equals(other.source)) {
        return false;
      }
      if (targets == null) {
        if (other.targets != null) {
          return false;
        }
      } else if (!targets.equals(other.targets)) {
        return false;
      }
      return true;
    }

  }

  public boolean isEmpty() {
    return instanceinvokeEdges.isEmpty() && staticinvokeEdges.isEmpty();
  }

  public Set<VirtualEdge> getAllVirtualEdges() {
    Set<VirtualEdge> allEdges = new HashSet<>(instanceinvokeEdges.size() + staticinvokeEdges.size());
    for (InstanceInvokeEdges f : instanceinvokeEdges.values()) {
      allEdges.addAll(f.edges);
    }
    allEdges.addAll(staticinvokeEdges.values());
    return allEdges;
  }

}
