/*
 * Copyright 2014-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.lastaflute.mixer2.template;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.optional.OptionalThing;
import org.dbflute.util.DfResourceUtil;
import org.lastaflute.mixer2.exception.Mixer2TemplateHtmlNofFoundException;
import org.lastaflute.mixer2.exception.Mixer2TemplateHtmlParseFailureException;
import org.lastaflute.web.ruts.process.ActionRuntime;
import org.lastaflute.web.servlet.request.RequestManager;
import org.lastaflute.web.util.LaServletContextUtil;
import org.mixer2.Mixer2Engine;
import org.mixer2.jaxb.exception.Mixer2JAXBException;
import org.mixer2.jaxb.xhtml.Html;

/**
 * @author jflute
 */
public class Mixer2TemplateReader {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    protected static final String DOCTYPE_DEF = "<!DOCTYPE html>";
    protected static final String HTML_PREFIX = "<html";
    protected static final String HTML_XMLNS_PREFIX = "<html xmlns=\"http://www.w3.org/1999/xhtml\"";
    protected static final String LF = "\n";

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final Mixer2Engine engine;
    protected final RequestManager requestManager;
    protected final ActionRuntime runtime;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public Mixer2TemplateReader(Mixer2Engine engine, RequestManager requestManager, ActionRuntime runtime) {
        this.engine = engine;
        this.requestManager = requestManager;
        this.runtime = runtime;
    }

    // ===================================================================================
    //                                                                           Load Html
    //                                                                           =========
    public OptionalThing<LoadedHtml> loadHtml(String templatePath) {
        return prepareStream(requestManager, runtime, templatePath).map(ins -> {
            final ReadHtmlText htmlText = readHtmlText(ins, templatePath);
            final Html staticHtml;
            try {
                staticHtml = engine.checkAndLoadHtmlTemplate(htmlText.getReadHtml());
            } catch (Mixer2JAXBException e) {
                throwMixer2TemplateHtmlParseFailureException(runtime, templatePath, e);
                return null; // unreachable
            }
            return new LoadedHtml(staticHtml, htmlText.isDocTypeDefined(), htmlText.isXmlnsFiltered());
        });
    }

