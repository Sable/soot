package soot;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2004 Ondrej Lhotak
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

/* THIS FILE IS AUTO-GENERATED FROM soot_options.xml. DO NOT MODIFY. */

import soot.*;
import org.apache.tools.ant.*;
import org.apache.tools.ant.taskdefs.*;
import org.apache.tools.ant.types.*;
import java.util.*;

/**
 * Soot ant task.
 * @author Ondrej Lhotak
 */
@javax.annotation.Generated(value = "Saxonica v3.0", comments = "from soot_options.xml")
public class AntTask extends MatchingTask {
    public static final boolean DEBUG = true;
    private void debug(String s) {
        if(DEBUG) System.err.println(s);
    }
    private ArrayList args = new ArrayList();
    public List args() { return args; }
    private void addArg( String s ) { args.add(s); }
    private void addArg( String s, String s2 ) { args.add(s); args.add(s2); }
    private Path appendToPath( Path old, Path newPath ) {
        if( old == null ) return newPath;
        old.append(newPath);
        return old;
    }
    private void addPath(String option, Path path) {
        if( path.size() == 0 ) return;
        addArg(option);
        addArg(path.toString());
    }
    private List phaseopts = new ArrayList(); 

        private Path phase_help = null;
  
        private Path process_dir = null;
  
        private Path process_jar_dir = null;
  
        private Path dump_body = null;
  
        private Path dump_cfg = null;
  
        private Path plugin = null;
  
        private Path include = null;
  
        private Path exclude = null;
  
        private Path dynamic_class = null;
  
        private Path dynamic_dir = null;
  
        private Path dynamic_package = null;
  
    public void execute() throws BuildException {

        if( phase_help != null ) addPath("-phase-help", phase_help);
  
        if( process_dir != null ) addPath("-process-dir", process_dir);
  
        if( process_jar_dir != null ) addPath("-process-jar-dir", process_jar_dir);
  
        if( dump_body != null ) addPath("-dump-body", dump_body);
  
        if( dump_cfg != null ) addPath("-dump-cfg", dump_cfg);
  
        if( plugin != null ) addPath("-plugin", plugin);
  
        if( include != null ) addPath("-include", include);
  
        if( exclude != null ) addPath("-exclude", exclude);
  
        if( dynamic_class != null ) addPath("-dynamic-class", dynamic_class);
  
        if( dynamic_dir != null ) addPath("-dynamic-dir", dynamic_dir);
  
        if( dynamic_package != null ) addPath("-dynamic-package", dynamic_package);
  
        if(DEBUG) System.out.println(args);
        try {
            soot.Main.main((String[]) args.toArray(new String[0]));
            soot.G.v().reset();
        } catch( Exception e ) {
            e.printStackTrace();
            throw new BuildException(e);
        }
    }



        public void setcoffi(boolean arg) {
            if(arg) addArg("-coffi");
        }
  
        public void setjasmin_backend(boolean arg) {
            if(arg) addArg("-jasmin-backend");
        }
  
        public void sethelp(boolean arg) {
            if(arg) addArg("-help");
        }
  
        public void setphase_list(boolean arg) {
            if(arg) addArg("-phase-list");
        }
  
        public void setphase_help(Path arg) {
            if(phase_help == null )
                phase_help = new Path(getProject());
            phase_help = appendToPath(phase_help, arg);
        }

        public Path createphase_help() {
            if(phase_help == null )
                phase_help = new Path(getProject());
            return phase_help.createPath();
        }
  
        public void setversion(boolean arg) {
            if(arg) addArg("-version");
        }
  
        public void setverbose(boolean arg) {
            if(arg) addArg("-verbose");
        }
  
        public void setinteractive_mode(boolean arg) {
            if(arg) addArg("-interactive-mode");
        }
  
        public void setunfriendly_mode(boolean arg) {
            if(arg) addArg("-unfriendly-mode");
        }
  
        public void setapp(boolean arg) {
            if(arg) addArg("-app");
        }
  
        public void setwhole_program(boolean arg) {
            if(arg) addArg("-whole-program");
        }
  
        public void setwhole_shimple(boolean arg) {
            if(arg) addArg("-whole-shimple");
        }
  
        public void seton_the_fly(boolean arg) {
            if(arg) addArg("-on-the-fly");
        }
  
        public void setvalidate(boolean arg) {
            if(arg) addArg("-validate");
        }
  
        public void setdebug(boolean arg) {
            if(arg) addArg("-debug");
        }
  
        public void setdebug_resolver(boolean arg) {
            if(arg) addArg("-debug-resolver");
        }
  
        public void setignore_resolving_levels(boolean arg) {
            if(arg) addArg("-ignore-resolving-levels");
        }
  
        public void setweak_map_structures(boolean arg) {
            if(arg) addArg("-weak-map-structures");
        }
  
        public void setsoot_classpath(String arg) {
            addArg("-soot-classpath");
            addArg(arg);
        }
  
        public void setsoot_modulepath(String arg) {
            addArg("-soot-modulepath");
            addArg(arg);
        }
  
        public void setprepend_classpath(boolean arg) {
            if(arg) addArg("-prepend-classpath");
        }
  
        public void setignore_classpath_errors(boolean arg) {
            if(arg) addArg("-ignore-classpath-errors");
        }
  
        public void setprocess_multiple_dex(boolean arg) {
            if(arg) addArg("-process-multiple-dex");
        }
  
        public void setsearch_dex_in_archives(boolean arg) {
            if(arg) addArg("-search-dex-in-archives");
        }
  
        public void setprocess_dir(Path arg) {
            if(process_dir == null )
                process_dir = new Path(getProject());
            process_dir = appendToPath(process_dir, arg);
        }

        public Path createprocess_dir() {
            if(process_dir == null )
                process_dir = new Path(getProject());
            return process_dir.createPath();
        }
  
        public void setprocess_jar_dir(Path arg) {
            if(process_jar_dir == null )
                process_jar_dir = new Path(getProject());
            process_jar_dir = appendToPath(process_jar_dir, arg);
        }

        public Path createprocess_jar_dir() {
            if(process_jar_dir == null )
                process_jar_dir = new Path(getProject());
            return process_jar_dir.createPath();
        }
  
        public void setderive_java_version(boolean arg) {
            if(arg) addArg("-derive-java-version");
        }
  
        public void setoaat(boolean arg) {
            if(arg) addArg("-oaat");
        }
  
        public void setandroid_jars(String arg) {
            addArg("-android-jars");
            addArg(arg);
        }
  
        public void setforce_android_jar(String arg) {
            addArg("-force-android-jar");
            addArg(arg);
        }
  
        public void setast_metrics(boolean arg) {
            if(arg) addArg("-ast-metrics");
        }
  
        public void setsrc_prec(String arg) {
            if(false
    
                || arg.equals( "c" )
                || arg.equals( "class" )
                || arg.equals( "only-class" )
                || arg.equals( "J" )
                || arg.equals( "jimple" )
                || arg.equals( "java" )
                || arg.equals( "apk" )
                || arg.equals( "apk-class-jimple" )
                || arg.equals( "apk-c-j" )
                ) {
                addArg("-src-prec");
                addArg(arg);
            } else {
                throw new BuildException("Bad value "+arg+" for option src_prec");
            }
        }
  
        public void setfull_resolver(boolean arg) {
            if(arg) addArg("-full-resolver");
        }
  
        public void setallow_phantom_refs(boolean arg) {
            if(arg) addArg("-allow-phantom-refs");
        }
  
        public void setallow_phantom_elms(boolean arg) {
            if(arg) addArg("-allow-phantom-elms");
        }
  
        public void setallow_cg_errors(boolean arg) {
            if(arg) addArg("-allow-cg-errors");
        }
  
        public void setno_bodies_for_excluded(boolean arg) {
            if(arg) addArg("-no-bodies-for-excluded");
        }
  
        public void setj2me(boolean arg) {
            if(arg) addArg("-j2me");
        }
  
        public void setmain_class(String arg) {
            addArg("-main-class");
            addArg(arg);
        }
  
        public void setpolyglot(boolean arg) {
            if(arg) addArg("-polyglot");
        }
  
        public void setpermissive_resolving(boolean arg) {
            if(arg) addArg("-permissive-resolving");
        }
  
        public void setdrop_bodies_after_load(boolean arg) {
            if(arg) addArg("-drop-bodies-after-load");
        }
  
        public void setoutput_dir(String arg) {
            addArg("-output-dir");
            addArg(arg);
        }
  
        public void setoutput_format(String arg) {
            if(false
    
                || arg.equals( "J" )
                || arg.equals( "jimple" )
                || arg.equals( "j" )
                || arg.equals( "jimp" )
                || arg.equals( "S" )
                || arg.equals( "shimple" )
                || arg.equals( "s" )
                || arg.equals( "shimp" )
                || arg.equals( "B" )
                || arg.equals( "baf" )
                || arg.equals( "b" )
                || arg.equals( "G" )
                || arg.equals( "grimple" )
                || arg.equals( "g" )
                || arg.equals( "grimp" )
                || arg.equals( "X" )
                || arg.equals( "xml" )
                || arg.equals( "dex" )
                || arg.equals( "force-dex" )
                || arg.equals( "n" )
                || arg.equals( "none" )
                || arg.equals( "jasmin" )
                || arg.equals( "c" )
                || arg.equals( "class" )
                || arg.equals( "d" )
                || arg.equals( "dava" )
                || arg.equals( "t" )
                || arg.equals( "template" )
                || arg.equals( "a" )
                || arg.equals( "asm" )
                ) {
                addArg("-output-format");
                addArg(arg);
            } else {
                throw new BuildException("Bad value "+arg+" for option output_format");
            }
        }
  
        public void setjava_version(String arg) {
            if(false
    
                || arg.equals( "default" )
                || arg.equals( "1.1" )
                || arg.equals( "1" )
                || arg.equals( "1.2" )
                || arg.equals( "2" )
                || arg.equals( "1.3" )
                || arg.equals( "3" )
                || arg.equals( "1.4" )
                || arg.equals( "4" )
                || arg.equals( "1.5" )
                || arg.equals( "5" )
                || arg.equals( "1.6" )
                || arg.equals( "6" )
                || arg.equals( "1.7" )
                || arg.equals( "7" )
                || arg.equals( "1.8" )
                || arg.equals( "8" )
                || arg.equals( "1.9" )
                || arg.equals( "9" )
                || arg.equals( "1.10" )
                || arg.equals( "10" )
                || arg.equals( "1.11" )
                || arg.equals( "11" )
                || arg.equals( "1.12" )
                || arg.equals( "12" )
                ) {
                addArg("-java-version");
                addArg(arg);
            } else {
                throw new BuildException("Bad value "+arg+" for option java_version");
            }
        }
  
        public void setoutput_jar(boolean arg) {
            if(arg) addArg("-output-jar");
        }
  
        public void sethierarchy_dirs(boolean arg) {
            if(arg) addArg("-hierarchy-dirs");
        }
  
        public void setxml_attributes(boolean arg) {
            if(arg) addArg("-xml-attributes");
        }
  
        public void setprint_tags_in_output(boolean arg) {
            if(arg) addArg("-print-tags-in-output");
        }
  
        public void setno_output_source_file_attribute(boolean arg) {
            if(arg) addArg("-no-output-source-file-attribute");
        }
  
