package soot.javaToJimple;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import polyglot.types.Type;

public class AnonConstructorFinder extends polyglot.visit.ContextVisitor {
	final static Logger logger = LoggerFactory.getLogger(AnonConstructorFinder.class);
    public AnonConstructorFinder(polyglot.frontend.Job job, polyglot.types.TypeSystem ts, polyglot.ast.NodeFactory nf) {
        super(job, ts, nf);
    }

    public polyglot.visit.NodeVisitor enter(polyglot.ast.Node parent, polyglot.ast.Node n){
        if (n instanceof polyglot.ast.New && ((polyglot.ast.New)n).anonType() != null){
            try {
                List<Type> argTypes = new ArrayList<Type>();
                for (Iterator it = ((polyglot.ast.New)n).arguments().iterator(); it.hasNext(); ){
                    argTypes.add(((polyglot.ast.Expr)it.next()).type());
                }
                polyglot.types.ConstructorInstance ci = typeSystem().findConstructor(((polyglot.ast.New)n).anonType().superType().toClass(), argTypes, ((polyglot.ast.New)n).anonType().superType().toClass());
                InitialResolver.v().addToAnonConstructorMap((polyglot.ast.New)n, ci);
            }
            catch(polyglot.types.SemanticException e){
                logger.info(e.getMessage());
                e.printStackTrace();
            }
        }
        return this;
    }

}
