package soot.options;

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

/* THIS FILE IS AUTO-GENERATED FROM soot_options.xml. DO NOT MODIFY. */

import soot.*;
import java.util.*;

/**
 * Soot command-line options parser.
 *
 * @author Ondrej Lhotak
 */
@javax.annotation.Generated(value = "Saxonica v3.0", comments = "from soot_options.xml")
public class Options extends OptionsBase {

    public Options(Singletons.Global g) {
    }

    public static Options v() {
        return G.v().soot_options_Options();
    }

    public static final int src_prec_c = 1;
    public static final int src_prec_class = 1;
    public static final int src_prec_only_class = 2;
    public static final int src_prec_J = 3;
    public static final int src_prec_jimple = 3;
    public static final int src_prec_java = 4;
    public static final int src_prec_apk = 5;
    public static final int src_prec_apk_class_jimple = 6;
    public static final int src_prec_apk_c_j = 6;
    public static final int src_prec_MAX = 6;
    public static final int output_format_J = 1;
    public static final int output_format_jimple = 1;
    public static final int output_format_j = 2;
    public static final int output_format_jimp = 2;
    public static final int output_format_S = 3;
    public static final int output_format_shimple = 3;
    public static final int output_format_s = 4;
    public static final int output_format_shimp = 4;
    public static final int output_format_B = 5;
    public static final int output_format_baf = 5;
    public static final int output_format_b = 6;
    public static final int output_format_G = 7;
    public static final int output_format_grimple = 7;
    public static final int output_format_g = 8;
    public static final int output_format_grimp = 8;
    public static final int output_format_X = 9;
    public static final int output_format_xml = 9;
    public static final int output_format_dex = 10;
    public static final int output_format_force_dex = 11;
    public static final int output_format_n = 12;
    public static final int output_format_none = 12;
    public static final int output_format_jasmin = 13;
    public static final int output_format_c = 14;
    public static final int output_format_class = 14;
    public static final int output_format_d = 15;
    public static final int output_format_dava = 15;
    public static final int output_format_t = 16;
    public static final int output_format_template = 16;
    public static final int output_format_a = 17;
    public static final int output_format_asm = 17;
    public static final int output_format_MAX = 17;
    public static final int java_version_default = 1;
    public static final int java_version_1_1 = 2;
    public static final int java_version_1 = 2;
    public static final int java_version_1_2 = 3;
    public static final int java_version_2 = 3;
    public static final int java_version_1_3 = 4;
    public static final int java_version_3 = 4;
    public static final int java_version_1_4 = 5;
    public static final int java_version_4 = 5;
    public static final int java_version_1_5 = 6;
    public static final int java_version_5 = 6;
    public static final int java_version_1_6 = 7;
    public static final int java_version_6 = 7;
    public static final int java_version_1_7 = 8;
    public static final int java_version_7 = 8;
    public static final int java_version_1_8 = 9;
    public static final int java_version_8 = 9;
    public static final int java_version_1_9 = 10;
    public static final int java_version_9 = 10;
    public static final int java_version_1_10 = 11;
    public static final int java_version_10 = 11;
    public static final int java_version_1_11 = 12;
    public static final int java_version_11 = 12;
    public static final int java_version_1_12 = 13;
    public static final int java_version_12 = 13;
    public static final int java_version_MAX = 13;
    public static final int wrong_staticness_fail = 1;
    public static final int wrong_staticness_ignore = 2;
    public static final int wrong_staticness_fix = 3;
    public static final int wrong_staticness_fixstrict = 4;
    public static final int wrong_staticness_MAX = 4;
    public static final int field_type_mismatches_fail = 1;
    public static final int field_type_mismatches_ignore = 2;
    public static final int field_type_mismatches_null = 3;
    public static final int field_type_mismatches_MAX = 3;
    public static final int throw_analysis_pedantic = 1;
    public static final int throw_analysis_unit = 2;
    public static final int throw_analysis_dalvik = 3;
    public static final int throw_analysis_auto_select = 4;
    public static final int throw_analysis_MAX = 4;
    public static final int check_init_throw_analysis_auto = 1;
    public static final int check_init_throw_analysis_pedantic = 2;
    public static final int check_init_throw_analysis_unit = 3;
    public static final int check_init_throw_analysis_dalvik = 4;
    public static final int check_init_throw_analysis_MAX = 4;

    @SuppressWarnings("unused")
    public boolean parse(String[] argv) {
        List<String> phaseOptions = new LinkedList<>();

        for(int i = argv.length; i > 0; i--) {
            pushOption(argv[i-1]);
        }

        while(hasMoreOptions()) {
            String option = nextOption();

            if(option.charAt(0) != '-') {
                classes.add(option);
                continue;
            }

            while(option.charAt(0) == '-') {
                option = option.substring(1);
            }

            if (false);
            else if (false
                    || option.equals("coffi")
            )
                coffi = true;
            else if (false
                    || option.equals("jasmin-backend")
            )
                jasmin_backend = true;
            else if (false
                    || option.equals("h")
                    || option.equals("help")
            )
                help = true;
            else if (false
                    || option.equals("pl")
                    || option.equals("phase-list")
            )
                phase_list = true;
            else if (false
                    || option.equals("ph")
                    || option.equals("phase-help")
            ) {
                if (!hasMoreOptions()) {
                    G.v().out.println("No value given for option -" + option);
                    return false;
                }

                String value = nextOption();
                if (phase_help == null)
                    phase_help = new LinkedList<>();
                phase_help.add(value);
            }
            else if (false
                    || option.equals("version")
            )
                version = true;
            else if (false
                    || option.equals("v")
                    || option.equals("verbose")
            )
                verbose = true;
            else if (false
                    || option.equals("interactive-mode")
            )
                interactive_mode = true;
            else if (false
                    || option.equals("unfriendly-mode")
            )
                unfriendly_mode = true;
            else if (false
                    || option.equals("app")
            )
                app = true;
            else if (false
                    || option.equals("w")
                    || option.equals("whole-program")
            )
                whole_program = true;
            else if (false
                    || option.equals("ws")
                    || option.equals("whole-shimple")
            )
                whole_shimple = true;
            else if (false
                    || option.equals("fly")
                    || option.equals("on-the-fly")
            )
                on_the_fly = true;
            else if (false
                    || option.equals("validate")
            )
                validate = true;
            else if (false
                    || option.equals("debug")
            )
                debug = true;
            else if (false
                    || option.equals("debug-resolver")
            )
                debug_resolver = true;
            else if (false
                    || option.equals("ignore-resolving-levels")
            )
                ignore_resolving_levels = true;
            else if (false
                    || option.equals("weak-map-structures")
            )
                weak_map_structures = true;
            else if (false
                    || option.equals("cp")
                    || option.equals("soot-class-path")
                    || option.equals("soot-classpath")
            ) {
                if (!hasMoreOptions()) {
                    G.v().out.println("No value given for option -" + option);
                    return false;
                }

                String value = nextOption();
                if (soot_classpath.isEmpty())
                    soot_classpath = value;
                else {
                    G.v().out.println("Duplicate values " + soot_classpath + " and " + value + " for option -" + option);
                    return false;
                }
            }
            else if (false
                    || option.equals("soot-modulepath")
            ) {
                if (!hasMoreOptions()) {
                    G.v().out.println("No value given for option -" + option);
                    return false;
                }

                String value = nextOption();
                if (soot_modulepath.isEmpty())
                    soot_modulepath = value;
                else {
                    G.v().out.println("Duplicate values " + soot_modulepath + " and " + value + " for option -" + option);
                    return false;
                }
            }
            else if (false
                    || option.equals("pp")
                    || option.equals("prepend-classpath")
            )
                prepend_classpath = true;
            else if (false
                    || option.equals("ice")
                    || option.equals("ignore-classpath-errors")
            )
                ignore_classpath_errors = true;
            else if (false
                    || option.equals("process-multiple-dex")
            )
                process_multiple_dex = true;
            else if (false
                    || option.equals("search-dex-in-archives")
            )
                search_dex_in_archives = true;
            else if (false
                    || option.equals("process-path")
                    || option.equals("process-dir")
            ) {
                if (!hasMoreOptions()) {
                    G.v().out.println("No value given for option -" + option);
                    return false;
                }

                String value = nextOption();
                if (process_dir == null)
                    process_dir = new LinkedList<>();
                process_dir.add(value);
            }
            else if (false
                    || option.equals("process-jar-dir")
            ) {
                if (!hasMoreOptions()) {
                    G.v().out.println("No value given for option -" + option);
                    return false;
                }

                String value = nextOption();
                if (process_jar_dir == null)
                    process_jar_dir = new LinkedList<>();
                process_jar_dir.add(value);
            }
            else if (false
                    || option.equals("no-derive-java-version")
            )
                derive_java_version = false;
            else if (false
                    || option.equals("oaat")
            )
                oaat = true;
            else if (false
                    || option.equals("android-jars")
            ) {
                if (!hasMoreOptions()) {
                    G.v().out.println("No value given for option -" + option);
                    return false;
                }

                String value = nextOption();
                if (android_jars.isEmpty())
                    android_jars = value;
                else {
                    G.v().out.println("Duplicate values " + android_jars + " and " + value + " for option -" + option);
                    return false;
                }
            }
            else if (false
                    || option.equals("force-android-jar")
            ) {
                if (!hasMoreOptions()) {
                    G.v().out.println("No value given for option -" + option);
                    return false;
                }

                String value = nextOption();
                if (force_android_jar.isEmpty())
                    force_android_jar = value;
                else {
                    G.v().out.println("Duplicate values " + force_android_jar + " and " + value + " for option -" + option);
                    return false;
                }
            }
            else if (false
                || option.equals("android-api-version")
            ) {
                if (!hasMoreOptions()) {
                    G.v().out.println("No value given for option -" + option);
                    return false;
                }

                String value = nextOption();
                if(android_api_version == -1)
                    android_api_version = Integer.valueOf(value);
                else {
                    G.v().out.println("Duplicate values " + android_api_version + " and " + value + " for option -" + option);
                    return false;
                }
            }
            else if (false
                    || option.equals("ast-metrics")
            )
                ast_metrics = true;
            else if (false
                    || option.equals("src-prec")
            ) {
                if (!hasMoreOptions()) {
                    G.v().out.println("No value given for option -" + option);
                    return false;
                }

                String value = nextOption();
        
                if (false);
                else if (false
                        || value.equals("c")
                        || value.equals("class")
                ) {
                    if (src_prec != 0 && src_prec != src_prec_class) {
                        G.v().out.println("Multiple values given for option " + option);
                        return false;
                    }
                    src_prec = src_prec_class;
                }
                else if (false
                        || value.equals("only-class")
                ) {
                    if (src_prec != 0 && src_prec != src_prec_only_class) {
                        G.v().out.println("Multiple values given for option " + option);
                        return false;
                    }
                    src_prec = src_prec_only_class;
                }
                else if (false
                        || value.equals("J")
                        || value.equals("jimple")
                ) {
                    if (src_prec != 0 && src_prec != src_prec_jimple) {
                        G.v().out.println("Multiple values given for option " + option);
                        return false;
                    }
                    src_prec = src_prec_jimple;
                }
                else if (false
                        || value.equals("java")
                ) {
                    if (src_prec != 0 && src_prec != src_prec_java) {
                        G.v().out.println("Multiple values given for option " + option);
                        return false;
                    }
                    src_prec = src_prec_java;
                }
                else if (false
                        || value.equals("apk")
                ) {
                    if (src_prec != 0 && src_prec != src_prec_apk) {
                        G.v().out.println("Multiple values given for option " + option);
                        return false;
                    }
                    src_prec = src_prec_apk;
                }
                else if (false
                        || value.equals("apk-class-jimple")
                        || value.equals("apk-c-j")
                ) {
                    if (src_prec != 0 && src_prec != src_prec_apk_c_j) {
                        G.v().out.println("Multiple values given for option " + option);
                        return false;
                    }
                    src_prec = src_prec_apk_c_j;
                }
                else {
                    G.v().out.println(String.format("Invalid value %s given for option -%s", value, option));
                    return false;
                }
            }
            else if (false
                    || option.equals("full-resolver")
            )
                full_resolver = true;
            else if (false
                    || option.equals("allow-phantom-refs")
            )
                allow_phantom_refs = true;
            else if (false
                    || option.equals("allow-phantom-elms")
            )
                allow_phantom_elms = true;
            else if (false
                    || option.equals("allow-cg-errors")
            )
                allow_cg_errors = true;
            else if (false
                    || option.equals("no-bodies-for-excluded")
            )
                no_bodies_for_excluded = true;
            else if (false
                    || option.equals("j2me")
            )
                j2me = true;
            else if (false
                    || option.equals("main-class")
            ) {
                if (!hasMoreOptions()) {
                    G.v().out.println("No value given for option -" + option);
                    return false;
                }

                String value = nextOption();
                if (main_class.isEmpty())
                    main_class = value;
                else {
                    G.v().out.println("Duplicate values " + main_class + " and " + value + " for option -" + option);
                    return false;
                }
            }
            else if (false
                    || option.equals("polyglot")
            )
                polyglot = true;
            else if (false
                    || option.equals("permissive-resolving")
            )
                permissive_resolving = true;
            else if (false
                    || option.equals("no-drop-bodies-after-load")
            )
                drop_bodies_after_load = false;
            else if (false
                    || option.equals("d")
                    || option.equals("output-dir")
            ) {
                if (!hasMoreOptions()) {
                    G.v().out.println("No value given for option -" + option);
                    return false;
                }

                String value = nextOption();
                if (output_dir.isEmpty())
                    output_dir = value;
                else {
                    G.v().out.println("Duplicate values " + output_dir + " and " + value + " for option -" + option);
                    return false;
                }
            }
            else if (false
                    || option.equals("f")
                    || option.equals("output-format")
            ) {
                if (!hasMoreOptions()) {
                    G.v().out.println("No value given for option -" + option);
                    return false;
                }

                String value = nextOption();
        
                if (false);
                else if (false
                        || value.equals("J")
                        || value.equals("jimple")
                ) {
                    if (output_format != 0 && output_format != output_format_jimple) {
                        G.v().out.println("Multiple values given for option " + option);
                        return false;
                    }
                    output_format = output_format_jimple;
                }
                else if (false
                        || value.equals("j")
                        || value.equals("jimp")
                ) {
                    if (output_format != 0 && output_format != output_format_jimp) {
                        G.v().out.println("Multiple values given for option " + option);
                        return false;
                    }
                    output_format = output_format_jimp;
                }
                else if (false
                        || value.equals("S")
                        || value.equals("shimple")
                ) {
                    if (output_format != 0 && output_format != output_format_shimple) {
                        G.v().out.println("Multiple values given for option " + option);
                        return false;
                    }
                    output_format = output_format_shimple;
                }
                else if (false
                        || value.equals("s")
                        || value.equals("shimp")
                ) {
                    if (output_format != 0 && output_format != output_format_shimp) {
                        G.v().out.println("Multiple values given for option " + option);
                        return false;
                    }
                    output_format = output_format_shimp;
                }
                else if (false
                        || value.equals("B")
                        || value.equals("baf")
                ) {
                    if (output_format != 0 && output_format != output_format_baf) {
                        G.v().out.println("Multiple values given for option " + option);
                        return false;
                    }
                    output_format = output_format_baf;
                }
                else if (false
                        || value.equals("b")
                ) {
                    if (output_format != 0 && output_format != output_format_b) {
                        G.v().out.println("Multiple values given for option " + option);
                        return false;
                    }
                    output_format = output_format_b;
                }
                else if (false
                        || value.equals("G")
                        || value.equals("grimple")
                ) {
                    if (output_format != 0 && output_format != output_format_grimple) {
                        G.v().out.println("Multiple values given for option " + option);
                        return false;
                    }
                    output_format = output_format_grimple;
                }
                else if (false
                        || value.equals("g")
                        || value.equals("grimp")
                ) {
                    if (output_format != 0 && output_format != output_format_grimp) {
                        G.v().out.println("Multiple values given for option " + option);
                        return false;
                    }
                    output_format = output_format_grimp;
                }
                else if (false
                        || value.equals("X")
                        || value.equals("xml")
                ) {
                    if (output_format != 0 && output_format != output_format_xml) {
                        G.v().out.println("Multiple values given for option " + option);
                        return false;
                    }
                    output_format = output_format_xml;
                }
                else if (false
                        || value.equals("dex")
                ) {
                    if (output_format != 0 && output_format != output_format_dex) {
                        G.v().out.println("Multiple values given for option " + option);
                        return false;
                    }
                    output_format = output_format_dex;
                }
                else if (false
                        || value.equals("force-dex")
                ) {
                    if (output_format != 0 && output_format != output_format_force_dex) {
                        G.v().out.println("Multiple values given for option " + option);
                        return false;
                    }
                    output_format = output_format_force_dex;
                }
                else if (false
                        || value.equals("n")
                        || value.equals("none")
                ) {
                    if (output_format != 0 && output_format != output_format_none) {
                        G.v().out.println("Multiple values given for option " + option);
                        return false;
                    }
                    output_format = output_format_none;
                }
                else if (false
                        || value.equals("jasmin")
                ) {
                    if (output_format != 0 && output_format != output_format_jasmin) {
                        G.v().out.println("Multiple values given for option " + option);
                        return false;
                    }
                    output_format = output_format_jasmin;
                }
                else if (false
                        || value.equals("c")
                        || value.equals("class")
                ) {
                    if (output_format != 0 && output_format != output_format_class) {
                        G.v().out.println("Multiple values given for option " + option);
                        return false;
                    }
                    output_format = output_format_class;
                }
                else if (false
                        || value.equals("d")
                        || value.equals("dava")
                ) {
                    if (output_format != 0 && output_format != output_format_dava) {
                        G.v().out.println("Multiple values given for option " + option);
                        return false;
                    }
                    output_format = output_format_dava;
                }
                else if (false
                        || value.equals("t")
                        || value.equals("template")
                ) {
                    if (output_format != 0 && output_format != output_format_template) {
                        G.v().out.println("Multiple values given for option " + option);
                        return false;
                    }
                    output_format = output_format_template;
                }
                else if (false
                        || value.equals("a")
                        || value.equals("asm")
                ) {
                    if (output_format != 0 && output_format != output_format_asm) {
                        G.v().out.println("Multiple values given for option " + option);
                        return false;
                    }
                    output_format = output_format_asm;
                }
                else {
                    G.v().out.println(String.format("Invalid value %s given for option -%s", value, option));
                    return false;
                }
            }
            else if (false
                    || option.equals("java-version")
            ) {
                if (!hasMoreOptions()) {
                    G.v().out.println("No value given for option -" + option);
                    return false;
                }

                String value = nextOption();
        
                if (false);
                else if (false
                        || value.equals("default")
                ) {
                    if (java_version != 0 && java_version != java_version_default) {
                        G.v().out.println("Multiple values given for option " + option);
                        return false;
                    }
                    java_version = java_version_default;
                }
                else if (false
                        || value.equals("1.1")
                        || value.equals("1")
                ) {
                    if (java_version != 0 && java_version != java_version_1) {
                        G.v().out.println("Multiple values given for option " + option);
                        return false;
                    }
                    java_version = java_version_1;
                }
                else if (false
                        || value.equals("1.2")
                        || value.equals("2")
                ) {
                    if (java_version != 0 && java_version != java_version_2) {
                        G.v().out.println("Multiple values given for option " + option);
                        return false;
                    }
                    java_version = java_version_2;
                }
                else if (false
                        || value.equals("1.3")
                        || value.equals("3")
                ) {
                    if (java_version != 0 && java_version != java_version_3) {
                        G.v().out.println("Multiple values given for option " + option);
                        return false;
                    }
                    java_version = java_version_3;
                }
                else if (false
                        || value.equals("1.4")
                        || value.equals("4")
                ) {
                    if (java_version != 0 && java_version != java_version_4) {
                        G.v().out.println("Multiple values given for option " + option);
                        return false;
                    }
                    java_version = java_version_4;
                }
                else if (false
                        || value.equals("1.5")
                        || value.equals("5")
                ) {
                    if (java_version != 0 && java_version != java_version_5) {
                        G.v().out.println("Multiple values given for option " + option);
                        return false;
                    }
                    java_version = java_version_5;
                }
                else if (false
                        || value.equals("1.6")
                        || value.equals("6")
                ) {
                    if (java_version != 0 && java_version != java_version_6) {
                        G.v().out.println("Multiple values given for option " + option);
                        return false;
                    }
                    java_version = java_version_6;
                }
                else if (false
                        || value.equals("1.7")
                        || value.equals("7")
                ) {
                    if (java_version != 0 && java_version != java_version_7) {
                        G.v().out.println("Multiple values given for option " + option);
                        return false;
                    }
                    java_version = java_version_7;
                }
                else if (false
                        || value.equals("1.8")
                        || value.equals("8")
                ) {
                    if (java_version != 0 && java_version != java_version_8) {
                        G.v().out.println("Multiple values given for option " + option);
                        return false;
                    }
                    java_version = java_version_8;
                }
                else if (false
                        || value.equals("1.9")
                        || value.equals("9")
                ) {
                    if (java_version != 0 && java_version != java_version_9) {
                        G.v().out.println("Multiple values given for option " + option);
                        return false;
                    }
                    java_version = java_version_9;
                }
                else if (false
                        || value.equals("1.10")
                        || value.equals("10")
                ) {
                    if (java_version != 0 && java_version != java_version_10) {
                        G.v().out.println("Multiple values given for option " + option);
                        return false;
                    }
                    java_version = java_version_10;
                }
                else if (false
                        || value.equals("1.11")
                        || value.equals("11")
                ) {
                    if (java_version != 0 && java_version != java_version_11) {
                        G.v().out.println("Multiple values given for option " + option);
                        return false;
                    }
                    java_version = java_version_11;
                }
                else if (false
                        || value.equals("1.12")
                        || value.equals("12")
                ) {
                    if (java_version != 0 && java_version != java_version_12) {
                        G.v().out.println("Multiple values given for option " + option);
                        return false;
                    }
                    java_version = java_version_12;
                }
                else {
                    G.v().out.println(String.format("Invalid value %s given for option -%s", value, option));
                    return false;
                }
            }
            else if (false
                    || option.equals("outjar")
                    || option.equals("output-jar")
            )
                output_jar = true;
            else if (false
                    || option.equals("hierarchy-dirs")
            )
                hierarchy_dirs = true;
            else if (false
                    || option.equals("xml-attributes")
            )
                xml_attributes = true;
            else if (false
                    || option.equals("print-tags")
                    || option.equals("print-tags-in-output")
            )
                print_tags_in_output = true;
            else if (false
                    || option.equals("no-output-source-file-attribute")
            )
                no_output_source_file_attribute = true;
            else if (false
                    || option.equals("no-output-inner-classes-attribute")
            )
                no_output_inner_classes_attribute = true;
            else if (false
                    || option.equals("dump-body")
            ) {
                if (!hasMoreOptions()) {
                    G.v().out.println("No value given for option -" + option);
                    return false;
                }

                String value = nextOption();
                if (dump_body == null)
                    dump_body = new LinkedList<>();
                dump_body.add(value);
            }
            else if (false
                    || option.equals("dump-cfg")
            ) {
                if (!hasMoreOptions()) {
                    G.v().out.println("No value given for option -" + option);
                    return false;
                }

                String value = nextOption();
                if (dump_cfg == null)
                    dump_cfg = new LinkedList<>();
                dump_cfg.add(value);
            }
            else if (false
                    || option.equals("no-show-exception-dests")
            )
                show_exception_dests = false;
            else if (false
                    || option.equals("gzip")
            )
                gzip = true;
            else if (false
                    || option.equals("force-overwrite")
            )
                force_overwrite = true;
            else if (false
                    || option.equals("plugin")
            ) {
                if (!hasMoreOptions()) {
                    G.v().out.println("No value given for option -" + option);
                    return false;
                }

                String value = nextOption();
                if (plugin == null)
                    plugin = new LinkedList<>();
                plugin.add(value);
                if (!loadPluginConfiguration(value)) {
                    G.v().out.println("Failed to load plugin " + value);
                    return false;
                }
        
            }
            else if (false
                    || option.equals("wrong-staticness")
            ) {
                if (!hasMoreOptions()) {
                    G.v().out.println("No value given for option -" + option);
                    return false;
                }

                String value = nextOption();
        
                if (false);
                else if (false
                        || value.equals("fail")
                ) {
                    if (wrong_staticness != 0 && wrong_staticness != wrong_staticness_fail) {
                        G.v().out.println("Multiple values given for option " + option);
                        return false;
                    }
                    wrong_staticness = wrong_staticness_fail;
                }
                else if (false
                        || value.equals("ignore")
                ) {
                    if (wrong_staticness != 0 && wrong_staticness != wrong_staticness_ignore) {
                        G.v().out.println("Multiple values given for option " + option);
                        return false;
                    }
                    wrong_staticness = wrong_staticness_ignore;
                }
                else if (false
                        || value.equals("fix")
                ) {
                    if (wrong_staticness != 0 && wrong_staticness != wrong_staticness_fix) {
                        G.v().out.println("Multiple values given for option " + option);
                        return false;
                    }
                    wrong_staticness = wrong_staticness_fix;
                }
                else if (false
                        || value.equals("fixstrict")
                ) {
                    if (wrong_staticness != 0 && wrong_staticness != wrong_staticness_fixstrict) {
                        G.v().out.println("Multiple values given for option " + option);
                        return false;
                    }
                    wrong_staticness = wrong_staticness_fixstrict;
                }
                else {
                    G.v().out.println(String.format("Invalid value %s given for option -%s", value, option));
                    return false;
                }
            }
            else if (false
                    || option.equals("field-type-mismatches")
            ) {
                if (!hasMoreOptions()) {
                    G.v().out.println("No value given for option -" + option);
                    return false;
                }

                String value = nextOption();
        
                if (false);
                else if (false
                        || value.equals("fail")
                ) {
                    if (field_type_mismatches != 0 && field_type_mismatches != field_type_mismatches_fail) {
                        G.v().out.println("Multiple values given for option " + option);
                        return false;
                    }
                    field_type_mismatches = field_type_mismatches_fail;
                }
                else if (false
                        || value.equals("ignore")
                ) {
                    if (field_type_mismatches != 0 && field_type_mismatches != field_type_mismatches_ignore) {
                        G.v().out.println("Multiple values given for option " + option);
                        return false;
                    }
                    field_type_mismatches = field_type_mismatches_ignore;
                }
                else if (false
                        || value.equals("null")
                ) {
                    if (field_type_mismatches != 0 && field_type_mismatches != field_type_mismatches_null) {
                        G.v().out.println("Multiple values given for option " + option);
                        return false;
                    }
                    field_type_mismatches = field_type_mismatches_null;
                }
                else {
                    G.v().out.println(String.format("Invalid value %s given for option -%s", value, option));
                    return false;
                }
            }
            else if (false
                || option.equals("p")
                || option.equals("phase-option")
            ) {
                if (!hasMoreOptions()) {
                    G.v().out.println("No phase name given for option -" + option);
                    return false;
                }
                String phaseName = nextOption();
                if (!hasMoreOptions()) {
                    G.v().out.println("No phase option given for option -" + option + " " + phaseName);
                    return false;
                }
                String phaseOption = nextOption();
        
                phaseOptions.add(phaseName);
                phaseOptions.add(phaseOption);
            }
            else if (false
                || option.equals("t")
                || option.equals("num-threads")
            ) {
                if (!hasMoreOptions()) {
                    G.v().out.println("No value given for option -" + option);
                    return false;
                }

                String value = nextOption();
                if(num_threads == -1)
                    num_threads = Integer.valueOf(value);
                else {
                    G.v().out.println("Duplicate values " + num_threads + " and " + value + " for option -" + option);
                    return false;
                }
            }
            else if (false
                || option.equals("O")
                || option.equals("optimize")
            ) {
                pushOption("enabled:true");
                pushOption("sop");
                pushOption("-p");
                pushOption("enabled:true");
                pushOption("jop");
                pushOption("-p");
                pushOption("enabled:true");
                pushOption("gop");
                pushOption("-p");
                pushOption("enabled:true");
                pushOption("bop");
                pushOption("-p");
                pushOption("only-stack-locals:false");
                pushOption("gb.a2");
                pushOption("-p");
                pushOption("only-stack-locals:false");
                pushOption("gb.a1");
                pushOption("-p");
            }
            else if (false
                || option.equals("W")
                || option.equals("whole-optimize")
            ) {
                pushOption("-O");
                pushOption("-w");
                pushOption("enabled:true");
                pushOption("wsop");
                pushOption("-p");
                pushOption("enabled:true");
                pushOption("wjop");
                pushOption("-p");
            }
            else if (false
                    || option.equals("via-grimp")
            )
                via_grimp = true;
            else if (false
                    || option.equals("via-shimple")
            )
                via_shimple = true;
            else if (false
                    || option.equals("throw-analysis")
            ) {
                if (!hasMoreOptions()) {
                    G.v().out.println("No value given for option -" + option);
                    return false;
                }

                String value = nextOption();
        
                if (false);
                else if (false
                        || value.equals("pedantic")
                ) {
                    if (throw_analysis != 0 && throw_analysis != throw_analysis_pedantic) {
                        G.v().out.println("Multiple values given for option " + option);
                        return false;
                    }
                    throw_analysis = throw_analysis_pedantic;
                }
                else if (false
                        || value.equals("unit")
                ) {
                    if (throw_analysis != 0 && throw_analysis != throw_analysis_unit) {
                        G.v().out.println("Multiple values given for option " + option);
                        return false;
                    }
                    throw_analysis = throw_analysis_unit;
                }
                else if (false
                        || value.equals("dalvik")
                ) {
                    if (throw_analysis != 0 && throw_analysis != throw_analysis_dalvik) {
                        G.v().out.println("Multiple values given for option " + option);
                        return false;
                    }
                    throw_analysis = throw_analysis_dalvik;
                }
                else if (false
                        || value.equals("auto-select")
                ) {
                    if (throw_analysis != 0 && throw_analysis != throw_analysis_auto_select) {
                        G.v().out.println("Multiple values given for option " + option);
                        return false;
                    }
                    throw_analysis = throw_analysis_auto_select;
                }
                else {
                    G.v().out.println(String.format("Invalid value %s given for option -%s", value, option));
                    return false;
                }
            }
            else if (false
                    || option.equals("check-init-ta")
                    || option.equals("check-init-throw-analysis")
            ) {
                if (!hasMoreOptions()) {
                    G.v().out.println("No value given for option -" + option);
                    return false;
                }

                String value = nextOption();
        
                if (false);
                else if (false
                        || value.equals("auto")
                ) {
                    if (check_init_throw_analysis != 0 && check_init_throw_analysis != check_init_throw_analysis_auto) {
                        G.v().out.println("Multiple values given for option " + option);
                        return false;
                    }
                    check_init_throw_analysis = check_init_throw_analysis_auto;
                }
                else if (false
                        || value.equals("pedantic")
                ) {
                    if (check_init_throw_analysis != 0 && check_init_throw_analysis != check_init_throw_analysis_pedantic) {
                        G.v().out.println("Multiple values given for option " + option);
                        return false;
                    }
                    check_init_throw_analysis = check_init_throw_analysis_pedantic;
                }
                else if (false
                        || value.equals("unit")
                ) {
                    if (check_init_throw_analysis != 0 && check_init_throw_analysis != check_init_throw_analysis_unit) {
                        G.v().out.println("Multiple values given for option " + option);
                        return false;
                    }
                    check_init_throw_analysis = check_init_throw_analysis_unit;
                }
                else if (false
                        || value.equals("dalvik")
                ) {
                    if (check_init_throw_analysis != 0 && check_init_throw_analysis != check_init_throw_analysis_dalvik) {
                        G.v().out.println("Multiple values given for option " + option);
                        return false;
                    }
                    check_init_throw_analysis = check_init_throw_analysis_dalvik;
                }
                else {
                    G.v().out.println(String.format("Invalid value %s given for option -%s", value, option));
                    return false;
                }
            }
            else if (false
                    || option.equals("omit-excepting-unit-edges")
            )
                omit_excepting_unit_edges = true;
            else if (false
                || option.equals("trim-cfgs")
            ) {
                pushOption("enabled:true");
                pushOption("jb.tt");
                pushOption("-p");
                pushOption("-omit-excepting-unit-edges");
                pushOption("unit");
                pushOption("-throw-analysis");
            }
            else if (false
                    || option.equals("ire")
                    || option.equals("ignore-resolution-errors")
            )
                ignore_resolution_errors = true;
            else if (false
                    || option.equals("i")
                    || option.equals("include")
            ) {
                if (!hasMoreOptions()) {
                    G.v().out.println("No value given for option -" + option);
                    return false;
                }

                String value = nextOption();
                if (include == null)
                    include = new LinkedList<>();
                include.add(value);
            }
            else if (false
                    || option.equals("x")
                    || option.equals("exclude")
            ) {
                if (!hasMoreOptions()) {
                    G.v().out.println("No value given for option -" + option);
                    return false;
                }

                String value = nextOption();
                if (exclude == null)
                    exclude = new LinkedList<>();
                exclude.add(value);
            }
            else if (false
                    || option.equals("include-all")
            )
                include_all = true;
            else if (false
                    || option.equals("dynamic-class")
            ) {
                if (!hasMoreOptions()) {
                    G.v().out.println("No value given for option -" + option);
                    return false;
                }

                String value = nextOption();
                if (dynamic_class == null)
                    dynamic_class = new LinkedList<>();
                dynamic_class.add(value);
            }
            else if (false
                    || option.equals("dynamic-dir")
            ) {
                if (!hasMoreOptions()) {
                    G.v().out.println("No value given for option -" + option);
                    return false;
                }

                String value = nextOption();
                if (dynamic_dir == null)
                    dynamic_dir = new LinkedList<>();
                dynamic_dir.add(value);
            }
            else if (false
                    || option.equals("dynamic-package")
            ) {
                if (!hasMoreOptions()) {
                    G.v().out.println("No value given for option -" + option);
                    return false;
                }

                String value = nextOption();
                if (dynamic_package == null)
                    dynamic_package = new LinkedList<>();
                dynamic_package.add(value);
            }
            else if (false
                    || option.equals("keep-line-number")
            )
                keep_line_number = true;
            else if (false
                    || option.equals("keep-bytecode-offset")
                    || option.equals("keep-offset")
            )
                keep_offset = true;
            else if (false
                    || option.equals("write-local-annotations")
            )
                write_local_annotations = true;
            else if (false
                || option.equals("annot-purity")
            ) {
                pushOption("enabled:true");
                pushOption("wjap.purity");
                pushOption("-p");
                pushOption("enabled:true");
                pushOption("cg.spark");
                pushOption("-p");
                pushOption("-w");
            }
            else if (false
                || option.equals("annot-nullpointer")
            ) {
                pushOption("enabled:true");
                pushOption("tag.an");
                pushOption("-p");
                pushOption("enabled:true");
                pushOption("jap.npc");
                pushOption("-p");
            }
            else if (false
                || option.equals("annot-arraybounds")
            ) {
                pushOption("enabled:true");
                pushOption("tag.an");
                pushOption("-p");
                pushOption("enabled:true");
                pushOption("jap.abc");
                pushOption("-p");
                pushOption("enabled:true");
                pushOption("wjap.ra");
                pushOption("-p");
            }
            else if (false
                || option.equals("annot-side-effect")
            ) {
                pushOption("enabled:true");
                pushOption("tag.dep");
                pushOption("-p");
                pushOption("enabled:true");
                pushOption("jap.sea");
                pushOption("-p");
                pushOption("-w");
            }
            else if (false
                || option.equals("annot-fieldrw")
            ) {
                pushOption("enabled:true");
                pushOption("tag.fieldrw");
                pushOption("-p");
                pushOption("enabled:true");
                pushOption("jap.fieldrw");
                pushOption("-p");
                pushOption("-w");
            }
            else if (false
                    || option.equals("time")
            )
                time = true;
            else if (false
                    || option.equals("subtract-gc")
            )
                subtract_gc = true;
            else if (false
                    || option.equals("no-writeout-body-releasing")
            )
                no_writeout_body_releasing = true;
            else {
                G.v().out.println("Invalid option -" + option);
                return false;
            }
        }

        Iterator<String> it = phaseOptions.iterator();
        while(it.hasNext()) {
            String phaseName = it.next();
            String phaseOption = it.next();
            if(!setPhaseOption(phaseName, "enabled:true"))
                return false;
        }

        it = phaseOptions.iterator();
        while(it.hasNext()) {
            String phaseName = it.next();
            String phaseOption = it.next();
            if(!setPhaseOption(phaseName, phaseOption))
                return false;
        }

        return true;
    }

