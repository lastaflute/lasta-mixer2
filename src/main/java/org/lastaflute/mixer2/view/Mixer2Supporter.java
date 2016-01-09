/*
 * Copyright 2015-2016 the original author or authors.
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.optional.OptionalThing;
import org.lastaflute.mixer2.exception.Mixer2GetByIDFailureException;
import org.lastaflute.mixer2.exception.Mixer2GetByIDNotFoundException;
import org.lastaflute.mixer2.exception.Mixer2ReplaceByIDFailureException;
import org.lastaflute.mixer2.exception.Mixer2ReplaceByIDNotFoundException;
import org.lastaflute.mixer2.template.Mixer2TemplateReader;
import org.lastaflute.web.UrlChain;
import org.lastaflute.web.path.ActionPathResolver;
import org.lastaflute.web.servlet.request.RequestManager;
import org.mixer2.Mixer2Engine;
import org.mixer2.jaxb.xhtml.A;
import org.mixer2.jaxb.xhtml.Dl;
import org.mixer2.jaxb.xhtml.Flow;
import org.mixer2.jaxb.xhtml.Footer;
import org.mixer2.jaxb.xhtml.Form;
import org.mixer2.jaxb.xhtml.Header;
import org.mixer2.jaxb.xhtml.Html;
import org.mixer2.jaxb.xhtml.Inline;
import org.mixer2.jaxb.xhtml.Input;
import org.mixer2.jaxb.xhtml.Ol;
import org.mixer2.jaxb.xhtml.Option;
import org.mixer2.jaxb.xhtml.Script;
import org.mixer2.jaxb.xhtml.Select;
import org.mixer2.jaxb.xhtml.Tbody;
import org.mixer2.jaxb.xhtml.Td;
import org.mixer2.jaxb.xhtml.Tr;
import org.mixer2.jaxb.xhtml.Ul;
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
    protected final ActionPathResolver actionPathResolver;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public Mixer2Supporter(Mixer2Engine engine, RequestManager requestManager, Mixer2TemplateReader templateReader,
            ActionPathResolver actionPathResolver) {
        assertArgumentNotNull("engine", engine);
        assertArgumentNotNull("requestManager", requestManager);
        assertArgumentNotNull("templateReader", templateReader);
        assertArgumentNotNull("actionPathResolver", actionPathResolver);
        this.engine = engine;
        this.requestManager = requestManager;
        this.templateReader = templateReader;
        this.actionPathResolver = actionPathResolver;
    }

    // ===================================================================================
    //                                                                            Find Tag
    //                                                                            ========
    // #pending now design
    //public <TAG extends AbstractJaxb> OptionalThing<TAG> findByDataItem(AbstractJaxb baseTag, String dataKey, Class<TAG> tagType) {
    //    final List<AbstractJaxb> tagList = new ArrayList<AbstractJaxb>();
    //    collectBy(Arrays.asList(baseTag), tag -> {
    //        // #pending tagType check
    //        final String data = tag.getData("item");
    //        return data != null && data.equals(dataKey); // e.g. data-item="sea"
    //    } , tagList); // #pending performance tuning as-one
    //    if (tagList.size() > 2) { // #pending rich message
    //        throw new IllegalStateException("Duplicate name for header tag: found=" + tagList);
    //    }
    //    @SuppressWarnings("unchecked")
    //    final TAG foundTag = (TAG) (!tagList.isEmpty() ? tagList.get(0) : null);
    //    return OptionalThing.ofNullable(foundTag, () -> {
    //        // #pending rich message
    //        throw new IllegalStateException("Not found the data tag by the key: " + dataKey);
    //    });
    //}

    public <TAG extends AbstractJaxb> OptionalThing<TAG> findById(AbstractJaxb baseTag, String id, Class<TAG> tagType) {
        assertArgumentNotNull("baseTag", baseTag);
        assertArgumentNotNull("id", id);
        assertArgumentNotNull("tagType", tagType);
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

    public OptionalThing<Header> findHeader(AbstractJaxb baseTag) {
        final List<AbstractJaxb> tagList = new ArrayList<AbstractJaxb>();
        collectBy(Arrays.asList(baseTag), tag -> {
            return Header.class.isAssignableFrom(tag.getClass());
        } , tagList); // #pending performance tuning as-one
        if (tagList.size() > 2) { // #pending rich message
            throw new IllegalStateException("Duplicate name for header tag: found=" + tagList);
        }
        final Header foundHeader = (Header) (!tagList.isEmpty() ? tagList.get(0) : null);
        return OptionalThing.ofNullable(foundHeader, () -> {
            // #pending rich message
            throw new IllegalStateException("Not found the header tag in the tag: " + baseTag);
        });
    }

    public OptionalThing<Footer> findFooter(AbstractJaxb baseTag) {
        final List<AbstractJaxb> tagList = new ArrayList<AbstractJaxb>();
        collectBy(Arrays.asList(baseTag), tag -> {
            return Footer.class.isAssignableFrom(tag.getClass());
        } , tagList); // #pending performance tuning as-one
        if (tagList.size() > 2) { // #pending rich message
            throw new IllegalStateException("Duplicate name for footer tag: found=" + tagList);
        }
        final Footer foundHeader = (Footer) (!tagList.isEmpty() ? tagList.get(0) : null);
        return OptionalThing.ofNullable(foundHeader, () -> {
            // #pending rich message
            throw new IllegalStateException("Not found the footer tag in the tag: " + baseTag);
        });
    }

    public OptionalThing<Input> findInput(AbstractJaxb baseTag, String name) {
        final List<AbstractJaxb> tagList = new ArrayList<AbstractJaxb>();
        collectBy(Arrays.asList(baseTag), tag -> {
            return Input.class.isAssignableFrom(tag.getClass()) && name.equals(((Input) tag).getName());
        } , tagList); // #pending performance tuning as-one
        if (tagList.size() > 2) { // #pending rich message
            throw new IllegalStateException("Duplicate name for input tag: " + name + ", found=" + tagList);
        }
        final Input foundInput = (Input) (!tagList.isEmpty() ? tagList.get(0) : null);
        return OptionalThing.ofNullable(foundInput, () -> {
            // #pending rich message
            throw new IllegalStateException("Not found the input tag by the name: " + name);
        });
    }

    public OptionalThing<Select> findSelect(AbstractJaxb baseTag, String name) {
        final List<AbstractJaxb> tagList = new ArrayList<AbstractJaxb>();
        collectBy(Arrays.asList(baseTag), tag -> {
            return Select.class.isAssignableFrom(tag.getClass()) && name.equals(((Select) tag).getName());
        } , tagList); // #pending performance tuning as-one
        if (tagList.size() > 2) { // #pending rich message
            throw new IllegalStateException("Duplicate name for select tag: " + name + ", found=" + tagList);
        }
        final Select foundInput = (Select) (!tagList.isEmpty() ? tagList.get(0) : null);
        return OptionalThing.ofNullable(foundInput, () -> {
            // #pending rich message
            throw new IllegalStateException("Not found the select tag by the name: " + name);
        });
    }

    // ===================================================================================
    //                                                                              Search
    //                                                                              ======
    public <TAG extends AbstractJaxb> List<TAG> searchTagList(AbstractJaxb baseTag, TagDeterminer oneArgLambda) {
        final List<AbstractJaxb> tagList = new ArrayList<AbstractJaxb>();
        collectBy(Arrays.asList(baseTag), oneArgLambda, tagList);
        @SuppressWarnings("unchecked")
        final List<TAG> castList = (List<TAG>) tagList;
        return castList;
    }

    @FunctionalInterface
    public static interface TagDeterminer {

        boolean isTarget(AbstractJaxb tag);
    }

    protected void collectBy(List<? extends Object> content, TagDeterminer determiner, List<AbstractJaxb> tagList) {
        for (Object element : content) {
            if (element instanceof AbstractJaxb) {
                final AbstractJaxb tag = (AbstractJaxb) element;
                if (determiner.isTarget(tag)) {
                    tagList.add(tag);
                }
                if (element instanceof Html) {
                    final Html html = (Html) tag;
                    collectBy(Arrays.asList(html.getHead()), determiner, tagList);
                    collectBy(html.getDescendants(Header.class), determiner, tagList);
                    collectBy(html.getDescendants(Footer.class), determiner, tagList);
                    collectBy(html.getDescendants(Script.class), determiner, tagList);
                }
                if (element instanceof Flow) {
                    collectBy(((Flow) tag).getContent(), determiner, tagList);
                }
                if (element instanceof Inline) {
                    collectBy(((Inline) tag).getContent(), determiner, tagList);
                }
                if (element instanceof Form) {
                    collectBy(((Form) element).getContent(), determiner, tagList);
                }
                if (element instanceof Ul) {
                    collectBy(((Ul) element).getLi(), determiner, tagList);
                }
                if (element instanceof Ol) {
                    collectBy(((Ol) element).getLi(), determiner, tagList);
                }
                if (element instanceof Dl) {
                    collectBy(((Dl) element).getDtOrDd(), determiner, tagList);
                }
                if (element instanceof Select) {
                    collectBy(((Select) element).getOptgroupOrOption(), determiner, tagList);
                }
            }
        }
    }

    // ===================================================================================
    //                                                                        Reflect Data
    //                                                                        ============
    public <ENTITY> void reflectListToTBody(List<ENTITY> entityList, AbstractJaxb baseTag, String tbodyId,
            Consumer<TableDataResource<ENTITY>> oneArgLambda) {
        assertArgumentNotNull("baseTag", baseTag);
        assertArgumentNotNull("entityList", entityList);
        assertArgumentNotNull("tbodyId", tbodyId);
        assertArgumentNotNull("oneArgLambda", oneArgLambda);
        final Tbody tbody = findById(baseTag, tbodyId, Tbody.class).get();
        final Tr baseTr = tbody.getTr().get(0).copy(Tr.class); // #pending check out of bounds
        tbody.unsetTr();
        entityList.forEach(entity -> {
            final Tr tr = baseTr.copy(Tr.class);
            final List<Td> tdList = tr.getThOrTd().stream().map(flow -> {
                return (Td) flow; // #pending check class cast
            }).collect(Collectors.toList());
            oneArgLambda.accept(new TableDataResource<ENTITY>(tbody, tr, tdList, entity));
            tbody.getTr().add(tr);
        });
    }

    public static class TableDataResource<ENTITY> {

        protected final Tbody tbody;
        protected final Tr tr;
        protected final List<Td> tdList;
        protected final ENTITY entity;
        protected int index;

        public TableDataResource(Tbody tbody, Tr tr, List<Td> tdList, ENTITY entity) {
            this.tbody = tbody;
            this.tr = tr;
            this.tdList = tdList;
            this.entity = entity;
        }

        public void register(Object text) {
            getCurrentTd().replaceInner(text.toString()); // #pending check out of bounds
            next();
        }

        public void registerTag(AbstractJaxb tag) {
            getCurrentTd().replaceInner(tag); // #pending check out of bounds
            next();
        }

        public void registerWithInner(Consumer<TableDataInner> oneArgLambda) {
            oneArgLambda.accept(new TableDataInner(getCurrentTd(), getCurrentTd().getContent()));
            next();
        }

        public void next() {
            ++index;
        }

        public Tbody getTbody() {
            return tbody;
        }

        public Tr getTr() {
            return tr;
        }

        public Td getCurrentTd() {
            return tdList.get(index); // #pending check out of bounds
        }

        public List<Td> getTdList() {
            return tdList;
        }

        public ENTITY getEntity() {
            return entity;
        }
    }

    public static class TableDataInner {

        protected final Td currentTd;
        protected final List<Object> innerObjList;

        public TableDataInner(Td currentTd, List<Object> innerObjList) {
            this.currentTd = currentTd;
            this.innerObjList = innerObjList;
        }

        public <TAG extends AbstractJaxb> OptionalThing<TAG> findFirst(Class<TAG> tagType) {
            return OptionalThing.migratedFrom(innerObjList.stream().filter(innerObj -> {
                return tagType.isAssignableFrom(innerObj.getClass());
            }).map(obj -> {
                @SuppressWarnings("unchecked")
                final TAG tag = (TAG) obj;
                return tag;
            }).findFirst(), () -> { // #pending rich message
                throw new IllegalStateException("Not found the first tag: " + tagType);
            });
        }

        public Td getCurrentTd() {
            return currentTd;
        }

        public List<Object> getInnerObjList() {
            return innerObjList;
        }
    }

    public void reflectSelectSelected(AbstractJaxb tag, String selectTagName, String value) {
        assertArgumentNotNull("tag", tag);
        assertArgumentNotNull("selectTagName", selectTagName);
        assertArgumentNotNull("value", value);
        findSelect(tag, selectTagName).alwaysPresent(select -> {
            final List<AbstractJaxb> groupOrOptList = select.getOptgroupOrOption();
            for (AbstractJaxb groupOrOpt : groupOrOptList) {
                final Option option = (Option) groupOrOpt; // #pending support optgroup
                if (value.equals(option.getValue())) {
                    option.setSelected("selected");
                }
            }
        });
    }

    public void reflectLinkUrl(A atag, String url) {
        assertArgumentNotNull("atag", atag);
        assertArgumentNotNull("url", url);
        atag.setHref(url);
    }

    // ===================================================================================
    //                                                                         Replace Tag
    //                                                                         ===========
    public void replaceById(AbstractJaxb baseTag, String id, AbstractJaxb replacememt) {
        assertArgumentNotNull("baseTag", baseTag);
        assertArgumentNotNull("id", id);
        assertArgumentNotNull("replacememt", replacememt);
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
    //                                                                    Resolve Link URL
    //                                                                    ================
    public void resolveLinkUrl(AbstractJaxb tag) {
        assertArgumentNotNull("tag", tag);
        PathAdjuster.replacePath(tag, Pattern.compile("@\\{/"), requestManager.getContextPath() + "/");
        PathAdjuster.replacePath(tag, Pattern.compile("}$"), ""); // e.g. @{/sea/land/} => /harbor/sea/land/
    }

    // ===================================================================================
    //                                                                          Convert to
    //                                                                          ==========
    public String toLinkUrl(Class<?> actionType, UrlChain moreUrl_or_params) {
        return "@{" + actionPathResolver.toActionUrl(actionType, moreUrl_or_params) + "}";
    }

    // ===================================================================================
    //                                                                          Load Parts
    //                                                                          ==========
    public OptionalThing<Html> loadPartsHtml(String path) {
        assertArgumentNotNull("path", path);
        return templateReader.loadHtml(path).map(loaded -> loaded.getHtml());
    }

    // ===================================================================================
    //                                                                      General Helper
    //                                                                      ==============
    protected void assertArgumentNotNull(String variableName, Object value) {
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
