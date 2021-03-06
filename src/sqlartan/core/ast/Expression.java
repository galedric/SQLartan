package sqlartan.core.ast;

import sqlartan.core.ast.gen.Builder;
import sqlartan.core.ast.parser.ParseException;
import sqlartan.core.ast.parser.Parser;
import sqlartan.core.ast.parser.ParserContext;
import sqlartan.core.ast.token.Token;
import sqlartan.core.ast.token.Tokenizable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import static sqlartan.core.ast.Keyword.*;
import static sqlartan.core.ast.Operator.*;

/**
 * https://www.sqlite.org/lang_expr.html
 */
@SuppressWarnings({ "OptionalUsedAsFieldOrParameterType", "WeakerAccess" })
public abstract class Expression implements Node {
	/**
	 * @see sqlartan.core.ast.parser.Parser
	 */
	public static Expression parse(ParserContext context) {
		Expression expr = parseOrStep.parse(context);
		return expr;
	}

	private static Expression parseFinal(ParserContext context) {
		return context.alternatives(
			Group::parse,
			Cast::parse,
			Case::parse,
			RaiseFunction::parse,
			Function::parse,
			LiteralValue::parse,
			Placeholder::parse,
			ColumnReference::parse,
			UnaryOperator::parse
		);
	}

	@SafeVarargs
	private static Parser<Expression> parseStep(Parser<Expression> parser, Tokenizable<? extends Token.Wrapper<? extends KeywordOrOperator>>... tokens) {
		return context -> {
			Expression lhs = parser.parse(context);
			KeywordOrOperator op;
			while ((op = context.optParse(consumeAny(tokens)).orElse(null)) != null) {
				Expression rhs = parser.parse(context);
				lhs = new BinaryOperator(lhs, op, rhs);
			}
			return lhs;
		};
	}

	@SafeVarargs
	private static Parser<KeywordOrOperator> consumeAny(Tokenizable<? extends Token.Wrapper<? extends KeywordOrOperator>>... tokens) {
		return ctx -> {
			for (Tokenizable<? extends Token.Wrapper<? extends KeywordOrOperator>> token : tokens) {
				Optional<? extends Token.Wrapper<? extends KeywordOrOperator>> consumed = ctx.optConsume(token);
				if (consumed.isPresent()) {
					return consumed.get().node();
				}
			}
			throw ParseException.UnexpectedCurrentToken((Tokenizable[]) tokens);
		};
	}

	private static Parser<Expression> parseConcatStep = parseStep(Expression::parseFinal, CONCAT);
	private static Parser<Expression> parseMulStep = parseStep(parseConcatStep, MUL, DIV, MOD);
	private static Parser<Expression> parseAddStep = parseStep(parseMulStep, PLUS, MINUS);
	private static Parser<Expression> parseBitsStep = parseStep(parseAddStep, SHIFT_LEFT, SHIFT_RIGHT, BIT_AND, BIT_OR);
	private static Parser<Expression> parseCompStep = parseStep(parseBitsStep, LT, LTE, GT, GTE);
	private static Parser<Expression> parseEqStep = parseStep(parseCompStep, EQ, NOT_EQ, IS, IS_NOT, IN, LIKE, GLOB, MATCH, REGEXP);
	private static Parser<Expression> parseAndStep = parseStep(parseEqStep, AND);
	private static Parser<Expression> parseOrStep = parseStep(parseAndStep, OR);

	/**
	 * [bind-parameter]
	 */
	public static class Placeholder extends Expression {
		public Token.Placeholder placeholder;

		public Placeholder(Token.Placeholder placeholder) {
			this.placeholder = placeholder;
		}

		/**
		 * @see sqlartan.core.ast.parser.Parser
		 */
		public static Placeholder parse(ParserContext context) {
			return new Placeholder(context.consume(Token.Placeholder.class));
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void toSQL(Builder sql) {
			sql.appendRaw(placeholder.stringValue());
		}
	}

	/**
	 * { { (schema) . } (table) . } (column)
	 */
	@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
	public static class ColumnReference extends Expression {
		public Optional<String> schema;
		public Optional<String> table;
		public String column;