    public boolean coffi() { return coffi; }
    private boolean coffi = false;
    public void set_coffi(boolean setting) { coffi = setting; }

    public boolean jasmin_backend() { return jasmin_backend; }
    private boolean jasmin_backend = false;
    public void set_jasmin_backend(boolean setting) { jasmin_backend = setting; }

    public boolean help() { return help; }
    private boolean help = false;
    public void set_help(boolean setting) { help = setting; }

    public boolean phase_list() { return phase_list; }
    private boolean phase_list = false;
    public void set_phase_list(boolean setting) { phase_list = setting; }

    public List<String> phase_help() {
        return phase_help == null ? Collections.emptyList() : phase_help;
    }
    public void set_phase_help(List<String> setting) { phase_help = setting; }
    private List<String> phase_help = null;

    public boolean version() { return version; }
    private boolean version = false;
    public void set_version(boolean setting) { version = setting; }

    public boolean verbose() { return verbose; }
    private boolean verbose = false;
    public void set_verbose(boolean setting) { verbose = setting; }

    public boolean interactive_mode() { return interactive_mode; }
    private boolean interactive_mode = false;
    public void set_interactive_mode(boolean setting) { interactive_mode = setting; }

    public boolean unfriendly_mode() { return unfriendly_mode; }
    private boolean unfriendly_mode = false;
    public void set_unfriendly_mode(boolean setting) { unfriendly_mode = setting; }

    public boolean app() { return app; }
    private boolean app = false;
    public void set_app(boolean setting) { app = setting; }

    public boolean whole_program() { return whole_program; }
    private boolean whole_program = false;
    public void set_whole_program(boolean setting) { whole_program = setting; }

    public boolean whole_shimple() { return whole_shimple; }
    private boolean whole_shimple = false;
    public void set_whole_shimple(boolean setting) { whole_shimple = setting; }

    public boolean on_the_fly() { return on_the_fly; }
    private boolean on_the_fly = false;
    public void set_on_the_fly(boolean setting) { on_the_fly = setting; }

    public boolean validate() { return validate; }
    private boolean validate = false;
    public void set_validate(boolean setting) { validate = setting; }

    public boolean debug() { return debug; }
    private boolean debug = false;
    public void set_debug(boolean setting) { debug = setting; }

    public boolean debug_resolver() { return debug_resolver; }
    private boolean debug_resolver = false;
    public void set_debug_resolver(boolean setting) { debug_resolver = setting; }

    public boolean ignore_resolving_levels() { return ignore_resolving_levels; }
    private boolean ignore_resolving_levels = false;
    public void set_ignore_resolving_levels(boolean setting) { ignore_resolving_levels = setting; }

    public boolean weak_map_structures() { return weak_map_structures; }
    private boolean weak_map_structures = false;
    public void set_weak_map_structures(boolean setting) { weak_map_structures = setting; }

    public String soot_classpath() { return soot_classpath; }
    public void set_soot_classpath(String setting) { soot_classpath = setting; }
    private String soot_classpath = "";

    public String soot_modulepath() { return soot_modulepath; }
    public void set_soot_modulepath(String setting) { soot_modulepath = setting; }
    private String soot_modulepath = "";

    public boolean prepend_classpath() { return prepend_classpath; }
    private boolean prepend_classpath = false;
    public void set_prepend_classpath(boolean setting) { prepend_classpath = setting; }

    public boolean ignore_classpath_errors() { return ignore_classpath_errors; }
    private boolean ignore_classpath_errors = false;
    public void set_ignore_classpath_errors(boolean setting) { ignore_classpath_errors = setting; }

    public boolean process_multiple_dex() { return process_multiple_dex; }
    private boolean process_multiple_dex = false;
    public void set_process_multiple_dex(boolean setting) { process_multiple_dex = setting; }

    public boolean search_dex_in_archives() { return search_dex_in_archives; }
    private boolean search_dex_in_archives = false;
    public void set_search_dex_in_archives(boolean setting) { search_dex_in_archives = setting; }

    public List<String> process_dir() {
        return process_dir == null ? Collections.emptyList() : process_dir;
    }
    public void set_process_dir(List<String> setting) { process_dir = setting; }
    private List<String> process_dir = null;

    public List<String> process_jar_dir() {
        return process_jar_dir == null ? Collections.emptyList() : process_jar_dir;
    }
    public void set_process_jar_dir(List<String> setting) { process_jar_dir = setting; }
    private List<String> process_jar_dir = null;

    public boolean derive_java_version() { return derive_java_version; }
    private boolean derive_java_version = true;
    public void set_derive_java_version(boolean setting) { derive_java_version = setting; }

    public boolean oaat() { return oaat; }
    private boolean oaat = false;
    public void set_oaat(boolean setting) { oaat = setting; }

    public String android_jars() { return android_jars; }
    public void set_android_jars(String setting) { android_jars = setting; }
    private String android_jars = "";

    public String force_android_jar() { return force_android_jar; }
    public void set_force_android_jar(String setting) { force_android_jar = setting; }
    private String force_android_jar = "";

    public int android_api_version() { return android_api_version; }
    public void set_android_api_version(int setting) { android_api_version = setting; }
    private int android_api_version = -1;

    public boolean ast_metrics() { return ast_metrics; }
    private boolean ast_metrics = false;
    public void set_ast_metrics(boolean setting) { ast_metrics = setting; }

    public int src_prec() {
        if (src_prec == 0) return src_prec_class;
        return src_prec; 
    }
    public void set_src_prec(int setting) { src_prec = setting; }
    private int src_prec = 0;

    public boolean full_resolver() { return full_resolver; }
    private boolean full_resolver = false;
    public void set_full_resolver(boolean setting) { full_resolver = setting; }

    public boolean allow_phantom_refs() { return allow_phantom_refs; }
    private boolean allow_phantom_refs = false;
    public void set_allow_phantom_refs(boolean setting) { allow_phantom_refs = setting; }

    public boolean allow_phantom_elms() { return allow_phantom_elms; }
    private boolean allow_phantom_elms = false;
    public void set_allow_phantom_elms(boolean setting) { allow_phantom_elms = setting; }

    public boolean allow_cg_errors() { return allow_cg_errors; }
    private boolean allow_cg_errors = false;
    public void set_allow_cg_errors(boolean setting) { allow_cg_errors = setting; }

    public boolean no_bodies_for_excluded() { return no_bodies_for_excluded; }
    private boolean no_bodies_for_excluded = false;
    public void set_no_bodies_for_excluded(boolean setting) { no_bodies_for_excluded = setting; }

    public boolean j2me() { return j2me; }
    private boolean j2me = false;
    public void set_j2me(boolean setting) { j2me = setting; }

    public String main_class() { return main_class; }
    public void set_main_class(String setting) { main_class = setting; }
    private String main_class = "";

    public boolean polyglot() { return polyglot; }
    private boolean polyglot = false;
    public void set_polyglot(boolean setting) { polyglot = setting; }

    public boolean permissive_resolving() { return permissive_resolving; }
    private boolean permissive_resolving = false;
    public void set_permissive_resolving(boolean setting) { permissive_resolving = setting; }

    public boolean drop_bodies_after_load() { return drop_bodies_after_load; }
    private boolean drop_bodies_after_load = true;
    public void set_drop_bodies_after_load(boolean setting) { drop_bodies_after_load = setting; }

    public String output_dir() { return output_dir; }
    public void set_output_dir(String setting) { output_dir = setting; }
    private String output_dir = "";

    public int output_format() {
        if (output_format == 0) return output_format_class;
        return output_format; 
    }
    public void set_output_format(int setting) { output_format = setting; }
    private int output_format = 0;

    public int java_version() {
        return java_version; 
    }
    public void set_java_version(int setting) { java_version = setting; }
    private int java_version = 0;

    public boolean output_jar() { return output_jar; }
    private boolean output_jar = false;
    public void set_output_jar(boolean setting) { output_jar = setting; }

    public boolean hierarchy_dirs() { return hierarchy_dirs; }
    private boolean hierarchy_dirs = false;
    public void set_hierarchy_dirs(boolean setting) { hierarchy_dirs = setting; }

    public boolean xml_attributes() { return xml_attributes; }
    private boolean xml_attributes = false;
    public void set_xml_attributes(boolean setting) { xml_attributes = setting; }

    public boolean print_tags_in_output() { return print_tags_in_output; }
    private boolean print_tags_in_output = false;
    public void set_print_tags_in_output(boolean setting) { print_tags_in_output = setting; }

    public boolean no_output_source_file_attribute() { return no_output_source_file_attribute; }
    private boolean no_output_source_file_attribute = false;
    public void set_no_output_source_file_attribute(boolean setting) { no_output_source_file_attribute = setting; }

    public boolean no_output_inner_classes_attribute() { return no_output_inner_classes_attribute; }
    private boolean no_output_inner_classes_attribute = false;
    public void set_no_output_inner_classes_attribute(boolean setting) { no_output_inner_classes_attribute = setting; }

    public List<String> dump_body() {
        return dump_body == null ? Collections.emptyList() : dump_body;
    }
    public void set_dump_body(List<String> setting) { dump_body = setting; }
    private List<String> dump_body = null;

    public List<String> dump_cfg() {
        return dump_cfg == null ? Collections.emptyList() : dump_cfg;
    }
    public void set_dump_cfg(List<String> setting) { dump_cfg = setting; }
    private List<String> dump_cfg = null;

    public boolean show_exception_dests() { return show_exception_dests; }
    private boolean show_exception_dests = true;
    public void set_show_exception_dests(boolean setting) { show_exception_dests = setting; }

    public boolean gzip() { return gzip; }
    private boolean gzip = false;
    public void set_gzip(boolean setting) { gzip = setting; }

    public boolean force_overwrite() { return force_overwrite; }
    private boolean force_overwrite = false;
    public void set_force_overwrite(boolean setting) { force_overwrite = setting; }

    public List<String> plugin() {
        return plugin == null ? Collections.emptyList() : plugin;
    }
    public void set_plugin(List<String> setting) { plugin = setting; }
    private List<String> plugin = null;

    public int wrong_staticness() {
        if (wrong_staticness == 0) return wrong_staticness_fixstrict;
        return wrong_staticness; 
    }
    public void set_wrong_staticness(int setting) { wrong_staticness = setting; }
    private int wrong_staticness = 0;

