/* Soot - a J*va Optimization Framework
 * Copyright (C) 2006 Nomair A. Naeem (nomair.naeem@mail.mcgill.ca)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 */

package soot.dava;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import java.util.*;
//import soot.util.*;
//import soot.dava.toolkits.base.finders.*;

public class CorruptASTException extends DecompilationException 
{

	private static final Logger logger =LoggerFactory.getLogger(CorruptASTException.class);

    public CorruptASTException(String message){
	super();
	logger.info("The Abstract Syntax Tree is corrupt");
	logger.info(message);
	report();
    }
}
