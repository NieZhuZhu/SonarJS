/*
 * SonarQube JavaScript Plugin
 * Copyright (C) 2011 SonarSource and Eriks Nukis
 * dev@sonar.codehaus.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.javascript.checks;

import static org.sonar.javascript.checks.Tags.MISRA;
import static org.sonar.javascript.checks.Tags.UNUSED;

import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.check.BelongsToProfile;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.javascript.api.EcmaScriptPunctuator;
import org.sonar.javascript.model.interfaces.Tree.Kind;
import org.sonar.javascript.model.interfaces.statement.ExpressionStatementTree;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.annotations.SqaleSubCharacteristic;
import org.sonar.squidbridge.checks.SquidCheck;
import org.sonar.sslr.parser.LexerlessGrammar;

import com.sonar.sslr.api.AstNode;

@Rule(
  key = "UnreachableCode",
  name = "Unreachable code",
  priority = Priority.MAJOR,
  tags = {MISRA, UNUSED})
@BelongsToProfile(title = CheckList.SONAR_WAY_PROFILE, priority = Priority.MAJOR)
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.UNDERSTANDABILITY)
@SqaleConstantRemediation("30min")
public class UnreachableCodeCheck extends SquidCheck<LexerlessGrammar> {

  @Override
  public void init() {
    subscribeTo(
      Kind.BREAK_STATEMENT,
      Kind.RETURN_STATEMENT,
      Kind.CONTINUE_STATEMENT,
      Kind.THROW_STATEMENT);
  }

  @Override
  public void visitNode(AstNode node) {
    AstNode nextStatement = node.getNextSibling();

    if (isUnReachableCode(nextStatement)) {
      getContext().createLineViolation(this, "This statement can't be reached and so start a dead code block.", nextStatement);
    }
  }

  public static boolean isUnReachableCode(AstNode node) {
    return node != null
      && !node.is(
        Kind.ELSE_CLAUSE,
        Kind.FUNCTION_DECLARATION,
        Kind.GENERATOR_DECLARATION,
        Kind.CLASS_DECLARATION,
      EcmaScriptPunctuator.RCURLYBRACE)
      && !(node.is(Kind.EXPRESSION_STATEMENT) && ((ExpressionStatementTree) node).expression().is(Kind.CLASS_EXPRESSION));
  }

}