    public int field_type_mismatches() {
        if (field_type_mismatches == 0) return field_type_mismatches_null;
        return field_type_mismatches; 
    }
    public void set_field_type_mismatches(int setting) { field_type_mismatches = setting; }
    private int field_type_mismatches = 0;

    public int num_threads() { return num_threads; }
    public void set_num_threads(int setting) { num_threads = setting; }
    private int num_threads = -1;

    public boolean via_grimp() { return via_grimp; }
    private boolean via_grimp = false;
    public void set_via_grimp(boolean setting) { via_grimp = setting; }

    public boolean via_shimple() { return via_shimple; }
    private boolean via_shimple = false;
    public void set_via_shimple(boolean setting) { via_shimple = setting; }

    public int throw_analysis() {
        if (throw_analysis == 0) return throw_analysis_auto_select;
        return throw_analysis; 
    }
    public void set_throw_analysis(int setting) { throw_analysis = setting; }
    private int throw_analysis = 0;

    public int check_init_throw_analysis() {
        if (check_init_throw_analysis == 0) return check_init_throw_analysis_auto;
        return check_init_throw_analysis; 
    }
    public void set_check_init_throw_analysis(int setting) { check_init_throw_analysis = setting; }
    private int check_init_throw_analysis = 0;

    public boolean omit_excepting_unit_edges() { return omit_excepting_unit_edges; }
    private boolean omit_excepting_unit_edges = false;
    public void set_omit_excepting_unit_edges(boolean setting) { omit_excepting_unit_edges = setting; }

    public boolean ignore_resolution_errors() { return ignore_resolution_errors; }
    private boolean ignore_resolution_errors = false;
    public void set_ignore_resolution_errors(boolean setting) { ignore_resolution_errors = setting; }

    public List<String> include() {
        return include == null ? Collections.emptyList() : include;
    }
    public void set_include(List<String> setting) { include = setting; }
    private List<String> include = null;

    public List<String> exclude() {
        return exclude == null ? Collections.emptyList() : exclude;
    }
    public void set_exclude(List<String> setting) { exclude = setting; }
    private List<String> exclude = null;

    public boolean include_all() { return include_all; }
    private boolean include_all = false;
    public void set_include_all(boolean setting) { include_all = setting; }

    public List<String> dynamic_class() {
        return dynamic_class == null ? Collections.emptyList() : dynamic_class;
    }
    public void set_dynamic_class(List<String> setting) { dynamic_class = setting; }
    private List<String> dynamic_class = null;

    public List<String> dynamic_dir() {
        return dynamic_dir == null ? Collections.emptyList() : dynamic_dir;
    }
    public void set_dynamic_dir(List<String> setting) { dynamic_dir = setting; }
    private List<String> dynamic_dir = null;

    public List<String> dynamic_package() {
        return dynamic_package == null ? Collections.emptyList() : dynamic_package;
    }
    public void set_dynamic_package(List<String> setting) { dynamic_package = setting; }
    private List<String> dynamic_package = null;

    public boolean keep_line_number() { return keep_line_number; }
    private boolean keep_line_number = false;
    public void set_keep_line_number(boolean setting) { keep_line_number = setting; }

    public boolean keep_offset() { return keep_offset; }
    private boolean keep_offset = false;
    public void set_keep_offset(boolean setting) { keep_offset = setting; }

    public boolean write_local_annotations() { return write_local_annotations; }
    private boolean write_local_annotations = false;
    public void set_write_local_annotations(boolean setting) { write_local_annotations = setting; }

    public boolean time() { return time; }
    private boolean time = false;
    public void set_time(boolean setting) { time = setting; }

    public boolean subtract_gc() { return subtract_gc; }
    private boolean subtract_gc = false;
    public void set_subtract_gc(boolean setting) { subtract_gc = setting; }

    public boolean no_writeout_body_releasing() { return no_writeout_body_releasing; }
    private boolean no_writeout_body_releasing = false;
    public void set_no_writeout_body_releasing(boolean setting) { no_writeout_body_releasing = setting; }

    public String getUsage() {
        return ""
                + "\nGeneral Options:\n"
                + padOpt("-coffi", "Use the good old Coffi front end for parsing Java bytecode (instead of using ASM).")
                + padOpt("-jasmin-backend", "Use the Jasmin back end for generating Java bytecode (instead of using ASM).")
                + padOpt("-h, -help", "Display help and exit")
                + padOpt("-pl, -phase-list", "Print list of available phases")
                + padOpt("-ph ARG -phase-help ARG", "Print help for specified ARG")
                + padOpt("-version", "Display version information and exit")
                + padOpt("-v, -verbose", "Verbose mode")
                + padOpt("-interactive-mode", "Run in interactive mode")
                + padOpt("-unfriendly-mode", "Allow Soot to run with no command-line options")
                + padOpt("-app", "Run in application mode")
                + padOpt("-w, -whole-program", "Run in whole-program mode")
                + padOpt("-ws, -whole-shimple", "Run in whole-shimple mode")
                + padOpt("-fly, -on-the-fly", "Run in on-the-fly mode")
                + padOpt("-validate", "Run internal validation on bodies")
                + padOpt("-debug", "Print various Soot debugging info")
                + padOpt("-debug-resolver", "Print debugging info from SootResolver")
                + padOpt("-ignore-resolving-levels", "Ignore mismatching resolving levels")
                + padOpt("-weak-map-structures", "Use weak references in Scene to prevent memory leakage when removing many classes/methods/locals")
                + "\nInput Options:\n"
                + padOpt("-cp ARG -soot-class-path ARG -soot-classpath ARG", "Use ARG as the classpath for finding classes.")
                + padOpt("-soot-modulepath ARG", "Use ARG as the modulepath for finding classes.")
                + padOpt("-pp, -prepend-classpath", "Prepend the given soot classpath to the default classpath.")
                + padOpt("-ice, -ignore-classpath-errors", "Ignores invalid entries on the Soot classpath.")
                + padOpt("-process-multiple-dex", "Process all DEX files found in APK.")
                + padOpt("-search-dex-in-archives", "Also includes Jar and Zip files when searching for DEX files under the provided classpath.")
                + padOpt("-process-path ARG -process-dir ARG", "Process all classes found in ARG (but not classes within JAR files in ARG , use process-jar-dir for that)")
                + padOpt("-process-jar-dir ARG", "Process all classes found in JAR files found in ARG")
                + padOpt("-derive-java-version", "Java version for output and internal processing will be derived from the given input classes")
                + padOpt("-oaat", "From the process-dir, processes one class at a time.")
                + padOpt("-android-jars ARG", "Use ARG as the path for finding the android.jar file")
                + padOpt("-force-android-jar ARG", "Force Soot to use ARG as the path for the android.jar file.")
                + padOpt("-ast-metrics", "Compute AST Metrics if performing java to jimple")
                + padOpt("-src-prec ARG", "Sets source precedence to ARG files")
                    + padVal("c class (default)", "Favour class files as Soot source")
                    + padVal("only-class", "Use only class files as Soot source")
                    + padVal("J jimple", "Favour Jimple files as Soot source")
                    + padVal("java", "Favour Java files as Soot source")
                    + padVal("apk", "Favour APK files as Soot source")
                    + padVal("apk-class-jimple apk-c-j", "Favour APK files as Soot source, disregard Java files")
                + padOpt("-full-resolver", "Force transitive resolving of referenced classes")
                + padOpt("-allow-phantom-refs", "Allow unresolved classes; may cause errors")
                + padOpt("-allow-phantom-elms", "Allow phantom methods and fields in non-phantom classes")
                + padOpt("-allow-cg-errors", "Allow Errors during callgraph construction")
                + padOpt("-no-bodies-for-excluded", "Do not load bodies for excluded classes")
                + padOpt("-j2me", "Use J2ME mode; changes assignment of types")
                + padOpt("-main-class ARG", "Sets the main class for whole-program analysis.")
                + padOpt("-polyglot", "Use Java 1.4 Polyglot frontend instead of JastAdd")
                + padOpt("-permissive-resolving", "Use alternative sources when classes cannot be found using the normal resolving strategy")
                + padOpt("-drop-bodies-after-load", "Drop the method source after it has served its purpose of loading the method body")
                + "\nOutput Options:\n"
                + padOpt("-d ARG -output-dir ARG", "Store output files in ARG")
                + padOpt("-f ARG -output-format ARG", "Set output format for Soot")
                    + padVal("J jimple", "Produce .jimple Files")
                    + padVal("j jimp", "Produce .jimp (abbreviated Jimple) files")
                    + padVal("S shimple", "Produce .shimple files")
                    + padVal("s shimp", "Produce .shimp (abbreviated Shimple) files")
                    + padVal("B baf", "Produce .baf files")
                    + padVal("b", "Produce .b (abbreviated Baf) files")
                    + padVal("G grimple", "Produce .grimple files")
                    + padVal("g grimp", "Produce .grimp (abbreviated Grimp) files")
                    + padVal("X xml", "Produce .xml Files")
                    + padVal("dex", "Produce Dalvik Virtual Machine files")
                    + padVal("force-dex", "Produce Dalvik DEX files")
                    + padVal("n none", "Produce no output")
                    + padVal("jasmin", "Produce .jasmin files")
                    + padVal("c class (default)", "Produce .class Files")
                    + padVal("d dava", "Produce dava-decompiled .java files")
                    + padVal("t template", "Produce .java files with Jimple templates.")
                    + padVal("a asm", "Produce .asm files as textual bytecode representation generated with the ASM back end.")
                + padOpt("-java-version ARG", "Force Java version of bytecode generated by Soot.")
                    + padVal("default", "Let Soot determine Java version of generated bytecode.")
                    + padVal("1.1 1", "Force Java 1.1 as output version.")
                    + padVal("1.2 2", "Force Java 1.2 as output version.")
                    + padVal("1.3 3", "Force Java 1.3 as output version.")
                    + padVal("1.4 4", "Force Java 1.4 as output version.")
                    + padVal("1.5 5", "Force Java 1.5 as output version.")
                    + padVal("1.6 6", "Force Java 1.6 as output version.")
                    + padVal("1.7 7", "Force Java 1.7 as output version.")
                    + padVal("1.8 8", "Force Java 1.8 as output version.")
                    + padVal("1.9 9", "Force Java 1.9 as output version (Experimental).")
                    + padVal("1.10 10", "Force Java 1.10 as output version (Experimental).")
                    + padVal("1.11 11", "Force Java 1.11 as output version (Experimental).")
                    + padVal("1.12 12", "Force Java 1.12 as output version (Experimental).")
                + padOpt("-outjar, -output-jar", "Make output dir a Jar file instead of dir")
                + padOpt("-hierarchy-dirs", "Generate class hierarchy directories for Jimple/Shimple")
                + padOpt("-xml-attributes", "Save tags to XML attributes for Eclipse")
                + padOpt("-print-tags, -print-tags-in-output", "Print tags in output files after stmt")
                + padOpt("-no-output-source-file-attribute", "Don't output Source File Attribute when producing class files")
                + padOpt("-no-output-inner-classes-attribute", "Don't output inner classes attribute in class files")
                + padOpt("-dump-body ARG", "Dump the internal representation of each method before and after phase ARG")
                + padOpt("-dump-cfg ARG", "Dump the internal representation of each CFG constructed during phase ARG")
                + padOpt("-show-exception-dests", "Include exception destination edges as well as CFG edges in dumped CFGs")
                + padOpt("-gzip", "GZip IR output files")
                + padOpt("-force-overwrite", "Force Overwrite Output Files")
                + "\nProcessing Options:\n"
                + padOpt("-plugin ARG", "Load all plugins found in ARG")
                + padOpt("-wrong-staticness ARG", "Ignores or fixes errors due to wrong staticness")
                    + padVal("fail", "Raise an error when wrong staticness is detected")
                    + padVal("ignore", "Ignore errors caused by wrong staticness")
                    + padVal("fix", "Transparently fix staticness errors")
                    + padVal("fixstrict (default)", "Transparently fix staticness errors, but do not ignore remaining errors")
                + padOpt("-field-type-mismatches ARG", "Specifies how errors shall be handled when resolving field references with mismatching types")
                    + padVal("fail", "Raise an error when a field type mismatch is detected")
                    + padVal("ignore", "Ignore field type mismatches")
                    + padVal("null (default)", "Return null in case of type mismatch")
                + padOpt("-p ARG -phase-option ARG", "Set PHASE 's OPT option to VALUE")
                + padOpt("-O, -optimize", "Perform intraprocedural optimizations")
                + padOpt("-W, -whole-optimize", "Perform whole program optimizations")
                + padOpt("-via-grimp", "Convert to bytecode via Grimp instead of via Baf")
                + padOpt("-via-shimple", "Enable Shimple SSA representation")
                + padOpt("-throw-analysis ARG", "")
                    + padVal("pedantic", "Pedantically conservative throw analysis")
                    + padVal("unit", "Unit Throw Analysis")
                    + padVal("dalvik", "Dalvik Throw Analysis")
                    + padVal("auto-select (default)", "Automatically Select Throw Analysis")
                + padOpt("-check-init-ta ARG -check-init-throw-analysis ARG", "")
                    + padVal("auto (default)", "Automatically select a throw analysis")
                    + padVal("pedantic", "Pedantically conservative throw analysis")
                    + padVal("unit", "Unit Throw Analysis")
                    + padVal("dalvik", "Dalvik Throw Analysis")
                + padOpt("-omit-excepting-unit-edges", "Omit CFG edges to handlers from excepting units which lack side effects")
                + padOpt("-trim-cfgs", "Trim unrealizable exceptional edges from CFGs")
                + padOpt("-ire, -ignore-resolution-errors", "Does not throw an exception when a program references an undeclared field or method.")
                + "\nApplication Mode Options:\n"
                + padOpt("-i ARG -include ARG", "Include classes in ARG as application classes")
                + padOpt("-x ARG -exclude ARG", "Exclude classes in ARG from application classes")
                + padOpt("-include-all", "Set default excluded packages to empty list")
                + padOpt("-dynamic-class ARG", "Note that ARG may be loaded dynamically")
                + padOpt("-dynamic-dir ARG", "Mark all classes in ARG as potentially dynamic")
                + padOpt("-dynamic-package ARG", "Marks classes in ARG as potentially dynamic")
                + "\nInput Attribute Options:\n"
                + padOpt("-keep-line-number", "Keep line number tables")
                + padOpt("-keep-bytecode-offset, -keep-offset", "Attach bytecode offset to IR")
                + "\nOutput Attribute Options:\n"
                + padOpt("-write-local-annotations", "Write out debug annotations on local names")
                + "\nAnnotation Options:\n"
                + padOpt("-annot-purity", "Emit purity attributes")
                + padOpt("-annot-nullpointer", "Emit null pointer attributes")
                + padOpt("-annot-arraybounds", "Emit array bounds check attributes")
                + padOpt("-annot-side-effect", "Emit side-effect attributes")
                + padOpt("-annot-fieldrw", "Emit field read/write attributes")
                + "\nMiscellaneous Options:\n"
                + padOpt("-time", "Report time required for transformations")
                + padOpt("-subtract-gc", "Subtract gc from time")
                + padOpt("-no-writeout-body-releasing", "Disables the release of method bodies after writeout. This flag is used internally.");
    }


    public String getPhaseList() {
        return ""
                + padOpt("jb", "Creates a JimpleBody for each method")
                    + padVal("jb.dtr", "Reduces chains of catch-all traps")
                    + padVal("jb.ese", "Removes empty switch statements")
                    + padVal("jb.ls", "Local splitter: one local per DU-UD web")
                    + padVal("jb.sils", "Splits primitive locals used as different types")
                    + padVal("jb.a", "Aggregator: removes some unnecessary copies")
                    + padVal("jb.ule", "Unused local eliminator")
                    + padVal("jb.tr", "Assigns types to locals")
                    + padVal("jb.ulp", "Local packer: minimizes number of locals")
                    + padVal("jb.lns", "Local name standardizer")
                    + padVal("jb.cp", "Copy propagator")
                    + padVal("jb.dae", "Dead assignment eliminator")
                    + padVal("jb.cp-ule", "Post-copy propagation unused local eliminator")
                    + padVal("jb.lp", "Local packer: minimizes number of locals")
                    + padVal("jb.ne", "Nop eliminator")
                    + padVal("jb.uce", "Unreachable code eliminator")
                    + padVal("jb.tt", "Trap Tightener")
                    + padVal("jb.cbf", "Conditional branch folder")
                + padOpt("jj", "Creates a JimpleBody for each method directly from source")
                    + padVal("jj.ls", "Local splitter: one local per DU-UD web")
                    + padVal("jj.sils", "Splits primitive locals used as different types")
                    + padVal("jj.a", "Aggregator: removes some unnecessary copies")
                    + padVal("jj.ule", "Unused local eliminator")
                    + padVal("jj.tr", "Assigns types to locals")
                    + padVal("jj.ulp", "Local packer: minimizes number of locals")
                    + padVal("jj.lns", "Local name standardizer")
                    + padVal("jj.cp", "Copy propagator")
                    + padVal("jj.dae", "Dead assignment eliminator")
                    + padVal("jj.cp-ule", "Post-copy propagation unused local eliminator")
                    + padVal("jj.lp", "Local packer: minimizes number of locals")
                    + padVal("jj.ne", "Nop eliminator")
                    + padVal("jj.uce", "Unreachable code eliminator")
                + padOpt("wjpp", "Whole Jimple Pre-processing Pack")
                    + padVal("wjpp.cimbt", "Replaces base objects of calls to Method.invoke() that are string constants by locals")
                + padOpt("wspp", "Whole Shimple Pre-processing Pack")
                + padOpt("cg", "Call graph constructor")
                    + padVal("cg.cha", "Builds call graph using Class Hierarchy Analysis")
                    + padVal("cg.spark", "Spark points-to analysis framework")
                    + padVal("cg.paddle", "Paddle points-to analysis framework")
                + padOpt("wstp", "Whole-shimple transformation pack")
                + padOpt("wsop", "Whole-shimple optimization pack")
                + padOpt("wjtp", "Whole-jimple transformation pack")
                    + padVal("wjtp.mhp", "Determines what statements may be run concurrently")
                    + padVal("wjtp.tn", "Finds critical sections, allocates locks")
                    + padVal("wjtp.rdc", "Rename duplicated classes when the file system is not case sensitive")
                + padOpt("wjop", "Whole-jimple optimization pack")
                    + padVal("wjop.smb", "Static method binder: Devirtualizes monomorphic calls")
                    + padVal("wjop.si", "Static inliner: inlines monomorphic calls")
                + padOpt("wjap", "Whole-jimple annotation pack: adds interprocedural tags")
                    + padVal("wjap.ra", "Rectangular array finder")
                    + padVal("wjap.umt", "Tags all unreachable methods")
                    + padVal("wjap.uft", "Tags all unreachable fields")
                    + padVal("wjap.tqt", "Tags all qualifiers that could be tighter")
                    + padVal("wjap.cgg", "Creates graphical call graph.")
                    + padVal("wjap.purity", "Emit purity attributes")
                + padOpt("shimple", "Sets parameters for Shimple SSA form")
                + padOpt("stp", "Shimple transformation pack")
                + padOpt("sop", "Shimple optimization pack")
                    + padVal("sop.cpf", "Shimple constant propagator and folder")
                + padOpt("jtp", "Jimple transformation pack: intraprocedural analyses added to Soot")
                + padOpt("jop", "Jimple optimization pack (intraprocedural)")
                    + padVal("jop.cse", "Common subexpression eliminator")
                    + padVal("jop.bcm", "Busy code motion: unaggressive partial redundancy elimination")
                    + padVal("jop.lcm", "Lazy code motion: aggressive partial redundancy elimination")
                    + padVal("jop.cp", "Copy propagator")
                    + padVal("jop.cpf", "Constant propagator and folder")
                    + padVal("jop.cbf", "Conditional branch folder")
                    + padVal("jop.dae", "Dead assignment eliminator")
                    + padVal("jop.nce", "Null Check Eliminator")
                    + padVal("jop.uce1", "Unreachable code eliminator, pass 1")
                    + padVal("jop.ubf1", "Unconditional branch folder, pass 1")
                    + padVal("jop.uce2", "Unreachable code eliminator, pass 2")
                    + padVal("jop.ubf2", "Unconditional branch folder, pass 2")
                    + padVal("jop.ule", "Unused local eliminator")
                + padOpt("jap", "Jimple annotation pack: adds intraprocedural tags")
                    + padVal("jap.npc", "Null pointer checker")
                    + padVal("jap.npcolorer", "Null pointer colourer: tags references for eclipse")
                    + padVal("jap.abc", "Array bound checker")
                    + padVal("jap.profiling", "Instruments null pointer and array checks")
                    + padVal("jap.sea", "Side effect tagger")
                    + padVal("jap.fieldrw", "Field read/write tagger")
                    + padVal("jap.cgtagger", "Call graph tagger")
                    + padVal("jap.parity", "Parity tagger")
                    + padVal("jap.pat", "Colour-codes method parameters that may be aliased")
                    + padVal("jap.lvtagger", "Creates color tags for live variables")
                    + padVal("jap.rdtagger", "Creates link tags for reaching defs")
                    + padVal("jap.che", "Indicates whether cast checks can be eliminated")
                    + padVal("jap.umt", "Inserts assertions into unreachable methods")
                    + padVal("jap.lit", "Tags loop invariants")
                    + padVal("jap.aet", "Tags statements with sets of available expressions")
                    + padVal("jap.dmt", "Tags dominators of statement")
                + padOpt("gb", "Creates a GrimpBody for each method")
                    + padVal("gb.a1", "Aggregator: removes some copies, pre-folding")
                    + padVal("gb.cf", "Constructor folder")
                    + padVal("gb.a2", "Aggregator: removes some copies, post-folding")
                    + padVal("gb.ule", "Unused local eliminator")
                + padOpt("gop", "Grimp optimization pack")
                + padOpt("bb", "Creates Baf bodies")
                    + padVal("bb.lso", "Load store optimizer")
                    + padVal("bb.sco", "Store chain optimizer")
                    + padVal("bb.pho", "Peephole optimizer")
                    + padVal("bb.ule", "Unused local eliminator")
                    + padVal("bb.lp", "Local packer: minimizes number of locals")
                    + padVal("bb.ne", "Nop eliminator")
                + padOpt("bop", "Baf optimization pack")
                + padOpt("tag", "Tag aggregator: turns tags into attributes")
                    + padVal("tag.ln", "Line number aggregator")
                    + padVal("tag.an", "Array bounds and null pointer check aggregator")
                    + padVal("tag.dep", "Dependence aggregator")
                    + padVal("tag.fieldrw", "Field read/write aggregator")
                + padOpt("db", "Dummy phase to store options for Dava")
                    + padVal("db.transformations", "The Dava back-end with all its transformations")
                    + padVal("db.renamer", "Apply heuristics based naming of local variables")
                    + padVal("db.deobfuscate", "Apply de-obfuscation analyses")
                    + padVal("db.force-recompile", "Try to get recompilable code.");
    }