        public void setno_output_inner_classes_attribute(boolean arg) {
            if(arg) addArg("-no-output-inner-classes-attribute");
        }
  
        public void setdump_body(Path arg) {
            if(dump_body == null )
                dump_body = new Path(getProject());
            dump_body = appendToPath(dump_body, arg);
        }

        public Path createdump_body() {
            if(dump_body == null )
                dump_body = new Path(getProject());
            return dump_body.createPath();
        }
  
        public void setdump_cfg(Path arg) {
            if(dump_cfg == null )
                dump_cfg = new Path(getProject());
            dump_cfg = appendToPath(dump_cfg, arg);
        }

        public Path createdump_cfg() {
            if(dump_cfg == null )
                dump_cfg = new Path(getProject());
            return dump_cfg.createPath();
        }
  
        public void setshow_exception_dests(boolean arg) {
            if(arg) addArg("-show-exception-dests");
        }
  
        public void setgzip(boolean arg) {
            if(arg) addArg("-gzip");
        }
  
        public void setforce_overwrite(boolean arg) {
            if(arg) addArg("-force-overwrite");
        }
  
        public void setplugin(Path arg) {
            if(plugin == null )
                plugin = new Path(getProject());
            plugin = appendToPath(plugin, arg);
        }

        public Path createplugin() {
            if(plugin == null )
                plugin = new Path(getProject());
            return plugin.createPath();
        }
  
        public void setwrong_staticness(String arg) {
            if(false
    
                || arg.equals( "fail" )
                || arg.equals( "ignore" )
                || arg.equals( "fix" )
                || arg.equals( "fixstrict" )
                ) {
                addArg("-wrong-staticness");
                addArg(arg);
            } else {
                throw new BuildException("Bad value "+arg+" for option wrong_staticness");
            }
        }
  
        public void setfield_type_mismatches(String arg) {
            if(false
    
                || arg.equals( "fail" )
                || arg.equals( "ignore" )
                || arg.equals( "null" )
                ) {
                addArg("-field-type-mismatches");
                addArg(arg);
            } else {
                throw new BuildException("Bad value "+arg+" for option field_type_mismatches");
            }
        }
  
        public void setoptimize(boolean arg) {
            if(arg) addArg("-optimize");
        }
  
        public void setwhole_optimize(boolean arg) {
            if(arg) addArg("-whole-optimize");
        }
  
        public void setvia_grimp(boolean arg) {
            if(arg) addArg("-via-grimp");
        }
  
        public void setvia_shimple(boolean arg) {
            if(arg) addArg("-via-shimple");
        }
  
        public void setthrow_analysis(String arg) {
            if(false
    
                || arg.equals( "pedantic" )
                || arg.equals( "unit" )
                || arg.equals( "dalvik" )
                || arg.equals( "auto-select" )
                ) {
                addArg("-throw-analysis");
                addArg(arg);
            } else {
                throw new BuildException("Bad value "+arg+" for option throw_analysis");
            }
        }
  
        public void setcheck_init_throw_analysis(String arg) {
            if(false
    
                || arg.equals( "auto" )
                || arg.equals( "pedantic" )
                || arg.equals( "unit" )
                || arg.equals( "dalvik" )
                ) {
                addArg("-check-init-throw-analysis");
                addArg(arg);
            } else {
                throw new BuildException("Bad value "+arg+" for option check_init_throw_analysis");
            }
        }
  
        public void setomit_excepting_unit_edges(boolean arg) {
            if(arg) addArg("-omit-excepting-unit-edges");
        }
  
        public void settrim_cfgs(boolean arg) {
            if(arg) addArg("-trim-cfgs");
        }
  
        public void setignore_resolution_errors(boolean arg) {
            if(arg) addArg("-ignore-resolution-errors");
        }
  
        public void setinclude(Path arg) {
            if(include == null )
                include = new Path(getProject());
            include = appendToPath(include, arg);
        }

        public Path createinclude() {
            if(include == null )
                include = new Path(getProject());
            return include.createPath();
        }
  
        public void setexclude(Path arg) {
            if(exclude == null )
                exclude = new Path(getProject());
            exclude = appendToPath(exclude, arg);
        }

        public Path createexclude() {
            if(exclude == null )
                exclude = new Path(getProject());
            return exclude.createPath();
        }
  
        public void setinclude_all(boolean arg) {
            if(arg) addArg("-include-all");
        }
  
        public void setdynamic_class(Path arg) {
            if(dynamic_class == null )
                dynamic_class = new Path(getProject());
            dynamic_class = appendToPath(dynamic_class, arg);
        }

        public Path createdynamic_class() {
            if(dynamic_class == null )
                dynamic_class = new Path(getProject());
            return dynamic_class.createPath();
        }
  
        public void setdynamic_dir(Path arg) {
            if(dynamic_dir == null )
                dynamic_dir = new Path(getProject());
            dynamic_dir = appendToPath(dynamic_dir, arg);
        }

        public Path createdynamic_dir() {
            if(dynamic_dir == null )
                dynamic_dir = new Path(getProject());
            return dynamic_dir.createPath();
        }
  
        public void setdynamic_package(Path arg) {
            if(dynamic_package == null )
                dynamic_package = new Path(getProject());
            dynamic_package = appendToPath(dynamic_package, arg);
        }

        public Path createdynamic_package() {
            if(dynamic_package == null )
                dynamic_package = new Path(getProject());
            return dynamic_package.createPath();
        }
  
        public void setkeep_line_number(boolean arg) {
            if(arg) addArg("-keep-line-number");
        }
  
        public void setkeep_offset(boolean arg) {
            if(arg) addArg("-keep-offset");
        }
  
        public void setwrite_local_annotations(boolean arg) {
            if(arg) addArg("-write-local-annotations");
        }
  
        public void setannot_purity(boolean arg) {
            if(arg) addArg("-annot-purity");
        }
  
        public void setannot_arraybounds(boolean arg) {
            if(arg) addArg("-annot-arraybounds");
        }
  
        public void setannot_nullpointer(boolean arg) {
            if(arg) addArg("-annot-nullpointer");
        }
  
        public void setannot_side_effect(boolean arg) {
            if(arg) addArg("-annot-side-effect");
        }
  
        public void setannot_fieldrw(boolean arg) {
            if(arg) addArg("-annot-fieldrw");
        }
  
        public void settime(boolean arg) {
            if(arg) addArg("-time");
        }
  
        public void setsubtract_gc(boolean arg) {
            if(arg) addArg("-subtract-gc");
        }
  
        public void setno_writeout_body_releasing(boolean arg) {
            if(arg) addArg("-no-writeout-body-releasing");
        }
  
        public Object createp_jb() {
            Object ret = new PhaseOptjb();
            phaseopts.add(ret);
            return ret;
        }
        public class PhaseOptjb {
      
          public void setenabled(boolean arg) {
            addArg("-p");
            addArg("jb");
            addArg("enabled:"+(arg?"true":"false"));
          }
      
          public void setuse_original_names(boolean arg) {
            addArg("-p");
            addArg("jb");
            addArg("use-original-names:"+(arg?"true":"false"));
          }
      
          public void setpreserve_source_annotations(boolean arg) {
            addArg("-p");
            addArg("jb");
            addArg("preserve-source-annotations:"+(arg?"true":"false"));
          }
      
          public void setstabilize_local_names(boolean arg) {
            addArg("-p");
            addArg("jb");
            addArg("stabilize-local-names:"+(arg?"true":"false"));
          }
      
          public void setmodel_lambdametafactory(boolean arg) {
            addArg("-p");
            addArg("jb");
            addArg("model-lambdametafactory:"+(arg?"true":"false"));
          }
      
        }
    
        public Object createp_jb_dtr() {
            Object ret = new PhaseOptjb_dtr();
            phaseopts.add(ret);
            return ret;
        }
        public class PhaseOptjb_dtr {
      
          public void setenabled(boolean arg) {
            addArg("-p");
            addArg("jb.dtr");
            addArg("enabled:"+(arg?"true":"false"));
          }
      
        }
    
        public Object createp_jb_ese() {
            Object ret = new PhaseOptjb_ese();
            phaseopts.add(ret);
            return ret;
        }
        public class PhaseOptjb_ese {
      
          public void setenabled(boolean arg) {
            addArg("-p");
            addArg("jb.ese");
            addArg("enabled:"+(arg?"true":"false"));
          }
      
        }
    
        public Object createp_jb_ls() {
            Object ret = new PhaseOptjb_ls();
            phaseopts.add(ret);
            return ret;
        }
        public class PhaseOptjb_ls {
      
          public void setenabled(boolean arg) {
            addArg("-p");
            addArg("jb.ls");
            addArg("enabled:"+(arg?"true":"false"));
          }
      
        }
    
        public Object createp_jb_sils() {
            Object ret = new PhaseOptjb_sils();
            phaseopts.add(ret);
            return ret;
        }
        public class PhaseOptjb_sils {
      
          public void setenabled(boolean arg) {
            addArg("-p");
            addArg("jb.sils");
            addArg("enabled:"+(arg?"true":"false"));
          }
      
        }
    
        public Object createp_jb_a() {
            Object ret = new PhaseOptjb_a();
            phaseopts.add(ret);
            return ret;
        }
        public class PhaseOptjb_a {
      
          public void setenabled(boolean arg) {
            addArg("-p");
            addArg("jb.a");
            addArg("enabled:"+(arg?"true":"false"));
          }
      
          public void setonly_stack_locals(boolean arg) {
            addArg("-p");
            addArg("jb.a");
            addArg("only-stack-locals:"+(arg?"true":"false"));
          }
      
        }
    
        public Object createp_jb_ule() {
            Object ret = new PhaseOptjb_ule();
            phaseopts.add(ret);
            return ret;
        }
        public class PhaseOptjb_ule {
      
          public void setenabled(boolean arg) {
            addArg("-p");
            addArg("jb.ule");
            addArg("enabled:"+(arg?"true":"false"));
          }
      
        }
    
        public Object createp_jb_tr() {
            Object ret = new PhaseOptjb_tr();
            phaseopts.add(ret);
            return ret;
        }
        public class PhaseOptjb_tr {
      
          public void setenabled(boolean arg) {
            addArg("-p");
            addArg("jb.tr");
            addArg("enabled:"+(arg?"true":"false"));
          }
      
          public void setuse_older_type_assigner(boolean arg) {
            addArg("-p");
            addArg("jb.tr");
            addArg("use-older-type-assigner:"+(arg?"true":"false"));
          }
      
          public void setcompare_type_assigners(boolean arg) {
            addArg("-p");
            addArg("jb.tr");
            addArg("compare-type-assigners:"+(arg?"true":"false"));
          }
      
          public void setignore_nullpointer_dereferences(boolean arg) {
            addArg("-p");
            addArg("jb.tr");
            addArg("ignore-nullpointer-dereferences:"+(arg?"true":"false"));
          }
      
        }
    
        public Object createp_jb_ulp() {
            Object ret = new PhaseOptjb_ulp();
            phaseopts.add(ret);
            return ret;
        }
        public class PhaseOptjb_ulp {
      
          public void setenabled(boolean arg) {
            addArg("-p");
            addArg("jb.ulp");
            addArg("enabled:"+(arg?"true":"false"));
          }
      
