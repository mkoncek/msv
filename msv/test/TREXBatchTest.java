/*
 * @(#)$Id$
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
import junit.framework.*;
import java.util.StringTokenizer;

/**
 * tests the entire RELAX test suite by using BatchVerifyTester.
 * 
 * for use by automated test by ant.
 * 
 * @author <a href="mailto:kohsuke.kawaguchi@eng.sun.com">Kohsuke KAWAGUCHI</a>
 */
public class TREXBatchTest {
	public static TestSuite suite() {
		StringTokenizer tokens = new StringTokenizer( System.getProperty("TREXBatchTestDir"), ";" );
		
		TestSuite s = new TestSuite();
		while( tokens.hasMoreTokens() ) {
			BatchVerifyTester t = new BatchVerifyTester();
			t.init("trex", tokens.nextToken(), ".trex", BatchTester.genericLoader);
			s.addTest( t.suite() );
		}
		
		return s;
	}
}