    public String getPhaseHelp(String phaseName) {
        if (phaseName.equals("jb"))
            return "Phase " + phaseName + ":\n"
                    + "\nJimple Body Creation creates a JimpleBody for each input method, \nusing either asm, to read .class files, or the jimple parser, to \nread .jimple files."
                    + "\n\nRecognized options (with default values):\n"
                    + padOpt("enabled (true)", "")
                    + padOpt("use-original-names (false)", "")
                    + padOpt("preserve-source-annotations (false)", "")
                    + padOpt("stabilize-local-names (false)", "")
                    + padOpt("model-lambdametafactory (true)", "Replace dynamic invoke instructions to the LambdaMetafactory by static invokes to a synthetic LambdaMetafactory implementation.");

        if (phaseName.equals("jb.dtr"))
            return "Phase " + phaseName + ":\n"
                    + "\nThis transformer detects cases in which the same code block is \ncovered by two different catch all traps with different \nexception handlers (A and B), and if there is at the same time a \nthird catch all trap that covers the second handler B and jumps \nto A, then the second trap is unnecessary, because it is already \ncovered by a combination of the other two traps. This \ntransformer removes the unnecessary trap."
                    + "\n\nRecognized options (with default values):\n"
                    + padOpt("enabled (true)", "");

        if (phaseName.equals("jb.ese"))
            return "Phase " + phaseName + ":\n"
                    + "\nThe Empty Switch Eliminator detects and removes switch \nstatements that have no data labels. Instead, the code is \ntransformed to contain a single jump statement to the default \nlabel."
                    + "\n\nRecognized options (with default values):\n"
                    + padOpt("enabled (true)", "");

        if (phaseName.equals("jb.ls"))
            return "Phase " + phaseName + ":\n"
                    + "\nThe Local Splitter identifies DU-UD webs for local variables and \nintroduces new variables so that each disjoint web is associated \nwith a single local."
                    + "\n\nRecognized options (with default values):\n"
                    + padOpt("enabled (true)", "");

        if (phaseName.equals("jb.sils"))
            return "Phase " + phaseName + ":\n"
                    + "\n"
                    + "\n\nRecognized options (with default values):\n"
                    + padOpt("enabled (true)", "");

        if (phaseName.equals("jb.a"))
            return "Phase " + phaseName + ":\n"
                    + "\nThe Jimple Local Aggregator removes some unnecessary copies by \ncombining local variables. Essentially, it finds definitions \nwhich have only a single use and, if it is safe to do so, \nremoves the original definition after replacing the use with the \ndefinition's right-hand side. At this stage in JimpleBody \nconstruction, local aggregation serves largely to remove the \ncopies to and from stack variables which simulate load and store \ninstructions in the original bytecode."
                    + "\n\nRecognized options (with default values):\n"
                    + padOpt("enabled (true)", "")
                    + padOpt("only-stack-locals (true)", "");

        if (phaseName.equals("jb.ule"))
            return "Phase " + phaseName + ":\n"
                    + "\nThe Unused Local Eliminator removes any unused locals from the \nmethod."
                    + "\n\nRecognized options (with default values):\n"
                    + padOpt("enabled (true)", "");

        if (phaseName.equals("jb.tr"))
            return "Phase " + phaseName + ":\n"
                    + "\nThe Type Assigner gives local variables types which will \naccommodate the values stored in them over the course of the \nmethod."
                    + "\n\nRecognized options (with default values):\n"
                    + padOpt("enabled (true)", "")
                    + padOpt("use-older-type-assigner (false)", "Enables the older type assigner")
                    + padOpt("compare-type-assigners (false)", "Compares Ben Bellamy's and the older type assigner")
                    + padOpt("ignore-nullpointer-dereferences (false)", "Ignores virtual method calls on base objects that may only be null");

        if (phaseName.equals("jb.ulp"))
            return "Phase " + phaseName + ":\n"
                    + "\nThe Unsplit-originals Local Packer executes only when the \n`use-original-names' option is chosen for the `jb' phase. The \nLocal Packer attempts to minimize the number of local variables \nrequired in a method by reusing the same variable for disjoint \nDU-UD webs. Conceptually, it is the inverse of the Local \nSplitter."
                    + "\n\nRecognized options (with default values):\n"
                    + padOpt("enabled (true)", "")
                    + padOpt("unsplit-original-locals (true)", "");

        if (phaseName.equals("jb.lns"))
            return "Phase " + phaseName + ":\n"
                    + "\nThe Local Name Standardizer assigns generic names to local \nvariables."
                    + "\n\nRecognized options (with default values):\n"
                    + padOpt("enabled (true)", "")
                    + padOpt("only-stack-locals (false)", "")
                    + padOpt("sort-locals (false)", "Specifies whether the locals shall be ordered.");

        if (phaseName.equals("jb.cp"))
            return "Phase " + phaseName + ":\n"
                    + "\nThis phase performs cascaded copy propagation. If the propagator \nencounters situations of the form: A: a = ...; ... B: x = a; ... \nC: ... = ... x; where a and x are each defined only once (at A \nand B, respectively), then it can propagate immediately without \nchecking between B and C for redefinitions of a. In this case \nthe propagator is global. Otherwise, if a has multiple \ndefinitions then the propagator checks for redefinitions and \npropagates copies only within extended basic blocks."
                    + "\n\nRecognized options (with default values):\n"
                    + padOpt("enabled (true)", "")
                    + padOpt("only-regular-locals (false)", "")
                    + padOpt("only-stack-locals (true)", "");

        if (phaseName.equals("jb.dae"))
            return "Phase " + phaseName + ":\n"
                    + "\nThe Dead Assignment Eliminator eliminates assignment statements \nto locals whose values are not subsequently used, unless \nevaluating the right-hand side of the assignment may cause \nside-effects."
                    + "\n\nRecognized options (with default values):\n"
                    + padOpt("enabled (true)", "")
                    + padOpt("only-stack-locals (true)", "");

        if (phaseName.equals("jb.cp-ule"))
            return "Phase " + phaseName + ":\n"
                    + "\nThis phase removes any locals that are unused after copy \npropagation."
                    + "\n\nRecognized options (with default values):\n"
                    + padOpt("enabled (true)", "");

        if (phaseName.equals("jb.lp"))
            return "Phase " + phaseName + ":\n"
                    + "\nThe Local Packer attempts to minimize the number of local \nvariables required in a method by reusing the same variable for \ndisjoint DU-UD webs. Conceptually, it is the inverse of the \nLocal Splitter."
                    + "\n\nRecognized options (with default values):\n"
                    + padOpt("enabled (false)", "")
                    + padOpt("unsplit-original-locals (false)", "");

        if (phaseName.equals("jb.ne"))
            return "Phase " + phaseName + ":\n"
                    + "\nThe Nop Eliminator removes nop statements from the method."
                    + "\n\nRecognized options (with default values):\n"
                    + padOpt("enabled (true)", "");

        if (phaseName.equals("jb.uce"))
            return "Phase " + phaseName + ":\n"
                    + "\nThe Unreachable Code Eliminator removes unreachable code and \ntraps whose catch blocks are empty."
                    + "\n\nRecognized options (with default values):\n"
                    + padOpt("enabled (true)", "")
                    + padOpt("remove-unreachable-traps (true)", "");

        if (phaseName.equals("jb.tt"))
            return "Phase " + phaseName + ":\n"
                    + "\nThe Trap Tightener changes the area protected by each exception \nhandler, so that it begins with the first instruction in the old \nprotected area which is actually capable of throwing an \nexception caught by the handler, and ends just after the last \ninstruction in the old protected area which can throw an \nexception caught by the handler. This reduces the chance of \nproducing unverifiable code as a byproduct of pruning \nexceptional control flow within CFGs."
                    + "\n\nRecognized options (with default values):\n"
                    + padOpt("enabled (false)", "");

        if (phaseName.equals("jb.cbf"))
            return "Phase " + phaseName + ":\n"
                    + "\nThe Conditional Branch Folder statically evaluates the \nconditional expression of Jimple if statements. If the condition \nis identically true or false, the Folder replaces the \nconditional branch statement with an unconditional goto \nstatement."
                    + "\n\nRecognized options (with default values):\n"
                    + padOpt("enabled (true)", "");

        if (phaseName.equals("jj"))
            return "Phase " + phaseName + ":\n"
                    + "\nJimple Body Creation creates a JimpleBody for each input method, \nusing polyglot, to read .java files."
                    + "\n\nRecognized options (with default values):\n"
                    + padOpt("enabled (true)", "")
                    + padOpt("use-original-names (true)", "");

        if (phaseName.equals("jj.ls"))
            return "Phase " + phaseName + ":\n"
                    + "\nThe Local Splitter identifies DU-UD webs for local variables and \nintroduces new variables so that each disjoint web is associated \nwith a single local."
                    + "\n\nRecognized options (with default values):\n"
                    + padOpt("enabled (false)", "");

        if (phaseName.equals("jj.sils"))
            return "Phase " + phaseName + ":\n"
                    + "\n"
                    + "\n\nRecognized options (with default values):\n"
                    + padOpt("enabled (true)", "");

        if (phaseName.equals("jj.a"))
            return "Phase " + phaseName + ":\n"
                    + "\nThe Jimple Local Aggregator removes some unnecessary copies by \ncombining local variables. Essentially, it finds definitions \nwhich have only a single use and, if it is safe to do so, \nremoves the original definition after replacing the use with the \ndefinition's right-hand side. At this stage in JimpleBody \nconstruction, local aggregation serves largely to remove the \ncopies to and from stack variables which simulate load and store \ninstructions in the original bytecode."
                    + "\n\nRecognized options (with default values):\n"
                    + padOpt("enabled (true)", "")
                    + padOpt("only-stack-locals (true)", "");

        if (phaseName.equals("jj.ule"))
            return "Phase " + phaseName + ":\n"
                    + "\nThe Unused Local Eliminator removes any unused locals from the \nmethod."
                    + "\n\nRecognized options (with default values):\n"
                    + padOpt("enabled (true)", "");

        if (phaseName.equals("jj.tr"))
            return "Phase " + phaseName + ":\n"
                    + "\nThe Type Assigner gives local variables types which will \naccommodate the values stored in them over the course of the \nmethod."
                    + "\n\nRecognized options (with default values):\n"
                    + padOpt("enabled (false)", "");

        if (phaseName.equals("jj.ulp"))
            return "Phase " + phaseName + ":\n"
                    + "\nThe Unsplit-originals Local Packer executes only when the \n`use-original-names' option is chosen for the `jb' phase. The \nLocal Packer attempts to minimize the number of local variables \nrequired in a method by reusing the same variable for disjoint \nDU-UD webs. Conceptually, it is the inverse of the Local \nSplitter."
                    + "\n\nRecognized options (with default values):\n"
                    + padOpt("enabled (false)", "")
                    + padOpt("unsplit-original-locals (false)", "");

        if (phaseName.equals("jj.lns"))
            return "Phase " + phaseName + ":\n"
                    + "\nThe Local Name Standardizer assigns generic names to local \nvariables."
                    + "\n\nRecognized options (with default values):\n"
                    + padOpt("enabled (true)", "")
                    + padOpt("only-stack-locals (false)", "");

        if (phaseName.equals("jj.cp"))
            return "Phase " + phaseName + ":\n"
                    + "\nThis phase performs cascaded copy propagation. If the propagator \nencounters situations of the form: A: a = ...; ... B: x = a; ... \nC: ... = ... x; where a and x are each defined only once (at A \nand B, respectively), then it can propagate immediately without \nchecking between B and C for redefinitions of a. In this case \nthe propagator is global. Otherwise, if a has multiple \ndefinitions then the propagator checks for redefinitions and \npropagates copies only within extended basic blocks."
                    + "\n\nRecognized options (with default values):\n"
                    + padOpt("enabled (true)", "")
                    + padOpt("only-regular-locals (false)", "")
                    + padOpt("only-stack-locals (true)", "");

        if (phaseName.equals("jj.dae"))
            return "Phase " + phaseName + ":\n"
                    + "\nThe Dead Assignment Eliminator eliminates assignment statements \nto locals whose values are not subsequently used, unless \nevaluating the right-hand side of the assignment may cause \nside-effects."
                    + "\n\nRecognized options (with default values):\n"
                    + padOpt("enabled (true)", "")
                    + padOpt("only-stack-locals (true)", "");

        if (phaseName.equals("jj.cp-ule"))
            return "Phase " + phaseName + ":\n"
                    + "\nThis phase removes any locals that are unused after copy \npropagation."
                    + "\n\nRecognized options (with default values):\n"
                    + padOpt("enabled (true)", "");

        if (phaseName.equals("jj.lp"))
            return "Phase " + phaseName + ":\n"
                    + "\nThe Local Packer attempts to minimize the number of local \nvariables required in a method by reusing the same variable for \ndisjoint DU-UD webs. Conceptually, it is the inverse of the \nLocal Splitter."
                    + "\n\nRecognized options (with default values):\n"
                    + padOpt("enabled (false)", "")
                    + padOpt("unsplit-original-locals (false)", "");

        if (phaseName.equals("jj.ne"))
            return "Phase " + phaseName + ":\n"
                    + "\nThe Nop Eliminator removes nop statements from the method."
                    + "\n\nRecognized options (with default values):\n"
                    + padOpt("enabled (true)", "");

        if (phaseName.equals("jj.uce"))
            return "Phase " + phaseName + ":\n"
                    + "\nThe Unreachable Code Eliminator removes unreachable code and \ntraps whose catch blocks are empty."
                    + "\n\nRecognized options (with default values):\n"
                    + padOpt("enabled (true)", "");

        if (phaseName.equals("wjpp"))
            return "Phase " + phaseName + ":\n"
                    + "\nThis pack allows you to insert pre-processors that are run \nbefore call-graph construction. Only enabled in whole-program \nmode."
                    + "\n\nRecognized options (with default values):\n"
                    + padOpt("enabled (true)", "");

        if (phaseName.equals("wjpp.cimbt"))
            return "Phase " + phaseName + ":\n"
                    + "\nWhen using the types-for-invoke option of the cg phase, problems \nmight occur if the base object of a call to Method.invoke() (the \nfirst argument) is a string constant. This option replaces all \nstring constants of such calls by locals and, therefore, allows \nthe static resolution of reflective call targets on constant \nstring objects."
                    + "\n\nRecognized options (with default values):\n"
                    + padOpt("enabled (false)", "")
                    + padOpt("verbose (false)", "");

        if (phaseName.equals("wspp"))
            return "Phase " + phaseName + ":\n"
                    + "\nThis pack allows you to insert pre-processors that are run \nbefore call-graph construction. Only enabled in whole-program \nShimple mode. In an unmodified copy of Soot, this pack is empty."
                    + "\n\nRecognized options (with default values):\n"
                    + padOpt("enabled (true)", "");

        if (phaseName.equals("cg"))
            return "Phase " + phaseName + ":\n"
                    + "\nThe Call Graph Constructor computes a call graph for whole \nprogram analysis. When this pack finishes, a call graph is \navailable in the Scene. The different phases in this pack are \ndifferent ways to construct the call graph. Exactly one phase in \nthis pack must be enabled; Soot will raise an error otherwise."
                    + "\n\nRecognized options (with default values):\n"
                    + padOpt("enabled (true)", "")
                    + padOpt("safe-forname (false)", "Handle Class.forName() calls conservatively")
                    + padOpt("safe-newinstance (false)", "Handle Class.newInstance() calls conservatively")
                    + padOpt("library", "Specifies whether the target classes should be treated as an application or a library.")
                        + padVal("disabled (default)", "Call(and pointer assignment) graph construction treat the target classes as application starting from the entry points.")
                        + padVal("any-subtype", "In this mode types of any accessible field, method parameter, this local, or caugth exception is set to any possible sub type according to the class hierarchy of the target library.")
                        + padVal("signature-resolution", "In this mode types of any accessible field, method parameter, this local, or caugth exception is set to any possible sub type according to a possible extended class hierarchy of the target library.")
                    + padOpt("verbose (false)", "Print warnings about where the call graph may be incomplete")
                    + padOpt("jdkver (3)", "JDK version for native methods")
                    + padOpt("all-reachable (false)", "Assume all methods of application classes are reachable.")
                    + padOpt("implicit-entry (true)", "Include methods called implicitly by the VM as entry points")
                    + padOpt("trim-clinit (true)", "Removes redundant static initializer calls")
                    + padOpt("reflection-log", "Uses a reflection log to resolve reflective calls.")
                    + padOpt("guards (ignore)", "Describes how to guard the program from unsound assumptions.")
                    + padOpt("types-for-invoke (false)", "Uses reaching types inferred by the pointer analysis to resolve reflective calls.")
                    + padOpt("resolve-all-abstract-invokes (false)", "Causes methods invoked on abstract classes to be resolved even if there are no non-abstract children of the classes in the Scene.");

        if (phaseName.equals("cg.cha"))
            return "Phase " + phaseName + ":\n"
                    + "\nThis phase uses Class Hierarchy Analysis to generate a call \ngraph."
                    + "\n\nRecognized options (with default values):\n"
                    + padOpt("enabled (true)", "")
                    + padOpt("verbose (false)", "Print statistics about the resulting call graph")
                    + padOpt("apponly (false)", "Consider only application classes");

        if (phaseName.equals("cg.spark"))
            return "Phase " + phaseName + ":\n"
                    + "\nSpark is a flexible points-to analysis framework. Aside from \nbuilding a call graph, it also generates information about the \ntargets of pointers. For details about Spark, please see Ondrej \nLhotak's M.Sc. thesis."
                    + "\n\nRecognized options (with default values):\n"
                    + padOpt("enabled (false)", "")
                    + padOpt("verbose (false)", "Print detailed information about the execution of Spark")
                    + padOpt("ignore-types (false)", "Make Spark completely ignore declared types of variables")
                    + padOpt("force-gc (false)", "Force garbage collection for measuring memory usage")
                    + padOpt("pre-jimplify (false)", "Jimplify all methods before starting Spark")
                    + padOpt("apponly (false)", "Consider only application classes")
                    + padOpt("vta (false)", "Emulate Variable Type Analysis")
                    + padOpt("rta (false)", "Emulate Rapid Type Analysis")
                    + padOpt("field-based (false)", "Use a field-based rather than field-sensitive representation")
                    + padOpt("types-for-sites (false)", "Represent objects by their actual type rather than allocation site")
                    + padOpt("merge-stringbuffer (true)", "Represent all StringBuffers as one object")
                    + padOpt("string-constants (false)", "Propagate all string constants, not just class names")
                    + padOpt("simulate-natives (true)", "Simulate effects of native methods in standard class library")
                    + padOpt("empties-as-allocs (false)", "Treat singletons for empty sets etc. as allocation sites")
                    + padOpt("simple-edges-bidirectional (false)", "Equality-based analysis between variable nodes")
                    + padOpt("on-fly-cg (true)", "Build call graph as receiver types become known")
                    + padOpt("simplify-offline (false)", "Collapse single-entry subgraphs of the PAG")
                    + padOpt("simplify-sccs (false)", "Collapse strongly-connected components of the PAG")
                    + padOpt("ignore-types-for-sccs (false)", "Ignore declared types when determining node equivalence for SCCs")
                    + padOpt("propagator", "Select propagation algorithm")
                        + padVal("iter", "Simple iterative algorithm")
                        + padVal("worklist (default)", "Fast, worklist-based algorithm")
                        + padVal("cycle", "Unfinished on-the-fly cycle detection algorithm")
                        + padVal("merge", "Unfinished field reference merging algorithms")
                        + padVal("alias", "Alias-edge based algorithm")
                        + padVal("none", "Disable propagation")
                    + padOpt("set-impl", "Select points-to set implementation")
                        + padVal("hash", "Use Java HashSet")
                        + padVal("bit", "Bit vector")
                        + padVal("hybrid", "Hybrid representation using bit vector for large sets")
                        + padVal("array", "Sorted array representation")
                        + padVal("heintze", "Heintze's shared bit-vector and overflow list representation")
                        + padVal("sharedlist", "Shared list representation")
                        + padVal("double (default)", "Double set representation for incremental propagation")
                    + padOpt("double-set-old", "Select implementation of points-to set for old part of double set")
                        + padVal("hash", "Use Java HashSet")
                        + padVal("bit", "Bit vector")
                        + padVal("hybrid (default)", "Hybrid representation using bit vector for large sets")
                        + padVal("array", "Sorted array representation")
                        + padVal("heintze", "Heintze's shared bit-vector and overflow list representation")
                        + padVal("sharedlist", "Shared list representation")
                    + padOpt("double-set-new", "Select implementation of points-to set for new part of double set")
                        + padVal("hash", "Use Java HashSet")
                        + padVal("bit", "Bit vector")
                        + padVal("hybrid (default)", "Hybrid representation using bit vector for large sets")
                        + padVal("array", "Sorted array representation")
                        + padVal("heintze", "Heintze's shared bit-vector and overflow list representation")
                        + padVal("sharedlist", "Shared list representation")
                    + padOpt("dump-html (false)", "Dump pointer assignment graph to HTML for debugging")
                    + padOpt("dump-pag (false)", "Dump pointer assignment graph for other solvers")
                    + padOpt("dump-solution (false)", "Dump final solution for comparison with other solvers")
                    + padOpt("topo-sort (false)", "Sort variable nodes in dump")
                    + padOpt("dump-types (true)", "Include declared types in dump")
                    + padOpt("class-method-var (true)", "In dump, label variables by class and method")
                    + padOpt("dump-answer (false)", "Dump computed reaching types for comparison with other solvers")
                    + padOpt("add-tags (false)", "Output points-to results in tags for viewing with the Jimple")
                    + padOpt("set-mass (false)", "Calculate statistics about points-to set sizes")
                    + padOpt("cs-demand (false)", "After running Spark, refine points-to sets on demand with context information")
                    + padOpt("lazy-pts (true)", "Create lazy points-to sets that create context information only when needed.")
                    + padOpt("traversal (75000)", "Make the analysis traverse at most this number of nodes per query.")
                    + padOpt("passes (10)", "Perform at most this number of refinement iterations.")
                    + padOpt("geom-pta (false)", "This switch enables/disables the geometric analysis.")
                    + padOpt("geom-encoding (Geom)", "Encoding methodology")
                        + padVal("Geom (default)", "Geometric Encoding")
                        + padVal("HeapIns", "Heap Insensitive Encoding")
                        + padVal("PtIns", "PtIns")
                    + padOpt("geom-worklist (PQ)", "Worklist type")
                        + padVal("PQ (default)", "Priority Queue")
                        + padVal("FIFO", "FIFO Queue")
                    + padOpt("geom-dump-verbose ()", "Filename for detailed execution log")
                    + padOpt("geom-verify-name ()", "Filename for verification file")
                    + padOpt("geom-eval (0)", "Precision evaluation methodologies")
                    + padOpt("geom-trans (false)", "Transform to context-insensitive result")
                    + padOpt("geom-frac-base (40)", "Fractional parameter for precision/performance trade-off")
                    + padOpt("geom-blocking (true)", "Enable blocking strategy for recursive calls")
                    + padOpt("geom-runs (1)", "Iterations of analysis")
                    + padOpt("geom-app-only (true)", "Processing pointers that impact pointers in application code only");

        if (phaseName.equals("cg.paddle"))
            return "Phase " + phaseName + ":\n"
                    + "\nPaddle is a BDD-based interprocedural analysis framework. It \nincludes points-to analysis, call graph construction, and \nvarious client analyses."
                    + "\n\nRecognized options (with default values):\n"
                    + padOpt("enabled (false)", "")
                    + padOpt("verbose (false)", "Print detailed information about the execution of Paddle")
                    + padOpt("conf", "Select Paddle configuration")
                        + padVal("ofcg (default)", "On-the fly call graph")
                        + padVal("cha", "CHA only")
                        + padVal("cha-aot", "CHA ahead-of-time callgraph")
                        + padVal("ofcg-aot", "OFCG-AOT callgraph")
                        + padVal("cha-context-aot", "CHA-Context-AOT callgraph")
                        + padVal("ofcg-context-aot", "OFCG-Context-AOT callgraph")
                        + padVal("cha-context", "CHA-Context callgraph")
                        + padVal("ofcg-context", "OFCG-Context callgraph")
                    + padOpt("bdd (false)", "Use BDD version of Paddle")
                    + padOpt("order (32)", "")
                    + padOpt("dynamic-order", "")
                    + padOpt("profile (false)", "Profile BDDs using JeddProfiler")
                    + padOpt("verbosegc (false)", "Print memory usage at each BDD garbage collection.")
                    + padOpt("q", "Select queue implementation")
                        + padVal("auto (default)", "Select queue implementation based on bdd option")
                        + padVal("trad", "Normal worklist queue implementation")
                        + padVal("bdd", "BDD-based queue implementation")
                        + padVal("debug", "Debugging queue implementation")
                        + padVal("trace", "Tracing queue implementation")
                        + padVal("numtrace", "Number-tracing queue implementation")
                    + padOpt("backend", "Select BDD backend")
                        + padVal("auto (default)", "Select backend based on bdd option")
                        + padVal("buddy", "BuDDy backend")
                        + padVal("cudd", "CUDD backend")
                        + padVal("sable", "SableJBDD backend")
                        + padVal("javabdd", "JavaBDD backend")
                        + padVal("none", "No BDDs")
                    + padOpt("bdd-nodes (0)", "Number of BDD nodes to allocate (0=unlimited)")
                    + padOpt("ignore-types (false)", "Make Paddle completely ignore declared types of variables")
                    + padOpt("pre-jimplify (false)", "Jimplify all methods before starting Paddle")
                    + padOpt("context", "Select context-sensitivity level")
                        + padVal("insens (default)", "Builds a context-insensitive call graph")
                        + padVal("1cfa", "Builds a 1-CFA call graph")
                        + padVal("kcfa", "Builds a k-CFA call graph")
                        + padVal("objsens", "Builds an object-sensitive call graph")
                        + padVal("kobjsens", "Builds a k-object-sensitive call graph")
                        + padVal("uniqkobjsens", "Builds a unique-k-object-sensitive call graph")
                        + padVal("threadkobjsens", "Experimental option for thread-entry-point sensitivity")
                    + padOpt("k (2)", "")
                    + padOpt("context-heap (false)", "Treat allocation sites context-sensitively")
                    + padOpt("rta (false)", "Emulate Rapid Type Analysis")
                    + padOpt("field-based (false)", "Use a field-based rather than field-sensitive representation")
                    + padOpt("types-for-sites (false)", "Represent objects by their actual type rather than allocation site")
                    + padOpt("merge-stringbuffer (true)", "Represent all StringBuffers as one object")
                    + padOpt("string-constants (false)", "Propagate all string constants, not just class names")
                    + padOpt("simulate-natives (true)", "Simulate effects of native methods in standard class library")
                    + padOpt("global-nodes-in-natives (false)", "Use global node to model variables in simulations of native methods")
                    + padOpt("simple-edges-bidirectional (false)", "Equality-based analysis between variable nodes")
                    + padOpt("this-edges (false)", "Use pointer assignment edges to model this parameters")
                    + padOpt("precise-newinstance (true)", "Make newInstance only allocate objects of dynamic classes")
                    + padOpt("propagator", "Select propagation algorithm")
                        + padVal("auto (default)", "Select propagation algorithm based on bdd option")
                        + padVal("iter", "Simple iterative algorithm")
                        + padVal("worklist", "Fast, worklist-based algorithm")
                        + padVal("alias", "Alias-edge based algorithm")
                        + padVal("bdd", "BDD-based propagator")
                        + padVal("incbdd", "Incrementalized BDD-based propagator")
                    + padOpt("set-impl", "Select points-to set implementation")
                        + padVal("hash", "Use Java HashSet")
                        + padVal("bit", "Bit vector")
                        + padVal("hybrid", "Hybrid representation using bit vector for large sets")
                        + padVal("array", "Sorted array representation")
                        + padVal("heintze", "Heintze's shared bit-vector and overflow list representation")
                        + padVal("double (default)", "Double set representation for incremental propagation")
                    + padOpt("double-set-old", "Select implementation of points-to set for old part of double set")
                        + padVal("hash", "Use Java HashSet")
                        + padVal("bit", "Bit vector")
                        + padVal("hybrid (default)", "Hybrid representation using bit vector for large sets")
                        + padVal("array", "Sorted array representation")
                        + padVal("heintze", "Heintze's shared bit-vector and overflow list representation")
                    + padOpt("double-set-new", "Select implementation of points-to set for new part of double set")
                        + padVal("hash", "Use Java HashSet")
                        + padVal("bit", "Bit vector")
                        + padVal("hybrid (default)", "Hybrid representation using bit vector for large sets")
                        + padVal("array", "Sorted array representation")
                        + padVal("heintze", "Heintze's shared bit-vector and overflow list representation")
                    + padOpt("context-counts (false)", "Print number of contexts for each method")
                    + padOpt("total-context-counts (false)", "Print total number of contexts")
                    + padOpt("method-context-counts (false)", "Print number of contexts for each method")
                    + padOpt("set-mass (false)", "Calculate statistics about points-to set sizes")
                    + padOpt("number-nodes (true)", "Print node numbers in dumps");

        if (phaseName.equals("wstp"))
            return "Phase " + phaseName + ":\n"
                    + "\nSoot can perform whole-program analyses. In whole-shimple mode, \nSoot applies the contents of the Whole-Shimple Transformation \nPack to the scene as a whole after constructing a call graph for \nthe program. In an unmodified copy of Soot the Whole-Shimple \nTransformation Pack is empty."
                    + "\n\nRecognized options (with default values):\n"
                    + padOpt("enabled (true)", "");

        if (phaseName.equals("wsop"))
            return "Phase " + phaseName + ":\n"
                    + "\nIf Soot is running in whole shimple mode and the Whole-Shimple \nOptimization Pack is enabled, the pack's transformations are \napplied to the scene as a whole after construction of the call \ngraph and application of any enabled Whole-Shimple \nTransformations. In an unmodified copy of Soot the Whole-Shimple \nOptimization Pack is empty."
                    + "\n\nRecognized options (with default values):\n"
                    + padOpt("enabled (false)", "");

        if (phaseName.equals("wjtp"))
            return "Phase " + phaseName + ":\n"
                    + "\nSoot can perform whole-program analyses. In whole-program mode, \nSoot applies the contents of the Whole-Jimple Transformation \nPack to the scene as a whole after constructing a call graph for \nthe program."
                    + "\n\nRecognized options (with default values):\n"
                    + padOpt("enabled (true)", "");

        if (phaseName.equals("wjtp.mhp"))
            return "Phase " + phaseName + ":\n"
                    + "\nMay Happen in Parallel (MHP) Analyses determine what program \nstatements may be run by different threads concurrently. This \nphase does not perform any transformation."
                    + "\n\nRecognized options (with default values):\n"
                    + padOpt("enabled (false)", "");

        if (phaseName.equals("wjtp.tn"))
            return "Phase " + phaseName + ":\n"
                    + "\nThe Lock Allocator finds critical sections (synchronized \nregions) in Java programs and assigns locks for execution on \nboth optimistic and pessimistic JVMs. It can also be used to \nanalyze the existing locks."
                    + "\n\nRecognized options (with default values):\n"
                    + padOpt("enabled (false)", "")
                    + padOpt("locking-scheme", "Selects the granularity of the generated lock allocation")
                        + padVal("medium-grained (default)", "Use a runtime object for synchronization where possible")
                        + padVal("coarse-grained", "Use static objects for synchronization")
                        + padVal("single-static", "Use just one static synchronization object for all transactional regions")
                        + padVal("leave-original", "Analyse the existing lock structure without making changes")
                    + padOpt("avoid-deadlock (true)", "Perform Deadlock Avoidance")
                    + padOpt("open-nesting (true)", "Use an open nesting model")
                    + padOpt("do-mhp (true)", "Perform a May-Happen-in-Parallel analysis")
                    + padOpt("do-tlo (true)", "Perform a Local-Objects analysis")
                    + padOpt("print-graph (false)", "Print topological graph of transactions")
                    + padOpt("print-table (false)", "Print table of transactions")
                    + padOpt("print-debug (false)", "Print debugging info");

        if (phaseName.equals("wjtp.rdc"))
            return "Phase " + phaseName + ":\n"
                    + "\nRename duplicated classes when the file system is not case \nsensitive. If the file system is case sensitive, this phase does \nnothing."
                    + "\n\nRecognized options (with default values):\n"
                    + padOpt("enabled (false)", "")
                    + padOpt("fcn fixed-class-names", "Set ARG for the fixed class names.");

        if (phaseName.equals("wjop"))
            return "Phase " + phaseName + ":\n"
                    + "\nIf Soot is running in whole program mode and the Whole-Jimple \nOptimization Pack is enabled, the pack's transformations are \napplied to the scene as a whole after construction of the call \ngraph and application of any enabled Whole-Jimple \nTransformations."
                    + "\n\nRecognized options (with default values):\n"
                    + padOpt("enabled (false)", "");

        if (phaseName.equals("wjop.smb"))
            return "Phase " + phaseName + ":\n"
                    + "\nThe Static Method Binder statically binds monomorphic call \nsites. That is, it searches the call graph for virtual method \ninvocations that can be determined statically to call only a \nsingle implementation of the called method. Then it replaces \nsuch virtual invocations with invocations of a static copy of \nthe single called implementation."
                    + "\n\nRecognized options (with default values):\n"
                    + padOpt("enabled (false)", "")
                    + padOpt("insert-null-checks (true)", "")
                    + padOpt("insert-redundant-casts (true)", "")
                    + padOpt("allowed-modifier-changes", "")
                        + padVal("unsafe (default)", "")
                        + padVal("safe", "")
                        + padVal("none", "");

        if (phaseName.equals("wjop.si"))
            return "Phase " + phaseName + ":\n"
                    + "\nThe Static Inliner visits all call sites in the call graph in a \nbottom-up fashion, replacing monomorphic calls with inlined \ncopies of the invoked methods."
                    + "\n\nRecognized options (with default values):\n"
                    + padOpt("enabled (true)", "")
                    + padOpt("rerun-jb (true)", "")
                    + padOpt("insert-null-checks (true)", "")
                    + padOpt("insert-redundant-casts (true)", "")
                    + padOpt("allowed-modifier-changes", "")
                        + padVal("unsafe (default)", "")
                        + padVal("safe", "")
                        + padVal("none", "")
                    + padOpt("expansion-factor (3)", "")
                    + padOpt("max-container-size (5000)", "")
                    + padOpt("max-inlinee-size (20)", "");

        if (phaseName.equals("wjap"))
            return "Phase " + phaseName + ":\n"
                    + "\nSome analyses do not transform Jimple body directly, but \nannotate statements or values with tags. Whole-Jimple annotation \npack provides a place for annotation-oriented analyses in whole \nprogram mode."
                    + "\n\nRecognized options (with default values):\n"
                    + padOpt("enabled (true)", "");

        if (phaseName.equals("wjap.ra"))
            return "Phase " + phaseName + ":\n"
                    + "\nThe Rectangular Array Finder traverses Jimple statements based \non the static call graph, and finds array variables which always \nhold rectangular two-dimensional array objects. In Java, a \nmulti-dimensional array is an array of arrays, which means the \nshape of the array can be ragged. Nevertheless, many \napplications use rectangular arrays. Knowing that an array is \nrectangular can be very helpful in proving safe array bounds \nchecks. The Rectangular Array Finder does not change the program \nbeing analyzed. Its results are used by the Array Bound Checker."
                    + "\n\nRecognized options (with default values):\n"
                    + padOpt("enabled (false)", "");

        if (phaseName.equals("wjap.umt"))
            return "Phase " + phaseName + ":\n"
                    + "\nUses the call graph to determine which methods are unreachable \nand adds color tags so they can be highlighted in a source \nbrowser."
                    + "\n\nRecognized options (with default values):\n"
                    + padOpt("enabled (false)", "");

        if (phaseName.equals("wjap.uft"))
            return "Phase " + phaseName + ":\n"
                    + "\nUses the call graph to determine which fields are unreachable \nand adds color tags so they can be highlighted in a source \nbrowser."
                    + "\n\nRecognized options (with default values):\n"
                    + padOpt("enabled (false)", "");

        if (phaseName.equals("wjap.tqt"))
            return "Phase " + phaseName + ":\n"
                    + "\nDetermines which methods and fields have qualifiers that could \nbe tightened. For example: if a field or method has the \nqualifier of public but is only used within the declaring class \nit could be private. This, this field or method is tagged with \ncolor tags so that the results can be highlighted in a source \nbrowser."
                    + "\n\nRecognized options (with default values):\n"
                    + padOpt("enabled (false)", "");

        if (phaseName.equals("wjap.cgg"))
            return "Phase " + phaseName + ":\n"
                    + "\nCreates graphical call graph."
                    + "\n\nRecognized options (with default values):\n"
                    + padOpt("enabled (false)", "")
                    + padOpt("show-lib-meths (false)", "");

        if (phaseName.equals("wjap.purity"))
            return "Phase " + phaseName + ":\n"
                    + "\nPurity anaysis implemented by Antoine Mine and based on the \npaper A Combined Pointer and Purity Analysis for Java Programs \nby Alexandru Salcianu and Martin Rinard."
                    + "\n\nRecognized options (with default values):\n"
                    + padOpt("enabled (false)", "")
                    + padOpt("dump-summaries (true)", "")
                    + padOpt("dump-cg (false)", "")
                    + padOpt("dump-intra (false)", "")
                    + padOpt("print (true)", "")
                    + padOpt("annotate (true)", "Marks pure methods with a purity bytecode attribute")
                    + padOpt("verbose (false)", "");

        if (phaseName.equals("shimple"))
            return "Phase " + phaseName + ":\n"
                    + "\nShimple Control sets parameters which apply throughout the \ncreation and manipulation of Shimple bodies. Shimple is Soot's \nSSA representation."
                    + "\n\nRecognized options (with default values):\n"
                    + padOpt("enabled (true)", "")
                    + padOpt("node-elim-opt (true)", "Node elimination optimizations")
                    + padOpt("standard-local-names (false)", "Uses naming scheme of the Local Name Standardizer.")
                    + padOpt("extended (false)", "Compute extended SSA (SSI) form.")
                    + padOpt("debug (false)", "Enables debugging output, if any.");

        if (phaseName.equals("stp"))
            return "Phase " + phaseName + ":\n"
                    + "\nWhen the Shimple representation is produced, Soot applies the \ncontents of the Shimple Transformation Pack to each method under \nanalysis. This pack contains no transformations in an unmodified \nversion of Soot."
                    + "\n\nRecognized options (with default values):\n"
                    + padOpt("enabled (true)", "");

        if (phaseName.equals("sop"))
            return "Phase " + phaseName + ":\n"
                    + "\nThe Shimple Optimization Pack contains transformations that \nperform optimizations on Shimple, Soot's SSA representation."
                    + "\n\nRecognized options (with default values):\n"
                    + padOpt("enabled (false)", "");

        if (phaseName.equals("sop.cpf"))
            return "Phase " + phaseName + ":\n"
                    + "\nA powerful constant propagator and folder based on an algorithm \nsketched by Cytron et al that takes conditional control flow \ninto account. This optimization demonstrates some of the \nbenefits of SSA -- particularly the fact that Phi nodes \nrepresent natural merge points in the control flow."
                    + "\n\nRecognized options (with default values):\n"
                    + padOpt("enabled (true)", "")
                    + padOpt("prune-cfg (true)", "Take advantage of CFG optimization opportunities.");

        if (phaseName.equals("jtp"))
            return "Phase " + phaseName + ":\n"
                    + "\nSoot applies the contents of the Jimple Transformation Pack to \neach method under analysis. This pack contains no \ntransformations in an unmodified version of Soot."
                    + "\n\nRecognized options (with default values):\n"
                    + padOpt("enabled (true)", "");

        if (phaseName.equals("jop"))
            return "Phase " + phaseName + ":\n"
                    + "\nWhen Soot's Optimize option is on, Soot applies the Jimple \nOptimization Pack to every JimpleBody in application classes. \nThis section lists the default transformations in the Jimple \nOptimization Pack."
                    + "\n\nRecognized options (with default values):\n"
                    + padOpt("enabled (false)", "Eliminates common subexpressions");

        if (phaseName.equals("jop.cse"))
            return "Phase " + phaseName + ":\n"
                    + "\nThe Common Subexpression Eliminator runs an available \nexpressions analysis on the method body, then eliminates common \nsubexpressions. This implementation is especially slow, as it \nruns on individual statements rather than on basic blocks. A \nbetter implementation (which would find most common \nsubexpressions, but not all) would use basic blocks instead. \nThis implementation is also slow because the flow universe is \nexplicitly created; it need not be. A better implementation \nwould implicitly compute the kill sets at every node. Because of \nits current slowness, this transformation is not enabled by \ndefault."
                    + "\n\nRecognized options (with default values):\n"
                    + padOpt("enabled (false)", "")
                    + padOpt("naive-side-effect (false)", "Use naive side effect analysis even if interprocedural information is available");

        if (phaseName.equals("jop.bcm"))
            return "Phase " + phaseName + ":\n"
                    + "\nBusy Code Motion is a straightforward implementation of Partial \nRedundancy Elimination. This implementation is not very \naggressive. Lazy Code Motion is an improved version which should \nbe used instead of Busy Code Motion."
                    + "\n\nRecognized options (with default values):\n"
                    + padOpt("enabled (false)", "")
                    + padOpt("naive-side-effect (false)", "Use a naive side effect analysis even if interprocedural information is available");

        if (phaseName.equals("jop.lcm"))
            return "Phase " + phaseName + ":\n"
                    + "\nLazy Code Motion is an enhanced version of Busy Code Motion, a \nPartial Redundancy Eliminator. Before doing Partial Redundancy \nElimination, this optimization performs loop inversion (turning \nwhile loops into do while loops inside an if statement). This \nallows the Partial Redundancy Eliminator to optimize loop \ninvariants of while loops."
                    + "\n\nRecognized options (with default values):\n"
                    + padOpt("enabled (false)", "")
                    + padOpt("safety", "")
                        + padVal("safe (default)", "")
                        + padVal("medium", "")
                        + padVal("unsafe", "")
                    + padOpt("unroll (true)", "")
                    + padOpt("naive-side-effect (false)", "Use a naive side effect analysis even if interprocedural information is available");

        if (phaseName.equals("jop.cp"))
            return "Phase " + phaseName + ":\n"
                    + "\nThis phase performs cascaded copy propagation."
                    + "\n\nRecognized options (with default values):\n"
                    + padOpt("enabled (true)", "")
                    + padOpt("only-regular-locals (false)", "")
                    + padOpt("only-stack-locals (false)", "");

        if (phaseName.equals("jop.cpf"))
            return "Phase " + phaseName + ":\n"
                    + "\nThe Jimple Constant Propagator and Folder evaluates any \nexpressions consisting entirely of compile-time constants, for \nexample 2 * 3, and replaces the expression with the constant \nresult, in this case 6."
                    + "\n\nRecognized options (with default values):\n"
                    + padOpt("enabled (true)", "");

        if (phaseName.equals("jop.cbf"))
            return "Phase " + phaseName + ":\n"
                    + "\nThe Conditional Branch Folder statically evaluates the \nconditional expression of Jimple if statements. If the condition \nis identically true or false, the Folder replaces the \nconditional branch statement with an unconditional goto \nstatement."
                    + "\n\nRecognized options (with default values):\n"
                    + padOpt("enabled (true)", "");

        if (phaseName.equals("jop.dae"))
            return "Phase " + phaseName + ":\n"
                    + "\nThe Dead Assignment Eliminator eliminates assignment statements \nto locals whose values are not subsequently used, unless \nevaluating the right-hand side of the assignment may cause \nside-effects."
                    + "\n\nRecognized options (with default values):\n"
                    + padOpt("enabled (true)", "")
                    + padOpt("only-tag (false)", "")
                    + padOpt("only-stack-locals (false)", "");

        if (phaseName.equals("jop.nce"))
            return "Phase " + phaseName + ":\n"
                    + "\nReplaces statements 'if(x!=null) goto y' with 'goto y' if x is \nknown to be non-null or with 'nop' if it is known to be null, \netc. Generates dead code and is hence followed by unreachable \ncode elimination. Disabled by default because it can be \nexpensive on methods with many locals."
                    + "\n\nRecognized options (with default values):\n"
                    + padOpt("enabled (false)", "");

        if (phaseName.equals("jop.uce1"))
            return "Phase " + phaseName + ":\n"
                    + "\nThe Unreachable Code Eliminator removes unreachable code and \ntraps whose catch blocks are empty."
                    + "\n\nRecognized options (with default values):\n"
                    + padOpt("enabled (true)", "")
                    + padOpt("remove-unreachable-traps (false)", "");

        if (phaseName.equals("jop.ubf1"))
            return "Phase " + phaseName + ":\n"
                    + "\nThe Unconditional Branch Folder removes unnecessary `goto' \nstatements from a JimpleBody. If a goto statement's target is \nthe next instruction, then the statement is removed. If a goto's \ntarget is another goto, with target y, then the first \nstatement's target is changed to y. If some if statement's \ntarget is a goto statement, then the if's target can be replaced \nwith the goto's target. (These situations can result from other \noptimizations, and branch folding may itself generate more \nunreachable code.)"
                    + "\n\nRecognized options (with default values):\n"
                    + padOpt("enabled (true)", "");

        if (phaseName.equals("jop.uce2"))
            return "Phase " + phaseName + ":\n"
                    + "\nAnother iteration of the Unreachable Code Eliminator."
                    + "\n\nRecognized options (with default values):\n"
                    + padOpt("enabled (true)", "")
                    + padOpt("remove-unreachable-traps (false)", "");

        if (phaseName.equals("jop.ubf2"))
            return "Phase " + phaseName + ":\n"
                    + "\nAnother iteration of the Unconditional Branch Folder."
                    + "\n\nRecognized options (with default values):\n"
                    + padOpt("enabled (true)", "");

        if (phaseName.equals("jop.ule"))
            return "Phase " + phaseName + ":\n"
                    + "\nThe Unused Local Eliminator phase removes any unused locals from \nthe method."
                    + "\n\nRecognized options (with default values):\n"
                    + padOpt("enabled (true)", "");

        if (phaseName.equals("jap"))
            return "Phase " + phaseName + ":\n"
                    + "\nThe Jimple Annotation Pack contains phases which add annotations \nto Jimple bodies individually (as opposed to the Whole-Jimple \nAnnotation Pack, which adds annotations based on the analysis of \nthe whole program)."
                    + "\n\nRecognized options (with default values):\n"
                    + padOpt("enabled (true)", "");

        if (phaseName.equals("jap.npc"))
            return "Phase " + phaseName + ":\n"
                    + "\nThe Null Pointer Checker finds instruction which have the \npotential to throw NullPointerExceptions and adds annotations \nindicating whether or not the pointer being dereferenced can be \ndetermined statically not to be null."
                    + "\n\nRecognized options (with default values):\n"
                    + padOpt("enabled (false)", "")
                    + padOpt("only-array-ref (false)", "Annotate only array references")
                    + padOpt("profiling (false)", "Insert instructions to count safe pointer accesses");

        if (phaseName.equals("jap.npcolorer"))
            return "Phase " + phaseName + ":\n"
                    + "\nProduce colour tags that the Soot plug-in for Eclipse can use to \nhighlight null and non-null references."
                    + "\n\nRecognized options (with default values):\n"
                    + padOpt("enabled (false)", "");

        if (phaseName.equals("jap.abc"))
            return "Phase " + phaseName + ":\n"
                    + "\nThe Array Bound Checker performs a static analysis to determine \nwhich array bounds checks may safely be eliminated and then \nannotates statements with the results of the analysis. If Soot \nis in whole-program mode, the Array Bound Checker can use the \nresults provided by the Rectangular Array Finder."
                    + "\n\nRecognized options (with default values):\n"
                    + padOpt("enabled (false)", "")
                    + padOpt("with-all (false)", "")
                    + padOpt("with-cse (false)", "")
                    + padOpt("with-arrayref (false)", "")
                    + padOpt("with-fieldref (false)", "")
                    + padOpt("with-classfield (false)", "")
                    + padOpt("with-rectarray (false)", "")
                    + padOpt("profiling (false)", "Profile the results of array bounds check analysis.")
                    + padOpt("add-color-tags (false)", "Add color tags to results of array bound check analysis.");

        if (phaseName.equals("jap.profiling"))
            return "Phase " + phaseName + ":\n"
                    + "\nThe Profiling Generator inserts the method invocations required \nto initialize and to report the results of any profiling \nperformed by the Null Pointer Checker and Array Bound Checker. \nUsers of the Profiling Generator must provide a MultiCounter \nclass implementing the methods invoked. For details, see the \nProfilingGenerator source code."
                    + "\n\nRecognized options (with default values):\n"
                    + padOpt("enabled (false)", "")
                    + padOpt("notmainentry (false)", "Instrument runBenchmark() instead of main()");

        if (phaseName.equals("jap.sea"))
            return "Phase " + phaseName + ":\n"
                    + "\nThe Side Effect Tagger uses the active invoke graph to produce \nside-effect attributes, as described in the Spark thesis, \nchapter 6."
                    + "\n\nRecognized options (with default values):\n"
                    + padOpt("enabled (false)", "")
                    + padOpt("naive (false)", "");

        if (phaseName.equals("jap.fieldrw"))
            return "Phase " + phaseName + ":\n"
                    + "\nThe Field Read/Write Tagger uses the active invoke graph to \nproduce tags indicating which fields may be read or written by \neach statement, including invoke statements."
                    + "\n\nRecognized options (with default values):\n"
                    + padOpt("enabled (false)", "")
                    + padOpt("threshold (100)", "");

        if (phaseName.equals("jap.cgtagger"))
            return "Phase " + phaseName + ":\n"
                    + "\nThe Call Graph Tagger produces LinkTags based on the call graph. \nThe Eclipse plugin uses these tags to produce linked popup lists \nwhich indicate the source and target methods of the statement. \nSelecting a link from the list moves the cursor to the indicated \nmethod."
                    + "\n\nRecognized options (with default values):\n"
                    + padOpt("enabled (false)", "");

        if (phaseName.equals("jap.parity"))
            return "Phase " + phaseName + ":\n"
                    + "\nThe Parity Tagger produces StringTags and ColorTags indicating \nthe parity of a variable (even, odd, top, or bottom). The \neclipse plugin can use tooltips and variable colouring to \ndisplay the information in these tags. For example, even \nvariables (such as x in x = 2) are coloured yellow."
                    + "\n\nRecognized options (with default values):\n"
                    + padOpt("enabled (false)", "");

        if (phaseName.equals("jap.pat"))
            return "Phase " + phaseName + ":\n"
                    + "\nFor each method with parameters of reference type, this tagger \nindicates the aliasing relationships between the parameters \nusing colour tags. Parameters that may be aliased are the same \ncolour. Parameters that may not be aliased are in different \ncolours."
                    + "\n\nRecognized options (with default values):\n"
                    + padOpt("enabled (false)", "");

        if (phaseName.equals("jap.lvtagger"))
            return "Phase " + phaseName + ":\n"
                    + "\nColors live variables."
                    + "\n\nRecognized options (with default values):\n"
                    + padOpt("enabled (false)", "");

        if (phaseName.equals("jap.rdtagger"))
            return "Phase " + phaseName + ":\n"
                    + "\nFor each use of a local in a stmt creates a link to the reaching \ndef."
                    + "\n\nRecognized options (with default values):\n"
                    + padOpt("enabled (false)", "");

        if (phaseName.equals("jap.che"))
            return "Phase " + phaseName + ":\n"
                    + "\nIndicates whether cast checks can be eliminated."
                    + "\n\nRecognized options (with default values):\n"
                    + padOpt("enabled (false)", "");

        if (phaseName.equals("jap.umt"))
            return "Phase " + phaseName + ":\n"
                    + "\nWhen the whole-program analysis determines a method to be \nunreachable, this transformer inserts an assertion into the \nmethod to check that it is indeed unreachable."
                    + "\n\nRecognized options (with default values):\n"
                    + padOpt("enabled (false)", "");

        if (phaseName.equals("jap.lit"))
            return "Phase " + phaseName + ":\n"
                    + "\nAn expression whose operands are constant or have reaching \ndefinitions from outside the loop body are tagged as loop \ninvariant."
                    + "\n\nRecognized options (with default values):\n"
                    + padOpt("enabled (false)", "");

        if (phaseName.equals("jap.aet"))
            return "Phase " + phaseName + ":\n"
                    + "\nA each statement a set of available expressions is after the \nstatement is added as a tag."
                    + "\n\nRecognized options (with default values):\n"
                    + padOpt("enabled (false)", "")
                    + padOpt("kind", "")
                        + padVal("optimistic (default)", "")
                        + padVal("pessimistic", "");

        if (phaseName.equals("jap.dmt"))
            return "Phase " + phaseName + ":\n"
                    + "\nProvides link tags at a statement to all of the satements \ndominators."
                    + "\n\nRecognized options (with default values):\n"
                    + padOpt("enabled (false)", "");

        if (phaseName.equals("gb"))
            return "Phase " + phaseName + ":\n"
                    + "\nThe Grimp Body Creation phase creates a GrimpBody for each \nsource method. It is run only if the output format is grimp or \ngrimple, or if class files are being output and the Via Grimp \noption has been specified."
                    + "\n\nRecognized options (with default values):\n"
                    + padOpt("enabled (true)", "");

        if (phaseName.equals("gb.a1"))
            return "Phase " + phaseName + ":\n"
                    + "\nThe Grimp Pre-folding Aggregator combines some local variables, \nfinding definitions with only a single use and removing the \ndefinition after replacing the use with the definition's \nright-hand side, if it is safe to do so. While the mechanism is \nthe same as that employed by the Jimple Local Aggregator, there \nis more scope for aggregation because of Grimp's more \ncomplicated expressions."
                    + "\n\nRecognized options (with default values):\n"
                    + padOpt("enabled (true)", "")
                    + padOpt("only-stack-locals (true)", "");

        if (phaseName.equals("gb.cf"))
            return "Phase " + phaseName + ":\n"
                    + "\nThe Grimp Constructor Folder combines new statements with the \nspecialinvoke statement that calls the new object's constructor. \nFor example, it turns r2 = new java.util.ArrayList; r2.init(); \ninto r2 = new java.util.ArrayList();"
                    + "\n\nRecognized options (with default values):\n"
                    + padOpt("enabled (true)", "");

        if (phaseName.equals("gb.a2"))
            return "Phase " + phaseName + ":\n"
                    + "\nThe Grimp Post-folding Aggregator combines local variables after \nconstructors have been folded. Constructor folding typically \nintroduces new opportunities for aggregation, since when a \nsequence of instructions like r2 = new java.util.ArrayList; \nr2.init(); r3 = r2 is replaced by r2 = new \njava.util.ArrayList(); r3 = r2 the invocation of init no longer \nrepresents a potential side-effect separating the two \ndefinitions, so they can be combined into r3 = new \njava.util.ArrayList(); (assuming there are no subsequent uses of \nr2)."
                    + "\n\nRecognized options (with default values):\n"
                    + padOpt("enabled (true)", "")
                    + padOpt("only-stack-locals (true)", "");

        if (phaseName.equals("gb.ule"))
            return "Phase " + phaseName + ":\n"
                    + "\nThis phase removes any locals that are unused after constructor \nfolding and aggregation."
                    + "\n\nRecognized options (with default values):\n"
                    + padOpt("enabled (true)", "");

        if (phaseName.equals("gop"))
            return "Phase " + phaseName + ":\n"
                    + "\nThe Grimp Optimization pack performs optimizations on GrimpBodys \n(currently there are no optimizations performed specifically on \nGrimpBodys, and the pack is empty). It is run only if the output \nformat is grimp or grimple, or if class files are being output \nand the Via Grimp option has been specified."
                    + "\n\nRecognized options (with default values):\n"
                    + padOpt("enabled (false)", "");

        if (phaseName.equals("bb"))
            return "Phase " + phaseName + ":\n"
                    + "\nThe Baf Body Creation phase creates a BafBody from each source \nmethod. It is run if the output format is baf or b or asm or a, \nor if class files are being output and the Via Grimp option has \nnot been specified."
                    + "\n\nRecognized options (with default values):\n"
                    + padOpt("enabled (true)", "");

        if (phaseName.equals("bb.lso"))
            return "Phase " + phaseName + ":\n"
                    + "\nThe Load Store Optimizer replaces some combinations of loads to \nand stores from local variables with stack instructions. A \nsimple example would be the replacement of store.r $r2; load.r \n$r2; with dup1.r in cases where the value of r2 is not used \nsubsequently."
                    + "\n\nRecognized options (with default values):\n"
                    + padOpt("enabled (true)", "")
                    + padOpt("debug (false)", "")
                    + padOpt("inter (false)", "")
                    + padOpt("sl (true)", "")
                    + padOpt("sl2 (false)", "")
                    + padOpt("sll (true)", "")
                    + padOpt("sll2 (false)", "");

        if (phaseName.equals("bb.sco"))
            return "Phase " + phaseName + ":\n"
                    + "\nThe store chain optimizer detects chains of push/store pairs \nthat write to the same variable and only retains the last store. \nIt removes the unnecessary previous push/stores that are \nsubsequently overwritten."
                    + "\n\nRecognized options (with default values):\n"
                    + padOpt("enabled (true)", "");

        if (phaseName.equals("bb.pho"))
            return "Phase " + phaseName + ":\n"
                    + "\nApplies peephole optimizations to the Baf intermediate \nrepresentation. Individual optimizations must be implemented by \nclasses implementing the Peephole interface. The Peephole \nOptimizer reads the names of the Peephole classes at runtime \nfrom the file peephole.dat and loads them dynamically. Then it \ncontinues to apply the Peepholes repeatedly until none of them \nare able to perform any further optimizations. Soot provides \nonly one Peephole, named ExamplePeephole, which is not enabled \nby the delivered peephole.dat file. ExamplePeephole removes all \ncheckcast instructions."
                    + "\n\nRecognized options (with default values):\n"
                    + padOpt("enabled (true)", "");

        if (phaseName.equals("bb.ule"))
            return "Phase " + phaseName + ":\n"
                    + "\nThis phase removes any locals that are unused after load store \noptimization and peephole optimization."
                    + "\n\nRecognized options (with default values):\n"
                    + padOpt("enabled (true)", "");

        if (phaseName.equals("bb.lp"))
            return "Phase " + phaseName + ":\n"
                    + "\nThe Local Packer attempts to minimize the number of local \nvariables required in a method by reusing the same variable for \ndisjoint DU-UD webs. Conceptually, it is the inverse of the \nLocal Splitter."
                    + "\n\nRecognized options (with default values):\n"
                    + padOpt("enabled (true)", "")
                    + padOpt("unsplit-original-locals (false)", "");

        if (phaseName.equals("bb.ne"))
            return "Phase " + phaseName + ":\n"
                    + "\nThe Nop Eliminator removes nop instructions from the method."
                    + "\n\nRecognized options (with default values):\n"
                    + padOpt("enabled (true)", "");

        if (phaseName.equals("bop"))
            return "Phase " + phaseName + ":\n"
                    + "\nThe Baf Optimization pack performs optimizations on BafBodys \n(currently there are no optimizations performed specifically on \nBafBodys, and the pack is empty). It is run only if the output \nformat is baf or b or asm or a, or if class files are being \noutput and the Via Grimp option has not been specified."
                    + "\n\nRecognized options (with default values):\n"
                    + padOpt("enabled (false)", "");

        if (phaseName.equals("tag"))
            return "Phase " + phaseName + ":\n"
                    + "\nThe Tag Aggregator pack aggregates tags attached to individual \nunits into a code attribute for each method, so that these \nattributes can be encoded in Java class files."
                    + "\n\nRecognized options (with default values):\n"
                    + padOpt("enabled (true)", "");

        if (phaseName.equals("tag.ln"))
            return "Phase " + phaseName + ":\n"
                    + "\nThe Line Number Tag Aggregator aggregates line number tags."
                    + "\n\nRecognized options (with default values):\n"
                    + padOpt("enabled (true)", "");

        if (phaseName.equals("tag.an"))
            return "Phase " + phaseName + ":\n"
                    + "\nThe Array Bounds and Null Pointer Tag Aggregator aggregates tags \nproduced by the Array Bound Checker and Null Pointer Checker."
                    + "\n\nRecognized options (with default values):\n"
                    + padOpt("enabled (false)", "");

        if (phaseName.equals("tag.dep"))
            return "Phase " + phaseName + ":\n"
                    + "\nThe Dependence Tag Aggregator aggregates tags produced by the \nSide Effect Tagger."
                    + "\n\nRecognized options (with default values):\n"
                    + padOpt("enabled (false)", "");

        if (phaseName.equals("tag.fieldrw"))
            return "Phase " + phaseName + ":\n"
                    + "\nThe Field Read/Write Tag Aggregator aggregates field read/write \ntags produced by the Field Read/Write Tagger, phase jap.fieldrw."
                    + "\n\nRecognized options (with default values):\n"
                    + padOpt("enabled (false)", "");

        if (phaseName.equals("db"))
            return "Phase " + phaseName + ":\n"
                    + "\nThe decompile (Dava) option is set using the -f dava options in \nSoot. Options provided by Dava are added to this dummy phase so \nas not to clutter the soot general arguments. -p db (option \nname):(value) will be used to set all required values for Dava."
                    + "\n\nRecognized options (with default values):\n"
                    + padOpt("enabled (true)", "")
                    + padOpt("source-is-javac (true)", "");

        if (phaseName.equals("db.transformations"))
            return "Phase " + phaseName + ":\n"
                    + "\nThe transformations implemented using AST Traversal and \nstructural flow analses on Dava's AST"
                    + "\n\nRecognized options (with default values):\n"
                    + padOpt("enabled (true)", "");

        if (phaseName.equals("db.renamer"))
            return "Phase " + phaseName + ":\n"
                    + "\nIf set, the renaming analyses implemented in Dava are applied to \neach method body being decompiled. The analyses use heuristics \nto choose potentially better names for local variables. (As of \nFebruary 14th 2006, work is still under progress on these \nanalyses (dava.toolkits.base.renamer)."
                    + "\n\nRecognized options (with default values):\n"
                    + padOpt("enabled (false)", "");

        if (phaseName.equals("db.deobfuscate"))
            return "Phase " + phaseName + ":\n"
                    + "\nCertain analyses make sense only when the bytecode is obfuscated \ncode. There are plans to implement such analyses and apply them \non methods only if this flag is set. Dead Code elimination which \nincludes removing code guarded by some condition which is always \nfalse or always true is one such analysis. Another suggested \nanalysis is giving default names to classes and fields. \nOnfuscators love to use weird names for fields and classes and \neven a simple re-naming of these could be a good help to the \nuser. Another more advanced analysis would be to check for \nredundant constant fields added by obfuscators and then remove \nuses of these constant fields from the code."
                    + "\n\nRecognized options (with default values):\n"
                    + padOpt("enabled (true)", "");

        if (phaseName.equals("db.force-recompile"))
            return "Phase " + phaseName + ":\n"
                    + "\nWhile decompiling we have to be clear what our aim is: do we \nwant to convert bytecode to Java syntax and stay as close to the \nactual execution of bytecode or do we want recompilably Java \nsource representing the bytecode. This distinction is important \nbecause some restrictions present in Java source are absent from \nthe bytecode. Examples of this include that fact that in Java a \ncall to a constructor or super needs to be the first statement \nin a constructors body. This restriction is absent from the \nbytecode. Similarly final fields HAVE to be initialized once and \nonly once in either the static initializer (static fields) or \nall the constructors (non-static fields). Additionally the \nfields should be initialized on all possible execution paths. \nThese restrictions are again absent from the bytecode. In doing \na one-one conversion of bytecode to Java source then no attempt \nshould be made to fix any of these and similar problems in the \nJava source. However, if the aim is to get recompilable code \nthen these and similar issues need to be fixed. Setting the \nforce-recompilability flag will ensure that the decompiler tries \nits best to produce recompilable Java source."
                    + "\n\nRecognized options (with default values):\n"
                    + padOpt("enabled (true)", "");

        return "Unrecognized phase: " + phaseName;
    }

