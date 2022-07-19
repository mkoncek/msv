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

package com.sun.msv.reader.trex;

import com.sun.msv.grammar.Expression;
import com.sun.msv.grammar.ReferenceExp;
import com.sun.msv.grammar.trex.TREXGrammar;
import com.sun.msv.reader.ExpressionWithoutChildState;
                                                           
/**
 * parses &lt;ref&gt; pattern.
 * 
 * @author <a href="mailto:kohsuke.kawaguchi@eng.sun.com">Kohsuke KAWAGUCHI</a>
 */
public class RefState extends ExpressionWithoutChildState {
    
    public RefState( boolean parentRef ) {
        this.parentRef = parentRef;
    }
    
    protected boolean parentRef;
    
    protected Expression makeExpression() {
        final String name = startTag.getCollapsedAttribute("name");
        
        if(name==null) {
            // name attribute is required.
            reader.reportError( TREXBaseReader.ERR_MISSING_ATTRIBUTE,
                "ref","name");
            // recover by returning something that can be interpreted as Pattern
            return Expression.nullSet;
        }
        
        TREXGrammar grammar = ((TREXBaseReader)this.reader).grammar;
        
        if( parentRef ) {
            grammar = grammar.getParentGrammar();
            
            if( grammar==null ) {
                reader.reportError( TREXBaseReader.ERR_NONEXISTENT_PARENT_GRAMMAR );
                return Expression.nullSet;
                // recover by returning something that can be interpreted as Pattern
            }
        }
        
        ReferenceExp r = grammar.namedPatterns.getOrCreate(name);
        wrapUp(r);
        return r;
    }
    
    /**
     * Performs the final wrap-up.
     */
    protected void wrapUp( ReferenceExp r ) {
        reader.backwardReference.memorizeLink(r);
    }

}
