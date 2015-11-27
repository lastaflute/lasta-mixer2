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
package org.lastaflute.mixer2.view.resolver;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.lastaflute.mixer2.messages.ErrorMessages;
import org.lastaflute.mixer2.messages.ResolvedMessage;
import org.lastaflute.mixer2.view.Mixer2Supporter;
import org.lastaflute.web.LastaWebKey;
import org.lastaflute.web.ruts.message.ActionMessages;
import org.lastaflute.web.servlet.request.RequestManager;
import org.mixer2.jaxb.xhtml.Body;
import org.mixer2.jaxb.xhtml.Html;
import org.mixer2.jaxb.xhtml.Li;
import org.mixer2.jaxb.xhtml.Ul;
import org.mixer2.xhtml.AbstractJaxb;

/**
 * @author jflute
 */
public class TypicalMixErrorsResolver {

    public void resolveErrors(Html html, Mixer2Supporter supporter) {
        if (html == null) {
            throw new IllegalArgumentException("The argument 'html' should not be null.");
        }
        if (supporter == null) {
            throw new IllegalArgumentException("The supporter 'html' should not be null.");
        }
        final ErrorMessages messages = createErrorMessages(supporter.getRequestManager());
        if (messages.isEmpty()) {
            return;
        }
        // #hope label coloring
        final Body body = html.getBody(); // #pending getByData
        final List<AbstractJaxb> errorsTagList = supporter.searchTagList(body, tag -> {
            return tag.getData("errors") != null;
        });
        final Set<String> propertySet = messages.toPropertySet();
        for (AbstractJaxb errorsTag : errorsTagList) {
            final String errorsName = errorsTag.getData("errors");
            if ("all".equalsIgnoreCase(errorsName)) {
                final List<ResolvedMessage> messageList = messages.getAll();
                final Ul ul = new Ul();
                final List<Object> liList = new ArrayList<Object>();
                for (ResolvedMessage message : messageList) {
                    final Li li = new Li();
                    li.replaceInner(message.getMessage());
                    liList.add(li);
                }
                ul.replaceInner(liList); // #hope List<? extends Object>
                errorsTag.replaceInner(ul);
            } else {
                final String propertyName = toPropertyName(errorsName);
                if (propertySet.contains(propertyName)) {
                    final List<ResolvedMessage> messageList = messages.part(propertyName);
                    final String joinedMessage = messageList.stream().map(message -> {
                        return message.getMessage();
                    }).collect(Collectors.joining(", ")); // #thinking use any tag?
                    errorsTag.replaceInner(joinedMessage);
                }
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

    protected String toPropertyName(String property) {
        return ActionMessages.GLOBAL_PROPERTY_KEY.equals(property) ? "global" : property;
    }
}
