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
package org.lastaflute.mixer2.view;

import java.util.regex.Pattern;

import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.optional.OptionalThing;
import org.lastaflute.mixer2.exception.Mixer2GetByIDFailureException;
import org.lastaflute.mixer2.exception.Mixer2GetByIDNotFoundException;
import org.lastaflute.mixer2.exception.Mixer2ReplaceByIDFailureException;
import org.lastaflute.mixer2.exception.Mixer2ReplaceByIDNotFoundException;
import org.lastaflute.mixer2.template.Mixer2TemplateReader;
import org.lastaflute.web.servlet.request.RequestManager;
import org.mixer2.Mixer2Engine;
import org.mixer2.jaxb.xhtml.Html;
import org.mixer2.jaxb.xhtml.Input;
import org.mixer2.xhtml.AbstractJaxb;
import org.mixer2.xhtml.PathAdjuster;

/**
 * @author jflute
 */
public class Mixer2Supporter {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final Mixer2Engine engine;
    protected final RequestManager requestManager;
    protected final Mixer2TemplateReader templateReader;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public Mixer2Supporter(Mixer2Engine engine, RequestManager requestManager, Mixer2TemplateReader templateReader) {
        assertObjectNotNull("engine", engine);
        assertObjectNotNull("requestManager", requestManager);
        assertObjectNotNull("templateReader", templateReader);
        this.engine = engine;
        this.requestManager = requestManager;
        this.templateReader = templateReader;
    }

    // ===================================================================================
    //                                                                            Find Tag
    //                                                                            ========
    public <TAG extends AbstractJaxb> OptionalThing<TAG> findById(AbstractJaxb baseTag, String id, Class<TAG> tagType) {
        assertObjectNotNull("baseTag", baseTag);
        assertObjectNotNull("id", id);
        assertObjectNotNull("tagType", tagType);
        final TAG found;
        try {
            found = baseTag.getById(id, tagType);
        } catch (RuntimeException e) {
            throwMixer2GetByIDFailureException(baseTag, id, tagType, e);
            return null; // unreachable
        }
        return OptionalThing.ofNullable(found, () -> throwMixer2GetByIDNotFoundException(baseTag, id, tagType));
    }

    protected <TAG extends AbstractJaxb> void throwMixer2GetByIDFailureException(AbstractJaxb baseTag, String id, Class<TAG> tagType,
            RuntimeException cause) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Failed to get by the ID.");
        br.addItem("Failure ID");
        br.addElement(id);
        br.addItem("Tag Type");
        br.addElement(tagType);
        br.addItem("Base Tag");
        br.addElement(baseTag);
        final String msg = br.buildExceptionMessage();
        throw new Mixer2GetByIDFailureException(msg, cause);
    }

    protected <TAG extends AbstractJaxb> void throwMixer2GetByIDNotFoundException(AbstractJaxb baseTag, String id, Class<TAG> tagType) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found the ID in the tag by getById().");
        br.addItem("NotFound ID");
        br.addElement(id);
        br.addItem("Tag Type");
        br.addElement(tagType);
        br.addItem("Base Tag");
        br.addElement(baseTag);
        final String msg = br.buildExceptionMessage();
        throw new Mixer2GetByIDNotFoundException(msg);
    }

    @SuppressWarnings("unchecked")
    public <TAG extends AbstractJaxb> OptionalThing<TAG> findInput(AbstractJaxb baseTag, String name) {
        return (OptionalThing<TAG>) findById(baseTag, name, Input.class); // #hope want to find by name
    }

    // ===================================================================================
    //                                                                         Replace Tag
    //                                                                         ===========
    public void replaceById(AbstractJaxb baseTag, String id, AbstractJaxb replacememt) {
        assertObjectNotNull("baseTag", baseTag);
        assertObjectNotNull("id", id);
        assertObjectNotNull("replacememt", replacememt);
        boolean replaced;
        try {
            replaced = baseTag.replaceById(id, replacememt);
        } catch (RuntimeException e) {
            throwMixer2ReplaceByIDFailureException(baseTag, id, replacememt, e);
            return; // unreachable
        }
        if (!replaced) {
            throwMixer2ReplaceByIDNotFoundException(baseTag, id, replacememt);
        }
    }

    protected <TAG extends AbstractJaxb> void throwMixer2ReplaceByIDFailureException(AbstractJaxb baseTag, String id,
            AbstractJaxb replacememt, RuntimeException cause) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Failed to replace by the ID.");
        br.addItem("Failure ID");
        br.addElement(id);
        br.addItem("Relacement");
        br.addElement(replacememt);
        br.addItem("Base Tag");
        br.addElement(baseTag);
        final String msg = br.buildExceptionMessage();
        throw new Mixer2ReplaceByIDFailureException(msg, cause);
    }

    protected <TAG extends AbstractJaxb> void throwMixer2ReplaceByIDNotFoundException(AbstractJaxb baseTag, String id,
            AbstractJaxb replacememt) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found the ID in the tag by replaceById().");
        br.addItem("NotFound ID");
        br.addElement(id);
        br.addItem("Relacement");
        br.addElement(replacememt);
        br.addItem("Base Tag");
        br.addElement(baseTag);
        final String msg = br.buildExceptionMessage();
        throw new Mixer2ReplaceByIDNotFoundException(msg);
    }

    // ===================================================================================
    //                                                                    Resolve URL Link
    //                                                                    ================
    public void resolveUrlLink(AbstractJaxb tag) {
        assertObjectNotNull("tag", tag);
        PathAdjuster.replacePath(tag, Pattern.compile("@\\{/"), requestManager.getContextPath() + "/");
        PathAdjuster.replacePath(tag, Pattern.compile("}$"), ""); // e.g. @{/sea/land/} => /harbor/sea/land/
    }

    // ===================================================================================
    //                                                                          Load Parts
    //                                                                          ==========
    public OptionalThing<Html> loadPartsHtml(String path) {
        assertObjectNotNull("path", path);
        return templateReader.loadHtml(path).map(loaded -> loaded.getHtml());
    }

    // ===================================================================================
    //                                                                      General Helper
    //                                                                      ==============
    protected void assertObjectNotNull(String variableName, Object value) {
        if (variableName == null) {
            throw new IllegalArgumentException("The argument 'variableName' should not be null.");
        }
        if (value == null) {
            throw new IllegalArgumentException("The argument '" + variableName + "' should not be null.");
        }
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public Mixer2Engine getEngine() {
        return engine;
    }

    public RequestManager getRequestManager() {
        return requestManager;
    }
}
