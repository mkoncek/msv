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

package com.sun.msv.verifier;

import org.relaxng.datatype.Datatype;
import org.xml.sax.Attributes;
import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 * SAX XMLFilter that verifies incoming SAX event stream.
 * 
 * This object can be reused to validate multiple documents.
 * Just be careful NOT to use the same object to validate more than one
 * documents <b>at the same time</b>.
 * 
 * @author <a href="mailto:kohsuke.kawaguchi@eng.sun.com">Kohsuke KAWAGUCHI</a>
 */
public class VerifierFilter extends XMLFilterImpl implements IVerifier {
    
    private final IVerifier verifier;

    /**
     * @param verifier
     *        Verifier object that performs actual validation.
     */
    public VerifierFilter( IVerifier verifier ) {
        this.verifier = verifier;
    }
    
    public VerifierFilter( DocumentDeclaration documentDecl,
                            ErrorHandler errorHandler ) {
        this( new Verifier(documentDecl,errorHandler) );
    }
    
    public boolean isValid() {
        return verifier.isValid();
    }
    public Object getCurrentElementType() {
        return verifier.getCurrentElementType();
    }
    public Datatype[] getLastCharacterType() {
        return verifier.getLastCharacterType();
    }
    public final Locator getLocator() {
        return verifier.getLocator();
    }
    public final ErrorHandler getErrorHandler() {
        return verifier.getErrorHandler();
    }
    public final void setErrorHandler( ErrorHandler handler ) {
        super.setErrorHandler(handler);
        verifier.setErrorHandler(handler);
    }
    public final void setPanicMode( boolean usePanicMode ) {
        verifier.setPanicMode(usePanicMode);
    }

    
    public IVerifier getVerifier() { return verifier; }
    
    public void setDocumentLocator(Locator locator) {
        verifier.setDocumentLocator(locator);
        super.setDocumentLocator(locator);
    }

    public void startDocument() throws SAXException {
        verifier.startDocument();
        super.startDocument();
    }

    public void endDocument() throws SAXException {
        verifier.endDocument();
        super.endDocument();
    }

    public void startPrefixMapping( String prefix, String uri ) throws SAXException {
        verifier.startPrefixMapping(prefix, uri);
        super.startPrefixMapping(prefix, uri);
    }

    public void endPrefixMapping(String prefix) throws SAXException {
        verifier.endPrefixMapping(prefix);
        super.endPrefixMapping(prefix);
    }

    public void startElement( String namespaceURI, String localName, String qName, Attributes atts ) throws SAXException {
        verifier.startElement(namespaceURI, localName, qName, atts);
        super.startElement(namespaceURI, localName, qName, atts);
    }

    public void endElement(    String namespaceURI, String localName, String qName ) throws SAXException {
        verifier.endElement(namespaceURI, localName, qName);
        super.endElement(namespaceURI, localName, qName);
    }

    public void characters( char ch[], int start, int length ) throws SAXException {
        verifier.characters(ch, start, length);
        super.characters(ch, start, length);
    }

    public void ignorableWhitespace( char ch[], int start, int length ) throws SAXException {
        verifier.ignorableWhitespace(ch, start, length);
        super.ignorableWhitespace(ch, start, length);
    }

    public void processingInstruction(String target, String data) throws SAXException {
        verifier.processingInstruction(target, data);
        super.processingInstruction(target, data);
    }

    public void skippedEntity(String name) throws SAXException {
        verifier.skippedEntity(name);
        super.skippedEntity(name);
    }
}
