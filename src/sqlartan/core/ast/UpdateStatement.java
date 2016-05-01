package sqlartan.core.ast;

import sqlartan.core.ast.parser.ParserContext;

public interface UpdateStatement extends Statement {
	static UpdateStatement parse(ParserContext context) {
		throw new UnsupportedOperationException();
	}
}