    public static String getDeclaredOptionsForPhase(String phaseName) {
        if (phaseName.equals("jb"))
            return String.join(" ", 
                    "enabled",
                    "use-original-names",
                    "preserve-source-annotations",
                    "stabilize-local-names",
                    "model-lambdametafactory"
            );

        if (phaseName.equals("jb.dtr"))
            return String.join(" ", 
                    "enabled"
            );

        if (phaseName.equals("jb.ese"))
            return String.join(" ", 
                    "enabled"
            );

        if (phaseName.equals("jb.ls"))
            return String.join(" ", 
                    "enabled"
            );

        if (phaseName.equals("jb.sils"))
            return String.join(" ", 
                    "enabled"
            );

        if (phaseName.equals("jb.a"))
            return String.join(" ", 
                    "enabled",
                    "only-stack-locals"
            );

        if (phaseName.equals("jb.ule"))
            return String.join(" ", 
                    "enabled"
            );

        if (phaseName.equals("jb.tr"))
            return String.join(" ", 
                    "enabled",
                    "use-older-type-assigner",
                    "compare-type-assigners",
                    "ignore-nullpointer-dereferences"
            );

        if (phaseName.equals("jb.ulp"))
            return String.join(" ", 
                    "enabled",
                    "unsplit-original-locals"
            );

        if (phaseName.equals("jb.lns"))
            return String.join(" ", 
                    "enabled",
                    "only-stack-locals",
                    "sort-locals"
            );

        if (phaseName.equals("jb.cp"))
            return String.join(" ", 
                    "enabled",
                    "only-regular-locals",
                    "only-stack-locals"
            );

        if (phaseName.equals("jb.dae"))
            return String.join(" ", 
                    "enabled",
                    "only-stack-locals"
            );

        if (phaseName.equals("jb.cp-ule"))
            return String.join(" ", 
                    "enabled"
            );

        if (phaseName.equals("jb.lp"))
            return String.join(" ", 
                    "enabled",
                    "unsplit-original-locals"
            );

        if (phaseName.equals("jb.ne"))
            return String.join(" ", 
                    "enabled"
            );

        if (phaseName.equals("jb.uce"))
            return String.join(" ", 
                    "enabled",
                    "remove-unreachable-traps"
            );

        if (phaseName.equals("jb.tt"))
            return String.join(" ", 
                    "enabled"
            );

        if (phaseName.equals("jb.cbf"))
            return String.join(" ", 
                    "enabled"
            );

        if (phaseName.equals("jj"))
            return String.join(" ", 
                    "enabled",
                    "use-original-names"
            );

        if (phaseName.equals("jj.ls"))
            return String.join(" ", 
                    "enabled"
            );

        if (phaseName.equals("jj.sils"))
            return String.join(" ", 
                    "enabled"
            );

        if (phaseName.equals("jj.a"))
            return String.join(" ", 
                    "enabled",
                    "only-stack-locals"
            );

        if (phaseName.equals("jj.ule"))
            return String.join(" ", 
                    "enabled"
            );

        if (phaseName.equals("jj.tr"))
            return String.join(" ", 
                    "enabled"
            );

        if (phaseName.equals("jj.ulp"))
            return String.join(" ", 
                    "enabled",
                    "unsplit-original-locals"
            );

        if (phaseName.equals("jj.lns"))
            return String.join(" ", 
                    "enabled",
                    "only-stack-locals"
            );

        if (phaseName.equals("jj.cp"))
            return String.join(" ", 
                    "enabled",
                    "only-regular-locals",
                    "only-stack-locals"
            );

        if (phaseName.equals("jj.dae"))
            return String.join(" ", 
                    "enabled",
                    "only-stack-locals"
            );

        if (phaseName.equals("jj.cp-ule"))
            return String.join(" ", 
                    "enabled"
            );

        if (phaseName.equals("jj.lp"))
            return String.join(" ", 
                    "enabled",
                    "unsplit-original-locals"
            );

        if (phaseName.equals("jj.ne"))
            return String.join(" ", 
                    "enabled"
            );

        if (phaseName.equals("jj.uce"))
            return String.join(" ", 
                    "enabled"
            );

        if (phaseName.equals("wjpp"))
            return String.join(" ", 
                    "enabled"
            );

        if (phaseName.equals("wjpp.cimbt"))
            return String.join(" ", 
                    "enabled",
                    "verbose"
            );

        if (phaseName.equals("wspp"))
            return String.join(" ", 
                    "enabled"
            );

        if (phaseName.equals("cg"))
            return String.join(" ", 
                    "enabled",
                    "safe-forname",
                    "safe-newinstance",
                    "library",
                    "verbose",
                    "jdkver",
                    "all-reachable",
                    "implicit-entry",
                    "trim-clinit",
                    "reflection-log",
                    "guards",
                    "types-for-invoke",
                    "resolve-all-abstract-invokes"
            );

        if (phaseName.equals("cg.cha"))
            return String.join(" ", 
                    "enabled",
                    "verbose",
                    "apponly"
            );

        if (phaseName.equals("cg.spark"))
            return String.join(" ", 
                    "enabled",
                    "verbose",
                    "ignore-types",
                    "force-gc",
                    "pre-jimplify",
                    "apponly",
                    "vta",
                    "rta",
                    "field-based",
                    "types-for-sites",
                    "merge-stringbuffer",
                    "string-constants",
                    "simulate-natives",
                    "empties-as-allocs",
                    "simple-edges-bidirectional",
                    "on-fly-cg",
                    "simplify-offline",
                    "simplify-sccs",
                    "ignore-types-for-sccs",
                    "propagator",
                    "set-impl",
                    "double-set-old",
                    "double-set-new",
                    "dump-html",
                    "dump-pag",
                    "dump-solution",
                    "topo-sort",
                    "dump-types",
                    "class-method-var",
                    "dump-answer",
                    "add-tags",
                    "set-mass",
                    "cs-demand",
                    "lazy-pts",
                    "traversal",
                    "passes",
                    "geom-pta",
                    "geom-encoding",
                    "geom-worklist",
                    "geom-dump-verbose",
                    "geom-verify-name",
                    "geom-eval",
                    "geom-trans",
                    "geom-frac-base",
                    "geom-blocking",
                    "geom-runs",
                    "geom-app-only"
            );

        if (phaseName.equals("cg.paddle"))
            return String.join(" ", 
                    "enabled",
                    "verbose",
                    "conf",
                    "bdd",
                    "order",
                    "dynamic-order",
                    "profile",
                    "verbosegc",
                    "q",
                    "backend",
                    "bdd-nodes",
                    "ignore-types",
                    "pre-jimplify",
                    "context",
                    "k",
                    "context-heap",
                    "rta",
                    "field-based",
                    "types-for-sites",
                    "merge-stringbuffer",
                    "string-constants",
                    "simulate-natives",
                    "global-nodes-in-natives",
                    "simple-edges-bidirectional",
                    "this-edges",
                    "precise-newinstance",
                    "propagator",
                    "set-impl",
                    "double-set-old",
                    "double-set-new",
                    "context-counts",
                    "total-context-counts",
                    "method-context-counts",
                    "set-mass",
                    "number-nodes"
            );

        if (phaseName.equals("wstp"))
            return String.join(" ", 
                    "enabled"
            );

        if (phaseName.equals("wsop"))
            return String.join(" ", 
                    "enabled"
            );

        if (phaseName.equals("wjtp"))
            return String.join(" ", 
                    "enabled"
            );

        if (phaseName.equals("wjtp.mhp"))
            return String.join(" ", 
                    "enabled"
            );

        if (phaseName.equals("wjtp.tn"))
            return String.join(" ", 
                    "enabled",
                    "locking-scheme",
                    "avoid-deadlock",
                    "open-nesting",
                    "do-mhp",
                    "do-tlo",
                    "print-graph",
                    "print-table",
                    "print-debug"
            );

        if (phaseName.equals("wjtp.rdc"))
            return String.join(" ", 
                    "enabled",
                    "fcn fixed-class-names"
            );

        if (phaseName.equals("wjop"))
            return String.join(" ", 
                    "enabled"
            );

        if (phaseName.equals("wjop.smb"))
            return String.join(" ", 
                    "enabled",
                    "insert-null-checks",
                    "insert-redundant-casts",
                    "allowed-modifier-changes"
            );

        if (phaseName.equals("wjop.si"))
            return String.join(" ", 
                    "enabled",
                    "rerun-jb",
                    "insert-null-checks",
                    "insert-redundant-casts",
                    "allowed-modifier-changes",
                    "expansion-factor",
                    "max-container-size",
                    "max-inlinee-size"
            );

        if (phaseName.equals("wjap"))
            return String.join(" ", 
                    "enabled"
            );

        if (phaseName.equals("wjap.ra"))
            return String.join(" ", 
                    "enabled"
            );

        if (phaseName.equals("wjap.umt"))
            return String.join(" ", 
                    "enabled"
            );

        if (phaseName.equals("wjap.uft"))
            return String.join(" ", 
                    "enabled"
            );

        if (phaseName.equals("wjap.tqt"))
            return String.join(" ", 
                    "enabled"
            );

        if (phaseName.equals("wjap.cgg"))
            return String.join(" ", 
                    "enabled",
                    "show-lib-meths"
            );

        if (phaseName.equals("wjap.purity"))
            return String.join(" ", 
                    "enabled",
                    "dump-summaries",
                    "dump-cg",
                    "dump-intra",
                    "print",
                    "annotate",
                    "verbose"
            );

        if (phaseName.equals("shimple"))
            return String.join(" ", 
                    "enabled",
                    "node-elim-opt",
                    "standard-local-names",
                    "extended",
                    "debug"
            );

        if (phaseName.equals("stp"))
            return String.join(" ", 
                    "enabled"
            );

        if (phaseName.equals("sop"))
            return String.join(" ", 
                    "enabled"
            );

        if (phaseName.equals("sop.cpf"))
            return String.join(" ", 
                    "enabled",
                    "prune-cfg"
            );

        if (phaseName.equals("jtp"))
            return String.join(" ", 
                    "enabled"
            );

        if (phaseName.equals("jop"))
            return String.join(" ", 
                    "enabled"
            );

        if (phaseName.equals("jop.cse"))
            return String.join(" ", 
                    "enabled",
                    "naive-side-effect"
            );

        if (phaseName.equals("jop.bcm"))
            return String.join(" ", 
                    "enabled",
                    "naive-side-effect"
            );

        if (phaseName.equals("jop.lcm"))
            return String.join(" ", 
                    "enabled",
                    "safety",
                    "unroll",
                    "naive-side-effect"
            );

        if (phaseName.equals("jop.cp"))
            return String.join(" ", 
                    "enabled",
                    "only-regular-locals",
                    "only-stack-locals"
            );

        if (phaseName.equals("jop.cpf"))
            return String.join(" ", 
                    "enabled"
            );

        if (phaseName.equals("jop.cbf"))
            return String.join(" ", 
                    "enabled"
            );

        if (phaseName.equals("jop.dae"))
            return String.join(" ", 
                    "enabled",
                    "only-tag",
                    "only-stack-locals"
            );

        if (phaseName.equals("jop.nce"))
            return String.join(" ", 
                    "enabled"
            );

        if (phaseName.equals("jop.uce1"))
            return String.join(" ", 
                    "enabled",
                    "remove-unreachable-traps"
            );

        if (phaseName.equals("jop.ubf1"))
            return String.join(" ", 
                    "enabled"
            );

        if (phaseName.equals("jop.uce2"))
            return String.join(" ", 
                    "enabled",
                    "remove-unreachable-traps"
            );

        if (phaseName.equals("jop.ubf2"))
            return String.join(" ", 
                    "enabled"
            );

        if (phaseName.equals("jop.ule"))
            return String.join(" ", 
                    "enabled"
            );

        if (phaseName.equals("jap"))
            return String.join(" ", 
                    "enabled"
            );

        if (phaseName.equals("jap.npc"))
            return String.join(" ", 
                    "enabled",
                    "only-array-ref",
                    "profiling"
            );

        if (phaseName.equals("jap.npcolorer"))
            return String.join(" ", 
                    "enabled"
            );

        if (phaseName.equals("jap.abc"))
            return String.join(" ", 
                    "enabled",
                    "with-all",
                    "with-cse",
                    "with-arrayref",
                    "with-fieldref",
                    "with-classfield",
                    "with-rectarray",
                    "profiling",
                    "add-color-tags"
            );

        if (phaseName.equals("jap.profiling"))
            return String.join(" ", 
                    "enabled",
                    "notmainentry"
            );

        if (phaseName.equals("jap.sea"))
            return String.join(" ", 
                    "enabled",
                    "naive"
            );

        if (phaseName.equals("jap.fieldrw"))
            return String.join(" ", 
                    "enabled",
                    "threshold"
            );

        if (phaseName.equals("jap.cgtagger"))
            return String.join(" ", 
                    "enabled"
            );

        if (phaseName.equals("jap.parity"))
            return String.join(" ", 
                    "enabled"
            );

        if (phaseName.equals("jap.pat"))
            return String.join(" ", 
                    "enabled"
            );

        if (phaseName.equals("jap.lvtagger"))
            return String.join(" ", 
                    "enabled"
            );

        if (phaseName.equals("jap.rdtagger"))
            return String.join(" ", 
                    "enabled"
            );

        if (phaseName.equals("jap.che"))
            return String.join(" ", 
                    "enabled"
            );

        if (phaseName.equals("jap.umt"))
            return String.join(" ", 
                    "enabled"
            );

        if (phaseName.equals("jap.lit"))
            return String.join(" ", 
                    "enabled"
            );

        if (phaseName.equals("jap.aet"))
            return String.join(" ", 
                    "enabled",
                    "kind"
            );

        if (phaseName.equals("jap.dmt"))
            return String.join(" ", 
                    "enabled"
            );

        if (phaseName.equals("gb"))
            return String.join(" ", 
                    "enabled"
            );

        if (phaseName.equals("gb.a1"))
            return String.join(" ", 
                    "enabled",
                    "only-stack-locals"
            );

        if (phaseName.equals("gb.cf"))
            return String.join(" ", 
                    "enabled"
            );

        if (phaseName.equals("gb.a2"))
            return String.join(" ", 
                    "enabled",
                    "only-stack-locals"
            );

        if (phaseName.equals("gb.ule"))
            return String.join(" ", 
                    "enabled"
            );

        if (phaseName.equals("gop"))
            return String.join(" ", 
                    "enabled"
            );

        if (phaseName.equals("bb"))
            return String.join(" ", 
                    "enabled"
            );

        if (phaseName.equals("bb.lso"))
            return String.join(" ", 
                    "enabled",
                    "debug",
                    "inter",
                    "sl",
                    "sl2",
                    "sll",
                    "sll2"
            );

        if (phaseName.equals("bb.sco"))
            return String.join(" ", 
                    "enabled"
            );

        if (phaseName.equals("bb.pho"))
            return String.join(" ", 
                    "enabled"
            );

        if (phaseName.equals("bb.ule"))
            return String.join(" ", 
                    "enabled"
            );

        if (phaseName.equals("bb.lp"))
            return String.join(" ", 
                    "enabled",
                    "unsplit-original-locals"
            );

        if (phaseName.equals("bb.ne"))
            return String.join(" ", 
                    "enabled"
            );

        if (phaseName.equals("bop"))
            return String.join(" ", 
                    "enabled"
            );

        if (phaseName.equals("tag"))
            return String.join(" ", 
                    "enabled"
            );

        if (phaseName.equals("tag.ln"))
            return String.join(" ", 
                    "enabled"
            );

        if (phaseName.equals("tag.an"))
            return String.join(" ", 
                    "enabled"
            );

        if (phaseName.equals("tag.dep"))
            return String.join(" ", 
                    "enabled"
            );

        if (phaseName.equals("tag.fieldrw"))
            return String.join(" ", 
                    "enabled"
            );

        if (phaseName.equals("db"))
            return String.join(" ", 
                    "enabled",
                    "source-is-javac"
            );

        if (phaseName.equals("db.transformations"))
            return String.join(" ", 
                    "enabled"
            );

        if (phaseName.equals("db.renamer"))
            return String.join(" ", 
                    "enabled"
            );

        if (phaseName.equals("db.deobfuscate"))
            return String.join(" ", 
                    "enabled"
            );

        if (phaseName.equals("db.force-recompile"))
            return String.join(" ", 
                    "enabled"
            );

        // The default set of options is just enabled.
        return "enabled";
    }

