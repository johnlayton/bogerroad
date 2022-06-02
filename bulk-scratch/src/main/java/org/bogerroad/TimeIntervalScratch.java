package org.bogerroad;

import com.brein.time.timeintervals.collections.ListIntervalCollection;
import com.brein.time.timeintervals.indexes.IntervalTree;
import com.brein.time.timeintervals.indexes.IntervalTreeBuilder;
import com.brein.time.timeintervals.intervals.IdInterval;
import com.brein.time.timeintervals.intervals.IntegerInterval;

import java.util.Iterator;
import java.util.Random;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class TimeIntervalScratch {
    public static void main(String[] args) {
        IntStream.iterate(1000, i -> i < 100000, value -> value + 1000)
                .forEach(TimeIntervalScratch::test);
        IntStream.iterate(100000, i -> i > 1000, value -> value - 1000)
                .forEach(TimeIntervalScratch::test);
    }

    private static void test(final int limit) {
        final int total = 5000;

        final IntervalTree tree = IntervalTreeBuilder.newBuilder()
                .usePredefinedType(IntervalTreeBuilder.IntervalType.NUMBER, false)
                .collectIntervals(interval -> new ListIntervalCollection())
                .enableWriteCollections()
                .build();
        zip(
                new Random().ints(0, total).limit(limit).boxed(),
                new Random().ints(0, total).limit(limit).boxed(),
                (a, b) -> new IdInterval<>(UUID.randomUUID(), Math.min(a, b), Math.max(a, b))
        ).forEach(tree::insert);

//        final IntegerInterval query = new IntegerInterval(
//                (total / 2) - (total * 100 / limit),
//                (total / 2) + (total * 100 / limit)
//        );
        final IntegerInterval query = new IntegerInterval(100, 200);

        final long start = System.currentTimeMillis();
        final int  count = tree.overlap(query).size();
        System.out.printf("%d,%d,%d,%d,%d,%d%n",
                limit, System.currentTimeMillis() - start, count, tree.size(), query.getNormStart(), query.getNormEnd());
    }

    public static <A, B, C> Stream<C> zip(Stream<A> streamA, Stream<B> streamB, BiFunction<A, B, C> zipper) {
        final Iterator<A> iteratorA = streamA.iterator();
        final Iterator<B> iteratorB = streamB.iterator();
        final Iterator<C> iteratorC = new Iterator<C>() {
            @Override
            public boolean hasNext() {
                return iteratorA.hasNext() && iteratorB.hasNext();
            }

            @Override
            public C next() {
                return zipper.apply(iteratorA.next(), iteratorB.next());
            }
        };
        final Iterable<C> iterable = () -> iteratorC;
        return StreamSupport.stream(iterable.spliterator(), streamA.isParallel() && streamB.isParallel());
    }
}