		/**
		 * @see sqlartan.core.ast.parser.Parser
		 */
		public static ColumnReference parse(ParserContext context) {
			Optional<String> table, schema = Optional.empty();

			if ((table = context.optConsumeSchema()).isPresent()) {
				if ((schema = context.optConsumeSchema()).isPresent()) {
					Optional<String> t = schema;
					schema = table;
					table = t;
				}
			}

			String column = context.consumeIdentifier();

			ColumnReference ref = new ColumnReference();
			ref.schema = schema;
			ref.table = table;
			ref.column = column;
			return ref;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void toSQL(Builder sql) {
			sql.appendSchema(schema).appendSchema(table).appendIdentifier(column);
		}
	}

	/**
	 * (unary-operator) [expr]
	 */
	public static class UnaryOperator extends Expression {
		public KeywordOrOperator op;
		public Expression operand;

		public UnaryOperator(KeywordOrOperator op, Expression operand) {
			this.op = op;
			this.operand = operand;
		}

		/**
		 * @see sqlartan.core.ast.parser.Parser
		 */
		public static UnaryOperator parse(ParserContext context) {
			return new UnaryOperator(
				consumeAny(MINUS, PLUS, BIT_NOT, NOT).parse(context),
				Expression.parse(context)
			);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void toSQL(Builder sql) {
			sql.appendUnary(op).append(operand);
		}
	}

	/**
	 * [expr] (binary-operator) [expr]
	 */
	public static class BinaryOperator extends Expression {
		public Expression lhs;
		public KeywordOrOperator op;
		public Expression rhs;

		public BinaryOperator(Expression lhs, KeywordOrOperator op, Expression rhs) {
			this.lhs = lhs;
			this.op = op;
			this.rhs = rhs;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void toSQL(Builder sql) {
			sql.append(lhs).append(op).append(rhs);
		}
	}

	/**
	 * (function) "(" ... ")"
	 */
	public static class Function extends Expression {
		public String name;
		public Arguments arguments;

		/**
		 * @see sqlartan.core.ast.parser.Parser
		 */
		public static Function parse(ParserContext context) {
			if (!context.next(LEFT_PAREN)) {
				throw ParseException.UnexpectedNextToken(LEFT_PAREN);
			}

			String name = context.consumeIdentifier();
			context.consume(LEFT_PAREN);
			Arguments args = Arguments.parse(context);
			context.consume(RIGHT_PAREN);

			Function function = new Function();
			function.name = name;
			function.arguments = args;
			return function;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void toSQL(Builder sql) {
			sql.appendIdentifier(name).append(LEFT_PAREN).append(arguments).append(RIGHT_PAREN);
		}

		public static abstract class Arguments implements Node {
			public static Arguments parse(ParserContext context) {
				return context.alternatives(Wildcard::parse, ArgsList::parse);
			}
		}

		/**
		 * [DISTINCT] (expr) , ...
		 */
		public static class ArgsList extends Arguments {
			public boolean distinct;
			public List<Expression> args = new ArrayList<>();

			/**
			 * @see sqlartan.core.ast.parser.Parser
			 */
			public static ArgsList parse(ParserContext context) {
				ArgsList args = new ArgsList();
				args.distinct = context.tryConsume(DISTINCT);
				if (!context.parseList(args.args, Expression::parse) && args.distinct) {
					throw ParseException.UnexpectedCurrentToken();
				}
				return args;
			}

			/**
			 * {@inheritDoc}
			 */
			@Override
			public void toSQL(Builder sql) {
				if (distinct) sql.append(DISTINCT);
				sql.append(args);
			}
		}

		/**
		 * (*)
		 */
		public static class Wildcard extends Arguments {
			public static final Wildcard instance = new Wildcard();
			private Wildcard() {}

			/**
			 * @see sqlartan.core.ast.parser.Parser
			 */
			public static Wildcard parse(ParserContext context) {
				context.consume(MUL);
				return instance;
			}

			/**
			 * {@inheritDoc}
			 */
			@Override
			public void toSQL(Builder sql) {
				sql.append(MUL);
			}
		}
	}

	/**
	 * "(" [expr] ")"
	 */
	public static class Group extends Expression {
		public Expression expression;

