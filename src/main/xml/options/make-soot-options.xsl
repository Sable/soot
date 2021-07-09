<?xml version="1.0"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="text" indent="no" encoding="UTF-8"/>
    <xsl:strip-space elements="*"/>

    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

    <!--perform normalize-space for text in node-->
    <xsl:template match="long_desc/text()|short_desc/text()">
        <xsl:value-of select="normalize-space(.)"/>
    </xsl:template>

    <!-- Adding spaces for descriptions parts -->
    <xsl:template match="long_desc|short_desc">
        <xsl:for-each select="node()">
            <xsl:apply-templates select="."/>
            <xsl:if test="position() != last()">
                <xsl:text> </xsl:text>
            </xsl:if>
        </xsl:for-each>
    </xsl:template>

    <!--*************************************************************************-->
    <!--* ROOT TEMPLATE *********************************************************-->
    <!--*************************************************************************-->

    <xsl:template match="/options">package soot.options;

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
 * &lt;http://www.gnu.org/licenses/lgpl-2.1.html&gt;.
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
@javax.annotation.Generated(value = "<xsl:copy-of select="system-property('xsl:vendor')"/> v<xsl:copy-of select="system-property('xsl:version')"/>", comments = "from <xsl:value-of select="tokenize(base-uri(), '/')[last()]"/>")
public class Options extends OptionsBase {

    public Options(Singletons.Global g) {
    }

    public static Options v() {
        return G.v().soot_options_Options();
    }<xsl:text>
</xsl:text>
        <xsl:apply-templates mode="constants" select="/options/section"/>

    @SuppressWarnings("unused")
    public boolean parse(String[] argv) {
        List&lt;String&gt; phaseOptions = new LinkedList&lt;&gt;();

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

            if (false);<xsl:text/>

        <xsl:apply-templates mode="parse" select="/options/section"/>
            else {
                G.v().out.println("Invalid option -" + option);
                return false;
            }
        }

        Iterator&lt;String&gt; it = phaseOptions.iterator();
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
<xsl:apply-templates mode="vars" select="/options/section"/>
    public String getUsage() {
        return ""<xsl:text/>
<xsl:apply-templates mode="usage" select="/options/section"/>;
    }

<xsl:apply-templates mode="phaselist" select="/options/section/phaseopt"/>
<xsl:apply-templates mode="declphaseopts" select="/options/section/phaseopt"/>
<xsl:apply-templates mode="warnforeign" select="/options/section/phaseopt"/>
}
<xsl:apply-templates mode="phaseopts" select="/options/section/phaseopt"/>
</xsl:template><!-- match="/options" -->

<!--*************************************************************************-->
<!--* PARSE TEMPLATES *******************************************************-->
<!--*************************************************************************-->

  <xsl:template mode="parse" match="section">
      <xsl:apply-templates mode="parse" select="boolopt|multiopt|listopt|phaseopt|intopt|stropt|macroopt"/>
  </xsl:template>