          public void setunsplit_original_locals(boolean arg) {
            addArg("-p");
            addArg("jb.ulp");
            addArg("unsplit-original-locals:"+(arg?"true":"false"));
          }
      
        }
    
        public Object createp_jb_lns() {
            Object ret = new PhaseOptjb_lns();
            phaseopts.add(ret);
            return ret;
        }
        public class PhaseOptjb_lns {
      
          public void setenabled(boolean arg) {
            addArg("-p");
            addArg("jb.lns");
            addArg("enabled:"+(arg?"true":"false"));
          }
      
          public void setonly_stack_locals(boolean arg) {
            addArg("-p");
            addArg("jb.lns");
            addArg("only-stack-locals:"+(arg?"true":"false"));
          }
      
          public void setsort_locals(boolean arg) {
            addArg("-p");
            addArg("jb.lns");
            addArg("sort-locals:"+(arg?"true":"false"));
          }
      
        }
    
        public Object createp_jb_cp() {
            Object ret = new PhaseOptjb_cp();
            phaseopts.add(ret);
            return ret;
        }
        public class PhaseOptjb_cp {
      
          public void setenabled(boolean arg) {
            addArg("-p");
            addArg("jb.cp");
            addArg("enabled:"+(arg?"true":"false"));
          }
      
          public void setonly_regular_locals(boolean arg) {
            addArg("-p");
            addArg("jb.cp");
            addArg("only-regular-locals:"+(arg?"true":"false"));
          }
      
          public void setonly_stack_locals(boolean arg) {
            addArg("-p");
            addArg("jb.cp");
            addArg("only-stack-locals:"+(arg?"true":"false"));
          }
      
        }
    
        public Object createp_jb_dae() {
            Object ret = new PhaseOptjb_dae();
            phaseopts.add(ret);
            return ret;
        }
        public class PhaseOptjb_dae {
      
          public void setenabled(boolean arg) {
            addArg("-p");
            addArg("jb.dae");
            addArg("enabled:"+(arg?"true":"false"));
          }
      
          public void setonly_stack_locals(boolean arg) {
            addArg("-p");
            addArg("jb.dae");
            addArg("only-stack-locals:"+(arg?"true":"false"));
          }
      
        }
    
        public Object createp_jb_cp_ule() {
            Object ret = new PhaseOptjb_cp_ule();
            phaseopts.add(ret);
            return ret;
        }
        public class PhaseOptjb_cp_ule {
      
          public void setenabled(boolean arg) {
            addArg("-p");
            addArg("jb.cp-ule");
            addArg("enabled:"+(arg?"true":"false"));
          }
      
        }
    
        public Object createp_jb_lp() {
            Object ret = new PhaseOptjb_lp();
            phaseopts.add(ret);
            return ret;
        }
        public class PhaseOptjb_lp {
      
          public void setenabled(boolean arg) {
            addArg("-p");
            addArg("jb.lp");
            addArg("enabled:"+(arg?"true":"false"));
          }
      
          public void setunsplit_original_locals(boolean arg) {
            addArg("-p");
            addArg("jb.lp");
            addArg("unsplit-original-locals:"+(arg?"true":"false"));
          }
      
        }
    
        public Object createp_jb_ne() {
            Object ret = new PhaseOptjb_ne();
            phaseopts.add(ret);
            return ret;
        }
        public class PhaseOptjb_ne {
      
          public void setenabled(boolean arg) {
            addArg("-p");
            addArg("jb.ne");
            addArg("enabled:"+(arg?"true":"false"));
          }
      
        }
    
        public Object createp_jb_uce() {
            Object ret = new PhaseOptjb_uce();
            phaseopts.add(ret);
            return ret;
        }
        public class PhaseOptjb_uce {
      
          public void setenabled(boolean arg) {
            addArg("-p");
            addArg("jb.uce");
            addArg("enabled:"+(arg?"true":"false"));
          }
      
          public void setremove_unreachable_traps(boolean arg) {
            addArg("-p");
            addArg("jb.uce");
            addArg("remove-unreachable-traps:"+(arg?"true":"false"));
          }
      
        }
    
        public Object createp_jb_tt() {
            Object ret = new PhaseOptjb_tt();
            phaseopts.add(ret);
            return ret;
        }
        public class PhaseOptjb_tt {
      
          public void setenabled(boolean arg) {
            addArg("-p");
            addArg("jb.tt");
            addArg("enabled:"+(arg?"true":"false"));
          }
      
        }
    
        public Object createp_jb_cbf() {
            Object ret = new PhaseOptjb_cbf();
            phaseopts.add(ret);
            return ret;
        }
        public class PhaseOptjb_cbf {
      
          public void setenabled(boolean arg) {
            addArg("-p");
            addArg("jb.cbf");
            addArg("enabled:"+(arg?"true":"false"));
          }
      
        }
    
        public Object createp_jj() {
            Object ret = new PhaseOptjj();
            phaseopts.add(ret);
            return ret;
        }
        public class PhaseOptjj {
      
          public void setenabled(boolean arg) {
            addArg("-p");
            addArg("jj");
            addArg("enabled:"+(arg?"true":"false"));
          }
      
          public void setuse_original_names(boolean arg) {
            addArg("-p");
            addArg("jj");
            addArg("use-original-names:"+(arg?"true":"false"));
          }
      
        }
    
        public Object createp_jj_ls() {
            Object ret = new PhaseOptjj_ls();
            phaseopts.add(ret);
            return ret;
        }
        public class PhaseOptjj_ls {
      
          public void setenabled(boolean arg) {
            addArg("-p");
            addArg("jj.ls");
            addArg("enabled:"+(arg?"true":"false"));
          }
      
        }
    
        public Object createp_jj_sils() {
            Object ret = new PhaseOptjj_sils();
            phaseopts.add(ret);
            return ret;
        }
        public class PhaseOptjj_sils {
      
          public void setenabled(boolean arg) {
            addArg("-p");
            addArg("jj.sils");
            addArg("enabled:"+(arg?"true":"false"));
          }
      
        }
    
        public Object createp_jj_a() {
            Object ret = new PhaseOptjj_a();
            phaseopts.add(ret);
            return ret;
        }
        public class PhaseOptjj_a {
      
          public void setenabled(boolean arg) {
            addArg("-p");
            addArg("jj.a");
            addArg("enabled:"+(arg?"true":"false"));
          }
      
          public void setonly_stack_locals(boolean arg) {
            addArg("-p");
            addArg("jj.a");
            addArg("only-stack-locals:"+(arg?"true":"false"));
          }
      
        }
    
        public Object createp_jj_ule() {
            Object ret = new PhaseOptjj_ule();
            phaseopts.add(ret);
            return ret;
        }
        public class PhaseOptjj_ule {
      
          public void setenabled(boolean arg) {
            addArg("-p");
            addArg("jj.ule");
            addArg("enabled:"+(arg?"true":"false"));
          }
      
        }
    
        public Object createp_jj_tr() {
            Object ret = new PhaseOptjj_tr();
            phaseopts.add(ret);
            return ret;
        }
        public class PhaseOptjj_tr {
      
          public void setenabled(boolean arg) {
            addArg("-p");
            addArg("jj.tr");
            addArg("enabled:"+(arg?"true":"false"));
          }
      
        }
    
        public Object createp_jj_ulp() {
            Object ret = new PhaseOptjj_ulp();
            phaseopts.add(ret);
            return ret;
        }
        public class PhaseOptjj_ulp {
      
          public void setenabled(boolean arg) {
            addArg("-p");
            addArg("jj.ulp");
            addArg("enabled:"+(arg?"true":"false"));
          }
      
          public void setunsplit_original_locals(boolean arg) {
            addArg("-p");
            addArg("jj.ulp");
            addArg("unsplit-original-locals:"+(arg?"true":"false"));
          }
      
        }
    
        public Object createp_jj_lns() {
            Object ret = new PhaseOptjj_lns();
            phaseopts.add(ret);
            return ret;
        }
        public class PhaseOptjj_lns {
      
          public void setenabled(boolean arg) {
            addArg("-p");
            addArg("jj.lns");
            addArg("enabled:"+(arg?"true":"false"));
          }
      
          public void setonly_stack_locals(boolean arg) {
            addArg("-p");
            addArg("jj.lns");
            addArg("only-stack-locals:"+(arg?"true":"false"));
          }
      
        }
    
        public Object createp_jj_cp() {
            Object ret = new PhaseOptjj_cp();
            phaseopts.add(ret);
            return ret;
        }
        public class PhaseOptjj_cp {
      
          public void setenabled(boolean arg) {
            addArg("-p");
            addArg("jj.cp");
            addArg("enabled:"+(arg?"true":"false"));
          }
      
          public void setonly_regular_locals(boolean arg) {
            addArg("-p");
            addArg("jj.cp");
            addArg("only-regular-locals:"+(arg?"true":"false"));
          }
      
          public void setonly_stack_locals(boolean arg) {
            addArg("-p");
            addArg("jj.cp");
            addArg("only-stack-locals:"+(arg?"true":"false"));
          }
      
        }
    
        public Object createp_jj_dae() {
            Object ret = new PhaseOptjj_dae();
            phaseopts.add(ret);
            return ret;
        }
        public class PhaseOptjj_dae {
      
          public void setenabled(boolean arg) {
            addArg("-p");
            addArg("jj.dae");
            addArg("enabled:"+(arg?"true":"false"));
          }
      
          public void setonly_stack_locals(boolean arg) {
            addArg("-p");
            addArg("jj.dae");
            addArg("only-stack-locals:"+(arg?"true":"false"));
          }
      
        }
    
        public Object createp_jj_cp_ule() {
            Object ret = new PhaseOptjj_cp_ule();
            phaseopts.add(ret);
            return ret;
        }
        public class PhaseOptjj_cp_ule {
      
          public void setenabled(boolean arg) {
            addArg("-p");
            addArg("jj.cp-ule");
            addArg("enabled:"+(arg?"true":"false"));
          }
      
        }
    
        public Object createp_jj_lp() {
            Object ret = new PhaseOptjj_lp();
            phaseopts.add(ret);
            return ret;
        }
        public class PhaseOptjj_lp {
      
          public void setenabled(boolean arg) {
            addArg("-p");
            addArg("jj.lp");
            addArg("enabled:"+(arg?"true":"false"));
          }
      
          public void setunsplit_original_locals(boolean arg) {
            addArg("-p");
            addArg("jj.lp");
            addArg("unsplit-original-locals:"+(arg?"true":"false"));
          }
      
        }
    
        public Object createp_jj_ne() {
            Object ret = new PhaseOptjj_ne();
            phaseopts.add(ret);
            return ret;
        }
        public class PhaseOptjj_ne {
      
          public void setenabled(boolean arg) {
            addArg("-p");
            addArg("jj.ne");
            addArg("enabled:"+(arg?"true":"false"));
          }
      
        }
    
        public Object createp_jj_uce() {
            Object ret = new PhaseOptjj_uce();
            phaseopts.add(ret);
            return ret;
        }
        public class PhaseOptjj_uce {
      
          public void setenabled(boolean arg) {
            addArg("-p");
            addArg("jj.uce");
            addArg("enabled:"+(arg?"true":"false"));
          }
      
        }
    
        public Object createp_wjpp() {
            Object ret = new PhaseOptwjpp();
            phaseopts.add(ret);
            return ret;
        }
        public class PhaseOptwjpp {
      