		/**
		 * @see sqlartan.core.ast.parser.Parser
		 */
		public static Group parse(ParserContext context) {
			context.consume(LEFT_PAREN);
			Expression expr = Expression.parse(context);
			context.consume(RIGHT_PAREN);

			Group group = new Group();
			group.expression = expr;
			return group;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void toSQL(Builder sql) {
			sql.append(LEFT_PAREN).append(expression).append(RIGHT_PAREN);
		}
	}

	/**
	 * CAST ( [expr] AS [type] )
	 */
	public static class Cast extends Expression {
		public Expression expression;
		public String type;

		/**
		 * @see sqlartan.core.ast.parser.Parser
		 */
		public static Cast parse(ParserContext context) {
			context.consume(CAST, LEFT_PAREN);
			Cast cast = new Cast();
			cast.expression = Expression.parse(context);
			context.consume(AS);
			cast.type = context.consumeIdentifier();
			context.consume(RIGHT_PAREN);
			return cast;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void toSQL(Builder sql) {
			sql.append(CAST, LEFT_PAREN).append(expression)
			   .append(AS).appendIdentifier(type).append(RIGHT_PAREN);
		}
	}

	/**
	 * [expr] COLLATE [collation]
	 */
	public static class Collate extends Expression {
		public Expression expression;
		public String collation;

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void toSQL(Builder sql) {
			sql.append(expression).append(COLLATE).appendIdentifier(collation);
		}
	}

	/**
	 * CASE [expr] { WHEN [expr] THEN [expr] } ( ELSE [expr] ) END
	 */
	public static class Case extends Expression {
		public Optional<Expression> expression = Optional.empty();
		public List<WhenClause> cases = new ArrayList<>();
		public Optional<Expression> otherwise = Optional.empty();

		/**
		 * @see sqlartan.core.ast.parser.Parser
		 */
		public static Case parse(ParserContext context) {
			context.consume(CASE);
			Case clause = new Case();
			clause.expression = context.optParse(Expression::parse);
			clause.cases = context.parseList(VOID, WhenClause::parse);
			if (context.tryConsume(ELSE)) {
				clause.otherwise = Optional.of(Expression.parse(context));
			}
			context.consume(END);
			return clause;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void toSQL(Builder sql) {
			sql.append(CASE).append(expression)
			   .append(cases, VOID);
			otherwise.ifPresent(o -> sql.append(ELSE).append(o));
			sql.append(END);
		}
	}

	/**
	 * RAISE ( ... )
	 */
	public static class RaiseFunction extends Expression {
		public enum Type implements Node.Enumerated {
			Ignore(IGNORE),
			Rollback(ROLLBACK),
			Abort(ABORT),
			Fail(FAIL);

			Keyword keyword;

			Type(Keyword keyword) {
				this.keyword = keyword;
			}

			/**
			 * @see sqlartan.core.ast.parser.Parser
			 */
			public static Type parse(ParserContext context) {
				switch (context.consume(Token.Keyword.class).node()) {
					case IGNORE:
						return Ignore;
					case ROLLBACK:
						return Rollback;
					case ABORT:
						return Abort;
					case FAIL:
						return Fail;
					default:
						throw ParseException.UnexpectedCurrentToken(IGNORE, ROLLBACK, ABORT, FAIL);
				}
			}

			/**
			 * {@inheritDoc}
			 */
			@Override
			public void toSQL(Builder sql) {
				sql.append(keyword);
			}
		}

		public Type type;
		public Optional<String> message = Optional.empty();

		/**
		 * @see sqlartan.core.ast.parser.Parser
		 */
		public static RaiseFunction parse(ParserContext context) {
			context.consume(RAISE, LEFT_PAREN);
			RaiseFunction raise = new RaiseFunction();
			raise.type = Type.parse(context);
			if (raise.type != Type.Ignore) {
				context.consume(COMMA);
				raise.message = Optional.of(context.consumeTextLiteral());
			}
			context.consume(RIGHT_PAREN);
			return raise;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void toSQL(Builder sql) {
			sql.append(RAISE, LEFT_PAREN).append(type);
			if (type != Type.Ignore) {
				sql.append(COMMA);
				message.ifPresent(sql::appendTextLiteral);
			}
			sql.append(RIGHT_PAREN);
		}
	}
}
