package org.fao.geonet.services.metadata.format.groovy.template;

import com.google.common.collect.Lists;
import org.fao.geonet.domain.Pair;
import org.xml.sax.Attributes;

import java.io.IOException;
import java.util.List;

/**
 * Parses xml attributes and elements and create TNode if applicable.
 *
 * @author Jesse on 11/29/2014.
 */
public abstract class TNodeFactory {
    static final List<String> ATTRIBUTE_NAME_PREFIXES = Lists.newArrayList("", "x-", "data-");
    /**
     * Test if this node factory can process the current node.
     */
    public abstract boolean applicable(String localName, String qName, Attributes attributes);

    /**
     * Create a node from the current XML element.
     */
    public abstract TNode create(String localName, String qName, Attributes attributes) throws IOException;

    protected String getValue(Attributes attributes, String name) {
        for (String attributeNamePrefix : ATTRIBUTE_NAME_PREFIXES) {
            final String value = attributes.getValue(attributeNamePrefix + name);
            if (value != null) {
                return value;
            }
        }

        return null;
    }
    protected boolean hasAttribute(Attributes attributes, String... attNames) {
        for (String att : attNames) {
            if (getValue(attributes, att) != null) {
                return true;
            }
        }
        return false;
    }

    protected Pair<String, String> getAttributeAndValue(Attributes attributes, String... attNames) {
        for (String att : attNames) {
            final String value = getValue(attributes, att);
            if (value != null) {
                return Pair.read(att, value);
            }
        }
        return null;
    }

}
