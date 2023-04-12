package org.bogerroad;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StreamScratch {


    public static void main(String[] args) throws JsonProcessingException {

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory().disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER));

        final Set<Set<Temp>> temps = Set.of(
                Set.of(temp("A"), temp("B"), temp("C")),
                Set.of(temp("A"), temp("C"), temp("D")),
                Set.of(temp("A"), temp("E"), temp("F"), temp("C")),
                Set.of(temp("A"), temp("E"), temp("C"), temp("Y"))
        );

        System.out.println(temps.stream().parallel().collect(intersecting()));
        System.out.println(mapper.writeValueAsString(temps.stream().parallel().collect(multiGroup(Function.identity()))));

        System.out.println("===========================================================");

        final Set<TempWrapper> wrappers = Set.of(
                wrapper("1", temp("A"), temp("B"), temp("C")),
                wrapper("2", temp("A"), temp("C"), temp("D")),
                wrapper("3", temp("A"), temp("E"), temp("F"), temp("C")),
                wrapper("4", temp("A"), temp("E"), temp("C"), temp("Y"))
        );

        System.out.println(wrappers.stream().parallel().map(TempWrapper::getTemps).collect(intersecting()));
        System.out.println(wrappers.stream().parallel().collect(multiGroup(TempWrapper::getTemps)));

        System.out.println("===========================================================");

        final Set<Set<String>> input = Set.of(
                Set.of("A", "B", "C"),
                Set.of("A", "C", "D"),
                Set.of("A", "E", "F", "C"),
                Set.of("A", "E", "C", "Y")
        );

        System.out.println(input.stream().parallel().collect(intersecting()));
        System.out.println(input.stream().parallel().collect(multiGroup(Function.identity())));
    }

    public static class Box<A> {
        public Set<A> a;

        public Box() {
        }
    }


    public static <S, T> Collector<S, ?, Map<T, Set<S>>> multiGroup(final Function<S, Set<T>> keys) {
        return Collector.of(
                HashMap::new,
                (t, s) -> {
                    keys.apply(s).forEach(b -> {
                        t.computeIfAbsent(b, k -> new HashSet<>()).add(s);
                    });
                },
                (a, b) -> {
                    return new HashMap<T, Set<S>>() {{
                        final BiConsumer<T, Set<S>> mergeIt = (key, value) -> merge(key, value,
                                (s1, s2) -> Stream.concat(s1.stream(), s2.stream()).collect(Collectors.toSet()));
                        b.forEach(mergeIt);
                        a.forEach(mergeIt);
                    }};
                }
        );
    }

    public static <T, S extends Collection<T>> Collector<S, ?, Set<T>> intersecting() {
        return Collector.<S, Box<T>, Set<T>>of(
                Box::new,
                (b, t) -> {
                    if (b.a == null) {
                        b.a = new HashSet<>(t);
                    } else {
                        b.a.retainAll(t);
                    }
                },
                (b1, b2) -> {
                    if (b1.a == null) {
                        return b2;
                    }
                    if (b2.a != null) {
                        b1.a.retainAll(b2.a);
                    }
                    return b1;
                },
                b -> b.a == null ? Collections.emptySet() : b.a,
                Collector.Characteristics.UNORDERED
        );
    }


    public static Temp temp(final String code) {
        return new Temp(code);
    }

    public static class Temp {
        private final String code;

        public Temp(final String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            Temp temp = (Temp) o;

            if (code != null ? !code.equals(temp.code) : temp.code != null) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
            return code != null ? code.hashCode() : 0;
        }

        @Override
        public String toString() {
            return "Temp{code='" + code + '\'' + '}';
        }
    }

    public static TempWrapper wrapper(final String code, final Temp... temp) {
        return new TempWrapper(code, Set.of(temp));
    }

    public static class TempWrapper {
        private final String code;
        private final Set<Temp> temps;

        public TempWrapper(String code, Set<Temp> temps) {
            this.code = code;
            this.temps = temps;
        }

        public String getCode() {
            return code;
        }

        public Set<Temp> getTemps() {
            return temps;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            TempWrapper that = (TempWrapper) o;

            if (!Objects.equals(code, that.code)) return false;
            return Objects.equals(temps, that.temps);
        }

        @Override
        public int hashCode() {
            int result = code != null ? code.hashCode() : 0;
            result = 31 * result + (temps != null ? temps.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "TempWrapper{code='" + code + '\'' + ", temps=" + temps + '}';
        }
    }
}