          public void setenabled(boolean arg) {
            addArg("-p");
            addArg("wjpp");
            addArg("enabled:"+(arg?"true":"false"));
          }
      
        }
    
        public Object createp_wjpp_cimbt() {
            Object ret = new PhaseOptwjpp_cimbt();
            phaseopts.add(ret);
            return ret;
        }
        public class PhaseOptwjpp_cimbt {
      
          public void setenabled(boolean arg) {
            addArg("-p");
            addArg("wjpp.cimbt");
            addArg("enabled:"+(arg?"true":"false"));
          }
      
          public void setverbose(boolean arg) {
            addArg("-p");
            addArg("wjpp.cimbt");
            addArg("verbose:"+(arg?"true":"false"));
          }
      
        }
    
        public Object createp_wspp() {
            Object ret = new PhaseOptwspp();
            phaseopts.add(ret);
            return ret;
        }
        public class PhaseOptwspp {
      
          public void setenabled(boolean arg) {
            addArg("-p");
            addArg("wspp");
            addArg("enabled:"+(arg?"true":"false"));
          }
      
        }
    
        public Object createp_cg() {
            Object ret = new PhaseOptcg();
            phaseopts.add(ret);
            return ret;
        }
        public class PhaseOptcg {
      
          public void setenabled(boolean arg) {
            addArg("-p");
            addArg("cg");
            addArg("enabled:"+(arg?"true":"false"));
          }
      
          public void setsafe_forname(boolean arg) {
            addArg("-p");
            addArg("cg");
            addArg("safe-forname:"+(arg?"true":"false"));
          }
      
          public void setsafe_newinstance(boolean arg) {
            addArg("-p");
            addArg("cg");
            addArg("safe-newinstance:"+(arg?"true":"false"));
          }
      
          public void setverbose(boolean arg) {
            addArg("-p");
            addArg("cg");
            addArg("verbose:"+(arg?"true":"false"));
          }
      
          public void setall_reachable(boolean arg) {
            addArg("-p");
            addArg("cg");
            addArg("all-reachable:"+(arg?"true":"false"));
          }
      
          public void setimplicit_entry(boolean arg) {
            addArg("-p");
            addArg("cg");
            addArg("implicit-entry:"+(arg?"true":"false"));
          }
      
          public void settrim_clinit(boolean arg) {
            addArg("-p");
            addArg("cg");
            addArg("trim-clinit:"+(arg?"true":"false"));
          }
      
          public void settypes_for_invoke(boolean arg) {
            addArg("-p");
            addArg("cg");
            addArg("types-for-invoke:"+(arg?"true":"false"));
          }
      
          public void setresolve_all_abstract_invokes(boolean arg) {
            addArg("-p");
            addArg("cg");
            addArg("resolve-all-abstract-invokes:"+(arg?"true":"false"));
          }
      
          public void setlibrary(String arg) {
            addArg("-p");
            addArg("cg");
            addArg("library:"+arg);
          }
      
          public void setjdkver(String arg) {
            addArg("-p");
            addArg("cg");
            addArg("jdkver:"+arg);
          }
      
          public void setreflection_log(String arg) {
            addArg("-p");
            addArg("cg");
            addArg("reflection-log:"+arg);
          }
      
          public void setguards(String arg) {
            addArg("-p");
            addArg("cg");
            addArg("guards:"+arg);
          }
      
        }
    
        public Object createp_cg_cha() {
            Object ret = new PhaseOptcg_cha();
            phaseopts.add(ret);
            return ret;
        }
        public class PhaseOptcg_cha {
      
          public void setenabled(boolean arg) {
            addArg("-p");
            addArg("cg.cha");
            addArg("enabled:"+(arg?"true":"false"));
          }
      
          public void setverbose(boolean arg) {
            addArg("-p");
            addArg("cg.cha");
            addArg("verbose:"+(arg?"true":"false"));
          }
      
          public void setapponly(boolean arg) {
            addArg("-p");
            addArg("cg.cha");
            addArg("apponly:"+(arg?"true":"false"));
          }
      
        }
    
        public Object createp_cg_spark() {
            Object ret = new PhaseOptcg_spark();
            phaseopts.add(ret);
            return ret;
        }
        public class PhaseOptcg_spark {
      
          public void setenabled(boolean arg) {
            addArg("-p");
            addArg("cg.spark");
            addArg("enabled:"+(arg?"true":"false"));
          }
      
          public void setverbose(boolean arg) {
            addArg("-p");
            addArg("cg.spark");
            addArg("verbose:"+(arg?"true":"false"));
          }
      
          public void setignore_types(boolean arg) {
            addArg("-p");
            addArg("cg.spark");
            addArg("ignore-types:"+(arg?"true":"false"));
          }
      
          public void setforce_gc(boolean arg) {
            addArg("-p");
            addArg("cg.spark");
            addArg("force-gc:"+(arg?"true":"false"));
          }
      
          public void setpre_jimplify(boolean arg) {
            addArg("-p");
            addArg("cg.spark");
            addArg("pre-jimplify:"+(arg?"true":"false"));
          }
      
          public void setapponly(boolean arg) {
            addArg("-p");
            addArg("cg.spark");
            addArg("apponly:"+(arg?"true":"false"));
          }
      
          public void setvta(boolean arg) {
            addArg("-p");
            addArg("cg.spark");
            addArg("vta:"+(arg?"true":"false"));
          }
      
          public void setrta(boolean arg) {
            addArg("-p");
            addArg("cg.spark");
            addArg("rta:"+(arg?"true":"false"));
          }
      
          public void setfield_based(boolean arg) {
            addArg("-p");
            addArg("cg.spark");
            addArg("field-based:"+(arg?"true":"false"));
          }
      
          public void settypes_for_sites(boolean arg) {
            addArg("-p");
            addArg("cg.spark");
            addArg("types-for-sites:"+(arg?"true":"false"));
          }
      
          public void setmerge_stringbuffer(boolean arg) {
            addArg("-p");
            addArg("cg.spark");
            addArg("merge-stringbuffer:"+(arg?"true":"false"));
          }
      
          public void setstring_constants(boolean arg) {
            addArg("-p");
            addArg("cg.spark");
            addArg("string-constants:"+(arg?"true":"false"));
          }
      
          public void setsimulate_natives(boolean arg) {
            addArg("-p");
            addArg("cg.spark");
            addArg("simulate-natives:"+(arg?"true":"false"));
          }
      
          public void setempties_as_allocs(boolean arg) {
            addArg("-p");
            addArg("cg.spark");
            addArg("empties-as-allocs:"+(arg?"true":"false"));
          }
      
          public void setsimple_edges_bidirectional(boolean arg) {
            addArg("-p");
            addArg("cg.spark");
            addArg("simple-edges-bidirectional:"+(arg?"true":"false"));
          }
      
          public void seton_fly_cg(boolean arg) {
            addArg("-p");
            addArg("cg.spark");
            addArg("on-fly-cg:"+(arg?"true":"false"));
          }
      
          public void setsimplify_offline(boolean arg) {
            addArg("-p");
            addArg("cg.spark");
            addArg("simplify-offline:"+(arg?"true":"false"));
          }
      
          public void setsimplify_sccs(boolean arg) {
            addArg("-p");
            addArg("cg.spark");
            addArg("simplify-sccs:"+(arg?"true":"false"));
          }
      
          public void setignore_types_for_sccs(boolean arg) {
            addArg("-p");
            addArg("cg.spark");
            addArg("ignore-types-for-sccs:"+(arg?"true":"false"));
          }
      
          public void setdump_html(boolean arg) {
            addArg("-p");
            addArg("cg.spark");
            addArg("dump-html:"+(arg?"true":"false"));
          }
      
          public void setdump_pag(boolean arg) {
            addArg("-p");
            addArg("cg.spark");
            addArg("dump-pag:"+(arg?"true":"false"));
          }
      
          public void setdump_solution(boolean arg) {
            addArg("-p");
            addArg("cg.spark");
            addArg("dump-solution:"+(arg?"true":"false"));
          }
      
          public void settopo_sort(boolean arg) {
            addArg("-p");
            addArg("cg.spark");
            addArg("topo-sort:"+(arg?"true":"false"));
          }
      
          public void setdump_types(boolean arg) {
            addArg("-p");
            addArg("cg.spark");
            addArg("dump-types:"+(arg?"true":"false"));
          }
      
          public void setclass_method_var(boolean arg) {
            addArg("-p");
            addArg("cg.spark");
            addArg("class-method-var:"+(arg?"true":"false"));
          }
      
          public void setdump_answer(boolean arg) {
            addArg("-p");
            addArg("cg.spark");
            addArg("dump-answer:"+(arg?"true":"false"));
          }
      
          public void setadd_tags(boolean arg) {
            addArg("-p");
            addArg("cg.spark");
            addArg("add-tags:"+(arg?"true":"false"));
          }
      
          public void setset_mass(boolean arg) {
            addArg("-p");
            addArg("cg.spark");
            addArg("set-mass:"+(arg?"true":"false"));
          }
      
          public void setcs_demand(boolean arg) {
            addArg("-p");
            addArg("cg.spark");
            addArg("cs-demand:"+(arg?"true":"false"));
          }
      
          public void setlazy_pts(boolean arg) {
            addArg("-p");
            addArg("cg.spark");
            addArg("lazy-pts:"+(arg?"true":"false"));
          }
      
          public void setgeom_pta(boolean arg) {
            addArg("-p");
            addArg("cg.spark");
            addArg("geom-pta:"+(arg?"true":"false"));
          }
      
          public void setgeom_trans(boolean arg) {
            addArg("-p");
            addArg("cg.spark");
            addArg("geom-trans:"+(arg?"true":"false"));
          }
      
          public void setgeom_blocking(boolean arg) {
            addArg("-p");
            addArg("cg.spark");
            addArg("geom-blocking:"+(arg?"true":"false"));
          }
      
          public void setgeom_app_only(boolean arg) {
            addArg("-p");
            addArg("cg.spark");
            addArg("geom-app-only:"+(arg?"true":"false"));
          }
      
          public void setpropagator(String arg) {
            addArg("-p");
            addArg("cg.spark");
            addArg("propagator:"+arg);
          }
      
          public void setset_impl(String arg) {
            addArg("-p");
            addArg("cg.spark");
            addArg("set-impl:"+arg);
          }
      
          public void setdouble_set_old(String arg) {
            addArg("-p");
            addArg("cg.spark");
            addArg("double-set-old:"+arg);
          }
      
          public void setdouble_set_new(String arg) {
            addArg("-p");
            addArg("cg.spark");
            addArg("double-set-new:"+arg);
          }
      
          public void settraversal(String arg) {
            addArg("-p");
            addArg("cg.spark");
            addArg("traversal:"+arg);
          }
      
          public void setpasses(String arg) {
            addArg("-p");
            addArg("cg.spark");
            addArg("passes:"+arg);
          }
      
          public void setgeom_encoding(String arg) {
            addArg("-p");
            addArg("cg.spark");
            addArg("geom-encoding:"+arg);
          }
      
          public void setgeom_worklist(String arg) {
            addArg("-p");
            addArg("cg.spark");
            addArg("geom-worklist:"+arg);
          }
      
          public void setgeom_dump_verbose(String arg) {
            addArg("-p");
            addArg("cg.spark");
            addArg("geom-dump-verbose:"+arg);
          }
      
