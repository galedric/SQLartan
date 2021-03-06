package sqlartan.core.ast;

import sqlartan.core.ast.gen.Builder;
import sqlartan.core.ast.parser.ParserContext;
import java.util.Optional;
import static sqlartan.core.ast.Keyword.COLLATE;

/**
 * https://www.sqlite.org/lang_createindex.html
 * There is no easy way to disambiguate between the column-name and expr case.
 * As a result, we always parse an Expression which may be a ColumnRef.
 */
@SuppressWarnings({ "OptionalUsedAsFieldOrParameterType", "WeakerAccess" })
public class IndexedColumn implements Node {
	public Expression expression;
	public Optional<String> collate = Optional.empty();
	public Ordering ordering = Ordering.None;

	/**
	 * @see sqlartan.core.ast.parser.Parser
	 */
	public static IndexedColumn parse(ParserContext context) {
		IndexedColumn column = new IndexedColumn();
		column.expression = Expression.parse(context);

		if (context.tryConsume(COLLATE)) {
			column.collate = Optional.of(context.consumeIdentifier());
		}

		column.ordering = Ordering.parse(context);
		return column;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void toSQL(Builder sql) {
		sql.append(expression);
		collate.ifPresent(c -> sql.append(COLLATE).appendIdentifier(c));
		sql.append(ordering);
	}
}
