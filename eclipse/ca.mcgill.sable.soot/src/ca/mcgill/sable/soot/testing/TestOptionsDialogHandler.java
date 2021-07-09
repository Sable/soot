/* Soot - a J*va Optimization Framework
 * Copyright (C) 2003 Jennifer Lhotak
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



/*
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
 *
 * This class is generated automajically from xml - DO NO EDIT - as
 * changes will be over written
 * 
 * 
 * The purpose of this class is to automajically generate a handler
 * for the options dialog the event that options change
 * 
 * Taking options away - should not damage the handler
 * Adding new sections of options - should not damage the handler
 * Adding new otpions to sections (of known option type) - should not
 * damage the handler
 *
 * Adding new option types may cause the handler to produce 
 * incorrect results
 *
 */

package ca.mcgill.sable.soot.testing;

import java.util.*;
import org.eclipse.jface.dialogs.IDialogSettings;
import ca.mcgill.sable.soot.SootPlugin;

public class TestOptionsDialogHandler {


	private final static String SPACE = " ";
	private final static String DASH = "--";
	private final static String COLON = ":";
	
	public TestOptionsDialogHandler() {
	}

	public String getCmdLine() {
		IDialogSettings settings = SootPlugin.getDefault().getDialogSettings();
		
		StringBuffer cmd = new StringBuffer();
		String path = null;
		String defaultVal = null;
		//String phaseOptsAlias = null;
		//String phaseAlias = null;
		//String subPhaseAlias = null;
		String key = null;
		boolean value = false;
		//HashMap phasePairs = new HashMap();
		boolean boolDefault = false;
	
		
		key = ""+" "+""+" "+"h";
		value = settings.getBoolean(key.trim());
		
		boolDefault = false;
		
		
		if (value != boolDefault) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = ""+" "+""+" "+"version";
		value = settings.getBoolean(key.trim());
		
		boolDefault = false;
		
		
		if (value != boolDefault) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = ""+" "+""+" "+"v";
		value = settings.getBoolean(key.trim());
		
		boolDefault = false;
		
		
		if (value != boolDefault) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = ""+" "+""+" "+"app";
		value = settings.getBoolean(key.trim());
		
		boolDefault = false;
		
		
		if (value != boolDefault) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = ""+" "+""+" "+"w";
		value = settings.getBoolean(key.trim());
		
		boolDefault = false;
		
		
		if (value != boolDefault) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = ""+" "+""+" "+"allow-phantom-refs";
		value = settings.getBoolean(key.trim());
		
		boolDefault = false;
		
		
		if (value != boolDefault) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = ""+" "+""+" "+"cp";
		
		
		
		path = settings.get(key.trim());
		if ((path != null) && (path.length() != 0) && (!path.equals(defaultVal)) ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
			cmd.append(path);
			cmd.append(SPACE);
		}
		 
		key = ""+" "+""+" "+"src-prec";
		path = settings.get(key.trim());
		
		defaultVal = "c";
		
		
		if ((path != null) && (path.length() != 0) && (!path.equals(defaultVal))) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
			cmd.append(path);
			cmd.append(SPACE);
		}	
		
		key = ""+" "+""+" "+"via-grimp";
		value = settings.getBoolean(key.trim());
		
		boolDefault = false;
		
		
		if (value != boolDefault) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = ""+" "+""+" "+"xml-attributes";
		value = settings.getBoolean(key.trim());
		
		boolDefault = false;
		
		
		if (value != boolDefault) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = ""+" "+""+" "+"d";
		
		
		
		path = settings.get(key.trim());
		if ((path != null) && (path.length() != 0) && (!path.equals(defaultVal)) ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
			cmd.append(path);
			cmd.append(SPACE);
		}
		 
		key = ""+" "+""+" "+"o";
		path = settings.get(key.trim());
		