          public void setgeom_verify_name(String arg) {
            addArg("-p");
            addArg("cg.spark");
            addArg("geom-verify-name:"+arg);
          }
      
          public void setgeom_eval(String arg) {
            addArg("-p");
            addArg("cg.spark");
            addArg("geom-eval:"+arg);
          }
      
          public void setgeom_frac_base(String arg) {
            addArg("-p");
            addArg("cg.spark");
            addArg("geom-frac-base:"+arg);
          }
      
          public void setgeom_runs(String arg) {
            addArg("-p");
            addArg("cg.spark");
            addArg("geom-runs:"+arg);
          }
      
        }
    
        public Object createp_cg_paddle() {
            Object ret = new PhaseOptcg_paddle();
            phaseopts.add(ret);
            return ret;
        }
        public class PhaseOptcg_paddle {
      
          public void setenabled(boolean arg) {
            addArg("-p");
            addArg("cg.paddle");
            addArg("enabled:"+(arg?"true":"false"));
          }
      
          public void setverbose(boolean arg) {
            addArg("-p");
            addArg("cg.paddle");
            addArg("verbose:"+(arg?"true":"false"));
          }
      
          public void setbdd(boolean arg) {
            addArg("-p");
            addArg("cg.paddle");
            addArg("bdd:"+(arg?"true":"false"));
          }
      
          public void setdynamic_order(boolean arg) {
            addArg("-p");
            addArg("cg.paddle");
            addArg("dynamic-order:"+(arg?"true":"false"));
          }
      
          public void setprofile(boolean arg) {
            addArg("-p");
            addArg("cg.paddle");
            addArg("profile:"+(arg?"true":"false"));
          }
      
          public void setverbosegc(boolean arg) {
            addArg("-p");
            addArg("cg.paddle");
            addArg("verbosegc:"+(arg?"true":"false"));
          }
      
          public void setignore_types(boolean arg) {
            addArg("-p");
            addArg("cg.paddle");
            addArg("ignore-types:"+(arg?"true":"false"));
          }
      
          public void setpre_jimplify(boolean arg) {
            addArg("-p");
            addArg("cg.paddle");
            addArg("pre-jimplify:"+(arg?"true":"false"));
          }
      
          public void setcontext_heap(boolean arg) {
            addArg("-p");
            addArg("cg.paddle");
            addArg("context-heap:"+(arg?"true":"false"));
          }
      
          public void setrta(boolean arg) {
            addArg("-p");
            addArg("cg.paddle");
            addArg("rta:"+(arg?"true":"false"));
          }
      
          public void setfield_based(boolean arg) {
            addArg("-p");
            addArg("cg.paddle");
            addArg("field-based:"+(arg?"true":"false"));
          }
      
          public void settypes_for_sites(boolean arg) {
            addArg("-p");
            addArg("cg.paddle");
            addArg("types-for-sites:"+(arg?"true":"false"));
          }
      
          public void setmerge_stringbuffer(boolean arg) {
            addArg("-p");
            addArg("cg.paddle");
            addArg("merge-stringbuffer:"+(arg?"true":"false"));
          }
      
          public void setstring_constants(boolean arg) {
            addArg("-p");
            addArg("cg.paddle");
            addArg("string-constants:"+(arg?"true":"false"));
          }
      
          public void setsimulate_natives(boolean arg) {
            addArg("-p");
            addArg("cg.paddle");
            addArg("simulate-natives:"+(arg?"true":"false"));
          }
      
          public void setglobal_nodes_in_natives(boolean arg) {
            addArg("-p");
            addArg("cg.paddle");
            addArg("global-nodes-in-natives:"+(arg?"true":"false"));
          }
      
          public void setsimple_edges_bidirectional(boolean arg) {
            addArg("-p");
            addArg("cg.paddle");
            addArg("simple-edges-bidirectional:"+(arg?"true":"false"));
          }
      
          public void setthis_edges(boolean arg) {
            addArg("-p");
            addArg("cg.paddle");
            addArg("this-edges:"+(arg?"true":"false"));
          }
      
          public void setprecise_newinstance(boolean arg) {
            addArg("-p");
            addArg("cg.paddle");
            addArg("precise-newinstance:"+(arg?"true":"false"));
          }
      
          public void setcontext_counts(boolean arg) {
            addArg("-p");
            addArg("cg.paddle");
            addArg("context-counts:"+(arg?"true":"false"));
          }
      
          public void settotal_context_counts(boolean arg) {
            addArg("-p");
            addArg("cg.paddle");
            addArg("total-context-counts:"+(arg?"true":"false"));
          }
      
          public void setmethod_context_counts(boolean arg) {
            addArg("-p");
            addArg("cg.paddle");
            addArg("method-context-counts:"+(arg?"true":"false"));
          }
      
          public void setset_mass(boolean arg) {
            addArg("-p");
            addArg("cg.paddle");
            addArg("set-mass:"+(arg?"true":"false"));
          }
      
          public void setnumber_nodes(boolean arg) {
            addArg("-p");
            addArg("cg.paddle");
            addArg("number-nodes:"+(arg?"true":"false"));
          }
      
          public void setconf(String arg) {
            addArg("-p");
            addArg("cg.paddle");
            addArg("conf:"+arg);
          }
      
          public void setorder(String arg) {
            addArg("-p");
            addArg("cg.paddle");
            addArg("order:"+arg);
          }
      
          public void setq(String arg) {
            addArg("-p");
            addArg("cg.paddle");
            addArg("q:"+arg);
          }
      
          public void setbackend(String arg) {
            addArg("-p");
            addArg("cg.paddle");
            addArg("backend:"+arg);
          }
      
          public void setbdd_nodes(String arg) {
            addArg("-p");
            addArg("cg.paddle");
            addArg("bdd-nodes:"+arg);
          }
      
          public void setcontext(String arg) {
            addArg("-p");
            addArg("cg.paddle");
            addArg("context:"+arg);
          }
      
          public void setk(String arg) {
            addArg("-p");
            addArg("cg.paddle");
            addArg("k:"+arg);
          }
      
          public void setpropagator(String arg) {
            addArg("-p");
            addArg("cg.paddle");
            addArg("propagator:"+arg);
          }
      
          public void setset_impl(String arg) {
            addArg("-p");
            addArg("cg.paddle");
            addArg("set-impl:"+arg);
          }
      
          public void setdouble_set_old(String arg) {
            addArg("-p");
            addArg("cg.paddle");
            addArg("double-set-old:"+arg);
          }
      
          public void setdouble_set_new(String arg) {
            addArg("-p");
            addArg("cg.paddle");
            addArg("double-set-new:"+arg);
          }
      
        }
    
        public Object createp_wstp() {
            Object ret = new PhaseOptwstp();
            phaseopts.add(ret);
            return ret;
        }
        public class PhaseOptwstp {
      
          public void setenabled(boolean arg) {
            addArg("-p");
            addArg("wstp");
            addArg("enabled:"+(arg?"true":"false"));
          }
      
        }
    
        public Object createp_wsop() {
            Object ret = new PhaseOptwsop();
            phaseopts.add(ret);
            return ret;
        }
        public class PhaseOptwsop {
      
          public void setenabled(boolean arg) {
            addArg("-p");
            addArg("wsop");
            addArg("enabled:"+(arg?"true":"false"));
          }
      
        }
    
        public Object createp_wjtp() {
            Object ret = new PhaseOptwjtp();
            phaseopts.add(ret);
            return ret;
        }
        public class PhaseOptwjtp {
      
          public void setenabled(boolean arg) {
            addArg("-p");
            addArg("wjtp");
            addArg("enabled:"+(arg?"true":"false"));
          }
      
        }
    
        public Object createp_wjtp_mhp() {
            Object ret = new PhaseOptwjtp_mhp();
            phaseopts.add(ret);
            return ret;
        }
        public class PhaseOptwjtp_mhp {
      
          public void setenabled(boolean arg) {
            addArg("-p");
            addArg("wjtp.mhp");
            addArg("enabled:"+(arg?"true":"false"));
          }
      
        }
    
        public Object createp_wjtp_tn() {
            Object ret = new PhaseOptwjtp_tn();
            phaseopts.add(ret);
            return ret;
        }
        public class PhaseOptwjtp_tn {
      
          public void setenabled(boolean arg) {
            addArg("-p");
            addArg("wjtp.tn");
            addArg("enabled:"+(arg?"true":"false"));
          }
      
          public void setavoid_deadlock(boolean arg) {
            addArg("-p");
            addArg("wjtp.tn");
            addArg("avoid-deadlock:"+(arg?"true":"false"));
          }
      
          public void setopen_nesting(boolean arg) {
            addArg("-p");
            addArg("wjtp.tn");
            addArg("open-nesting:"+(arg?"true":"false"));
          }
      
          public void setdo_mhp(boolean arg) {
            addArg("-p");
            addArg("wjtp.tn");
            addArg("do-mhp:"+(arg?"true":"false"));
          }
      
          public void setdo_tlo(boolean arg) {
            addArg("-p");
            addArg("wjtp.tn");
            addArg("do-tlo:"+(arg?"true":"false"));
          }
      
          public void setprint_graph(boolean arg) {
            addArg("-p");
            addArg("wjtp.tn");
            addArg("print-graph:"+(arg?"true":"false"));
          }
      
          public void setprint_table(boolean arg) {
            addArg("-p");
            addArg("wjtp.tn");
            addArg("print-table:"+(arg?"true":"false"));
          }
      
          public void setprint_debug(boolean arg) {
            addArg("-p");
            addArg("wjtp.tn");
            addArg("print-debug:"+(arg?"true":"false"));
          }
      
          public void setlocking_scheme(String arg) {
            addArg("-p");
            addArg("wjtp.tn");
            addArg("locking-scheme:"+arg);
          }
      
        }
    
        public Object createp_wjtp_rdc() {
            Object ret = new PhaseOptwjtp_rdc();
            phaseopts.add(ret);
            return ret;
        }
        public class PhaseOptwjtp_rdc {
      
          public void setenabled(boolean arg) {
            addArg("-p");
            addArg("wjtp.rdc");
            addArg("enabled:"+(arg?"true":"false"));
          }
      
          public void setfixed_class_names(String arg) {
            addArg("-p");
            addArg("wjtp.rdc");
            addArg("fixed-class-names:"+arg);
          }
      
        }
    
        public Object createp_wjop() {
            Object ret = new PhaseOptwjop();
            phaseopts.add(ret);
            return ret;
        }
        public class PhaseOptwjop {
      
          public void setenabled(boolean arg) {
            addArg("-p");
            addArg("wjop");
            addArg("enabled:"+(arg?"true":"false"));
          }
      
        }
    
        public Object createp_wjop_smb() {
            Object ret = new PhaseOptwjop_smb();
            phaseopts.add(ret);
            return ret;
        }
        public class PhaseOptwjop_smb {
      
          public void setenabled(boolean arg) {
            addArg("-p");
            addArg("wjop.smb");
            addArg("enabled:"+(arg?"true":"false"));
          }
      
          public void setinsert_null_checks(boolean arg) {
            addArg("-p");
            addArg("wjop.smb");
            addArg("insert-null-checks:"+(arg?"true":"false"));
          }
      