<!--* BOOLEAN_OPTION *******************************************************-->
    <xsl:template mode="parse" match="boolopt">
            else if (false<xsl:text/>
        <xsl:choose>
            <xsl:when test="default='true'">
                <xsl:for-each select="alias">
                    || option.equals("no-<xsl:value-of select="."/>")<xsl:text/>
                </xsl:for-each>
            )
                <xsl:value-of select="translate(alias[last()],'-. ','___')"/> = false;<xsl:text/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:for-each select="alias">
                    || option.equals("<xsl:value-of select="."/>")<xsl:text/>
                </xsl:for-each>
            )
                <xsl:value-of select="translate(alias[last()],'-. ','___')"/> = true;<xsl:text/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!--* MULTI_OPTION *******************************************************-->
    <xsl:template mode="parse" match="multiopt">
            else if (false<xsl:text/>
        <xsl:for-each select="alias">
                    || option.equals("<xsl:value-of select="."/>")<xsl:text/>
        </xsl:for-each>
            ) {
                if (!hasMoreOptions()) {
                    G.v().out.println("No value given for option -" + option);
                    return false;
                }

                String value = nextOption();
        <xsl:variable name="name" select="translate(alias[last()],'-. ','___')"/>
                if (false);<xsl:text/>
        <xsl:for-each select="value">
                else if (false<xsl:text/>
            <xsl:for-each select="alias">
                        || value.equals("<xsl:value-of select="."/>")<xsl:text/>
            </xsl:for-each>
                ) {
                    if (<xsl:copy-of select="$name"/> != 0 &#38;&#38; <xsl:copy-of select="$name"/> != <xsl:copy-of select="$name"/>_<xsl:value-of select="translate(alias[last()],'-. ','___')"/>) {
                        G.v().out.println("Multiple values given for option " + option);
                        return false;
                    }
                    <xsl:copy-of select="$name"/> = <xsl:copy-of select="$name"/>_<xsl:value-of select="translate(alias[last()],'-. ','___')"/>;
                }<xsl:text/>
        </xsl:for-each>
                else {
                    G.v().out.println(String.format("Invalid value %s given for option -%s", value, option));
                    return false;
                }
            }<xsl:text/>
    </xsl:template>

    <!--* PATH_OPTION *******************************************************-->
    <xsl:template mode="parse" match="listopt">
            else if (false<xsl:text/>
        <xsl:for-each select="alias">
                    || option.equals("<xsl:value-of select="."/>")<xsl:text/>
        </xsl:for-each>
            ) {
                if (!hasMoreOptions()) {
                    G.v().out.println("No value given for option -" + option);
                    return false;
                }

                String value = nextOption();<xsl:text/>
        <xsl:variable name="name" select="translate(alias[last()],'-. ','___')"/>
                if (<xsl:copy-of select="$name"/> == null)
                    <xsl:copy-of select="$name"/> = new LinkedList&lt;&gt;();
                <xsl:copy-of select="$name"/>.add(value);<xsl:text/>
        <xsl:if test="'plugin' = $name">
                if (!loadPluginConfiguration(value)) {
                    G.v().out.println("Failed to load plugin " + value);
                    return false;
                }
        </xsl:if>
            }<xsl:text/>
    </xsl:template>

    <!--* PHASE_OPTION *******************************************************-->
    <xsl:template mode="parse" match="phaseopt">
            else if (false<xsl:text/>
        <xsl:for-each select="alias">
                || option.equals("<xsl:value-of select="."/>")<xsl:text/>
        </xsl:for-each>
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
        <xsl:variable name="name" select="translate(alias[last()],'-. ','___')"/>
                phaseOptions.add(phaseName);
                phaseOptions.add(phaseOption);
            }<xsl:text/>
    </xsl:template>

    <!--* STRING_OPTION *******************************************************-->
    <xsl:template mode="parse" match="stropt">
            else if (false<xsl:text/>
        <xsl:for-each select="alias">
                    || option.equals("<xsl:value-of select="."/>")<xsl:text/>
        </xsl:for-each>
            ) {
                if (!hasMoreOptions()) {
                    G.v().out.println("No value given for option -" + option);
                    return false;
                }

                String value = nextOption();<xsl:text/>
        <xsl:variable name="name" select="translate(alias[last()],'-. ','___')"/>
                if (<xsl:copy-of select="$name"/>.isEmpty())
                    <xsl:copy-of select="$name"/> = value;
                else {
                    G.v().out.println("Duplicate values " + <xsl:copy-of select="$name"/> + " and " + value + " for option -" + option);
                    return false;
                }
            }<xsl:text/>
    </xsl:template>

    <!--* INT_OPTION *******************************************************-->
    <xsl:template mode="parse" match="intopt">
            else if (false<xsl:text/>
        <xsl:for-each select="alias">
                || option.equals("<xsl:value-of select="."/>")<xsl:text/>
        </xsl:for-each>
            ) {
                if (!hasMoreOptions()) {
                    G.v().out.println("No value given for option -" + option);
                    return false;
                }

                String value = nextOption();<xsl:text/>
        <xsl:variable name="name" select="translate(alias[last()],'-. ','___')"/>
                if(<xsl:copy-of select="$name"/> == -1)
                    <xsl:copy-of select="$name"/> = Integer.valueOf(value);
                else {
                    G.v().out.println("Duplicate values " + <xsl:copy-of select="$name"/> + " and " + value + " for option -" + option);
                    return false;
                }
            }<xsl:text/>
    </xsl:template>

    <!--* MACRO_OPTION *******************************************************-->
    <xsl:template mode="parse" match="macroopt">
            else if (false<xsl:text/>
        <xsl:for-each select="alias">
                || option.equals("<xsl:value-of select="."/>")<xsl:text/>
        </xsl:for-each>
            ) {<xsl:text/>
        <xsl:for-each select="expansion">
            <xsl:sort select="string(position())" data-type="number" order="descending"/>
                pushOption("<xsl:value-of select="."/>");<xsl:text/>
                </xsl:for-each>
            }<xsl:text/>
    </xsl:template>

