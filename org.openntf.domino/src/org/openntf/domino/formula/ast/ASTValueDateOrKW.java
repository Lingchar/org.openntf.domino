/* Generated By:JJTree: Do not edit this line. ASTValueDateOrKW.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=true,VISITOR=false,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
/*
 * © Copyright FOCONIS AG, 2014
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at:
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing 
 * permissions and limitations under the License.
 */
package org.openntf.domino.formula.ast;

import org.openntf.domino.DateTime;
import org.openntf.domino.formula.AtFormulaParser;
import org.openntf.domino.formula.FormulaContext;
import org.openntf.domino.formula.ParseException;
import org.openntf.domino.formula.ValueHolder;

public class ASTValueDateOrKW extends SimpleNode {
	DateTime dateValue = null;
	String image = null;

	public ASTValueDateOrKW(final int id) {
		super(id);
	}

	public ASTValueDateOrKW(final AtFormulaParser p, final int id) {
		super(p, id);
	}

	@Override
	public ValueHolder evaluate(final FormulaContext ctx) {
		if (dateValue != null)
			return ValueHolder.valueOf(dateValue);
		return ValueHolder.valueOf(image);
	}

	public void init(final String image) throws ParseException {
		String inner = image.substring(1, image.length() - 1); // remove first [ and last ]
		try {
			dateValue = parser.getFormatter().parseDate(inner);
		} catch (java.text.ParseException e) {
			if (inner.contains(".") || inner.contains("/") || inner.contains("-")) {
				// this MUST be a date
				throw new ParseException(parser, e.getMessage());
			}
		}
		this.image = image; // tried to parse. but this seems to be a Keyword
	}

	@Override
	public void toFormula(final StringBuilder sb) {
		sb.append(image);
	}

}
/* JavaCC - OriginalChecksum=56ca1fdb501387745d81cc4e6f2b1b55 (do not edit this line) */
