package sqlartan.core.ast;

import sqlartan.core.ast.gen.Builder;
import sqlartan.core.ast.parser.ParseException;
import sqlartan.core.ast.parser.ParserContext;
import sqlartan.core.ast.token.Token;
import static sqlartan.core.ast.Keyword.*;

/**
 * https://www.sqlite.org/syntaxdiagrams.html#conflict-clause
 */
public enum ConflictClause implements Node.Enumerated {
	None(null), Rollback(ROLLBACK), Abort(ABORT), Fail(FAIL), Ignore(IGNORE), Replace(REPLACE);

	public final Keyword keyword;

	ConflictClause(Keyword keyword) {
		this.keyword = keyword;
	}

	/**
	 * @see sqlartan.core.ast.parser.Parser
	 */
	public static ConflictClause parse(ParserContext context) {
		if (context.tryConsume(ON, CONFLICT)) {
			switch (context.consume(Token.Keyword.class).node()) {
				case ROLLBACK:
					return Rollback;
				case ABORT:
					return Rollback;
				case FAIL:
					return Fail;
				case IGNORE:
					return Ignore;
				case REPLACE:
					return Replace;
				default:
					throw ParseException.UnexpectedCurrentToken(ROLLBACK, ABORT, FAIL, IGNORE, REPLACE);
			}
		}
		return None;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void toSQL(Builder sql) {
		if (this != None) {
			sql.append(ON, CONFLICT).append(keyword);
		}
	}
}
