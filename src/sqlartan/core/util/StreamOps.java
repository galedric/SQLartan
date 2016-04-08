package sqlartan.core.util;

import java.util.Comparator;
import java.util.Optional;
import java.util.function.*;
import java.util.stream.*;

public interface StreamOps<T> extends Streamable<T> {
	default boolean allMatch(Predicate<? super T> predicate) {
		return stream().allMatch(predicate);
	}

	default boolean anyMatch(Predicate<? super T> predicate) {
		return stream().anyMatch(predicate);
	}

	default <R, A> R collect(Collector<? super T, A, R> collector) {
		return stream().collect(collector);
	}

	default <R> R collect(Supplier<R> supplier, BiConsumer<R, ? super T> accumulator, BiConsumer<R, R> combiner) {
		return stream().collect(supplier, accumulator, combiner);
	}

	default long count() {
		return stream().count();
	}

	default Stream<T> distinct() {
		return stream().distinct();
	}

	default Stream<T> filter(Predicate<? super T> predicate) {
		return stream().filter(predicate);
	}

	default Optional<T> findAny() {
		return stream().findAny();
	}

	default Optional<T> findFirst() {
		return stream().findFirst();
	}

	default <R> Stream<R> flatMap(Function<? super T, ? extends Stream<? extends R>> mapper) {
		return stream().flatMap(mapper);
	}

	default DoubleStream flatMapToDouble(Function<? super T, ? extends DoubleStream> mapper) {
		return stream().flatMapToDouble(mapper);
	}

	default IntStream flatMapToInt(Function<? super T, ? extends IntStream> mapper) {
		return stream().flatMapToInt(mapper);
	}

	default LongStream flatMapToLong(Function<? super T, ? extends LongStream> mapper) {
		return stream().flatMapToLong(mapper);
	}

	default void forEach(Consumer<? super T> action) {
		stream().forEach(action);
	}

	default void forEachOrdered(Consumer<? super T> action) {
		stream().forEachOrdered(action);
	}

	default Stream<T> limit(long maxSize) {
		return stream().limit(maxSize);
	}

	default <R> Stream<R> map(Function<? super T, ? extends R> mapper) {
		return stream().map(mapper);
	}

	default DoubleStream mapToDouble(ToDoubleFunction<? super T> mapper) {
		return stream().mapToDouble(mapper);
	}

	default IntStream mapToInt(ToIntFunction<? super T> mapper) {
		return stream().mapToInt(mapper);
	}

	default LongStream mapToLong(ToLongFunction<? super T> mapper) {
		return stream().mapToLong(mapper);
	}

	default Optional<T> max(Comparator<? super T> comparator) {
		return stream().max(comparator);
	}

	default Optional<T> min(Comparator<? super T> comparator) {
		return stream().min(comparator);
	}

	default boolean noneMatch(Predicate<? super T> predicate) {
		return stream().noneMatch(predicate);
	}

	default Stream<T> peek(Consumer<? super T> action) {
		return stream().peek(action);
	}

	default Optional<T> reduce(BinaryOperator<T> accumulator) {
		return stream().reduce(accumulator);
	}

	default T reduce(T identity, BinaryOperator<T> accumulator) {
		return stream().reduce(identity, accumulator);
	}

	default <U> U reduce(U identity, BiFunction<U, ? super T, U> accumulator, BinaryOperator<U> combiner) {
		return stream().reduce(identity, accumulator, combiner);
	}

	default Stream<T> skip(long n) {
		return stream().skip(n);
	}

	default Stream<T> sorted() {
		return stream().sorted();
	}

	default Stream<T> sorted(Comparator<? super T> comparator) {
		return stream().sorted(comparator);
	}

	default Object[] toArray() {
		return stream().toArray();
	}

	default <A> A[] toArray(IntFunction<A[]> generator) {
		return stream().toArray(generator);
	}
}
