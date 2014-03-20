/* Generated By:JJTree: Do not edit this line. ASTAtTranform.java Version 4.3 */
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

import org.openntf.domino.formula.AtFormulaParser;
import org.openntf.domino.formula.EvaluateException;
import org.openntf.domino.formula.FormulaContext;
import org.openntf.domino.formula.ValueHolder;

public class ASTAtTranform extends SimpleNode {
	public ASTAtTranform(final int id) {
		super(id);
	}

	public ASTAtTranform(final AtFormulaParser p, final int id) {
		super(p, id);
	}

	@Override
	public ValueHolder evaluate(final FormulaContext ctx) throws EvaluateException {
		ValueHolder list = children[0].evaluate(ctx);
		String temp = (String) children[1].evaluate(ctx).get(0);
		temp = temp.toLowerCase();

		ValueHolder ret = new ValueHolder();
		for (int i = 0; i < list.size; i++) {
			@SuppressWarnings("deprecation")
			ValueHolder iter = ValueHolder.valueOf(list.get(i)); // as multi values are alle boxed. it should not affect performance here
			ValueHolder old = ctx.setVarLC(temp, iter);
			try {
				// Cumulate all return values
				ret.addAll(children[2].evaluate(ctx));
			} finally {
				ctx.setVarLC(temp, old);
			}
		}
		return ret;
	}

	public void toFormula(final StringBuilder sb) {
		sb.append("@Transform");
		appendParams(sb);
	}
}
/* JavaCC - OriginalChecksum=f8ff21cd578897d72ffa7dd1a0c2e077 (do not edit this line) */