          public void setinsert_redundant_casts(boolean arg) {
            addArg("-p");
            addArg("wjop.smb");
            addArg("insert-redundant-casts:"+(arg?"true":"false"));
          }
      
          public void setallowed_modifier_changes(String arg) {
            addArg("-p");
            addArg("wjop.smb");
            addArg("allowed-modifier-changes:"+arg);
          }
      
        }
    
        public Object createp_wjop_si() {
            Object ret = new PhaseOptwjop_si();
            phaseopts.add(ret);
            return ret;
        }
        public class PhaseOptwjop_si {
      
          public void setenabled(boolean arg) {
            addArg("-p");
            addArg("wjop.si");
            addArg("enabled:"+(arg?"true":"false"));
          }
      
          public void setrerun_jb(boolean arg) {
            addArg("-p");
            addArg("wjop.si");
            addArg("rerun-jb:"+(arg?"true":"false"));
          }
      
          public void setinsert_null_checks(boolean arg) {
            addArg("-p");
            addArg("wjop.si");
            addArg("insert-null-checks:"+(arg?"true":"false"));
          }
      
          public void setinsert_redundant_casts(boolean arg) {
            addArg("-p");
            addArg("wjop.si");
            addArg("insert-redundant-casts:"+(arg?"true":"false"));
          }
      
          public void setallowed_modifier_changes(String arg) {
            addArg("-p");
            addArg("wjop.si");
            addArg("allowed-modifier-changes:"+arg);
          }
      
          public void setexpansion_factor(String arg) {
            addArg("-p");
            addArg("wjop.si");
            addArg("expansion-factor:"+arg);
          }
      
          public void setmax_container_size(String arg) {
            addArg("-p");
            addArg("wjop.si");
            addArg("max-container-size:"+arg);
          }
      
          public void setmax_inlinee_size(String arg) {
            addArg("-p");
            addArg("wjop.si");
            addArg("max-inlinee-size:"+arg);
          }
      
        }
    
        public Object createp_wjap() {
            Object ret = new PhaseOptwjap();
            phaseopts.add(ret);
            return ret;
        }
        public class PhaseOptwjap {
      
          public void setenabled(boolean arg) {
            addArg("-p");
            addArg("wjap");
            addArg("enabled:"+(arg?"true":"false"));
          }
      
        }
    
        public Object createp_wjap_ra() {
            Object ret = new PhaseOptwjap_ra();
            phaseopts.add(ret);
            return ret;
        }
        public class PhaseOptwjap_ra {
      
          public void setenabled(boolean arg) {
            addArg("-p");
            addArg("wjap.ra");
            addArg("enabled:"+(arg?"true":"false"));
          }
      
        }
    
        public Object createp_wjap_umt() {
            Object ret = new PhaseOptwjap_umt();
            phaseopts.add(ret);
            return ret;
        }
        public class PhaseOptwjap_umt {
      
          public void setenabled(boolean arg) {
            addArg("-p");
            addArg("wjap.umt");
            addArg("enabled:"+(arg?"true":"false"));
          }
      
        }
    
        public Object createp_wjap_uft() {
            Object ret = new PhaseOptwjap_uft();
            phaseopts.add(ret);
            return ret;
        }
        public class PhaseOptwjap_uft {
      
          public void setenabled(boolean arg) {
            addArg("-p");
            addArg("wjap.uft");
            addArg("enabled:"+(arg?"true":"false"));
          }
      
        }
    
        public Object createp_wjap_tqt() {
            Object ret = new PhaseOptwjap_tqt();
            phaseopts.add(ret);
            return ret;
        }
        public class PhaseOptwjap_tqt {
      
          public void setenabled(boolean arg) {
            addArg("-p");
            addArg("wjap.tqt");
            addArg("enabled:"+(arg?"true":"false"));
          }
      
        }
    
        public Object createp_wjap_cgg() {
            Object ret = new PhaseOptwjap_cgg();
            phaseopts.add(ret);
            return ret;
        }
        public class PhaseOptwjap_cgg {
      
          public void setenabled(boolean arg) {
            addArg("-p");
            addArg("wjap.cgg");
            addArg("enabled:"+(arg?"true":"false"));
          }
      
          public void setshow_lib_meths(boolean arg) {
            addArg("-p");
            addArg("wjap.cgg");
            addArg("show-lib-meths:"+(arg?"true":"false"));
          }
      
        }
    
        public Object createp_wjap_purity() {
            Object ret = new PhaseOptwjap_purity();
            phaseopts.add(ret);
            return ret;
        }
        public class PhaseOptwjap_purity {
      
          public void setenabled(boolean arg) {
            addArg("-p");
            addArg("wjap.purity");
            addArg("enabled:"+(arg?"true":"false"));
          }
      
          public void setdump_summaries(boolean arg) {
            addArg("-p");
            addArg("wjap.purity");
            addArg("dump-summaries:"+(arg?"true":"false"));
          }
      
          public void setdump_cg(boolean arg) {
            addArg("-p");
            addArg("wjap.purity");
            addArg("dump-cg:"+(arg?"true":"false"));
          }
      
          public void setdump_intra(boolean arg) {
            addArg("-p");
            addArg("wjap.purity");
            addArg("dump-intra:"+(arg?"true":"false"));
          }
      
          public void setprint(boolean arg) {
            addArg("-p");
            addArg("wjap.purity");
            addArg("print:"+(arg?"true":"false"));
          }
      
          public void setannotate(boolean arg) {
            addArg("-p");
            addArg("wjap.purity");
            addArg("annotate:"+(arg?"true":"false"));
          }
      
          public void setverbose(boolean arg) {
            addArg("-p");
            addArg("wjap.purity");
            addArg("verbose:"+(arg?"true":"false"));
          }
      
        }
    
        public Object createp_wjap_abc() {
            Object ret = new PhaseOptwjap_abc();
            phaseopts.add(ret);
            return ret;
        }
        public class PhaseOptwjap_abc {
      
          public void setenabled(boolean arg) {
            addArg("-p");
            addArg("wjap.abc");
            addArg("enabled:"+(arg?"true":"false"));
          }
      
          public void setwith_all(boolean arg) {
            addArg("-p");
            addArg("wjap.abc");
            addArg("with-all:"+(arg?"true":"false"));
          }
      
          public void setwith_cse(boolean arg) {
            addArg("-p");
            addArg("wjap.abc");
            addArg("with-cse:"+(arg?"true":"false"));
          }
      
          public void setwith_arrayref(boolean arg) {
            addArg("-p");
            addArg("wjap.abc");
            addArg("with-arrayref:"+(arg?"true":"false"));
          }
      
          public void setwith_fieldref(boolean arg) {
            addArg("-p");
            addArg("wjap.abc");
            addArg("with-fieldref:"+(arg?"true":"false"));
          }
      
          public void setwith_classfield(boolean arg) {
            addArg("-p");
            addArg("wjap.abc");
            addArg("with-classfield:"+(arg?"true":"false"));
          }
      
          public void setwith_rectarray(boolean arg) {
            addArg("-p");
            addArg("wjap.abc");
            addArg("with-rectarray:"+(arg?"true":"false"));
          }
      
          public void setprofiling(boolean arg) {
            addArg("-p");
            addArg("wjap.abc");
            addArg("profiling:"+(arg?"true":"false"));
          }
      
          public void setadd_color_tags(boolean arg) {
            addArg("-p");
            addArg("wjap.abc");
            addArg("add-color-tags:"+(arg?"true":"false"));
          }
      
        }
    
        public Object createp_shimple() {
            Object ret = new PhaseOptshimple();
            phaseopts.add(ret);
            return ret;
        }
        public class PhaseOptshimple {
      
          public void setenabled(boolean arg) {
            addArg("-p");
            addArg("shimple");
            addArg("enabled:"+(arg?"true":"false"));
          }
      
          public void setnode_elim_opt(boolean arg) {
            addArg("-p");
            addArg("shimple");
            addArg("node-elim-opt:"+(arg?"true":"false"));
          }
      
          public void setstandard_local_names(boolean arg) {
            addArg("-p");
            addArg("shimple");
            addArg("standard-local-names:"+(arg?"true":"false"));
          }
      
          public void setextended(boolean arg) {
            addArg("-p");
            addArg("shimple");
            addArg("extended:"+(arg?"true":"false"));
          }
      
          public void setdebug(boolean arg) {
            addArg("-p");
            addArg("shimple");
            addArg("debug:"+(arg?"true":"false"));
          }
      
        }
    
        public Object createp_stp() {
            Object ret = new PhaseOptstp();
            phaseopts.add(ret);
            return ret;
        }
        public class PhaseOptstp {
      
          public void setenabled(boolean arg) {
            addArg("-p");
            addArg("stp");
            addArg("enabled:"+(arg?"true":"false"));
          }
      
        }
    
        public Object createp_sop() {
            Object ret = new PhaseOptsop();
            phaseopts.add(ret);
            return ret;
        }
        public class PhaseOptsop {
      
          public void setenabled(boolean arg) {
            addArg("-p");
            addArg("sop");
            addArg("enabled:"+(arg?"true":"false"));
          }
      
        }
    
        public Object createp_sop_cpf() {
            Object ret = new PhaseOptsop_cpf();
            phaseopts.add(ret);
            return ret;
        }
        public class PhaseOptsop_cpf {
      
          public void setenabled(boolean arg) {
            addArg("-p");
            addArg("sop.cpf");
            addArg("enabled:"+(arg?"true":"false"));
          }
      
          public void setprune_cfg(boolean arg) {
            addArg("-p");
            addArg("sop.cpf");
            addArg("prune-cfg:"+(arg?"true":"false"));
          }
      
        }
    
        public Object createp_jtp() {
            Object ret = new PhaseOptjtp();
            phaseopts.add(ret);
            return ret;
        }
        public class PhaseOptjtp {
      
          public void setenabled(boolean arg) {
            addArg("-p");
            addArg("jtp");
            addArg("enabled:"+(arg?"true":"false"));
          }
      
        }
    
        public Object createp_jop() {
            Object ret = new PhaseOptjop();
            phaseopts.add(ret);
            return ret;
        }
        public class PhaseOptjop {
      
          public void setenabled(boolean arg) {
            addArg("-p");
            addArg("jop");
            addArg("enabled:"+(arg?"true":"false"));
          }
      
        }
    
        public Object createp_jop_cse() {
            Object ret = new PhaseOptjop_cse();
            phaseopts.add(ret);
            return ret;
        }
        public class PhaseOptjop_cse {
      
          public void setenabled(boolean arg) {
            addArg("-p");
            addArg("jop.cse");
            addArg("enabled:"+(arg?"true":"false"));
          }
      
          public void setnaive_side_effect(boolean arg) {
            addArg("-p");
            addArg("jop.cse");
            addArg("naive-side-effect:"+(arg?"true":"false"));
          }
      
        }
    
        public Object createp_jop_bcm() {
            Object ret = new PhaseOptjop_bcm();
            phaseopts.add(ret);
            return ret;
        }
        public class PhaseOptjop_bcm {
      
          public void setenabled(boolean arg) {
            addArg("-p");
            addArg("jop.bcm");
            addArg("enabled:"+(arg?"true":"false"));
          }
      
          public void setnaive_side_effect(boolean arg) {
            addArg("-p");
            addArg("jop.bcm");
            addArg("naive-side-effect:"+(arg?"true":"false"));
          }
      
        }
    