<!--*************************************************************************-->
<!--* VARS TEMPLATES ********************************************************-->
<!--*************************************************************************-->

    <xsl:template mode="vars" match="section">
        <xsl:apply-templates mode="vars" select="boolopt|multiopt|listopt|phaseopt|stropt|intopt|macroopt"/>
    </xsl:template>

    <!--* BOOLEAN_OPTION *******************************************************-->
    <xsl:template mode="vars" match="boolopt">
    public boolean <xsl:value-of select="translate(alias[last()],'-. ','___')"/>() { return <xsl:value-of select="translate(alias[last()],'-. ','___')"/>; }
    private boolean <xsl:value-of select="translate(alias[last()],'-. ','___')"/> = <xsl:choose> <xsl:when test="default"><xsl:value-of select="default"/></xsl:when><xsl:otherwise>false</xsl:otherwise></xsl:choose>;<xsl:text/>
    public void set_<xsl:value-of select="translate(alias[last()],'-. ','___')"/>(boolean setting) { <xsl:value-of select="translate(alias[last()],'-. ','___')"/> = setting; }
<xsl:text/>
    </xsl:template>

    <!--* MULTI_OPTION *******************************************************-->
    <xsl:template mode="vars" match="multiopt">
    public int <xsl:value-of select="translate(alias[last()],'-. ','___')"/>() {<xsl:text/>
    <xsl:variable name="name" select="translate(alias[last()],'-. ','___')"/><xsl:text/>
    <xsl:for-each select="value">
      <xsl:if test="default"><xsl:text/>
        if (<xsl:value-of select="$name"/> == 0) return <xsl:value-of select="$name"/>_<xsl:value-of select="translate(alias[last()],'-. ','___')"/>;<xsl:text/>
      </xsl:if>
    </xsl:for-each>
        return <xsl:value-of select="translate(alias[last()],'-. ','___')"/>; 
    }
    public void set_<xsl:value-of select="translate(alias[last()],'-. ','___')"/>(int setting) { <xsl:value-of select="translate(alias[last()],'-. ','___')"/> = setting; }
    private int <xsl:value-of select="translate(alias[last()],'-. ','___')"/> = 0;
<xsl:text/>
    </xsl:template>

    <!--* PATH_OPTION *******************************************************-->
    <xsl:template mode="vars" match="listopt">
    public List&lt;String&gt; <xsl:value-of select="translate(alias[last()],'-. ','___')"/>() {
        return <xsl:value-of select="translate(alias[last()],'-. ','___')"/> == null ? Collections.emptyList() : <xsl:value-of select="translate(alias[last()],'-. ','___')"/>;
    }
    public void set_<xsl:value-of select="translate(alias[last()],'-. ','___')"/>(List&lt;String&gt; setting) { <xsl:value-of select="translate(alias[last()],'-. ','___')"/> = setting; }
    private List&lt;String&gt; <xsl:value-of select="translate(alias[last()],'-. ','___')"/> = null;
<xsl:text/>
    </xsl:template>

    <!--* PHASE_OPTION *******************************************************-->
    <xsl:template mode="vars" match="phaseopt">
    </xsl:template>

    <!--* STRING_OPTION *******************************************************-->
    <xsl:template mode="vars" match="stropt">
    public String <xsl:value-of select="translate(alias[last()],'-. ','___')"/>() { return <xsl:value-of select="translate(alias[last()],'-. ','___')"/>; }
    public void set_<xsl:value-of select="translate(alias[last()],'-. ','___')"/>(String setting) { <xsl:value-of select="translate(alias[last()],'-. ','___')"/> = setting; }
    private String <xsl:value-of select="translate(alias[last()],'-. ','___')"/> = "";
<xsl:text/>
    </xsl:template>

    <!--* INT_OPTION *******************************************************-->
    <xsl:template mode="vars" match="intopt">
    public int <xsl:value-of select="translate(alias[last()],'-. ','___')"/>() { return <xsl:value-of select="translate(alias[last()],'-. ','___')"/>; }
    public void set_<xsl:value-of select="translate(alias[last()],'-. ','___')"/>(int setting) { <xsl:value-of select="translate(alias[last()],'-. ','___')"/> = setting; }
    private int <xsl:value-of select="translate(alias[last()],'-. ','___')"/> = -1;
