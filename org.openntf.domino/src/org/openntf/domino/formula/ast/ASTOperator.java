/* Generated By:JJTree: Do not edit this line. ASTOperator.java Version 4.3 */
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

import org.openntf.domino.formula.AtFormulaParserImpl;

public class ASTOperator extends ASTFunction {

	public ASTOperator(final AtFormulaParserImpl p, final int id) {
		super(p, id);
	}

	@Override
	public void toFormula(final StringBuilder sb) {
		if (children.length == 1) {
			sb.append(function.getImage());
			children[0].toFormula(sb);
		} else {
			sb.append('(');
			children[0].toFormula(sb);
			sb.append(function.getImage());
			children[1].toFormula(sb);
			sb.append(')');
		}
	}
}
/* JavaCC - OriginalChecksum=8690fa85587044e61f27816a1daf23fc (do not edit this line) */