        public Object createp_jop_lcm() {
            Object ret = new PhaseOptjop_lcm();
            phaseopts.add(ret);
            return ret;
        }
        public class PhaseOptjop_lcm {
      
          public void setenabled(boolean arg) {
            addArg("-p");
            addArg("jop.lcm");
            addArg("enabled:"+(arg?"true":"false"));
          }
      
          public void setunroll(boolean arg) {
            addArg("-p");
            addArg("jop.lcm");
            addArg("unroll:"+(arg?"true":"false"));
          }
      
          public void setnaive_side_effect(boolean arg) {
            addArg("-p");
            addArg("jop.lcm");
            addArg("naive-side-effect:"+(arg?"true":"false"));
          }
      
          public void setsafety(String arg) {
            addArg("-p");
            addArg("jop.lcm");
            addArg("safety:"+arg);
          }
      
        }
    
        public Object createp_jop_cp() {
            Object ret = new PhaseOptjop_cp();
            phaseopts.add(ret);
            return ret;
        }
        public class PhaseOptjop_cp {
      
          public void setenabled(boolean arg) {
            addArg("-p");
            addArg("jop.cp");
            addArg("enabled:"+(arg?"true":"false"));
          }
      
          public void setonly_regular_locals(boolean arg) {
            addArg("-p");
            addArg("jop.cp");
            addArg("only-regular-locals:"+(arg?"true":"false"));
          }
      
          public void setonly_stack_locals(boolean arg) {
            addArg("-p");
            addArg("jop.cp");
            addArg("only-stack-locals:"+(arg?"true":"false"));
          }
      
        }
    
        public Object createp_jop_cpf() {
            Object ret = new PhaseOptjop_cpf();
            phaseopts.add(ret);
            return ret;
        }
        public class PhaseOptjop_cpf {
      
          public void setenabled(boolean arg) {
            addArg("-p");
            addArg("jop.cpf");
            addArg("enabled:"+(arg?"true":"false"));
          }
      
        }
    
        public Object createp_jop_cbf() {
            Object ret = new PhaseOptjop_cbf();
            phaseopts.add(ret);
            return ret;
        }
        public class PhaseOptjop_cbf {
      
          public void setenabled(boolean arg) {
            addArg("-p");
            addArg("jop.cbf");
            addArg("enabled:"+(arg?"true":"false"));
          }
      
        }
    
        public Object createp_jop_dae() {
            Object ret = new PhaseOptjop_dae();
            phaseopts.add(ret);
            return ret;
        }
        public class PhaseOptjop_dae {
      
          public void setenabled(boolean arg) {
            addArg("-p");
            addArg("jop.dae");
            addArg("enabled:"+(arg?"true":"false"));
          }
      
          public void setonly_tag(boolean arg) {
            addArg("-p");
            addArg("jop.dae");
            addArg("only-tag:"+(arg?"true":"false"));
          }
      
          public void setonly_stack_locals(boolean arg) {
            addArg("-p");
            addArg("jop.dae");
            addArg("only-stack-locals:"+(arg?"true":"false"));
          }
      
        }
    
        public Object createp_jop_nce() {
            Object ret = new PhaseOptjop_nce();
            phaseopts.add(ret);
            return ret;
        }
        public class PhaseOptjop_nce {
      
          public void setenabled(boolean arg) {
            addArg("-p");
            addArg("jop.nce");
            addArg("enabled:"+(arg?"true":"false"));
          }
      
        }
    
        public Object createp_jop_uce1() {
            Object ret = new PhaseOptjop_uce1();
            phaseopts.add(ret);
            return ret;
        }
        public class PhaseOptjop_uce1 {
      
          public void setenabled(boolean arg) {
            addArg("-p");
            addArg("jop.uce1");
            addArg("enabled:"+(arg?"true":"false"));
          }
      
          public void setremove_unreachable_traps(boolean arg) {
            addArg("-p");
            addArg("jop.uce1");
            addArg("remove-unreachable-traps:"+(arg?"true":"false"));
          }
      
        }
    
        public Object createp_jop_ubf1() {
            Object ret = new PhaseOptjop_ubf1();
            phaseopts.add(ret);
            return ret;
        }
        public class PhaseOptjop_ubf1 {
      
          public void setenabled(boolean arg) {
            addArg("-p");
            addArg("jop.ubf1");
            addArg("enabled:"+(arg?"true":"false"));
          }
      
        }
    
        public Object createp_jop_uce2() {
            Object ret = new PhaseOptjop_uce2();
            phaseopts.add(ret);
            return ret;
        }
        public class PhaseOptjop_uce2 {
      
          public void setenabled(boolean arg) {
            addArg("-p");
            addArg("jop.uce2");
            addArg("enabled:"+(arg?"true":"false"));
          }
      
          public void setremove_unreachable_traps(boolean arg) {
            addArg("-p");
            addArg("jop.uce2");
            addArg("remove-unreachable-traps:"+(arg?"true":"false"));
          }
      
        }
    
        public Object createp_jop_ubf2() {
            Object ret = new PhaseOptjop_ubf2();
            phaseopts.add(ret);
            return ret;
        }
        public class PhaseOptjop_ubf2 {
      
          public void setenabled(boolean arg) {
            addArg("-p");
            addArg("jop.ubf2");
            addArg("enabled:"+(arg?"true":"false"));
          }
      
        }
    
        public Object createp_jop_ule() {
            Object ret = new PhaseOptjop_ule();
            phaseopts.add(ret);
            return ret;
        }
        public class PhaseOptjop_ule {
      
          public void setenabled(boolean arg) {
            addArg("-p");
            addArg("jop.ule");
            addArg("enabled:"+(arg?"true":"false"));
          }
      
        }
    
        public Object createp_jap() {
            Object ret = new PhaseOptjap();
            phaseopts.add(ret);
            return ret;
        }
        public class PhaseOptjap {
      
          public void setenabled(boolean arg) {
            addArg("-p");
            addArg("jap");
            addArg("enabled:"+(arg?"true":"false"));
          }
      
        }
    
        public Object createp_jap_npc() {
            Object ret = new PhaseOptjap_npc();
            phaseopts.add(ret);
            return ret;
        }
        public class PhaseOptjap_npc {
      
          public void setenabled(boolean arg) {
            addArg("-p");
            addArg("jap.npc");
            addArg("enabled:"+(arg?"true":"false"));
          }
      
          public void setonly_array_ref(boolean arg) {
            addArg("-p");
            addArg("jap.npc");
            addArg("only-array-ref:"+(arg?"true":"false"));
          }
      
          public void setprofiling(boolean arg) {
            addArg("-p");
            addArg("jap.npc");
            addArg("profiling:"+(arg?"true":"false"));
          }
      
        }
    
        public Object createp_jap_npcolorer() {
            Object ret = new PhaseOptjap_npcolorer();
            phaseopts.add(ret);
            return ret;
        }
        public class PhaseOptjap_npcolorer {
      
          public void setenabled(boolean arg) {
            addArg("-p");
            addArg("jap.npcolorer");
            addArg("enabled:"+(arg?"true":"false"));
          }
      
        }
    
        public Object createp_jap_profiling() {
            Object ret = new PhaseOptjap_profiling();
            phaseopts.add(ret);
            return ret;
        }
        public class PhaseOptjap_profiling {
      
          public void setenabled(boolean arg) {
            addArg("-p");
            addArg("jap.profiling");
            addArg("enabled:"+(arg?"true":"false"));
          }
      
          public void setnotmainentry(boolean arg) {
            addArg("-p");
            addArg("jap.profiling");
            addArg("notmainentry:"+(arg?"true":"false"));
          }
      
        }
    
        public Object createp_jap_sea() {
            Object ret = new PhaseOptjap_sea();
            phaseopts.add(ret);
            return ret;
        }
        public class PhaseOptjap_sea {
      
          public void setenabled(boolean arg) {
            addArg("-p");
            addArg("jap.sea");
            addArg("enabled:"+(arg?"true":"false"));
          }
      
          public void setnaive(boolean arg) {
            addArg("-p");
            addArg("jap.sea");
            addArg("naive:"+(arg?"true":"false"));
          }
      
        }
    
        public Object createp_jap_fieldrw() {
            Object ret = new PhaseOptjap_fieldrw();
            phaseopts.add(ret);
            return ret;
        }
        public class PhaseOptjap_fieldrw {
      
          public void setenabled(boolean arg) {
            addArg("-p");
            addArg("jap.fieldrw");
            addArg("enabled:"+(arg?"true":"false"));
          }
      
          public void setthreshold(String arg) {
            addArg("-p");
            addArg("jap.fieldrw");
            addArg("threshold:"+arg);
          }
      
        }
    
        public Object createp_jap_cgtagger() {
            Object ret = new PhaseOptjap_cgtagger();
            phaseopts.add(ret);
            return ret;
        }
        public class PhaseOptjap_cgtagger {
      
          public void setenabled(boolean arg) {
            addArg("-p");
            addArg("jap.cgtagger");
            addArg("enabled:"+(arg?"true":"false"));
          }
      
        }
    
        public Object createp_jap_parity() {
            Object ret = new PhaseOptjap_parity();
            phaseopts.add(ret);
            return ret;
        }
        public class PhaseOptjap_parity {
      
          public void setenabled(boolean arg) {
            addArg("-p");
            addArg("jap.parity");
            addArg("enabled:"+(arg?"true":"false"));
          }
      
        }
    
        public Object createp_jap_pat() {
            Object ret = new PhaseOptjap_pat();
            phaseopts.add(ret);
            return ret;
        }
        public class PhaseOptjap_pat {
      
          public void setenabled(boolean arg) {
            addArg("-p");
            addArg("jap.pat");
            addArg("enabled:"+(arg?"true":"false"));
          }
      
        }
    
        public Object createp_jap_lvtagger() {
            Object ret = new PhaseOptjap_lvtagger();
            phaseopts.add(ret);
            return ret;
        }
        public class PhaseOptjap_lvtagger {
      
          public void setenabled(boolean arg) {
            addArg("-p");
            addArg("jap.lvtagger");
            addArg("enabled:"+(arg?"true":"false"));
          }
      
        }
    
        public Object createp_jap_rdtagger() {
            Object ret = new PhaseOptjap_rdtagger();
            phaseopts.add(ret);
            return ret;
        }
        public class PhaseOptjap_rdtagger {
      
          public void setenabled(boolean arg) {
            addArg("-p");
            addArg("jap.rdtagger");
            addArg("enabled:"+(arg?"true":"false"));
          }
      
        }
    
        public Object createp_jap_che() {
            Object ret = new PhaseOptjap_che();
            phaseopts.add(ret);
            return ret;
        }
        public class PhaseOptjap_che {
      
          public void setenabled(boolean arg) {
            addArg("-p");
            addArg("jap.che");
            addArg("enabled:"+(arg?"true":"false"));
          }
      
        }
    
        public Object createp_jap_umt() {
            Object ret = new PhaseOptjap_umt();
            phaseopts.add(ret);
            return ret;
        }
        public class PhaseOptjap_umt {
      
          public void setenabled(boolean arg) {
            addArg("-p");
            addArg("jap.umt");
            addArg("enabled:"+(arg?"true":"false"));
          }
      
        }
    