    protected ReadHtmlText readHtmlText(InputStream ins, String templatePath) {
        final String encoding = "UTF-8"; // fixedly
        final StringBuilder sb = new StringBuilder();
        boolean docTypeDefined = false;
        boolean xmlnsFiltered = false;
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(ins, encoding));
            int index = 0;
            String line;
            while (true) {
                line = br.readLine();
                if (line == null) {
                    break;
                }
                if (index == 0) {
                    line = removeInitialUnicodeBomIfNeeds(encoding, line);
                    docTypeDefined = line.equals(DOCTYPE_DEF);
                    if (!docTypeDefined) {
                        handleNonDocTypeFirstLine(templatePath, line);
                    }
                } else { // second or more lines
                    if (index == 1) {
                        if (line.startsWith(HTML_PREFIX) && line.endsWith(">")) {
                            if (line.endsWith(">") && !line.contains("xmlns")) {
                                line = line.replace(HTML_PREFIX, HTML_XMLNS_PREFIX);
                                xmlnsFiltered = true;
                            }
                        } else {
                            handleNonHtmlTagSecondLine(templatePath, line);
                        }
                    }
                    sb.append(LF); // LF fixedly
                }
                sb.append(line);
                ++index;
            }
        } catch (IOException e) {
            String msg = "Failed to read the html file: " + templatePath;
            throw new IllegalStateException(msg, e);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ignored) {}
            }
        }
        return new ReadHtmlText(sb.toString(), docTypeDefined, xmlnsFiltered);
    }

    protected String removeInitialUnicodeBomIfNeeds(String encoding, String line) {
        if ("UTF-8".equalsIgnoreCase(encoding) && line.length() > 0 && line.charAt(0) == '\uFEFF') {
            return line.substring(1);
        } else {
            return line;
        }
    }

    protected void handleNonDocTypeFirstLine(String templatePath, String line) {
        // #thinking very strict for now
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Template should have by DOCTYPE definition in first line.");
        br.addItem("Advice");
        br.addElement("Lasta Mixer2 expects DOCTYPE in first line like this:");
        br.addElement("  (x):");
        br.addElement("    <html> -- *Bad");
        br.addElement("    <head>");
        br.addElement("    ...");
        br.addElement("  (x):");
        br.addElement("    <!DOCTYPE html><html><head> // *Bad");
        br.addElement("    ...");
        br.addElement("  (o):");
        br.addElement("    <!DOCTYPE html> // Good");
        br.addElement("    <html>");
        br.addElement("    <head>");
        br.addElement("    ...");
        br.addItem("Action");
        br.addElement(runtime);
        br.addItem("Template");
        br.addElement(templatePath);
        br.addItem("First Line");
        br.addElement(line);
        final String msg = br.buildExceptionMessage();
        throw new Mixer2TemplateHtmlParseFailureException(msg);
    }

    protected void handleNonHtmlTagSecondLine(String templatePath, String line) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Template should have by HTML tag in second line.");
        br.addItem("Advice");
        br.addElement("Lasta Mixer2 expects HTML tag in second line like this:");
        br.addElement("  (x):");
        br.addElement("    <!DOCTYPE html>");
        br.addElement("    <!-- Let's dance! --> // *Bad");
        br.addElement("    <html>");
        br.addElement("    <head>");
        br.addElement("    ...");
        br.addElement("  (x):");
        br.addElement("    <!DOCTYPE html>");
        br.addElement("    <html><head> // *Bad");
        br.addElement("    ...");
        br.addElement("  (o):");
        br.addElement("    <!DOCTYPE html>");
        br.addElement("    <html> // Good");
        br.addElement("    <head>");
        br.addElement("    ...");
        br.addItem("Action");
        br.addElement(runtime);
        br.addItem("Template");
        br.addElement(templatePath);
        br.addItem("Second Line");
        br.addElement(line);
        final String msg = br.buildExceptionMessage();
        throw new Mixer2TemplateHtmlParseFailureException(msg);
    }

    protected void throwMixer2TemplateHtmlParseFailureException(ActionRuntime runtime, String templatePath, Mixer2JAXBException cause) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Failed to parse the template html.");
        br.addItem("Action");
        br.addElement(runtime);
        br.addItem("Template");
        br.addElement(templatePath);
        final String saxMsg = cause.getSAXParseExceptionMessage(); // #thinking how to get line number?
        if (saxMsg != null) {
            br.addItem("SAX Message");
            br.addElement(saxMsg);
        }
        final String msg = br.buildExceptionMessage();
        throw new Mixer2TemplateHtmlParseFailureException(msg, cause);
    }

    protected static class ReadHtmlText {

        protected final String readHtml;
        protected final boolean docTypeDefined;
        protected final boolean xmlnsFiltered;

        public ReadHtmlText(String readHtml, boolean docTypeDefined, boolean xmlnsFiltered) {
            this.readHtml = readHtml;
            this.docTypeDefined = docTypeDefined;
            this.xmlnsFiltered = xmlnsFiltered;
        }

        public String getReadHtml() {
            return readHtml;
        }

        public boolean isDocTypeDefined() {
            return docTypeDefined;
        }

        public boolean isXmlnsFiltered() {
            return xmlnsFiltered;
        }
    }

    public static class LoadedHtml {

        protected final Html html;
        protected final boolean docTypeDefined;
        protected final boolean xmlnsFiltered;

        public LoadedHtml(Html html, boolean docTypeDefined, boolean xmlnsFiltered) {
            this.html = html;
            this.docTypeDefined = docTypeDefined;
            this.xmlnsFiltered = xmlnsFiltered;
        }

        public Html getHtml() {
            return html;
        }

        public boolean isDocTypeDefined() {
            return docTypeDefined;
        }

        public boolean isXmlnsFiltered() {
            return xmlnsFiltered;
        }
    }

    // ===================================================================================
    //                                                                     Template Stream
    //                                                                     ===============
    protected OptionalThing<InputStream> prepareStream(RequestManager requestManager, ActionRuntime runtime, String templatePath) {
        final String webPath = buildWebPath(templatePath);
        InputStream ins = requestManager.getServletContext().getResourceAsStream(webPath);
        if (ins == null) {
            ins = DfResourceUtil.getResourceStream(templatePath);
        }
        return OptionalThing.ofNullable(ins, () -> {
            throwMixer2TemplateHtmlNotFoundException(runtime, templatePath, webPath);
        });
    }

    protected String buildWebPath(String routingPath) {
        return LaServletContextUtil.getHtmlViewPrefix() + routingPath;
    }

    protected void throwMixer2TemplateHtmlNotFoundException(ActionRuntime runtime, String templatePath, String webPath) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found the Mixer2 template HTML file.");
        br.addItem("Action");
        br.addElement(runtime);
        br.addItem("Template");
        br.addElement(templatePath);
        br.addItem("Web Path");
        br.addElement(webPath);
        final String msg = br.buildExceptionMessage();
        throw new Mixer2TemplateHtmlNofFoundException(msg);
    }

    // ===================================================================================
    //                                                                     HTML Definition
    //                                                                     ===============
    public String resolveHtmlDef(String htmlText, LoadedHtml loaded) {
        String resolved = htmlText;
        if (loaded.isDocTypeDefined()) {
            resolved = DOCTYPE_DEF + LF + resolved;
        }
        if (loaded.isXmlnsFiltered()) {
            resolved = resolved.replace(HTML_XMLNS_PREFIX, HTML_PREFIX);
        }
        return resolved;
    }
}
