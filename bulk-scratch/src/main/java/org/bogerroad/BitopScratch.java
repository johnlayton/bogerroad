package org.bogerroad;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static org.bogerroad.BitopScratch.MyEnum.A;
import static org.bogerroad.BitopScratch.MyEnum.B;
import static org.bogerroad.BitopScratch.MyEnum.C;
import static org.bogerroad.BitopScratch.MyEnum.D;

public class BitopScratch {

    public enum MyEnum {A, B, C, D}

    public static void main(String[] args) {

        for(int i = 997; i < 1010; i++) {
            System.out.printf("%d -> %d", i, i % 999);
        }



/*
        final int value = List.of(A,B,D).stream()
                .collect(Collectors.summingInt(e -> 1 << e.ordinal()));

        System.out.println("Value " + value);

        Arrays.stream(MyEnum.values()).forEach(e -> {
            System.out.printf("%s -> %d & %d = %d%n", e, value, (1 << e.ordinal()), (value & (1 << e.ordinal())));
            System.out.printf("%s = %s%n", e.name(), (value & (1 << e.ordinal())) == (1 << e.ordinal()) ? "yes" : "no");
        });
*/

//        Map<String, Integer> map = Map.of("A", 1, "B", 2);
//
//        map.forEach((key, value) -> {
//
//
//        });
//
//
//        System.out.println(map.entrySet().stream().map(Tuple::new).collect(Collectors.toSet()));
    }

    public static class Tuple<S, T> {
        private final S first;
        private final T second;

        public Tuple(final S first, final T second) {
            this.first = first;
            this.second = second;
        }

        public Tuple(final Map.Entry<S, T> entry) {
            this(entry.getKey(), entry.getValue());
        }

        public S getFirst() {
            return first;
        }

        public T getSecond() {
            return second;
        }

        @Override
        public String toString() {
            return "Tuple{first=" + first + ", second=" + second + '}';
        }
    }
}
