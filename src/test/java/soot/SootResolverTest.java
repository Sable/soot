package soot;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2021 canliture
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

import org.junit.Assert;
import org.junit.Test;
import soot.options.Options;

/**
 * Created by canliture on 2021/6/28 <br/>
 *
 * 1. Bug fixing description about 'test1' and 'test2':
 * A minimal Test for evaluating the bug in the Method {@link soot.Scene#defaultJavaClassPath}. <br/>
 *
 * When {@link soot.SootResolver} and {@link soot.SourceLocator#getClassSource} resolve classes,
 * they will get default java classPath by call `Scene.v().getSootClassPath()`. <br/>
 *
 * <b>Test under Java 8 environment: </b> <br/>
 *
 * Before fixed the bug, we will get default java class path when <br/>
 * - we set <pre>Options.v().set_whole_program(true);</pre>: `path/to/rt.jar;path/to/jce.jar` <br/>
 * - we set <pre>Options.v().set_whole_shimple(true);</pre>: `path/to/rt.jar` <br/>
 *
 * After fixed the bug, we will get default java class path when <br/>
 * - we set <pre>Options.v().set_whole_program(true);</pre>: `path/to/rt.jar;path/to/jce.jar` <br/>
 * - we set <pre>Options.v().set_whole_shimple(true);</pre>: `path/to/rt.jar;path/to/jce.jar` <br/>
 *
 * 2. Bug fixing description about 'test3':
 * In method {@link soot.jimple.toolkits.callgraph.OnFlyCallGraphBuilder#addType), it calls {@link Scene#getTypeUnsafe(String)},
 * but the argument passed into the method {@link Scene#getTypeUnsafe(String)} may be <b>quoted</b>, just like: <br/>
 * <li>1. "sun.reflect.'annotation'.AnnotationType"</li> <br/>
 * <li>2. "java.lang.'annotation'.Annotation" </li> <br/>
 * But {@link Scene#getTypeUnsafe(String)} will return null if the argument passed into is <b>quoted</b>, it will lead to <br/>
 * Soot crashing with NullPointerException or IllegalArgumentException somewhere, just like 'test3' failing to pass the test <br/>
 * with IllegalArgumentException
 *
 * @author canliture
 */
public class SootResolverTest {

    @Test
    public void test1() {
        G.reset();

        // setting
        Options.v().set_whole_program(true);

        // No throw. ^_^
        Scene.v().loadNecessaryClasses();
    }

    @Test
    public void test2() {
        G.reset();

        // setting
        Options.v().set_whole_shimple(true);

        // throw java.lang.AssertionError !!!
        Scene.v().loadNecessaryClasses();
    }

    @Test
    public void test3() {
        G.reset();

        Options.v().set_whole_program(true);

        Scene.v().loadNecessaryClasses();

        Assert.assertNotNull(Scene.v().getTypeUnsafe("java.lang.annotation.Annotation"));
        Assert.assertNotNull(Scene.v().getTypeUnsafeUnescape("java.lang.annotation.Annotation"));

        Assert.assertNull(Scene.v().getTypeUnsafe("java.lang.'annotation'.Annotation"));
        Assert.assertNotNull(Scene.v().getTypeUnsafeUnescape("java.lang.'annotation'.Annotation"));

        /* returnType maybe be null in SootMethodRefImpl's constructor, resulting in throwing IllegalArgumentException */
        PackManager.v().runPacks();
    }
}
