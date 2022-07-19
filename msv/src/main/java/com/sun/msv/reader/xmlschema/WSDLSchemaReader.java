/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2001-2013 Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and  use in  source and binary  forms, with  or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * - Redistributions  of  source code  must  retain  the above  copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * - Redistribution  in binary  form must  reproduct the  above copyright
 *   notice, this list of conditions  and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * Neither  the  name   of  Sun  Microsystems,  Inc.  or   the  names  of
 * contributors may be  used to endorse or promote  products derived from
 * this software without specific prior written permission.
 * 
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS  OR   IMPLIED  CONDITIONS,  REPRESENTATIONS   AND  WARRANTIES,
 * INCLUDING  ANY  IMPLIED WARRANTY  OF  MERCHANTABILITY,  FITNESS FOR  A
 * PARTICULAR PURPOSE  OR NON-INFRINGEMENT, ARE HEREBY  EXCLUDED. SUN AND
 * ITS  LICENSORS SHALL  NOT BE  LIABLE  FOR ANY  DAMAGES OR  LIABILITIES
 * SUFFERED BY LICENSEE  AS A RESULT OF OR  RELATING TO USE, MODIFICATION
 * OR DISTRIBUTION OF  THE SOFTWARE OR ITS DERIVATIVES.  IN NO EVENT WILL
 * SUN OR ITS  LICENSORS BE LIABLE FOR ANY LOST  REVENUE, PROFIT OR DATA,
 * OR  FOR  DIRECT,   INDIRECT,  SPECIAL,  CONSEQUENTIAL,  INCIDENTAL  OR
 * PUNITIVE  DAMAGES, HOWEVER  CAUSED  AND REGARDLESS  OF  THE THEORY  OF
 * LIABILITY, ARISING  OUT OF  THE USE OF  OR INABILITY TO  USE SOFTWARE,
 * EVEN IF SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 */

package com.sun.msv.reader.xmlschema;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.sun.msv.grammar.xmlschema.XMLSchemaGrammar;
import com.sun.msv.reader.GrammarReaderController2;

/**
 * A utility class that reads all the schemas from a WSDL.
 */
public final class WSDLSchemaReader {
    private static final class SimpleNamespaceContext implements NamespaceContext {
        public String getNamespaceURI(String prefix) {
            if ("xs".equals(prefix)) {
                return XMLConstants.W3C_XML_SCHEMA_NS_URI;
            } else {
                return null;
            }
        }

        public String getPrefix(String namespaceURI) {
            if (XMLConstants.W3C_XML_SCHEMA_NS_URI.equals(namespaceURI)) {
                return "xs";
            } else {
                return null;
            }

        }

        public Iterator getPrefixes(String namespaceURI) {
            List<String> prefixes = new ArrayList<String>();
            if (XMLConstants.W3C_XML_SCHEMA_NS_URI.equals(namespaceURI)) {
                prefixes.add("xs");
            }
            return prefixes.iterator();
        }
    }

    private WSDLSchemaReader() {
    }

    /**
     * Read the schemas from a WSDL.
     * 
     * @param wsdlSource the WSDL, in any of the TRaX sources.
     * @param factory a SAX parser factory, used to obtain a SAX parser used internally in the reading
     *            process.
     * @param controller Object to handle errors, warnings, and provide a resolver for non-local schemas.
     * @return the MSV grammar.
     * @throws XPathExpressionException
     * @throws TransformerException
     * @throws TransformerConfigurationException
     */
    public static XMLSchemaGrammar read(Source wsdlSource, SAXParserFactory factory,
                                        GrammarReaderController2 controller) throws XPathExpressionException,
        TransformerConfigurationException, TransformerException {

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        DOMResult wsdlDom = new DOMResult();
        transformerFactory.newTransformer().transform(wsdlSource, wsdlDom);
        Node wsdl = wsdlDom.getNode();

        // for the xml schema a:b references to work,
        // we have to push the wsdl mappings down when not already overriden
        Map<String, String> wsdlNamespaceMappings = new HashMap<String, String>();
        Document wsdlDoc = (Document)wsdl;
        NamedNodeMap attrMap = wsdlDoc.getDocumentElement().getAttributes();
        if (attrMap != null) {
            for (int x = 0; x < attrMap.getLength(); x++) {
                Attr attr = (Attr)attrMap.item(x);
                String ns = attr.getNamespaceURI();
                if (XMLConstants.XMLNS_ATTRIBUTE_NS_URI.equals(ns)) {
                    String localName = attr.getLocalName();
                    String uri = attr.getValue();
                    wsdlNamespaceMappings.put(localName, uri);
                }
            }
        }

        String wsdlSystemId = wsdlSource.getSystemId();
        XPath xpath = XPathFactory.newInstance().newXPath();
        xpath.setNamespaceContext(new SimpleNamespaceContext());

        Map<String, EmbeddedSchema> schemas = new HashMap<String, EmbeddedSchema>();

        NodeList schemaNodes = (NodeList)xpath.evaluate("//xs:schema", wsdl, XPathConstants.NODESET);
        for (int x = 0; x < schemaNodes.getLength(); x++) {
            Element schema = (Element)schemaNodes.item(x);
            String targetNamespace = schema.getAttribute("targetNamespace");
            String systemId = wsdlSystemId + "#" + x;
            EmbeddedSchema embeddedWSDLSchema = new EmbeddedSchema(systemId, schema);
            schemas.put(targetNamespace, embeddedWSDLSchema);
        }

        WSDLGrammarReaderController wsdlController = new WSDLGrammarReaderController(controller,
                                                                                     wsdlSystemId, schemas);

        XMLSchemaReader reader = new XMLSchemaReader(wsdlController);
        reader.setAdditionalNamespaceMap(wsdlNamespaceMappings);
        MultiSchemaReader multiSchemaReader = new MultiSchemaReader(reader);
        for (EmbeddedSchema schema : schemas.values()) {
            DOMSource source = new DOMSource(schema.getSchemaElement());
            source.setSystemId(schema.getSystemId());
            multiSchemaReader.parse(source);
        }
        return multiSchemaReader.getResult();
    }
}