    public static String getDefaultOptionsForPhase(String phaseName) {
        if (phaseName.equals("jb"))
            return ""
                    + "enabled:true "
                    + "use-original-names:false "
                    + "preserve-source-annotations:false "
                    + "stabilize-local-names:false "
                    + "model-lambdametafactory:true ";

        if (phaseName.equals("jb.dtr"))
            return ""
                    + "enabled:true ";

        if (phaseName.equals("jb.ese"))
            return ""
                    + "enabled:true ";

        if (phaseName.equals("jb.ls"))
            return ""
                    + "enabled:true ";

        if (phaseName.equals("jb.sils"))
            return ""
                    + "enabled:true ";

        if (phaseName.equals("jb.a"))
            return ""
                    + "enabled:true "
                    + "only-stack-locals:true ";

        if (phaseName.equals("jb.ule"))
            return ""
                    + "enabled:true ";

        if (phaseName.equals("jb.tr"))
            return ""
                    + "enabled:true "
                    + "use-older-type-assigner:false "
                    + "compare-type-assigners:false "
                    + "ignore-nullpointer-dereferences:false ";

        if (phaseName.equals("jb.ulp"))
            return ""
                    + "enabled:true "
                    + "unsplit-original-locals:true ";

        if (phaseName.equals("jb.lns"))
            return ""
                    + "enabled:true "
                    + "only-stack-locals:false "
                    + "sort-locals:false ";

        if (phaseName.equals("jb.cp"))
            return ""
                    + "enabled:true "
                    + "only-regular-locals:false "
                    + "only-stack-locals:true ";

        if (phaseName.equals("jb.dae"))
            return ""
                    + "enabled:true "
                    + "only-stack-locals:true ";

        if (phaseName.equals("jb.cp-ule"))
            return ""
                    + "enabled:true ";

        if (phaseName.equals("jb.lp"))
            return ""
                    + "enabled:false "
                    + "unsplit-original-locals:false ";

        if (phaseName.equals("jb.ne"))
            return ""
                    + "enabled:true ";

        if (phaseName.equals("jb.uce"))
            return ""
                    + "enabled:true "
                    + "remove-unreachable-traps:true ";

        if (phaseName.equals("jb.tt"))
            return ""
                    + "enabled:false ";

        if (phaseName.equals("jb.cbf"))
            return ""
                    + "enabled:true ";

        if (phaseName.equals("jj"))
            return ""
                    + "enabled:true "
                    + "use-original-names:true ";

        if (phaseName.equals("jj.ls"))
            return ""
                    + "enabled:false ";

        if (phaseName.equals("jj.sils"))
            return ""
                    + "enabled:true ";

        if (phaseName.equals("jj.a"))
            return ""
                    + "enabled:true "
                    + "only-stack-locals:true ";

        if (phaseName.equals("jj.ule"))
            return ""
                    + "enabled:true ";

        if (phaseName.equals("jj.tr"))
            return ""
                    + "enabled:false ";

        if (phaseName.equals("jj.ulp"))
            return ""
                    + "enabled:false "
                    + "unsplit-original-locals:false ";

        if (phaseName.equals("jj.lns"))
            return ""
                    + "enabled:true "
                    + "only-stack-locals:false ";

        if (phaseName.equals("jj.cp"))
            return ""
                    + "enabled:true "
                    + "only-regular-locals:false "
                    + "only-stack-locals:true ";

        if (phaseName.equals("jj.dae"))
            return ""
                    + "enabled:true "
                    + "only-stack-locals:true ";

        if (phaseName.equals("jj.cp-ule"))
            return ""
                    + "enabled:true ";

        if (phaseName.equals("jj.lp"))
            return ""
                    + "enabled:false "
                    + "unsplit-original-locals:false ";

        if (phaseName.equals("jj.ne"))
            return ""
                    + "enabled:true ";

        if (phaseName.equals("jj.uce"))
            return ""
                    + "enabled:true ";

        if (phaseName.equals("wjpp"))
            return ""
                    + "enabled:true ";

        if (phaseName.equals("wjpp.cimbt"))
            return ""
                    + "enabled:false "
                    + "verbose:false ";

        if (phaseName.equals("wspp"))
            return ""
                    + "enabled:true ";

        if (phaseName.equals("cg"))
            return ""
                    + "enabled:true "
                    + "safe-forname:false "
                    + "safe-newinstance:false "
                    + "library:disabled "
                    + "verbose:false "
                    + "jdkver:3 "
                    + "all-reachable:false "
                    + "implicit-entry:true "
                    + "trim-clinit:true "
                    + "guards:ignore "
                    + "types-for-invoke:false "
                    + "resolve-all-abstract-invokes:false ";

        if (phaseName.equals("cg.cha"))
            return ""
                    + "enabled:true "
                    + "verbose:false "
                    + "apponly:false ";

        if (phaseName.equals("cg.spark"))
            return ""
                    + "enabled:false "
                    + "verbose:false "
                    + "ignore-types:false "
                    + "force-gc:false "
                    + "pre-jimplify:false "
                    + "apponly:false "
                    + "vta:false "
                    + "rta:false "
                    + "field-based:false "
                    + "types-for-sites:false "
                    + "merge-stringbuffer:true "
                    + "string-constants:false "
                    + "simulate-natives:true "
                    + "empties-as-allocs:false "
                    + "simple-edges-bidirectional:false "
                    + "on-fly-cg:true "
                    + "simplify-offline:false "
                    + "simplify-sccs:false "
                    + "ignore-types-for-sccs:false "
                    + "propagator:worklist "
                    + "set-impl:double "
                    + "double-set-old:hybrid "
                    + "double-set-new:hybrid "
                    + "dump-html:false "
                    + "dump-pag:false "
                    + "dump-solution:false "
                    + "topo-sort:false "
                    + "dump-types:true "
                    + "class-method-var:true "
                    + "dump-answer:false "
                    + "add-tags:false "
                    + "set-mass:false "
                    + "cs-demand:false "
                    + "lazy-pts:true "
                    + "traversal:75000 "
                    + "passes:10 "
                    + "geom-pta:false "
                    + "geom-encoding:Geom "
                    + "geom-encoding:Geom "
                    + "geom-worklist:PQ "
                    + "geom-worklist:PQ "
                    + "geom-dump-verbose: "
                    + "geom-verify-name: "
                    + "geom-eval:0 "
                    + "geom-trans:false "
                    + "geom-frac-base:40 "
                    + "geom-blocking:true "
                    + "geom-runs:1 "
                    + "geom-app-only:true ";

        if (phaseName.equals("cg.paddle"))
            return ""
                    + "enabled:false "
                    + "verbose:false "
                    + "conf:ofcg "
                    + "bdd:false "
                    + "order:32 "
                    + "profile:false "
                    + "verbosegc:false "
                    + "q:auto "
                    + "backend:auto "
                    + "bdd-nodes:0 "
                    + "ignore-types:false "
                    + "pre-jimplify:false "
                    + "context:insens "
                    + "k:2 "
                    + "context-heap:false "
                    + "rta:false "
                    + "field-based:false "
                    + "types-for-sites:false "
                    + "merge-stringbuffer:true "
                    + "string-constants:false "
                    + "simulate-natives:true "
                    + "global-nodes-in-natives:false "
                    + "simple-edges-bidirectional:false "
                    + "this-edges:false "
                    + "precise-newinstance:true "
                    + "propagator:auto "
                    + "set-impl:double "
                    + "double-set-old:hybrid "
                    + "double-set-new:hybrid "
                    + "context-counts:false "
                    + "total-context-counts:false "
                    + "method-context-counts:false "
                    + "set-mass:false "
                    + "number-nodes:true ";

        if (phaseName.equals("wstp"))
            return ""
                    + "enabled:true ";

        if (phaseName.equals("wsop"))
            return ""
                    + "enabled:false ";

        if (phaseName.equals("wjtp"))
            return ""
                    + "enabled:true ";

        if (phaseName.equals("wjtp.mhp"))
            return ""
                    + "enabled:false ";

        if (phaseName.equals("wjtp.tn"))
            return ""
                    + "enabled:false "
                    + "locking-scheme:medium-grained "
                    + "avoid-deadlock:true "
                    + "open-nesting:true "
                    + "do-mhp:true "
                    + "do-tlo:true "
                    + "print-graph:false "
                    + "print-table:false "
                    + "print-debug:false ";

        if (phaseName.equals("wjtp.rdc"))
            return ""
                    + "enabled:false ";

        if (phaseName.equals("wjop"))
            return ""
                    + "enabled:false ";

        if (phaseName.equals("wjop.smb"))
            return ""
                    + "enabled:false "
                    + "insert-null-checks:true "
                    + "insert-redundant-casts:true "
                    + "allowed-modifier-changes:unsafe ";

        if (phaseName.equals("wjop.si"))
            return ""
                    + "enabled:true "
                    + "rerun-jb:true "
                    + "insert-null-checks:true "
                    + "insert-redundant-casts:true "
                    + "allowed-modifier-changes:unsafe "
                    + "expansion-factor:3 "
                    + "max-container-size:5000 "
                    + "max-inlinee-size:20 ";

        if (phaseName.equals("wjap"))
            return ""
                    + "enabled:true ";

        if (phaseName.equals("wjap.ra"))
            return ""
                    + "enabled:false ";

        if (phaseName.equals("wjap.umt"))
            return ""
                    + "enabled:false ";

        if (phaseName.equals("wjap.uft"))
            return ""
                    + "enabled:false ";

        if (phaseName.equals("wjap.tqt"))
            return ""
                    + "enabled:false ";

        if (phaseName.equals("wjap.cgg"))
            return ""
                    + "enabled:false "
                    + "show-lib-meths:false ";

        if (phaseName.equals("wjap.purity"))
            return ""
                    + "enabled:false "
                    + "dump-summaries:true "
                    + "dump-cg:false "
                    + "dump-intra:false "
                    + "print:true "
                    + "annotate:true "
                    + "verbose:false ";

        if (phaseName.equals("shimple"))
            return ""
                    + "enabled:true "
                    + "node-elim-opt:true "
                    + "standard-local-names:false "
                    + "extended:false "
                    + "debug:false ";

        if (phaseName.equals("stp"))
            return ""
                    + "enabled:true ";

        if (phaseName.equals("sop"))
            return ""
                    + "enabled:false ";

        if (phaseName.equals("sop.cpf"))
            return ""
                    + "enabled:true "
                    + "prune-cfg:true ";

        if (phaseName.equals("jtp"))
            return ""
                    + "enabled:true ";

        if (phaseName.equals("jop"))
            return ""
                    + "enabled:false ";

        if (phaseName.equals("jop.cse"))
            return ""
                    + "enabled:false "
                    + "naive-side-effect:false ";

        if (phaseName.equals("jop.bcm"))
            return ""
                    + "enabled:false "
                    + "naive-side-effect:false ";

        if (phaseName.equals("jop.lcm"))
            return ""
                    + "enabled:false "
                    + "safety:safe "
                    + "unroll:true "
                    + "naive-side-effect:false ";

        if (phaseName.equals("jop.cp"))
            return ""
                    + "enabled:true "
                    + "only-regular-locals:false "
                    + "only-stack-locals:false ";

        if (phaseName.equals("jop.cpf"))
            return ""
                    + "enabled:true ";

        if (phaseName.equals("jop.cbf"))
            return ""
                    + "enabled:true ";

        if (phaseName.equals("jop.dae"))
            return ""
                    + "enabled:true "
                    + "only-tag:false "
                    + "only-stack-locals:false ";

        if (phaseName.equals("jop.nce"))
            return ""
                    + "enabled:false ";

        if (phaseName.equals("jop.uce1"))
            return ""
                    + "enabled:true "
                    + "remove-unreachable-traps:false ";

        if (phaseName.equals("jop.ubf1"))
            return ""
                    + "enabled:true ";

        if (phaseName.equals("jop.uce2"))
            return ""
                    + "enabled:true "
                    + "remove-unreachable-traps:false ";

        if (phaseName.equals("jop.ubf2"))
            return ""
                    + "enabled:true ";

        if (phaseName.equals("jop.ule"))
            return ""
                    + "enabled:true ";

        if (phaseName.equals("jap"))
            return ""
                    + "enabled:true ";

        if (phaseName.equals("jap.npc"))
            return ""
                    + "enabled:false "
                    + "only-array-ref:false "
                    + "profiling:false ";

        if (phaseName.equals("jap.npcolorer"))
            return ""
                    + "enabled:false ";

        if (phaseName.equals("jap.abc"))
            return ""
                    + "enabled:false "
                    + "with-all:false "
                    + "with-cse:false "
                    + "with-arrayref:false "
                    + "with-fieldref:false "
                    + "with-classfield:false "
                    + "with-rectarray:false "
                    + "profiling:false "
                    + "add-color-tags:false ";

        if (phaseName.equals("jap.profiling"))
            return ""
                    + "enabled:false "
                    + "notmainentry:false ";

        if (phaseName.equals("jap.sea"))
            return ""
                    + "enabled:false "
                    + "naive:false ";

        if (phaseName.equals("jap.fieldrw"))
            return ""
                    + "enabled:false "
                    + "threshold:100 ";

        if (phaseName.equals("jap.cgtagger"))
            return ""
                    + "enabled:false ";

        if (phaseName.equals("jap.parity"))
            return ""
                    + "enabled:false ";

        if (phaseName.equals("jap.pat"))
            return ""
                    + "enabled:false ";

        if (phaseName.equals("jap.lvtagger"))
            return ""
                    + "enabled:false ";

        if (phaseName.equals("jap.rdtagger"))
            return ""
                    + "enabled:false ";

        if (phaseName.equals("jap.che"))
            return ""
                    + "enabled:false ";

        if (phaseName.equals("jap.umt"))
            return ""
                    + "enabled:false ";

        if (phaseName.equals("jap.lit"))
            return ""
                    + "enabled:false ";

        if (phaseName.equals("jap.aet"))
            return ""
                    + "enabled:false "
                    + "kind:optimistic ";

        if (phaseName.equals("jap.dmt"))
            return ""
                    + "enabled:false ";

        if (phaseName.equals("gb"))
            return ""
                    + "enabled:true ";

        if (phaseName.equals("gb.a1"))
            return ""
                    + "enabled:true "
                    + "only-stack-locals:true ";

        if (phaseName.equals("gb.cf"))
            return ""
                    + "enabled:true ";

        if (phaseName.equals("gb.a2"))
            return ""
                    + "enabled:true "
                    + "only-stack-locals:true ";

        if (phaseName.equals("gb.ule"))
            return ""
                    + "enabled:true ";

        if (phaseName.equals("gop"))
            return ""
                    + "enabled:false ";

        if (phaseName.equals("bb"))
            return ""
                    + "enabled:true ";

        if (phaseName.equals("bb.lso"))
            return ""
                    + "enabled:true "
                    + "debug:false "
                    + "inter:false "
                    + "sl:true "
                    + "sl2:false "
                    + "sll:true "
                    + "sll2:false ";

        if (phaseName.equals("bb.sco"))
            return ""
                    + "enabled:true ";

        if (phaseName.equals("bb.pho"))
            return ""
                    + "enabled:true ";

        if (phaseName.equals("bb.ule"))
            return ""
                    + "enabled:true ";

        if (phaseName.equals("bb.lp"))
            return ""
                    + "enabled:true "
                    + "unsplit-original-locals:false ";

        if (phaseName.equals("bb.ne"))
            return ""
                    + "enabled:true ";

        if (phaseName.equals("bop"))
            return ""
                    + "enabled:false ";

        if (phaseName.equals("tag"))
            return ""
                    + "enabled:true ";

        if (phaseName.equals("tag.ln"))
            return ""
                    + "enabled:true ";

        if (phaseName.equals("tag.an"))
            return ""
                    + "enabled:false ";

        if (phaseName.equals("tag.dep"))
            return ""
                    + "enabled:false ";

        if (phaseName.equals("tag.fieldrw"))
            return ""
                    + "enabled:false ";

        if (phaseName.equals("db"))
            return ""
                    + "enabled:true "
                    + "source-is-javac:true ";

        if (phaseName.equals("db.transformations"))
            return ""
                    + "enabled:true ";

        if (phaseName.equals("db.renamer"))
            return ""
                    + "enabled:false ";

        if (phaseName.equals("db.deobfuscate"))
            return ""
                    + "enabled:true ";

        if (phaseName.equals("db.force-recompile"))
            return ""
                    + "enabled:true ";

        // The default default value is enabled.
        return "enabled";
    }