		defaultVal = "c";
		
		
		if ((path != null) && (path.length() != 0) && (!path.equals(defaultVal))) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
			cmd.append(path);
			cmd.append(SPACE);
		}	
		
		key = ""+" "+""+" "+"O";
		value = settings.getBoolean(key.trim());
		
		boolDefault = false;
		
		
		if (value != boolDefault) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = ""+" "+""+" "+"W";
		value = settings.getBoolean(key.trim());
		
		boolDefault = false;
		
		
		if (value != boolDefault) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = ""+" "+""+" "+"via-shimple";
		value = settings.getBoolean(key.trim());
		
		boolDefault = false;
		
		
		if (value != boolDefault) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = ""+" "+""+" "+"process-path";
		
		path = settings.get(key.trim());
		if ((path != null) && (path.length() != 0)) {

			StringTokenizer st = new StringTokenizer(path);
			while (st.hasMoreTokens()) {
			
				cmd.append(DASH);
				cmd.append(key.trim());
				cmd.append(SPACE);
				cmd.append(st.nextToken());
				cmd.append(SPACE);
			}
		}
		
		key = ""+" "+""+" "+"a";
		value = settings.getBoolean(key.trim());
		
		boolDefault = false;
		
		
		if (value != boolDefault) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = ""+" "+""+" "+"i";
		
		path = settings.get(key.trim());
		if ((path != null) && (path.length() != 0)) {

			StringTokenizer st = new StringTokenizer(path);
			while (st.hasMoreTokens()) {
			
				cmd.append(DASH);
				cmd.append(key.trim());
				cmd.append(SPACE);
				cmd.append(st.nextToken());
				cmd.append(SPACE);
			}
		}
		
		key = ""+" "+""+" "+"x";
		
		path = settings.get(key.trim());
		if ((path != null) && (path.length() != 0)) {

			StringTokenizer st = new StringTokenizer(path);
			while (st.hasMoreTokens()) {
			
				cmd.append(DASH);
				cmd.append(key.trim());
				cmd.append(SPACE);
				cmd.append(st.nextToken());
				cmd.append(SPACE);
			}
		}
		
		key = ""+" "+""+" "+"dynamic-classes";
		
		path = settings.get(key.trim());
		if ((path != null) && (path.length() != 0)) {

			StringTokenizer st = new StringTokenizer(path);
			while (st.hasMoreTokens()) {
			
				cmd.append(DASH);
				cmd.append(key.trim());
				cmd.append(SPACE);
				cmd.append(st.nextToken());
				cmd.append(SPACE);
			}
		}
		
		key = ""+" "+""+" "+"dynamic-path";
		
		path = settings.get(key.trim());
		if ((path != null) && (path.length() != 0)) {

			StringTokenizer st = new StringTokenizer(path);
			while (st.hasMoreTokens()) {
			
				cmd.append(DASH);
				cmd.append(key.trim());
				cmd.append(SPACE);
				cmd.append(st.nextToken());
				cmd.append(SPACE);
			}
		}
		
		key = ""+" "+""+" "+"dynamic-package";
		
		path = settings.get(key.trim());
		if ((path != null) && (path.length() != 0)) {

			StringTokenizer st = new StringTokenizer(path);
			while (st.hasMoreTokens()) {
			
				cmd.append(DASH);
				cmd.append(key.trim());
				cmd.append(SPACE);
				cmd.append(st.nextToken());
				cmd.append(SPACE);
			}
		}
		
		key = ""+" "+""+" "+"keep-line-number";
		value = settings.getBoolean(key.trim());
		
		boolDefault = false;
		
		
		if (value != boolDefault) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = ""+" "+""+" "+"keep-bytecode-offset";
		value = settings.getBoolean(key.trim());
		
		boolDefault = false;
		
		
		if (value != boolDefault) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = ""+" "+""+" "+"annot-nullpointer";
		value = settings.getBoolean(key.trim());
		
		boolDefault = false;
		
		
		if (value != boolDefault) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = ""+" "+""+" "+"annot-arraybounds";
		value = settings.getBoolean(key.trim());
		
		boolDefault = false;
		
		
		if (value != boolDefault) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = ""+" "+""+" "+"time";
		value = settings.getBoolean(key.trim());
		
		boolDefault = false;
		
		
		if (value != boolDefault) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = ""+" "+""+" "+"subtract-gc";
		value = settings.getBoolean(key.trim());
		
		boolDefault = false;
		
		
		if (value != boolDefault) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"jb"+" "+"disabled";
		value = settings.getBoolean(key.trim());
		
		boolDefault = false;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"jb"+" "+"no-splitting";
		value = settings.getBoolean(key.trim());
		
		boolDefault = false;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"jb"+" "+"no-typing";
		value = settings.getBoolean(key.trim());
		
		boolDefault = false;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"jb"+" "+"aggregate-all-locals";
		value = settings.getBoolean(key.trim());
		
		boolDefault = false;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"jb"+" "+"no-aggregating";
		value = settings.getBoolean(key.trim());
		
		boolDefault = false;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"jb"+" "+"use-original-names";
		value = settings.getBoolean(key.trim());
		
		boolDefault = false;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"jb"+" "+"pack-locals";
		value = settings.getBoolean(key.trim());
		
		boolDefault = false;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"jb"+" "+"no-cp";
		value = settings.getBoolean(key.trim());
		
		boolDefault = false;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"jb"+" "+"no-nop-elimination";
		value = settings.getBoolean(key.trim());
		
		boolDefault = false;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"jb"+" "+"no-unreachable-code-elimination";
		value = settings.getBoolean(key.trim());
		
		boolDefault = false;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"jb"+" "+"verbatim";
		value = settings.getBoolean(key.trim());
		
		boolDefault = false;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"jb.asv"+" "+"disabled";
		value = settings.getBoolean(key.trim());
		
		boolDefault = false;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"jb.asv"+" "+"only-stack-locals";
		value = settings.getBoolean(key.trim());
		
		boolDefault = true;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"jb.ulp"+" "+"disabled";
		value = settings.getBoolean(key.trim());
		
		boolDefault = false;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"jb.ulp"+" "+"unsplit-original-locals";
		value = settings.getBoolean(key.trim());
		
		boolDefault = true;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"jb.lns"+" "+"disabled";
		value = settings.getBoolean(key.trim());
		
		boolDefault = false;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"jb.lns"+" "+"only-stack-locals";
		value = settings.getBoolean(key.trim());
		
		boolDefault = false;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"jb.cp"+" "+"disabled";
		value = settings.getBoolean(key.trim());
		
		boolDefault = false;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"jb.cp"+" "+"only-regular-locals";
		value = settings.getBoolean(key.trim());
		
		boolDefault = false;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"jb.cp"+" "+"only-stack-locals";
		value = settings.getBoolean(key.trim());
		
		boolDefault = true;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"jb.dae"+" "+"disabled";
		value = settings.getBoolean(key.trim());
		
		boolDefault = false;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"jb.dae"+" "+"only-stack-locals";
		value = settings.getBoolean(key.trim());
		
		boolDefault = true;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"jb.ls"+" "+"disabled";
		value = settings.getBoolean(key.trim());
		
		boolDefault = false;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"jb.a"+" "+"disabled";
		value = settings.getBoolean(key.trim());
		
		boolDefault = false;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"jb.a"+" "+"only-stack-locals";
		value = settings.getBoolean(key.trim());
		
		boolDefault = false;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"jb.ule"+" "+"disabled";
		value = settings.getBoolean(key.trim());
		
		boolDefault = false;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"jb.tr"+" "+"disabled";
		value = settings.getBoolean(key.trim());
		
		boolDefault = false;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"jb.cp-ule"+" "+"disabled";
		value = settings.getBoolean(key.trim());
		
		boolDefault = false;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"jb.lp"+" "+"disabled";
		value = settings.getBoolean(key.trim());
		
		boolDefault = false;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"jb.lp"+" "+"unsplit-original-locals";
		value = settings.getBoolean(key.trim());
		
		boolDefault = false;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"jb.ne"+" "+"disabled";
		value = settings.getBoolean(key.trim());
		
		boolDefault = false;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"jb.uce"+" "+"disabled";
		value = settings.getBoolean(key.trim());
		
		boolDefault = false;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"cg"+" "+"disabled";
		value = settings.getBoolean(key.trim());
		
		boolDefault = false;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"cg.oldcha"+" "+"disabled";
		value = settings.getBoolean(key.trim());
		
		boolDefault = true;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"cg.vta"+" "+"disabled";
		value = settings.getBoolean(key.trim());
		
		boolDefault = true;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"cg.vta"+" "+"passes";
		
		
		defaultVal = "1";
		
				
		
		path = settings.get(key.trim());
		if ((path != null) && (path.length() != 0) && (!path.equals(defaultVal)) ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(COLON);
			cmd.append(path);
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"cg.cha"+" "+"disabled";
		value = settings.getBoolean(key.trim());
		
		boolDefault = true;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"cg.cha"+" "+"verbose";
		value = settings.getBoolean(key.trim());
		
		boolDefault = false;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"cg.spark"+" "+"disabled";
		value = settings.getBoolean(key.trim());
		
		boolDefault = true;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"cg.spark"+" "+"verbose";
		value = settings.getBoolean(key.trim());
		
		boolDefault = false;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"cg.spark"+" "+"ignore-types";
		value = settings.getBoolean(key.trim());
		
		boolDefault = false;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"cg.spark"+" "+"force-gc";
		value = settings.getBoolean(key.trim());
		
		boolDefault = false;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"cg.spark"+" "+"pre-jimplify";
		value = settings.getBoolean(key.trim());
		
		boolDefault = false;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"cg.spark"+" "+"vta";
		value = settings.getBoolean(key.trim());
		
		boolDefault = false;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"cg.spark"+" "+"rta";
		value = settings.getBoolean(key.trim());
		
		boolDefault = false;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"cg.spark"+" "+"field-based";
		value = settings.getBoolean(key.trim());
		
		boolDefault = false;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"cg.spark"+" "+"types-for-sites";
		value = settings.getBoolean(key.trim());
		
		boolDefault = false;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"cg.spark"+" "+"merge-stringbuffer";
		value = settings.getBoolean(key.trim());
		
		boolDefault = true;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"cg.spark"+" "+"simulate-natives";
		value = settings.getBoolean(key.trim());
		
		boolDefault = true;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"cg.spark"+" "+"simple-edges-bidirectional";
		value = settings.getBoolean(key.trim());
		
		boolDefault = false;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"cg.spark"+" "+"on-fly-cg";
		value = settings.getBoolean(key.trim());
		
		boolDefault = false;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"cg.spark"+" "+"parms-as-fields";
		value = settings.getBoolean(key.trim());
		
		boolDefault = false;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"cg.spark"+" "+"returns-as-fields";
		value = settings.getBoolean(key.trim());
		
		boolDefault = false;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"cg.spark"+" "+"simplify-offline";
		value = settings.getBoolean(key.trim());
		
		boolDefault = false;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"cg.spark"+" "+"simplify-sccs";
		value = settings.getBoolean(key.trim());
		
		boolDefault = false;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"cg.spark"+" "+"ignore-types-for-sccs";
		value = settings.getBoolean(key.trim());
		
		boolDefault = false;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		 
		key = "p"+" "+"cg.spark"+" "+"propagator";
		path = settings.get(key.trim());
		
		defaultVal = "worklist";
		
		
		if ((path != null) && (path.length() != 0) && (!path.equals(defaultVal))) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(COLON);
			cmd.append(path);
			cmd.append(SPACE);
		}	
		 
		key = "p"+" "+"cg.spark"+" "+"set-impl";
		path = settings.get(key.trim());
		
		defaultVal = "double";
		
		
		if ((path != null) && (path.length() != 0) && (!path.equals(defaultVal))) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(COLON);
			cmd.append(path);
			cmd.append(SPACE);
		}	
		 
		key = "p"+" "+"cg.spark"+" "+"double-set-old";
		path = settings.get(key.trim());
		
		defaultVal = "hybrid";
		
		
		if ((path != null) && (path.length() != 0) && (!path.equals(defaultVal))) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(COLON);
			cmd.append(path);
			cmd.append(SPACE);
		}	
		 
		key = "p"+" "+"cg.spark"+" "+"double-set-new";
		path = settings.get(key.trim());
		
		defaultVal = "hybrid";
		
		
		if ((path != null) && (path.length() != 0) && (!path.equals(defaultVal))) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(COLON);
			cmd.append(path);
			cmd.append(SPACE);
		}	
		
		key = "p"+" "+"cg.spark"+" "+"dump-html";
		value = settings.getBoolean(key.trim());
		
		boolDefault = false;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"cg.spark"+" "+"dump-pag";
		value = settings.getBoolean(key.trim());
		
		boolDefault = false;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"cg.spark"+" "+"dump-solution";
		value = settings.getBoolean(key.trim());
		
		boolDefault = false;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"cg.spark"+" "+"topo-sort";
		value = settings.getBoolean(key.trim());
		
		boolDefault = false;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"cg.spark"+" "+"dump-types";
		value = settings.getBoolean(key.trim());
		
		boolDefault = true;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"cg.spark"+" "+"class-method-var";
		value = settings.getBoolean(key.trim());
		
		boolDefault = true;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"cg.spark"+" "+"dump-answer";
		value = settings.getBoolean(key.trim());
		
		boolDefault = false;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"cg.spark"+" "+"trim-invoke-graph";
		value = settings.getBoolean(key.trim());
		
		boolDefault = true;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"cg.spark"+" "+"add-tags";
		value = settings.getBoolean(key.trim());
		
		boolDefault = false;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"wstp"+" "+"disabled";
		value = settings.getBoolean(key.trim());
		
		boolDefault = false;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"wsop"+" "+"disabled";
		value = settings.getBoolean(key.trim());
		
		boolDefault = false;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"wjtp"+" "+"disabled";
		value = settings.getBoolean(key.trim());
		
		boolDefault = true;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"wjop"+" "+"disabled";
		value = settings.getBoolean(key.trim());
		
		boolDefault = true;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"wjop.smb"+" "+"disabled";
		value = settings.getBoolean(key.trim());
		
		boolDefault = true;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"wjop.smb"+" "+"insert-null-checks";
		value = settings.getBoolean(key.trim());
		
		boolDefault = true;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"wjop.smb"+" "+"insert-redundant-casts";
		value = settings.getBoolean(key.trim());
		
		boolDefault = true;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		 
		key = "p"+" "+"wjop.smb"+" "+"allowed-modifier-changes";
		path = settings.get(key.trim());
		
		defaultVal = "unsafe";
		
		
		if ((path != null) && (path.length() != 0) && (!path.equals(defaultVal))) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(COLON);
			cmd.append(path);
			cmd.append(SPACE);
		}	
		
		key = "p"+" "+"wjop.si"+" "+"disabled";
		value = settings.getBoolean(key.trim());
		
		boolDefault = false;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"wjop.si"+" "+"insert-null-checks";
		value = settings.getBoolean(key.trim());
		
		boolDefault = true;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"wjop.si"+" "+"insert-redundant-casts";
		value = settings.getBoolean(key.trim());
		
		boolDefault = true;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"wjop.si"+" "+"expansion-factor";
		
		
		defaultVal = "3";
		
				
		
		path = settings.get(key.trim());
		if ((path != null) && (path.length() != 0) && (!path.equals(defaultVal)) ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(COLON);
			cmd.append(path);
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"wjop.si"+" "+"max-container-size";
		
		
		defaultVal = "5000";
		
				
		
		path = settings.get(key.trim());
		if ((path != null) && (path.length() != 0) && (!path.equals(defaultVal)) ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(COLON);
			cmd.append(path);
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"wjop.si"+" "+"max-inlinee-size";
		
		
		defaultVal = "20";
		
				
		
		path = settings.get(key.trim());
		if ((path != null) && (path.length() != 0) && (!path.equals(defaultVal)) ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(COLON);
			cmd.append(path);
			cmd.append(SPACE);
		}
		 
		key = "p"+" "+"wjop.si"+" "+"allowed-modifier-changes";
		path = settings.get(key.trim());
		
		defaultVal = "unsafe";
		
		
		if ((path != null) && (path.length() != 0) && (!path.equals(defaultVal))) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(COLON);
			cmd.append(path);
			cmd.append(SPACE);
		}

		key = "p"+" "+"wjap.abc"+" "+"disabled";
		value = settings.getBoolean(key.trim());

		boolDefault = true;


		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}

		key = "p"+" "+"wjap.abc"+" "+"with-all";
		value = settings.getBoolean(key.trim());

		boolDefault = false;


		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}

		key = "p"+" "+"wjap.abc"+" "+"with-fieldref";
		value = settings.getBoolean(key.trim());

		boolDefault = false;


		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}

		key = "p"+" "+"wjap.abc"+" "+"with-arrayref";
		value = settings.getBoolean(key.trim());

		boolDefault = false;


		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}

		key = "p"+" "+"wjap.abc"+" "+"with-cse";
		value = settings.getBoolean(key.trim());

		boolDefault = false;


		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}

		key = "p"+" "+"wjap.abc"+" "+"with-classfield";
		value = settings.getBoolean(key.trim());

		boolDefault = false;


		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}

		key = "p"+" "+"wjap.abc"+" "+"with-rectarray";
		value = settings.getBoolean(key.trim());

		boolDefault = false;


		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}

		key = "p"+" "+"wjap.abc"+" "+"profiling";
		value = settings.getBoolean(key.trim());

		boolDefault = false;


		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"wjtp2"+" "+"disabled";
		value = settings.getBoolean(key.trim());
		
		boolDefault = false;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"wjtp2.ra"+" "+"disabled";
		value = settings.getBoolean(key.trim());
		
		boolDefault = true;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"stp"+" "+"disabled";
		value = settings.getBoolean(key.trim());
		
		boolDefault = false;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"sop"+" "+"disabled";
		value = settings.getBoolean(key.trim());
		
		boolDefault = false;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"jtp"+" "+"disabled";
		value = settings.getBoolean(key.trim());
		
		boolDefault = false;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"jop"+" "+"disabled";
		value = settings.getBoolean(key.trim());
		
		boolDefault = false;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"jop.cse"+" "+"disabled";
		value = settings.getBoolean(key.trim());
		
		boolDefault = true;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"jop.cse"+" "+"naive-side-effect";
		value = settings.getBoolean(key.trim());
		
		boolDefault = false;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"jop.bcm"+" "+"disabled";
		value = settings.getBoolean(key.trim());
		
		boolDefault = true;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"jop.bcm"+" "+"naive-side-effect";
		value = settings.getBoolean(key.trim());
		
		boolDefault = false;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"jop.lcm"+" "+"disabled";
		value = settings.getBoolean(key.trim());
		
		boolDefault = true;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"jop.lcm"+" "+"unroll";
		value = settings.getBoolean(key.trim());
		
		boolDefault = true;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"jop.lcm"+" "+"naive-side-effect";
		value = settings.getBoolean(key.trim());
		
		boolDefault = false;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		 
		key = "p"+" "+"jop.lcm"+" "+"safe";
		path = settings.get(key.trim());
		
		defaultVal = "safe";
		
		
		if ((path != null) && (path.length() != 0) && (!path.equals(defaultVal))) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(COLON);
			cmd.append(path);
			cmd.append(SPACE);
		}	
		
		key = "p"+" "+"jop.cp"+" "+"disabled";
		value = settings.getBoolean(key.trim());
		
		boolDefault = false;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"jop.cp"+" "+"only-regular-locals";
		value = settings.getBoolean(key.trim());
		
		boolDefault = false;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"jop.cp"+" "+"only-stack-locals";
		value = settings.getBoolean(key.trim());
		
		boolDefault = false;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"jop.cpf"+" "+"disabled";
		value = settings.getBoolean(key.trim());
		
		boolDefault = false;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"jop.cbf"+" "+"disabled";
		value = settings.getBoolean(key.trim());
		
		boolDefault = false;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"jop.dae"+" "+"disabled";
		value = settings.getBoolean(key.trim());
		
		boolDefault = false;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"jop.dae"+" "+"only-stack-locals";
		value = settings.getBoolean(key.trim());
		
		boolDefault = false;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"jop.uce1"+" "+"disabled";
		value = settings.getBoolean(key.trim());
		
		boolDefault = false;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"jop.uce2"+" "+"disabled";
		value = settings.getBoolean(key.trim());
		
		boolDefault = false;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"jop.ubf1"+" "+"disabled";
		value = settings.getBoolean(key.trim());
		
		boolDefault = false;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"jop.ubf2"+" "+"disabled";
		value = settings.getBoolean(key.trim());
		
		boolDefault = false;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"jop.ule"+" "+"disabled";
		value = settings.getBoolean(key.trim());
		
		boolDefault = false;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"jap"+" "+"disabled";
		value = settings.getBoolean(key.trim());
		
		boolDefault = false;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"jap.npc"+" "+"disabled";
		value = settings.getBoolean(key.trim());
		
		boolDefault = true;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"jap.npc"+" "+"only-array-ref";
		value = settings.getBoolean(key.trim());
		
		boolDefault = false;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"jap.npc"+" "+"profiling";
		value = settings.getBoolean(key.trim());
		
		boolDefault = false;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"jap.profiling"+" "+"disabled";
		value = settings.getBoolean(key.trim());
		
		boolDefault = true;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"jap.profiling"+" "+"notmainentry";
		value = settings.getBoolean(key.trim());
		
		boolDefault = false;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"jap.sea"+" "+"disabled";
		value = settings.getBoolean(key.trim());
		
		boolDefault = true;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"jap.sea"+" "+"naive";
		value = settings.getBoolean(key.trim());
		
		boolDefault = false;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"jap.fieldrw"+" "+"disabled";
		value = settings.getBoolean(key.trim());
		
		boolDefault = true;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"jap.fieldrw"+" "+"threshold";
		
		
		defaultVal = "100";
		
				
		
		path = settings.get(key.trim());
		if ((path != null) && (path.length() != 0) && (!path.equals(defaultVal)) ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(COLON);
			cmd.append(path);
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"gb"+" "+"disabled";
		value = settings.getBoolean(key.trim());
		
		boolDefault = false;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"gb.a1"+" "+"disabled";
		value = settings.getBoolean(key.trim());
		
		boolDefault = false;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"gb.a1"+" "+"only-stack-locals";
		value = settings.getBoolean(key.trim());
		
		boolDefault = true;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"gb.cf"+" "+"disabled";
		value = settings.getBoolean(key.trim());
		
		boolDefault = false;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"gb.a2"+" "+"disabled";
		value = settings.getBoolean(key.trim());
		
		boolDefault = false;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"gb.a2"+" "+"only-stack-locals";
		value = settings.getBoolean(key.trim());
		
		boolDefault = true;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"gb.ule"+" "+"disabled";
		value = settings.getBoolean(key.trim());
		
		boolDefault = false;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"gop"+" "+"disabled";
		value = settings.getBoolean(key.trim());
		
		boolDefault = false;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"bb"+" "+"disabled";
		value = settings.getBoolean(key.trim());
		
		boolDefault = false;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"bb.lso"+" "+"disabled";
		value = settings.getBoolean(key.trim());
		
		boolDefault = false;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"bb.lso"+" "+"debug";
		value = settings.getBoolean(key.trim());
		
		boolDefault = false;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"bb.lso"+" "+"inter";
		value = settings.getBoolean(key.trim());
		
		boolDefault = false;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"bb.lso"+" "+"sl";
		value = settings.getBoolean(key.trim());
		
		boolDefault = true;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"bb.lso"+" "+"sl2";
		value = settings.getBoolean(key.trim());
		
		boolDefault = false;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"bb.lso"+" "+"sll";
		value = settings.getBoolean(key.trim());
		
		boolDefault = true;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"bb.lso"+" "+"sll2";
		value = settings.getBoolean(key.trim());
		
		boolDefault = false;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"bb.pho"+" "+"disabled";
		value = settings.getBoolean(key.trim());
		
		boolDefault = false;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"bb.ule"+" "+"disabled";
		value = settings.getBoolean(key.trim());
		
		boolDefault = false;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"bb.lp"+" "+"disabled";
		value = settings.getBoolean(key.trim());
		
		boolDefault = false;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"bb.lp"+" "+"unsplit-original-locals";
		value = settings.getBoolean(key.trim());
		
		boolDefault = false;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"bop"+" "+"disabled";
		value = settings.getBoolean(key.trim());
		
		boolDefault = false;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"tag"+" "+"disabled";
		value = settings.getBoolean(key.trim());
		
		boolDefault = false;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"tag.ln"+" "+"disabled";
		value = settings.getBoolean(key.trim());
		
		boolDefault = true;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"tag.an"+" "+"disabled";
		value = settings.getBoolean(key.trim());
		
		boolDefault = true;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"tag.dep"+" "+"disabled";
		value = settings.getBoolean(key.trim());
		
		boolDefault = true;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		key = "p"+" "+"tag.fieldrw"+" "+"disabled";
		value = settings.getBoolean(key.trim());
		
		boolDefault = true;
		
		
		if (value != boolDefault ) {
			cmd.append(DASH);
			cmd.append(key.trim());
			cmd.append(SPACE);
		}
		
		
		return cmd.toString();
	}

}

