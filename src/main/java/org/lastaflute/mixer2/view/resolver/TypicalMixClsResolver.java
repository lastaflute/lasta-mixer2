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

import org.dbflute.jdbc.Classification;
import org.dbflute.jdbc.ClassificationMeta;
import org.lastaflute.core.direction.FwAssistantDirector;
import org.lastaflute.core.util.ContainerUtil;
import org.lastaflute.db.dbflute.classification.ListedClassificationProvider;
import org.lastaflute.db.dbflute.exception.ProvidedClassificationNotFoundException;
import org.lastaflute.mixer2.view.Mixer2Supporter;
import org.mixer2.jaxb.xhtml.Body;
import org.mixer2.jaxb.xhtml.Html;
import org.mixer2.jaxb.xhtml.Optgroup;
import org.mixer2.jaxb.xhtml.Option;
import org.mixer2.jaxb.xhtml.Select;
import org.mixer2.xhtml.AbstractJaxb;

/**
 * @author jflute
 */
public class TypicalMixClsResolver {

    public void resolveCls(Html html, Mixer2Supporter supporter) {
        if (html == null) {
            throw new IllegalArgumentException("The argument 'html' should not be null.");
        }
        if (supporter == null) {
            throw new IllegalArgumentException("The supporter 'html' should not be null.");
        }
        final Body body = html.getBody();
        final List<Select> selectList = supporter.searchTagList(body, tag -> {
            return tag instanceof Select && hasClsOption((Select) tag);
        });
        // #pending selected (needs form)
        for (Select select : selectList) {
            final ListedClassificationProvider provider = getClassificationProvider();
            final List<AbstractJaxb> groupOrOptList = select.getOptgroupOrOption();
            for (AbstractJaxb groupOrOpt : new ArrayList<AbstractJaxb>(groupOrOptList)) { // avoid ConcurrentModification
                if (groupOrOpt instanceof Optgroup) {
                    final Optgroup optgroup = (Optgroup) groupOrOpt;
                    final List<Option> optionList = optgroup.getOption();
                    for (Option option : new ArrayList<Option>(optionList)) { // avoid ConcurrentModification
                        handleOptionCls(provider, option, optionList);
                    }
                } else if (groupOrOpt instanceof Option) {
                    final Option option = (Option) groupOrOpt;
                    handleOptionCls(provider, option, groupOrOptList);
                }
            }
        }
    }

    protected ListedClassificationProvider getClassificationProvider() { // #pending
        return ContainerUtil.getComponent(FwAssistantDirector.class).assistDbDirection().assistListedClassificationProvider();
    }

    protected boolean hasClsOption(Select select) {
        final List<AbstractJaxb> groupOrOptList = select.getOptgroupOrOption();
        for (AbstractJaxb groupOrOpt : groupOrOptList) {
            if (groupOrOpt instanceof Optgroup) {
                List<Option> nestedOptionList = ((Optgroup) groupOrOpt).getOption();
                for (Option option : nestedOptionList) {
                    if (option.getData("cls") != null) {
                        return true;
                    }
                }
            } else if (groupOrOpt instanceof Option) {
                if (groupOrOpt.getData("cls") != null) {
                    return true;
                }
            }
        }
        return false;
    }

    protected <TAG extends AbstractJaxb> void handleOptionCls(ListedClassificationProvider provider, Option option,
            List<TAG> addedOptionList) {
        final String classificationName = option.getData("cls");
        if (classificationName == null) {
            return;
        }
        final ClassificationMeta meta;
        try {
            meta = provider.provide(classificationName);
        } catch (ProvidedClassificationNotFoundException e) { // #pending rich message
            throw new IllegalStateException("Not found the classification: " + classificationName, e);
        }
        reflectCls(meta, option, addedOptionList);
    }

    protected <TAG extends AbstractJaxb> void reflectCls(ClassificationMeta meta, Option option, List<TAG> addedOptionList) {
        final List<Classification> clsList = meta.listAll();
        int index = 0;
        for (Classification cls : clsList) {
            String code = cls.code();
            String alias = cls.alias();
            if (index == 0) {
                option.setValue(code);
                option.setContent(alias);
            } else {
                final Option copied = option.copy(Option.class);
                copied.setValue(code);
                copied.setContent(alias);
                @SuppressWarnings("unchecked")
                final TAG addedTag = (TAG) copied;
                addedOptionList.add(addedTag); // #pending want to next tag (latest row for now)
            }
            ++index;
        }
    }
}
