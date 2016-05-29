package sqlartan.util;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * A lazy value computation.
 * <p>
 * The value will be lazily computed when first required and keep in cache
 * for future requests.
 *
 * @param <T> type of the computed value
 */
public abstract class Lazy<T> {
	/**
	 * Constructs a new lazy value, generated by the given supplier.
	 *
	 * @param generator the supplier of a value
	 * @param <U>       the type of the supplied value
	 * @return a new lazy value
	 */
	public static <U> Lazy<U> lazy(Supplier<U> generator) {
		return new OfGenerator<>(generator);
	}

	/**
	 * Whether this lazy value has already been generated
	 */
	private boolean generated = false;

	/**
	 * The value of this lazy value
	 */
	private T value;

	/**
	 * Generates the value of this lazy.
	 * <p>
	 * This method generates a new value from the supplier, but does not store
	 * the result as cached value for this lazy value. It can be used to
	 * generate additional instances of this lazy value.
	 *
	 * @return a new value from the generator
	 */
	public abstract T gen();

	/**
	 * Returns the value of this lazy value.
	 * <p>
	 * The first time this method is called, a new value is generated from the
	 * generator and stored for future calls.
	 *
	 * @return the value of this lazy value
	 */
	public synchronized T get() {
		if (!generated) {
			value = gen();
			generated = true;
		}
		return value;
	}

	/**
	 * Returns the value of this lazy value, if already generated.
	 *
	 * @return the value of this lazy value
	 */
	public synchronized Optional<T> opt() {
		return generated ? Optional.of(value) : Optional.empty();
	}

	/**
	 * A lazy value with an associated supplier.
	 *
	 * @param <T> the type of value generated by the generator
	 */
	private static class OfGenerator<T> extends Lazy<T> {
		/**
		 * The supplier to use for generating value of this lazy val
		 */
		private Supplier<T> generator;

		/**
		 * @param generator the supplier to use for generating value of this
		 *                  lazy val
		 */
		private OfGenerator(Supplier<T> generator) {
			this.generator = generator;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public T gen() {
			return generator.get();
		}
	}
}