<xsl:text/>
    </xsl:template>

    <!--* MACRO_OPTION *******************************************************-->
    <xsl:template mode="vars" match="macroopt">
    </xsl:template>

    <!--*************************************************************************-->
    <!--* CONSTANTS TEMPLATES ***************************************************-->
    <!--*************************************************************************-->

    <xsl:template mode="constants" match="section">
        <xsl:apply-templates mode="constants" select="multiopt"/>
    </xsl:template>

    <!--* MULTI_OPTION *******************************************************-->
    <xsl:template mode="constants" match="multiopt">
        <xsl:variable name="name" select="translate(alias[last()],'-. ','___')"/>
        <xsl:for-each select="value">
            <xsl:variable name="number">
                <xsl:number/>
            </xsl:variable>
            <xsl:for-each select="alias">
    public static final int <xsl:copy-of select="$name"/>_<xsl:value-of select="translate(.,'-. ','___')"/> = <xsl:value-of select="$number"/>;<xsl:text/>
            </xsl:for-each>
        </xsl:for-each>
    public static final int <xsl:copy-of select="$name"/>_MAX = <xsl:value-of select="count(value)"/>;<xsl:text/>
    </xsl:template>

    <!--*************************************************************************-->
    <!--* USAGE TEMPLATES *******************************************************-->
    <!--*************************************************************************-->

    <xsl:template mode="usage" match="section">
                + "\n<xsl:value-of select="name"/><xsl:text>:\n"</xsl:text>
        <xsl:apply-templates mode="usage" select="boolopt|multiopt|listopt|phaseopt|stropt|macroopt"/>
    </xsl:template>

    <!--* BOOLEAN_OPTION *******************************************************-->
    <xsl:template mode="usage" match="boolopt">
                + padOpt("<xsl:text>-</xsl:text><xsl:value-of select="alias" separator=", -"/><xsl:text>", "</xsl:text>
        <xsl:apply-templates select="short_desc"/>
        <xsl:text>")</xsl:text>
    </xsl:template>

    <!--* MULTI_OPTION *******************************************************-->
    <xsl:template mode="usage" match="multiopt">
                + padOpt("<xsl:text/>
        <xsl:for-each select="alias">
            <xsl:text>-</xsl:text>
            <xsl:value-of select="."/>
            <xsl:text> </xsl:text>
            <xsl:call-template name="arg-label"/>
            <xsl:if test="position() != last()">
                <xsl:text> </xsl:text>
            </xsl:if>
        </xsl:for-each>
        <xsl:text>", "</xsl:text>
        <xsl:apply-templates select="short_desc"/>
        <xsl:text>")</xsl:text>
        <xsl:for-each select="value">
                    + padVal("<xsl:value-of select="alias" separator=" "/>
            <xsl:if test="default">
                <xsl:text> (default)</xsl:text>
            </xsl:if>
            <xsl:text>", "</xsl:text>
            <xsl:apply-templates select="short_desc"/>
            <xsl:text>")</xsl:text>
        </xsl:for-each>
    </xsl:template>

    <!--* PATH_OPTION *******************************************************-->
    <xsl:template mode="usage" match="listopt">
                + padOpt("<xsl:text/>
        <xsl:for-each select="alias">
            <xsl:text>-</xsl:text>
            <xsl:value-of select="."/>
            <xsl:text> </xsl:text>
            <xsl:call-template name="arg-label"/>
            <xsl:if test="position() != last()">
                <xsl:text> </xsl:text>
            </xsl:if>
        </xsl:for-each>
        <xsl:text>", "</xsl:text>
        <xsl:apply-templates select="short_desc"/>
        <xsl:text>")</xsl:text>
    </xsl:template>

    <!--* PHASE_OPTION *******************************************************-->
    <xsl:template mode="usage" match="phaseopt">
                + padOpt("<xsl:text/>
        <xsl:for-each select="alias">
            <xsl:text>-</xsl:text>
            <xsl:value-of select="."/>
            <xsl:text> </xsl:text>
            <xsl:call-template name="arg-label"/>
            <xsl:if test="position() != last()">
                <xsl:text> </xsl:text>
            </xsl:if>
        </xsl:for-each>
        <xsl:text>", "</xsl:text>
        <xsl:apply-templates select="short_desc"/>
        <xsl:text>")</xsl:text>
    </xsl:template>

    <!--* STRING_OPTION *******************************************************-->
    <xsl:template mode="usage" match="stropt">
                + padOpt("<xsl:text/>
        <xsl:for-each select="alias">
            <xsl:text>-</xsl:text>
            <xsl:value-of select="."/>
            <xsl:text> </xsl:text>
            <xsl:call-template name="arg-label"/>
            <xsl:if test="position() != last()">
                <xsl:text> </xsl:text>
            </xsl:if>
        </xsl:for-each>
        <xsl:text>", "</xsl:text>
        <xsl:apply-templates select="short_desc"/>
        <xsl:text>")</xsl:text>
    </xsl:template>

    <!--* INT_OPTION *******************************************************-->
    <xsl:template mode="usage" match="intopt">
                + padOpt("<xsl:text/>
        <xsl:for-each select="alias">
            <xsl:text>-</xsl:text>
            <xsl:value-of select="."/>
            <xsl:text> </xsl:text>
            <xsl:call-template name="arg-label"/>
            <xsl:if test="position() != last()">
                <xsl:text> </xsl:text>
            </xsl:if>
        </xsl:for-each>
        <xsl:text>", "</xsl:text>
        <xsl:apply-templates select="short_desc"/>
        <xsl:text>")</xsl:text>
    </xsl:template>

    <!--* MACRO_OPTION *******************************************************-->
    <xsl:template mode="usage" match="macroopt">
                + padOpt("<xsl:text>-</xsl:text><xsl:value-of select="alias" separator=", -"/><xsl:text>", "</xsl:text>
        <xsl:apply-templates select="short_desc"/><xsl:text>")</xsl:text>
    </xsl:template>

<!--*************************************************************************-->
<!--* PHASE OPTION TEMPLATES ************************************************-->
<!--*************************************************************************-->

    <xsl:template mode="phaseopts" match="phaseopt">
        <xsl:for-each select="phase|radio_phase|phase/sub_phase|radio_phase/sub_phase">
            <xsl:if test="class">
                <xsl:variable name="filename" select="class"/>
                <xsl:variable name="fullname" select="concat($filename,'.java')"/>
                <xsl:variable name="fullpath" select="concat('src/main/generated/options/soot/options/',$fullname)"/>
                <xsl:result-document href="{$fullpath}">package soot.options;

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
 * &lt;http://www.gnu.org/licenses/lgpl-2.1.html&gt;.
 * #L%
 */

/* THIS FILE IS AUTO-GENERATED FROM soot_options.xml. DO NOT MODIFY. */

import java.util.*;

/** Option parser for <xsl:value-of select="name|name"/>. */
@javax.annotation.Generated(value = "<xsl:copy-of select="system-property('xsl:vendor')"/> v<xsl:copy-of select="system-property('xsl:version')"/>", comments = "from <xsl:value-of select="tokenize(base-uri(), '/')[last()]"/>")
public class <xsl:copy-of select="$filename"/><xsl:if test="extends"> extends <xsl:copy-of select="extends"/></xsl:if> {

    private Map&lt;String, String&gt; options;

    public <xsl:copy-of select="$filename"/>(Map&lt;String, String&gt; options) {
        this.options = options;
    }<xsl:text>
</xsl:text>

    <xsl:for-each select="boolopt|section/boolopt">
    /**
     * <xsl:value-of select="name"/>
       <xsl:if test="short_desc != ''"> --<xsl:call-template name="wrap-comment"><xsl:with-param name="text" select="short_desc"/></xsl:call-template>
           <xsl:if test="not(ends-with(short_desc, '.'))">.</xsl:if>
     *</xsl:if>
       <xsl:if test="long_desc != ''"><xsl:call-template name="wrap-comment"><xsl:with-param name="text" select="long_desc"/></xsl:call-template></xsl:if>
     */
    public boolean <xsl:value-of select="translate(alias[last()],'-. ','___')"/>() {
        return soot.PhaseOptions.getBoolean(options, "<xsl:value-of select="alias"/>");
    }
<xsl:text/>
    </xsl:for-each>

    <xsl:for-each select="intopt|section/intopt">
    /**
     * <xsl:value-of select="name"/>
       <xsl:if test="short_desc != ''"> --<xsl:call-template name="wrap-comment"><xsl:with-param name="text" select="short_desc"/></xsl:call-template>
            <xsl:if test="not(ends-with(short_desc, '.'))">.</xsl:if>
     *</xsl:if>
      <xsl:if test="long_desc != ''"><xsl:call-template name="wrap-comment"><xsl:with-param name="text" select="long_desc"/></xsl:call-template></xsl:if>
     */
    public int <xsl:value-of select="translate(alias[last()],'-. ','___')"/>() {
        return soot.PhaseOptions.getInt(options, "<xsl:value-of select="alias"/>");
    }
<xsl:text/>
    </xsl:for-each>

    <xsl:for-each select="flopt|section/flopt"><xsl:text/>
    /**
     * <xsl:value-of select="name"/>
       <xsl:if test="short_desc != ''"> --<xsl:call-template name="wrap-comment"><xsl:with-param name="text" select="short_desc"/></xsl:call-template>
           <xsl:if test="not(ends-with(short_desc, '.'))">.</xsl:if>
     *</xsl:if>
      <xsl:if test="long_desc != ''"><xsl:call-template name="wrap-comment"><xsl:with-param name="text" select="long_desc"/></xsl:call-template></xsl:if>
     */
    public float <xsl:value-of select="translate(alias[last()],'-. ','___')"/>() {
        return soot.PhaseOptions.getFloat(options, "<xsl:value-of select="alias"/>");
    }
<xsl:text/>
    </xsl:for-each>

    <xsl:for-each select="stropt|section/stropt"><xsl:text/>
    /**
     * <xsl:value-of select="name"/>
       <xsl:if test="short_desc != ''"> --<xsl:call-template name="wrap-comment"><xsl:with-param name="text" select="short_desc"/></xsl:call-template>
           <xsl:if test="not(ends-with(short_desc, '.'))">.</xsl:if>
     *</xsl:if>
      <xsl:if test="long_desc != ''"><xsl:call-template name="wrap-comment"><xsl:with-param name="text" select="long_desc"/></xsl:call-template></xsl:if>
     */
    public String <xsl:value-of select="translate(alias[last()],'-. ','___')"/>() {
        return soot.PhaseOptions.getString(options, "<xsl:value-of select="alias"/>");
    }
<xsl:text/>
    </xsl:for-each>

    <xsl:for-each select="multiopt|section/multiopt">
        <xsl:text/>
        <xsl:variable name="name" select="translate(alias[last()],'-. ','___')"/>
        <xsl:for-each select="value">
            <xsl:variable name="number">
                <xsl:number/>
            </xsl:variable>
        <xsl:for-each select="alias">
    public static final int <xsl:value-of select="$name"/>_<xsl:value-of select="translate(.,'-. ','___')"/> = <xsl:value-of select="$number"/>;<xsl:text/>
            </xsl:for-each>
    </xsl:for-each>

    /**
     * <xsl:value-of select="name"/>
    <xsl:if test="short_desc != ''"> --<xsl:call-template name="wrap-comment"><xsl:with-param name="text" select="short_desc"/></xsl:call-template>
        <xsl:if test="not(ends-with(short_desc, '.'))">.</xsl:if>
     *</xsl:if>
    <xsl:if test="long_desc != ''"><xsl:call-template name="wrap-comment"><xsl:with-param name="text" select="long_desc"/></xsl:call-template></xsl:if>
     */
    public int <xsl:value-of select="translate(alias[last()],'-. ','___')"/>() {
        String s = soot.PhaseOptions.getString(options, "<xsl:value-of select="alias"/>");
        if (s == null || s.isEmpty())
        	<xsl:for-each select="value"> <xsl:if test="default">return <xsl:value-of select="$name"/>_<xsl:value-of select="translate(alias[last()],'-. ','___')"/>;<xsl:text/></xsl:if></xsl:for-each>
	<xsl:text>
	</xsl:text>
    <xsl:for-each select="value">
        if (s.equalsIgnoreCase("<xsl:value-of select="alias"/>"))
            return <xsl:value-of select="$name"/>_<xsl:value-of select="translate(alias[last()],'-. ','___')"/>;<xsl:text/>
        </xsl:for-each>

        throw new RuntimeException(String.format("Invalid value %s of phase option <xsl:value-of select="alias"/>", s));
    }
<xsl:text/>
    </xsl:for-each>
}
<xsl:text/>
          </xsl:result-document>
      </xsl:if>
    </xsl:for-each>
  </xsl:template>

<!--*************************************************************************-->
<!--* PHASE LIST TEMPLATES **************************************************-->
<!--*************************************************************************-->

    <xsl:template mode="phaselist" match="phaseopt">
    public String getPhaseList() {
        return ""<xsl:text/>
        <xsl:for-each select="phase|radio_phase">
                + padOpt("<xsl:value-of select="alias"/>", "<xsl:apply-templates select="short_desc"/>")<xsl:text/>
            <xsl:for-each select="sub_phase">
                    + padVal("<xsl:value-of select="alias"/>", "<xsl:apply-templates select="short_desc"/>")<xsl:text/>
            </xsl:for-each>
        </xsl:for-each>;
    }

    public String getPhaseHelp(String phaseName) {<xsl:text/>
        <xsl:for-each select="phase|radio_phase|phase/sub_phase|radio_phase/sub_phase">
        if (phaseName.equals("<xsl:value-of select="alias|alias"/>"))
            return "Phase " + phaseName + ":\n"
                    + "<xsl:call-template name="wrap-string"><xsl:with-param name="text" select="long_desc"/></xsl:call-template>"<xsl:text/>
                    + "\n\nRecognized options (with default values):\n"<xsl:text/>
            <xsl:for-each select="boolopt|multiopt|intopt|flopt|stropt|section/boolopt|section/multiopt|section/intopt|section/flopt|section/stropt">
                    + padOpt("<xsl:value-of select="alias"/><xsl:if test="default"> (<xsl:value-of select="default"/>)</xsl:if>", "<xsl:apply-templates select="short_desc"/>")<xsl:text/>
                <xsl:if test="value">
                    <xsl:for-each select="value">
                        + padVal("<xsl:value-of select="alias"/><xsl:if test="default"> (default)</xsl:if>", "<xsl:apply-templates select="short_desc"/>")<xsl:text/>
                    </xsl:for-each>
                </xsl:if>
            </xsl:for-each>;
<xsl:text/>
        </xsl:for-each>
        return "Unrecognized phase: " + phaseName;
    }
<xsl:text/>
    </xsl:template>

<!--*************************************************************************-->
<!--* DECLARED PHASE OPTION TEMPLATES ***************************************-->
<!--*************************************************************************-->

    <xsl:template mode="declphaseopts" match="phaseopt">
    public static String getDeclaredOptionsForPhase(String phaseName) {<xsl:text/>
        <xsl:for-each select="phase|radio_phase|phase/sub_phase|radio_phase/sub_phase">
        if (phaseName.equals("<xsl:value-of select="alias|alias"/>"))
            return String.join(" ", <xsl:text/>
            <xsl:for-each select="boolopt|multiopt|intopt|flopt|stropt|section/boolopt|section/multiopt|section/intopt|section/flopt|section/stropt">
                    "<xsl:value-of select="alias"/>"<xsl:if test="position() != last()">,</xsl:if>
            </xsl:for-each>
            );
<xsl:text/>
        </xsl:for-each>
        // The default set of options is just enabled.
        return "enabled";
    }

    public static String getDefaultOptionsForPhase(String phaseName) {<xsl:text/>
        <xsl:for-each select="phase|radio_phase|phase/sub_phase|radio_phase/sub_phase">
        if (phaseName.equals("<xsl:value-of select="alias|alias"/>"))
            return ""<xsl:text/>
            <xsl:for-each select="boolopt|multiopt|intopt|flopt|stropt|section/boolopt|section/multiopt|section/intopt|section/flopt|section/stropt">
            <xsl:if test="default">
                    + "<xsl:value-of select="alias"/>:<xsl:value-of select="default"/> "<xsl:text/>
            </xsl:if>
                <xsl:variable name="key_alias" select="alias"/>
                <xsl:for-each select="value">
                    <xsl:if test="default">
                    + "<xsl:value-of select="$key_alias"/>:<xsl:value-of select="alias"/> "<xsl:text/>
                    </xsl:if>
                </xsl:for-each>
            </xsl:for-each>;
<xsl:text/>
        </xsl:for-each>
        // The default default value is enabled.
        return "enabled";
    }
<xsl:text/>
    </xsl:template>

<!--*************************************************************************-->
<!--* WARN FOREIGN TEMPLATES ************************************************-->
<!--*************************************************************************-->

    <xsl:template mode="warnforeign" match="phaseopt">
    public void warnForeignPhase(String phaseName) {<xsl:text/>
        if (false<xsl:text/>
        <xsl:for-each select="phase|radio_phase|phase/sub_phase|radio_phase/sub_phase">
                || phaseName.equals("<xsl:value-of select="alias|alias"/>")<xsl:text/>
        </xsl:for-each>
        ) return;

        G.v().out.println("Warning: Phase " + phaseName + " is not a standard Soot phase listed in XML files.");
    }

    public void warnNonexistentPhase() {<xsl:text/>
        <xsl:for-each select="phase|radio_phase|phase/sub_phase|radio_phase/sub_phase">
        if (!PackManager.v().hasPhase("<xsl:value-of select="alias|alias"/>"))
            G.v().out.println("Warning: Options exist for non-existent phase <xsl:value-of select="alias|alias"/>");<xsl:text/>
        </xsl:for-each>
        }
<xsl:text/>
  </xsl:template>



<!-- code to justify comments -->
  <xsl:template name="wrap-string">
    <xsl:param name="text"/>
    <xsl:call-template name="wrap">
      <xsl:with-param name="text" select="$text"/>
      <xsl:with-param name="newline"><xsl:text>\n</xsl:text></xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <xsl:template name="wrap-comment">
    <xsl:param name="text"/>
    <xsl:call-template name="wrap">
      <xsl:with-param name="text" select="$text"/>
      <xsl:with-param name="newline"><xsl:text>
     * </xsl:text></xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <xsl:template name="wrap">
    <xsl:param name="text"/>
    <xsl:param name="newline"/>
    <xsl:call-template name="wrap-guts">
      <xsl:with-param name="text" select="translate(normalize-space($text),'&#10;',' ')"/>
      <xsl:with-param name="width" select='0'/>
      <xsl:with-param name="newline" select="$newline"/>
    </xsl:call-template>
  </xsl:template>

  <xsl:template name="wrap-guts">
    <xsl:param name="text"/>
    <xsl:param name="width"/>
    <xsl:param name="newline"/>
    <xsl:variable name="print" select="concat(substring-before(concat($text,' '),' '),' ')"/>
    <xsl:choose>
      <xsl:when test="string-length($print) > number($width)">
        <xsl:copy-of select="$newline"/>
        <xsl:call-template name="wrap-guts">
          <xsl:with-param name="text" select="$text"/>
          <xsl:with-param name="width" select='65'/>
          <xsl:with-param name="newline" select="$newline"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:copy-of select="substring($print,1,string-length($print)-1)"/>
        <xsl:if test="contains($text,' ')">
          <xsl:if test="string-length($print) > 1">
            <xsl:text> </xsl:text>
          </xsl:if>
          <xsl:call-template name="wrap-guts">
            <xsl:with-param name="text" select="substring-after($text,' ')"/>
            <xsl:with-param name="width" select="number($width) - string-length($print)"/>
            <xsl:with-param name="newline" select="$newline"/>
          </xsl:call-template>
        </xsl:if>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

    <xsl:template match="use_arg_label">
        <xsl:call-template name="arg-label"/>
    </xsl:template>

  <!-- Factored out so it can be used to print the argument labels in
       the option summary, e.g. "-src-prec format",
       as well as argument labels in short_desc and long_desc. -->
  <xsl:template name="arg-label">
  <xsl:choose>
    <xsl:when test="ancestor::set_arg_label">
        <xsl:value-of select="translate(string(ancestor::set_arg_label),
                            'abcdefghijklmnopqrstuvwxyz',
                            'ABCDEFGHIJKLMNOPQRSTUVWXYZ')"/>
    </xsl:when>
    <xsl:otherwise>ARG</xsl:otherwise>
  </xsl:choose>
  </xsl:template>

    <xsl:template match="var">
        <xsl:value-of select="translate(string(),
                            'abcdefghijklmnopqrstuvwxyz',
                            'ABCDEFGHIJKLMNOPQRSTUVWXYZ')"/>
    </xsl:template>

    <xsl:template match="tt">
        <xsl:value-of select="string()"/>
    </xsl:template>

</xsl:stylesheet>