    public void warnForeignPhase(String phaseName) {
        if (false
                || phaseName.equals("jb")
                || phaseName.equals("jb.dtr")
                || phaseName.equals("jb.ese")
                || phaseName.equals("jb.ls")
                || phaseName.equals("jb.sils")
                || phaseName.equals("jb.a")
                || phaseName.equals("jb.ule")
                || phaseName.equals("jb.tr")
                || phaseName.equals("jb.ulp")
                || phaseName.equals("jb.lns")
                || phaseName.equals("jb.cp")
                || phaseName.equals("jb.dae")
                || phaseName.equals("jb.cp-ule")
                || phaseName.equals("jb.lp")
                || phaseName.equals("jb.ne")
                || phaseName.equals("jb.uce")
                || phaseName.equals("jb.tt")
                || phaseName.equals("jb.cbf")
                || phaseName.equals("jj")
                || phaseName.equals("jj.ls")
                || phaseName.equals("jj.sils")
                || phaseName.equals("jj.a")
                || phaseName.equals("jj.ule")
                || phaseName.equals("jj.tr")
                || phaseName.equals("jj.ulp")
                || phaseName.equals("jj.lns")
                || phaseName.equals("jj.cp")
                || phaseName.equals("jj.dae")
                || phaseName.equals("jj.cp-ule")
                || phaseName.equals("jj.lp")
                || phaseName.equals("jj.ne")
                || phaseName.equals("jj.uce")
                || phaseName.equals("wjpp")
                || phaseName.equals("wjpp.cimbt")
                || phaseName.equals("wspp")
                || phaseName.equals("cg")
                || phaseName.equals("cg.cha")
                || phaseName.equals("cg.spark")
                || phaseName.equals("cg.paddle")
                || phaseName.equals("wstp")
                || phaseName.equals("wsop")
                || phaseName.equals("wjtp")
                || phaseName.equals("wjtp.mhp")
                || phaseName.equals("wjtp.tn")
                || phaseName.equals("wjtp.rdc")
                || phaseName.equals("wjop")
                || phaseName.equals("wjop.smb")
                || phaseName.equals("wjop.si")
                || phaseName.equals("wjap")
                || phaseName.equals("wjap.ra")
                || phaseName.equals("wjap.umt")
                || phaseName.equals("wjap.uft")
                || phaseName.equals("wjap.tqt")
                || phaseName.equals("wjap.cgg")
                || phaseName.equals("wjap.purity")
                || phaseName.equals("shimple")
                || phaseName.equals("stp")
                || phaseName.equals("sop")
                || phaseName.equals("sop.cpf")
                || phaseName.equals("jtp")
                || phaseName.equals("jop")
                || phaseName.equals("jop.cse")
                || phaseName.equals("jop.bcm")
                || phaseName.equals("jop.lcm")
                || phaseName.equals("jop.cp")
                || phaseName.equals("jop.cpf")
                || phaseName.equals("jop.cbf")
                || phaseName.equals("jop.dae")
                || phaseName.equals("jop.nce")
                || phaseName.equals("jop.uce1")
                || phaseName.equals("jop.ubf1")
                || phaseName.equals("jop.uce2")
                || phaseName.equals("jop.ubf2")
                || phaseName.equals("jop.ule")
                || phaseName.equals("jap")
                || phaseName.equals("jap.npc")
                || phaseName.equals("jap.npcolorer")
                || phaseName.equals("jap.abc")
                || phaseName.equals("jap.profiling")
                || phaseName.equals("jap.sea")
                || phaseName.equals("jap.fieldrw")
                || phaseName.equals("jap.cgtagger")
                || phaseName.equals("jap.parity")
                || phaseName.equals("jap.pat")
                || phaseName.equals("jap.lvtagger")
                || phaseName.equals("jap.rdtagger")
                || phaseName.equals("jap.che")
                || phaseName.equals("jap.umt")
                || phaseName.equals("jap.lit")
                || phaseName.equals("jap.aet")
                || phaseName.equals("jap.dmt")
                || phaseName.equals("gb")
                || phaseName.equals("gb.a1")
                || phaseName.equals("gb.cf")
                || phaseName.equals("gb.a2")
                || phaseName.equals("gb.ule")
                || phaseName.equals("gop")
                || phaseName.equals("bb")
                || phaseName.equals("bb.lso")
                || phaseName.equals("bb.sco")
                || phaseName.equals("bb.pho")
                || phaseName.equals("bb.ule")
                || phaseName.equals("bb.lp")
                || phaseName.equals("bb.ne")
                || phaseName.equals("bop")
                || phaseName.equals("tag")
                || phaseName.equals("tag.ln")
                || phaseName.equals("tag.an")
                || phaseName.equals("tag.dep")
                || phaseName.equals("tag.fieldrw")
                || phaseName.equals("db")
                || phaseName.equals("db.transformations")
                || phaseName.equals("db.renamer")
                || phaseName.equals("db.deobfuscate")
                || phaseName.equals("db.force-recompile")
        ) return;

        G.v().out.println("Warning: Phase " + phaseName + " is not a standard Soot phase listed in XML files.");
    }