        public Object createp_jap_lit() {
            Object ret = new PhaseOptjap_lit();
            phaseopts.add(ret);
            return ret;
        }
        public class PhaseOptjap_lit {
      
          public void setenabled(boolean arg) {
            addArg("-p");
            addArg("jap.lit");
            addArg("enabled:"+(arg?"true":"false"));
          }
      
        }
    
        public Object createp_jap_aet() {
            Object ret = new PhaseOptjap_aet();
            phaseopts.add(ret);
            return ret;
        }
        public class PhaseOptjap_aet {
      
          public void setenabled(boolean arg) {
            addArg("-p");
            addArg("jap.aet");
            addArg("enabled:"+(arg?"true":"false"));
          }
      
          public void setkind(String arg) {
            addArg("-p");
            addArg("jap.aet");
            addArg("kind:"+arg);
          }
      
        }
    
        public Object createp_jap_dmt() {
            Object ret = new PhaseOptjap_dmt();
            phaseopts.add(ret);
            return ret;
        }
        public class PhaseOptjap_dmt {
      
          public void setenabled(boolean arg) {
            addArg("-p");
            addArg("jap.dmt");
            addArg("enabled:"+(arg?"true":"false"));
          }
      
        }
    
        public Object createp_gb() {
            Object ret = new PhaseOptgb();
            phaseopts.add(ret);
            return ret;
        }
        public class PhaseOptgb {
      
          public void setenabled(boolean arg) {
            addArg("-p");
            addArg("gb");
            addArg("enabled:"+(arg?"true":"false"));
          }
      
        }
    
        public Object createp_gb_a1() {
            Object ret = new PhaseOptgb_a1();
            phaseopts.add(ret);
            return ret;
        }
        public class PhaseOptgb_a1 {
      
          public void setenabled(boolean arg) {
            addArg("-p");
            addArg("gb.a1");
            addArg("enabled:"+(arg?"true":"false"));
          }
      
          public void setonly_stack_locals(boolean arg) {
            addArg("-p");
            addArg("gb.a1");
            addArg("only-stack-locals:"+(arg?"true":"false"));
          }
      
        }
    
        public Object createp_gb_cf() {
            Object ret = new PhaseOptgb_cf();
            phaseopts.add(ret);
            return ret;
        }
        public class PhaseOptgb_cf {
      
          public void setenabled(boolean arg) {
            addArg("-p");
            addArg("gb.cf");
            addArg("enabled:"+(arg?"true":"false"));
          }
      
        }
    
        public Object createp_gb_a2() {
            Object ret = new PhaseOptgb_a2();
            phaseopts.add(ret);
            return ret;
        }
        public class PhaseOptgb_a2 {
      
          public void setenabled(boolean arg) {
            addArg("-p");
            addArg("gb.a2");
            addArg("enabled:"+(arg?"true":"false"));
          }
      
          public void setonly_stack_locals(boolean arg) {
            addArg("-p");
            addArg("gb.a2");
            addArg("only-stack-locals:"+(arg?"true":"false"));
          }
      
        }
    
        public Object createp_gb_ule() {
            Object ret = new PhaseOptgb_ule();
            phaseopts.add(ret);
            return ret;
        }
        public class PhaseOptgb_ule {
      
          public void setenabled(boolean arg) {
            addArg("-p");
            addArg("gb.ule");
            addArg("enabled:"+(arg?"true":"false"));
          }
      
        }
    
        public Object createp_gop() {
            Object ret = new PhaseOptgop();
            phaseopts.add(ret);
            return ret;
        }
        public class PhaseOptgop {
      
          public void setenabled(boolean arg) {
            addArg("-p");
            addArg("gop");
            addArg("enabled:"+(arg?"true":"false"));
          }
      
        }
    
        public Object createp_bb() {
            Object ret = new PhaseOptbb();
            phaseopts.add(ret);
            return ret;
        }
        public class PhaseOptbb {
      
          public void setenabled(boolean arg) {
            addArg("-p");
            addArg("bb");
            addArg("enabled:"+(arg?"true":"false"));
          }
      
        }
    
        public Object createp_bb_lso() {
            Object ret = new PhaseOptbb_lso();
            phaseopts.add(ret);
            return ret;
        }
        public class PhaseOptbb_lso {
      
          public void setenabled(boolean arg) {
            addArg("-p");
            addArg("bb.lso");
            addArg("enabled:"+(arg?"true":"false"));
          }
      
          public void setdebug(boolean arg) {
            addArg("-p");
            addArg("bb.lso");
            addArg("debug:"+(arg?"true":"false"));
          }
      
          public void setinter(boolean arg) {
            addArg("-p");
            addArg("bb.lso");
            addArg("inter:"+(arg?"true":"false"));
          }
      
          public void setsl(boolean arg) {
            addArg("-p");
            addArg("bb.lso");
            addArg("sl:"+(arg?"true":"false"));
          }
      
          public void setsl2(boolean arg) {
            addArg("-p");
            addArg("bb.lso");
            addArg("sl2:"+(arg?"true":"false"));
          }
      
          public void setsll(boolean arg) {
            addArg("-p");
            addArg("bb.lso");
            addArg("sll:"+(arg?"true":"false"));
          }
      
          public void setsll2(boolean arg) {
            addArg("-p");
            addArg("bb.lso");
            addArg("sll2:"+(arg?"true":"false"));
          }
      
        }
    
        public Object createp_bb_sco() {
            Object ret = new PhaseOptbb_sco();
            phaseopts.add(ret);
            return ret;
        }
        public class PhaseOptbb_sco {
      
          public void setenabled(boolean arg) {
            addArg("-p");
            addArg("bb.sco");
            addArg("enabled:"+(arg?"true":"false"));
          }
      
        }
    
        public Object createp_bb_pho() {
            Object ret = new PhaseOptbb_pho();
            phaseopts.add(ret);
            return ret;
        }
        public class PhaseOptbb_pho {
      
          public void setenabled(boolean arg) {
            addArg("-p");
            addArg("bb.pho");
            addArg("enabled:"+(arg?"true":"false"));
          }
      
        }
    
        public Object createp_bb_ule() {
            Object ret = new PhaseOptbb_ule();
            phaseopts.add(ret);
            return ret;
        }
        public class PhaseOptbb_ule {
      
          public void setenabled(boolean arg) {
            addArg("-p");
            addArg("bb.ule");
            addArg("enabled:"+(arg?"true":"false"));
          }
      
        }
    
        public Object createp_bb_lp() {
            Object ret = new PhaseOptbb_lp();
            phaseopts.add(ret);
            return ret;
        }
        public class PhaseOptbb_lp {
      
          public void setenabled(boolean arg) {
            addArg("-p");
            addArg("bb.lp");
            addArg("enabled:"+(arg?"true":"false"));
          }
      
          public void setunsplit_original_locals(boolean arg) {
            addArg("-p");
            addArg("bb.lp");
            addArg("unsplit-original-locals:"+(arg?"true":"false"));
          }
      
        }
    
        public Object createp_bb_ne() {
            Object ret = new PhaseOptbb_ne();
            phaseopts.add(ret);
            return ret;
        }
        public class PhaseOptbb_ne {
      
          public void setenabled(boolean arg) {
            addArg("-p");
            addArg("bb.ne");
            addArg("enabled:"+(arg?"true":"false"));
          }
      
        }
    
        public Object createp_bop() {
            Object ret = new PhaseOptbop();
            phaseopts.add(ret);
            return ret;
        }
        public class PhaseOptbop {
      
          public void setenabled(boolean arg) {
            addArg("-p");
            addArg("bop");
            addArg("enabled:"+(arg?"true":"false"));
          }
      
        }
    
        public Object createp_tag() {
            Object ret = new PhaseOpttag();
            phaseopts.add(ret);
            return ret;
        }
        public class PhaseOpttag {
      
          public void setenabled(boolean arg) {
            addArg("-p");
            addArg("tag");
            addArg("enabled:"+(arg?"true":"false"));
          }
      
        }
    
        public Object createp_tag_ln() {
            Object ret = new PhaseOpttag_ln();
            phaseopts.add(ret);
            return ret;
        }
        public class PhaseOpttag_ln {
      
          public void setenabled(boolean arg) {
            addArg("-p");
            addArg("tag.ln");
            addArg("enabled:"+(arg?"true":"false"));
          }
      
        }
    
        public Object createp_tag_an() {
            Object ret = new PhaseOpttag_an();
            phaseopts.add(ret);
            return ret;
        }
        public class PhaseOpttag_an {
      
          public void setenabled(boolean arg) {
            addArg("-p");
            addArg("tag.an");
            addArg("enabled:"+(arg?"true":"false"));
          }
      
        }
    
        public Object createp_tag_dep() {
            Object ret = new PhaseOpttag_dep();
            phaseopts.add(ret);
            return ret;
        }
        public class PhaseOpttag_dep {
      
          public void setenabled(boolean arg) {
            addArg("-p");
            addArg("tag.dep");
            addArg("enabled:"+(arg?"true":"false"));
          }
      
        }
    
        public Object createp_tag_fieldrw() {
            Object ret = new PhaseOpttag_fieldrw();
            phaseopts.add(ret);
            return ret;
        }
        public class PhaseOpttag_fieldrw {
      
          public void setenabled(boolean arg) {
            addArg("-p");
            addArg("tag.fieldrw");
            addArg("enabled:"+(arg?"true":"false"));
          }
      
        }
    
        public Object createp_db() {
            Object ret = new PhaseOptdb();
            phaseopts.add(ret);
            return ret;
        }
        public class PhaseOptdb {
      
          public void setenabled(boolean arg) {
            addArg("-p");
            addArg("db");
            addArg("enabled:"+(arg?"true":"false"));
          }
      
          public void setsource_is_javac(boolean arg) {
            addArg("-p");
            addArg("db");
            addArg("source-is-javac:"+(arg?"true":"false"));
          }
      
        }
    
        public Object createp_db_transformations() {
            Object ret = new PhaseOptdb_transformations();
            phaseopts.add(ret);
            return ret;
        }
        public class PhaseOptdb_transformations {
      
          public void setenabled(boolean arg) {
            addArg("-p");
            addArg("db.transformations");
            addArg("enabled:"+(arg?"true":"false"));
          }
      
        }
    
        public Object createp_db_renamer() {
            Object ret = new PhaseOptdb_renamer();
            phaseopts.add(ret);
            return ret;
        }
        public class PhaseOptdb_renamer {
      
          public void setenabled(boolean arg) {
            addArg("-p");
            addArg("db.renamer");
            addArg("enabled:"+(arg?"true":"false"));
          }
      
        }
    
        public Object createp_db_deobfuscate() {
            Object ret = new PhaseOptdb_deobfuscate();
            phaseopts.add(ret);
            return ret;
        }
        public class PhaseOptdb_deobfuscate {
      
          public void setenabled(boolean arg) {
            addArg("-p");
            addArg("db.deobfuscate");
            addArg("enabled:"+(arg?"true":"false"));
          }
      
        }
    
        public Object createp_db_force_recompile() {
            Object ret = new PhaseOptdb_force_recompile();
            phaseopts.add(ret);
            return ret;
        }
        public class PhaseOptdb_force_recompile {
      
          public void setenabled(boolean arg) {
            addArg("-p");
            addArg("db.force-recompile");
            addArg("enabled:"+(arg?"true":"false"));
          }
      
        }
    
}
  