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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.dbflute.util.Srl;
import org.lastaflute.mixer2.messages.ErrorMessages;
import org.lastaflute.mixer2.messages.ResolvedMessage;
import org.lastaflute.web.LastaWebKey;
import org.lastaflute.web.ruts.message.ActionMessages;
import org.lastaflute.web.servlet.request.RequestManager;
import org.mixer2.jaxb.xhtml.Body;
import org.mixer2.jaxb.xhtml.Footer;
import org.mixer2.jaxb.xhtml.Header;
import org.mixer2.jaxb.xhtml.Html;
import org.mixer2.jaxb.xhtml.Li;
import org.mixer2.jaxb.xhtml.Ul;
import org.mixer2.xhtml.AbstractJaxb;
import org.mixer2.xhtml.PathAdjuster;
import org.mixer2.xhtml.exception.TagTypeUnmatchException;

/**
 * @author jflute
 */
public abstract class TypicalMixView implements Mixer2View {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    static {
        System.setProperty("com.sun.xml.internal.bind.v2.runtime.JAXBContextImpl.fastBoot", "true");
    }

    // ===================================================================================
    //                                                                        Dynamic HTML
    //                                                                        ============
    @Override
    public void beDynamic(Html html, Mixer2Supporter supporter) throws TagTypeUnmatchException {
        adjustCssPath(html, supporter);
        adjustHeaderFooter(html, supporter);
        adjustErrors(html, supporter);
        // #pending adjust all href path
        render(html, supporter);
    }

    protected abstract void render(Html html, Mixer2Supporter supporter) throws TagTypeUnmatchException;

    // ===================================================================================
    //                                                                            CSS Path
    //                                                                            ========
    protected void adjustCssPath(Html html, Mixer2Supporter supporter) {
        final Pattern pattern = Pattern.compile("^\\.+/.*css/(.*)$");
        PathAdjuster.replacePath(html, pattern, supporter.getRequestManager().getContextPath() + "/css/$1");
    }

    // ===================================================================================
    //                                                                       Header/Footer
    //                                                                       =============
    protected void adjustHeaderFooter(Html html, Mixer2Supporter supporter) {
        // #pending jflute embedded? (2015/11/16)
        supporter.loadHtml("/common/mix_layout.html").alwaysPresent(loaded -> {
            final Header header = supporter.getById(loaded, "header", Header.class).get();
            supporter.resolveLink(header);
            supporter.replaceById(html, supporter, "header", header);
            final Footer footer = supporter.getById(loaded, "footer", Footer.class).get();
            supporter.resolveLink(footer);
            supporter.replaceById(html, supporter, "footer", footer);
        });
    }

    // ===================================================================================
    //                                                                              Errors
    //                                                                              ======
    protected void adjustErrors(Html html, Mixer2Supporter supporter) throws TagTypeUnmatchException {
        final ErrorMessages messages = createErrorMessages(supporter.getRequestManager());
        if (messages.isEmpty()) {
            return;
        }
        // #hope label coloring
        final Body body = html.getBody();
        final AbstractJaxb errorsAll = body.getById("errors-all"); // #hope Jaxb interaface (not abstract)
        if (errorsAll != null) {
            final List<ResolvedMessage> messageList = messages.getAll();
            final Ul ul = new Ul();
            final List<Object> liList = new ArrayList<Object>();
            for (ResolvedMessage message : messageList) {
                final Li li = new Li();
                li.replaceInner(message.getMessage());
                liList.add(li);
            }
            ul.replaceInner(liList); // #hope List<? extends Object>
            errorsAll.replaceInner(ul);
        } else {
            for (String property : messages.toPropertySet()) {
                final AbstractJaxb errorsProperty = body.getById("errors-" + buildPropertyIdExp(property));
                if (errorsProperty == null) {
                    continue; // #thinking should be error?
                }
                final List<ResolvedMessage> messageList = messages.part(property);
                final String joinedMessage = messageList.stream().map(message -> {
                    return message.getMessage();
                }).collect(Collectors.joining(", ")); // #thinking use any tag?
                errorsProperty.replaceInner(joinedMessage);
            }
        }
    }

    protected ErrorMessages createErrorMessages(RequestManager requestManager) {
        return new ErrorMessages(extractActionErrors(requestManager), requestManager);
    }

    protected ActionMessages extractActionErrors(RequestManager requestManager) { // from request and session
        final String attributeKey = getMessagesAttributeKey();
        final Class<ActionMessages> attributeType = ActionMessages.class;
        return requestManager.getAttribute(attributeKey, ActionMessages.class).orElseGet(() -> {
            return requestManager.getSessionManager().getAttribute(attributeKey, attributeType).orElseGet(() -> {
                return newEmptyMessages();
            });
        });
    }

    protected String getMessagesAttributeKey() {
        return LastaWebKey.ACTION_ERRORS_KEY;
    }

    protected ActionMessages newEmptyMessages() {
        return new ActionMessages();
    }

    protected String buildPropertyIdExp(String property) {
        return ActionMessages.GLOBAL_PROPERTY_KEY.equals(property) ? "global" : property;
    }

    // ===================================================================================
    //                                                                        Small Helper
    //                                                                        ============
    // -----------------------------------------------------
    //                                        Empty Handling
    //                                        --------------
    protected boolean isEmpty(String str) {
        return Srl.is_Null_or_Empty(str);
    }

    protected boolean isNotEmpty(String str) {
        return Srl.is_NotNull_and_NotEmpty(str);
    }
}