    public void warnNonexistentPhase() {
        if (!PackManager.v().hasPhase("jb"))
            G.v().out.println("Warning: Options exist for non-existent phase jb");
        if (!PackManager.v().hasPhase("jb.dtr"))
            G.v().out.println("Warning: Options exist for non-existent phase jb.dtr");
        if (!PackManager.v().hasPhase("jb.ese"))
            G.v().out.println("Warning: Options exist for non-existent phase jb.ese");
        if (!PackManager.v().hasPhase("jb.ls"))
            G.v().out.println("Warning: Options exist for non-existent phase jb.ls");
        if (!PackManager.v().hasPhase("jb.sils"))
            G.v().out.println("Warning: Options exist for non-existent phase jb.sils");
        if (!PackManager.v().hasPhase("jb.a"))
            G.v().out.println("Warning: Options exist for non-existent phase jb.a");
        if (!PackManager.v().hasPhase("jb.ule"))
            G.v().out.println("Warning: Options exist for non-existent phase jb.ule");
        if (!PackManager.v().hasPhase("jb.tr"))
            G.v().out.println("Warning: Options exist for non-existent phase jb.tr");
        if (!PackManager.v().hasPhase("jb.ulp"))
            G.v().out.println("Warning: Options exist for non-existent phase jb.ulp");
        if (!PackManager.v().hasPhase("jb.lns"))
            G.v().out.println("Warning: Options exist for non-existent phase jb.lns");
        if (!PackManager.v().hasPhase("jb.cp"))
            G.v().out.println("Warning: Options exist for non-existent phase jb.cp");
        if (!PackManager.v().hasPhase("jb.dae"))
            G.v().out.println("Warning: Options exist for non-existent phase jb.dae");
        if (!PackManager.v().hasPhase("jb.cp-ule"))
            G.v().out.println("Warning: Options exist for non-existent phase jb.cp-ule");
        if (!PackManager.v().hasPhase("jb.lp"))
            G.v().out.println("Warning: Options exist for non-existent phase jb.lp");
        if (!PackManager.v().hasPhase("jb.ne"))
            G.v().out.println("Warning: Options exist for non-existent phase jb.ne");
        if (!PackManager.v().hasPhase("jb.uce"))
            G.v().out.println("Warning: Options exist for non-existent phase jb.uce");
        if (!PackManager.v().hasPhase("jb.tt"))
            G.v().out.println("Warning: Options exist for non-existent phase jb.tt");
        if (!PackManager.v().hasPhase("jb.cbf"))
            G.v().out.println("Warning: Options exist for non-existent phase jb.cbf");
        if (!PackManager.v().hasPhase("jj"))
            G.v().out.println("Warning: Options exist for non-existent phase jj");
        if (!PackManager.v().hasPhase("jj.ls"))
            G.v().out.println("Warning: Options exist for non-existent phase jj.ls");
        if (!PackManager.v().hasPhase("jj.sils"))
            G.v().out.println("Warning: Options exist for non-existent phase jj.sils");
        if (!PackManager.v().hasPhase("jj.a"))
            G.v().out.println("Warning: Options exist for non-existent phase jj.a");
        if (!PackManager.v().hasPhase("jj.ule"))
            G.v().out.println("Warning: Options exist for non-existent phase jj.ule");
        if (!PackManager.v().hasPhase("jj.tr"))
            G.v().out.println("Warning: Options exist for non-existent phase jj.tr");
        if (!PackManager.v().hasPhase("jj.ulp"))
            G.v().out.println("Warning: Options exist for non-existent phase jj.ulp");
        if (!PackManager.v().hasPhase("jj.lns"))
            G.v().out.println("Warning: Options exist for non-existent phase jj.lns");
        if (!PackManager.v().hasPhase("jj.cp"))
            G.v().out.println("Warning: Options exist for non-existent phase jj.cp");
        if (!PackManager.v().hasPhase("jj.dae"))
            G.v().out.println("Warning: Options exist for non-existent phase jj.dae");
        if (!PackManager.v().hasPhase("jj.cp-ule"))
            G.v().out.println("Warning: Options exist for non-existent phase jj.cp-ule");
        if (!PackManager.v().hasPhase("jj.lp"))
            G.v().out.println("Warning: Options exist for non-existent phase jj.lp");
        if (!PackManager.v().hasPhase("jj.ne"))
            G.v().out.println("Warning: Options exist for non-existent phase jj.ne");
        if (!PackManager.v().hasPhase("jj.uce"))
            G.v().out.println("Warning: Options exist for non-existent phase jj.uce");
        if (!PackManager.v().hasPhase("wjpp"))
            G.v().out.println("Warning: Options exist for non-existent phase wjpp");
        if (!PackManager.v().hasPhase("wjpp.cimbt"))
            G.v().out.println("Warning: Options exist for non-existent phase wjpp.cimbt");
        if (!PackManager.v().hasPhase("wspp"))
            G.v().out.println("Warning: Options exist for non-existent phase wspp");
        if (!PackManager.v().hasPhase("cg"))
            G.v().out.println("Warning: Options exist for non-existent phase cg");
        if (!PackManager.v().hasPhase("cg.cha"))
            G.v().out.println("Warning: Options exist for non-existent phase cg.cha");
        if (!PackManager.v().hasPhase("cg.spark"))
            G.v().out.println("Warning: Options exist for non-existent phase cg.spark");
        if (!PackManager.v().hasPhase("cg.paddle"))
            G.v().out.println("Warning: Options exist for non-existent phase cg.paddle");
        if (!PackManager.v().hasPhase("wstp"))
            G.v().out.println("Warning: Options exist for non-existent phase wstp");
        if (!PackManager.v().hasPhase("wsop"))
            G.v().out.println("Warning: Options exist for non-existent phase wsop");
        if (!PackManager.v().hasPhase("wjtp"))
            G.v().out.println("Warning: Options exist for non-existent phase wjtp");
        if (!PackManager.v().hasPhase("wjtp.mhp"))
            G.v().out.println("Warning: Options exist for non-existent phase wjtp.mhp");
        if (!PackManager.v().hasPhase("wjtp.tn"))
            G.v().out.println("Warning: Options exist for non-existent phase wjtp.tn");
        if (!PackManager.v().hasPhase("wjtp.rdc"))
            G.v().out.println("Warning: Options exist for non-existent phase wjtp.rdc");
        if (!PackManager.v().hasPhase("wjop"))
            G.v().out.println("Warning: Options exist for non-existent phase wjop");
        if (!PackManager.v().hasPhase("wjop.smb"))
            G.v().out.println("Warning: Options exist for non-existent phase wjop.smb");
        if (!PackManager.v().hasPhase("wjop.si"))
            G.v().out.println("Warning: Options exist for non-existent phase wjop.si");
        if (!PackManager.v().hasPhase("wjap"))
            G.v().out.println("Warning: Options exist for non-existent phase wjap");
        if (!PackManager.v().hasPhase("wjap.ra"))
            G.v().out.println("Warning: Options exist for non-existent phase wjap.ra");
        if (!PackManager.v().hasPhase("wjap.umt"))
            G.v().out.println("Warning: Options exist for non-existent phase wjap.umt");
        if (!PackManager.v().hasPhase("wjap.uft"))
            G.v().out.println("Warning: Options exist for non-existent phase wjap.uft");
        if (!PackManager.v().hasPhase("wjap.tqt"))
            G.v().out.println("Warning: Options exist for non-existent phase wjap.tqt");
        if (!PackManager.v().hasPhase("wjap.cgg"))
            G.v().out.println("Warning: Options exist for non-existent phase wjap.cgg");
        if (!PackManager.v().hasPhase("wjap.purity"))
            G.v().out.println("Warning: Options exist for non-existent phase wjap.purity");
        if (!PackManager.v().hasPhase("shimple"))
            G.v().out.println("Warning: Options exist for non-existent phase shimple");
        if (!PackManager.v().hasPhase("stp"))
            G.v().out.println("Warning: Options exist for non-existent phase stp");
        if (!PackManager.v().hasPhase("sop"))
            G.v().out.println("Warning: Options exist for non-existent phase sop");
        if (!PackManager.v().hasPhase("sop.cpf"))
            G.v().out.println("Warning: Options exist for non-existent phase sop.cpf");
        if (!PackManager.v().hasPhase("jtp"))
            G.v().out.println("Warning: Options exist for non-existent phase jtp");
        if (!PackManager.v().hasPhase("jop"))
            G.v().out.println("Warning: Options exist for non-existent phase jop");
        if (!PackManager.v().hasPhase("jop.cse"))
            G.v().out.println("Warning: Options exist for non-existent phase jop.cse");
        if (!PackManager.v().hasPhase("jop.bcm"))
            G.v().out.println("Warning: Options exist for non-existent phase jop.bcm");
        if (!PackManager.v().hasPhase("jop.lcm"))
            G.v().out.println("Warning: Options exist for non-existent phase jop.lcm");
        if (!PackManager.v().hasPhase("jop.cp"))
            G.v().out.println("Warning: Options exist for non-existent phase jop.cp");
        if (!PackManager.v().hasPhase("jop.cpf"))
            G.v().out.println("Warning: Options exist for non-existent phase jop.cpf");
        if (!PackManager.v().hasPhase("jop.cbf"))
            G.v().out.println("Warning: Options exist for non-existent phase jop.cbf");
        if (!PackManager.v().hasPhase("jop.dae"))
            G.v().out.println("Warning: Options exist for non-existent phase jop.dae");
        if (!PackManager.v().hasPhase("jop.nce"))
            G.v().out.println("Warning: Options exist for non-existent phase jop.nce");
        if (!PackManager.v().hasPhase("jop.uce1"))
            G.v().out.println("Warning: Options exist for non-existent phase jop.uce1");
        if (!PackManager.v().hasPhase("jop.ubf1"))
            G.v().out.println("Warning: Options exist for non-existent phase jop.ubf1");
        if (!PackManager.v().hasPhase("jop.uce2"))
            G.v().out.println("Warning: Options exist for non-existent phase jop.uce2");
        if (!PackManager.v().hasPhase("jop.ubf2"))
            G.v().out.println("Warning: Options exist for non-existent phase jop.ubf2");
        if (!PackManager.v().hasPhase("jop.ule"))
            G.v().out.println("Warning: Options exist for non-existent phase jop.ule");
        if (!PackManager.v().hasPhase("jap"))
            G.v().out.println("Warning: Options exist for non-existent phase jap");
        if (!PackManager.v().hasPhase("jap.npc"))
            G.v().out.println("Warning: Options exist for non-existent phase jap.npc");
        if (!PackManager.v().hasPhase("jap.npcolorer"))
            G.v().out.println("Warning: Options exist for non-existent phase jap.npcolorer");
        if (!PackManager.v().hasPhase("jap.abc"))
            G.v().out.println("Warning: Options exist for non-existent phase jap.abc");
        if (!PackManager.v().hasPhase("jap.profiling"))
            G.v().out.println("Warning: Options exist for non-existent phase jap.profiling");
        if (!PackManager.v().hasPhase("jap.sea"))
            G.v().out.println("Warning: Options exist for non-existent phase jap.sea");
        if (!PackManager.v().hasPhase("jap.fieldrw"))
            G.v().out.println("Warning: Options exist for non-existent phase jap.fieldrw");
        if (!PackManager.v().hasPhase("jap.cgtagger"))
            G.v().out.println("Warning: Options exist for non-existent phase jap.cgtagger");
        if (!PackManager.v().hasPhase("jap.parity"))
            G.v().out.println("Warning: Options exist for non-existent phase jap.parity");
        if (!PackManager.v().hasPhase("jap.pat"))
            G.v().out.println("Warning: Options exist for non-existent phase jap.pat");
        if (!PackManager.v().hasPhase("jap.lvtagger"))
            G.v().out.println("Warning: Options exist for non-existent phase jap.lvtagger");
        if (!PackManager.v().hasPhase("jap.rdtagger"))
            G.v().out.println("Warning: Options exist for non-existent phase jap.rdtagger");
        if (!PackManager.v().hasPhase("jap.che"))
            G.v().out.println("Warning: Options exist for non-existent phase jap.che");
        if (!PackManager.v().hasPhase("jap.umt"))
            G.v().out.println("Warning: Options exist for non-existent phase jap.umt");
        if (!PackManager.v().hasPhase("jap.lit"))
            G.v().out.println("Warning: Options exist for non-existent phase jap.lit");
        if (!PackManager.v().hasPhase("jap.aet"))
            G.v().out.println("Warning: Options exist for non-existent phase jap.aet");
        if (!PackManager.v().hasPhase("jap.dmt"))
            G.v().out.println("Warning: Options exist for non-existent phase jap.dmt");
        if (!PackManager.v().hasPhase("gb"))
            G.v().out.println("Warning: Options exist for non-existent phase gb");
        if (!PackManager.v().hasPhase("gb.a1"))
            G.v().out.println("Warning: Options exist for non-existent phase gb.a1");
        if (!PackManager.v().hasPhase("gb.cf"))
            G.v().out.println("Warning: Options exist for non-existent phase gb.cf");
        if (!PackManager.v().hasPhase("gb.a2"))
            G.v().out.println("Warning: Options exist for non-existent phase gb.a2");
        if (!PackManager.v().hasPhase("gb.ule"))
            G.v().out.println("Warning: Options exist for non-existent phase gb.ule");
        if (!PackManager.v().hasPhase("gop"))
            G.v().out.println("Warning: Options exist for non-existent phase gop");
        if (!PackManager.v().hasPhase("bb"))
            G.v().out.println("Warning: Options exist for non-existent phase bb");
        if (!PackManager.v().hasPhase("bb.lso"))
            G.v().out.println("Warning: Options exist for non-existent phase bb.lso");
        if (!PackManager.v().hasPhase("bb.sco"))
            G.v().out.println("Warning: Options exist for non-existent phase bb.sco");
        if (!PackManager.v().hasPhase("bb.pho"))
            G.v().out.println("Warning: Options exist for non-existent phase bb.pho");
        if (!PackManager.v().hasPhase("bb.ule"))
            G.v().out.println("Warning: Options exist for non-existent phase bb.ule");
        if (!PackManager.v().hasPhase("bb.lp"))
            G.v().out.println("Warning: Options exist for non-existent phase bb.lp");
        if (!PackManager.v().hasPhase("bb.ne"))
            G.v().out.println("Warning: Options exist for non-existent phase bb.ne");
        if (!PackManager.v().hasPhase("bop"))
            G.v().out.println("Warning: Options exist for non-existent phase bop");
        if (!PackManager.v().hasPhase("tag"))
            G.v().out.println("Warning: Options exist for non-existent phase tag");
        if (!PackManager.v().hasPhase("tag.ln"))
            G.v().out.println("Warning: Options exist for non-existent phase tag.ln");
        if (!PackManager.v().hasPhase("tag.an"))
            G.v().out.println("Warning: Options exist for non-existent phase tag.an");
        if (!PackManager.v().hasPhase("tag.dep"))
            G.v().out.println("Warning: Options exist for non-existent phase tag.dep");
        if (!PackManager.v().hasPhase("tag.fieldrw"))
            G.v().out.println("Warning: Options exist for non-existent phase tag.fieldrw");
        if (!PackManager.v().hasPhase("db"))
            G.v().out.println("Warning: Options exist for non-existent phase db");
        if (!PackManager.v().hasPhase("db.transformations"))
            G.v().out.println("Warning: Options exist for non-existent phase db.transformations");
        if (!PackManager.v().hasPhase("db.renamer"))
            G.v().out.println("Warning: Options exist for non-existent phase db.renamer");
        if (!PackManager.v().hasPhase("db.deobfuscate"))
            G.v().out.println("Warning: Options exist for non-existent phase db.deobfuscate");
        if (!PackManager.v().hasPhase("db.force-recompile"))
            G.v().out.println("Warning: Options exist for non-existent phase db.force-recompile");
        }

}
