package org.bogerroad;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.TimeZone;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.lang.System.out;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static org.bogerroad.CompositeScratch.DateRange.INIT;

public final class CompositeScratch {

    private CompositeScratch() {
    }

    public static void main(final String[] args) {
        final List<Instance> instances = createEvents(10, 4);

        final Function<List<Inst>, Inst> makeInst =
                (List<Inst> insts) -> insts.size() == 1 ? insts.get(0) : new Instances(insts);

        final Function<List<Inst>, List<? extends Inst>> overlappingInstances =
                (List<Inst> insts) -> insts.size() == 1 ? insts : new Instances(insts).splitPartialOverlapping();

        final Function<List<Inst>, List<? extends Inst>> matchingInstances =
                (List<Inst> insts) -> insts.size() == 1 ? insts : new Instances(insts).splitFullOverlapping();

        final Function<Instance, String> byLocation = instance -> instance.getLocation().getName();
        final Function<Instance, String> byPerson = instance -> instance.getPerson().getName();

        final Function<Instance, String> byTypeAndRoom =
                instance -> format("Type [%s] - Location [%s]", instance.getType(),
                        instance.getLocation().getName());

        final Function<Instance, TypeAndDateRange> byTypeAndDateRange =
                instance -> new TypeAndDateRange(instance.getType(), instance.getDateRange());

        final Function<Instance, String> byTypeRoomAndDateRange =
                instance -> format("Type [%s] - Location [%s] - DateRange [%s]",
                        instance.getType(),
                        instance.getLocation().getName(),
                        instance.getDateRange());

        out.println("\n\n============= Ungrouped =================================================================\n");

        instances.forEach(out::println);

        out.println("\n\n============= Grouped By Type and Room ==================================================\n");

        instances.stream().collect(groupingBy(byTypeAndRoom, collectingAndThen(toList(), makeInst)))
                .forEach((key, value) -> out.printf("%s ... %n%s%n%n", key, value));

        out.println("\n\n============= Grouped By Type and DateRange =============================================\n");

        instances.stream().collect(groupingBy(byTypeAndDateRange, collectingAndThen(toList(), makeInst)))
                .forEach((key, value) -> {
                    out.printf("Type [%s] - DateRange [%s] ... %n%s%n%n", key.getType(), key.getDateRange(), value);
                });

        out.println("\n\n============= Grouped By Type, Room and DateRange =======================================\n");

        instances.stream().collect(groupingBy(byTypeRoomAndDateRange, collectingAndThen(toList(), makeInst)))
                .forEach((key, value) -> out.printf("%s ... %n%s%n%n", key, value));

        out.println("\n\n============= Grouped By Type and Room then Split into Matching DateRanges ==============\n");

        instances.stream().collect(groupingBy(byTypeAndRoom, collectingAndThen(toList(), matchingInstances)))
                .forEach((key, value) -> {
                    out.printf("%s ... %n", key);
                    value.forEach(inst -> out.printf("%s%n%n", inst));
                });

        out.println("\n\n============= Grouped By Type and Room then Split into Overlapping DateRanges ===========\n");

        instances.stream().collect(groupingBy(byTypeAndRoom, collectingAndThen(toList(), overlappingInstances)))
                .forEach((key, value) -> {
                    out.printf("%s ... %n", key);
                    value.forEach(inst -> out.printf("%s%n%n", inst));
                });

        out.println("\n\n============= Find Room Conflicts =======================================================\n");

        instances.stream().collect(groupingBy(byLocation, collectingAndThen(toList(), overlappingInstances)))
                .forEach((key, value) -> {
                    out.printf("%s ... %n", key);
                    value.forEach(inst -> out.printf("%s%n%n", inst));
                });

        out.println("\n\n============= Find Person Conflicts =====================================================\n");

        instances.stream().collect(groupingBy(byPerson, collectingAndThen(toList(), overlappingInstances)))
                .forEach((key, value) -> {
                    out.printf("%s ... %n", key);
                    value.forEach(inst -> out.printf("%s%n%n", inst));
                });

/*
        final Instances parsed = new Instances(createEvents(
         "Instance [TRAINING] Time [12:43 pm -> 1:43 pm] Person [Mike] Location [Drama Hall]\n"
                + "Instance [MEETING] Time [1:13 pm -> 3:13 pm] Person [Harsha] Location [Drama Hall]\n"
                + "Instance [TRAINING] Time [1:13 pm -> 2:13 pm] Person [Marco] Location [Drama Hall]\n"
                + "Instance [MEETING] Time [1:13 pm -> 3:13 pm] Person [Mike] Location [Drama Hall]\n"
                + "Instance [TRAINING] Time [1:13 pm -> 2:13 pm] Person [Richard] Location [Drama Hall]\n"
                + "Instance [MEETING] Time [1:13 pm -> 3:13 pm] Person [Richard] Location [Drama Hall]\n"
                + "Instance [TRAINING] Time [1:43 pm -> 4:13 pm] Person [Cathy] Location [Drama Hall]\n"
                + "Instance [TRAINING] Time [1:43 pm -> 4:13 pm] Person [Flora] Location [Drama Hall]\n"
                + "Instance [MEETING] Time [1:43 pm -> 3:13 pm] Person [Flora] Location [Drama Hall]\n"
                + "Instance [MEETING] Time [1:43 pm -> 3:13 pm] Person [Harsha] Location [Drama Hall]\n"
                + "Instance [TRAINING] Time [1:43 pm -> 4:13 pm] Person [Marco] Location [Drama Hall]\n"
                + "Instance [MEETING] Time [1:43 pm -> 3:13 pm] Person [Mike] Location [Drama Hall]\n"
                + "Instance [MEETING] Time [2:43 pm -> 3:13 pm] Person [Cathy] Location [Drama Hall]\n"
                + "Instance [MEETING] Time [2:43 pm -> 3:13 pm] Person [Marco] Location [Drama Hall]\n"
                + "Instance [MEETING] Time [2:43 pm -> 3:13 pm] Person [Mike] Location [Drama Hall]\n"
                + "Instance [MEETING] Time [2:43 pm -> 3:13 pm] Person [Richard] Location [Drama Hall]\n"
                + "Instance [MEETING] Time [3:43 pm -> 4:13 pm] Person [Cathy] Location [Drama Hall]\n"
                + "Instance [MEETING] Time [3:43 pm -> 4:13 pm] Person [Flora] Location [Drama Hall]\n"
                + "Instance [MEETING] Time [3:43 pm -> 4:13 pm] Person [Marco] Location [Drama Hall]\n"
                + "Instance [MEETING] Time [3:43 pm -> 4:13 pm] Person [Mike] Location [Drama Hall]\n"
                + "Instance [MEETING] Time [3:43 pm -> 4:13 pm] Person [Richard] Location [Drama Hall]\n"
                + "Instance [MEETING] Time [4:13 pm -> 5:13 pm] Person [Flora] Location [Drama Hall]\n"
                + "Instance [MEETING] Time [4:13 pm -> 5:13 pm] Person [Marco] Location [Drama Hall]\n"
                + "Instance [MEETING] Time [4:13 pm -> 5:13 pm] Person [Richard] Location [Drama Hall]\n"
                + "Instance [MEETING] Time [5:13 pm -> 6:13 pm] Person [Harsha] Location [Drama Hall]\n"
                + "Instance [MEETING] Time [5:13 pm -> 6:13 pm] Person [Marco] Location [Drama Hall]\n"
                + "Instance [MEETING] Time [5:13 pm -> 6:13 pm] Person [Mike] Location [Drama Hall]\n"
                + "Instance [TRAINING] Time [12:43 pm -> 1:43 pm] Person [Harsha] Location [Drama Hall]\n"
                + "Instance [TRAINING] Time [1:13 pm -> 2:13 pm] Person [Cathy] Location [Drama Hall]\n")
        );

        out.println(parsed.splitPartialOverlapping());
*/
    }

    // Component
    public interface Inst {
        DateRange getDateRange();
    }

    // Composite (or Node)
    public static class Instances implements Inst {
        private final List<Inst> insts;

        public Instances(final List<Inst> insts) {
            this.insts = insts;
        }

        @SuppressWarnings("checkstyle:DesignForExtension")
        public Instances add(final Inst inst) {
            insts.add(inst);
            return this;
        }

        @SuppressWarnings("checkstyle:DesignForExtension")
        public List<Instances> splitPartialOverlapping() {
            return split(this::overlapsInstance);
        }

        @SuppressWarnings("checkstyle:DesignForExtension")
        public List<Instances> splitFullOverlapping() {
            return split(this::matchingInstance);
        }

        private List<Instances> split(final Function<Inst, Predicate<Instances>> filter) {
            return insts.stream()
                    .sorted(Comparator.comparing(i -> i.getDateRange().getStart()))
                    .reduce(
                            new ArrayList<>(),
                            (list, inst) -> {
                                list.stream()
                                        .filter(filter.apply(inst))
                                        .findFirst()
                                        .ifPresentOrElse(
                                                addToExistingInstance(inst),
                                                addNewInstancesToListOfInstances(list, inst)
                                        );
                                return list;
                            },
                            (insts1, insts2) -> {
                                return Stream.concat(insts1.stream(), insts2.stream())
                                        .collect(Collectors.toCollection(ArrayList::new));
                            }
                    );
        }

        private Predicate<Instances> overlapsInstance(final Inst inst) {
            return i -> i.getDateRange().overlaps(inst.getDateRange());
        }

        private Predicate<Instances> matchingInstance(final Inst inst) {
            return i -> i.getDateRange().equals(inst.getDateRange());
        }

        private Consumer<Instances> addToExistingInstance(final Inst inst) {
            return overlappingInstance -> overlappingInstance.add(inst);
        }

        private Runnable addNewInstancesToListOfInstances(final ArrayList<Instances> list, final Inst inst) {
            return () -> list.add(new Instances(new ArrayList<>(List.of(inst))));
        }

        @Override
        public DateRange getDateRange() {
            return insts.stream().map(Inst::getDateRange).reduce(INIT, (current, next) -> next.concat(current));
        }

        @Override
        public String toString() {
            return format("Instances [%s] ...%n", getDateRange()) + insts.stream()
                    .sorted(Comparator.comparing(instance -> instance.getDateRange().getStart()))
                    .map(Inst::toString).collect(Collectors.joining("\n"));
        }
    }

    // Leaf
    public static class Instance implements Inst {

        private static final Pattern PATTERN =
                Pattern.compile(".*\\[(.*)\\].*\\[(.*)\\].*\\[(.*)\\].*\\[(.*)\\].*");

        private final Type type;
        private final DateRange dateRange;
        private final Person person;
        private final Location location;

        public Instance(final Type type, final DateRange dateRange, final Person person, final Location location) {
            this.type = type;
            this.dateRange = dateRange;
            this.person = person;
            this.location = location;
        }

        public static Instance parse(final String text) {
            final Matcher matcher = PATTERN.matcher(text);
            if (matcher.matches()) {
                final Type type = Type.valueOf(matcher.group(1));
                final DateRange dateRange = DateRange.parse(matcher.group(2));
                final Person person = new Person(matcher.group(3));
                final Location location = new Location(matcher.group(4));
                return new Instance(type, dateRange, person, location);
            } else {
                throw new RuntimeException("");
            }
        }

        @SuppressWarnings("checkstyle:DesignForExtension")
        public Type getType() {
            return type;
        }

        @SuppressWarnings("checkstyle:DesignForExtension")
        public DateRange getDateRange() {
            return dateRange;
        }

        @SuppressWarnings("checkstyle:DesignForExtension")
        public Person getPerson() {
            return person;
        }

        @SuppressWarnings("checkstyle:DesignForExtension")
        public Location getLocation() {
            return location;
        }

        @SuppressWarnings("checkstyle:DesignForExtension")
        public String toString() {
            return format("Instance [%s] Time [%s] Person [%s] Location [%s] (%s)",
                    type, dateRange, person.getName(), location.getName(),
                    System.identityHashCode(this));
        }
    }

    // Utilities and Beans

    public static final class Dates {

        private Dates() {
        }

        public static OffsetDateTime earliest(final OffsetDateTime first, final OffsetDateTime second) {
            return first.isBefore(second) ? first : second;
        }

        public static OffsetDateTime latest(final OffsetDateTime first, final OffsetDateTime second) {
            return first.isAfter(second) ? first : second;
        }
    }

    public static class DateRange {
        public static final DateRange INIT = new DateRange(OffsetDateTime.MAX, OffsetDateTime.MIN);
        private static final Pattern PATTERN = Pattern.compile("(.*) -> (.*)");
        private final OffsetDateTime start;
        private final OffsetDateTime finish;

        public DateRange(final OffsetDateTime start, final OffsetDateTime finish) {
            this.start = start;
            this.finish = finish;
        }

        public static DateRange parse(final String text) {
            final Matcher matcher = PATTERN.matcher(text);
            if (matcher.matches()) {
                final OffsetDateTime start = ceateOffsetDateTime(matcher.group(1));
                final OffsetDateTime finish = ceateOffsetDateTime(matcher.group(2));
                return new DateRange(start, finish);
            } else {
                throw new RuntimeException("");
            }
        }

        private static OffsetDateTime ceateOffsetDateTime(final String text) {
            return LocalTime.parse(text, DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))
                    .atDate(LocalDate.now())
                    .atOffset(TimeZone.getDefault().toZoneId().getRules().getOffset(LocalDateTime.now()));
        }

        @SuppressWarnings("checkstyle:DesignForExtension")
        public DateRange concat(final DateRange other) {
            return new DateRange(Dates.earliest(start, other.start), Dates.latest(finish, other.finish));
        }

        @SuppressWarnings("checkstyle:DesignForExtension")
        public boolean overlaps(final DateRange other) {
            return start.isBefore(other.finish) && finish.isAfter(other.start);
        }

        @SuppressWarnings("checkstyle:DesignForExtension")
        public OffsetDateTime getStart() {
            return start;
        }

        @SuppressWarnings("checkstyle:DesignForExtension")
        public OffsetDateTime getFinish() {
            return finish;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            DateRange dateRange = (DateRange) o;

            if (start != null ? !start.equals(dateRange.start) : dateRange.start != null) {
                return false;
            }
            return finish != null ? finish.equals(dateRange.finish) : dateRange.finish == null;
        }

        @Override
        @SuppressWarnings("checkstyle:MagicNumber")
        public int hashCode() {
            int result = start != null ? start.hashCode() : 0;
            result = 31 * result + (finish != null ? finish.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return format("%s -> %s",
                    start.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)),
                    finish.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)));
        }
    }

    public static class TypeAndDateRange {
        private final Type type;
        private final DateRange dateRange;

        public TypeAndDateRange(final Type type, final DateRange dateRange) {
            this.type = type;
            this.dateRange = dateRange;
        }

        @SuppressWarnings("checkstyle:DesignForExtension")
        public Type getType() {
            return type;
        }

        @SuppressWarnings("checkstyle:DesignForExtension")
        public DateRange getDateRange() {
            return dateRange;
        }

        @Override
        public String toString() {
            return "Key{type=" + type + ", dateRange=" + dateRange + '}';
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            TypeAndDateRange key = (TypeAndDateRange) o;

            if (type != key.type) {
                return false;
            }
            return dateRange != null ? dateRange.equals(key.dateRange) : key.dateRange == null;
        }

        @Override
        @SuppressWarnings("checkstyle:MagicNumber")
        public int hashCode() {
            int result = type != null ? type.hashCode() : 0;
            result = 31 * result + (dateRange != null ? dateRange.hashCode() : 0);
            return result;
        }
    }

    public enum Type {
        MEETING,
        TRAINING,
        COVER
    }

    public static class Person {
        private final String name;

        public Person(final String name) {
            this.name = name;
        }

        @SuppressWarnings("checkstyle:DesignForExtension")
        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return "Person{name='" + name + '\'' + '}';
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            Person person = (Person) o;

            return name != null ? name.equals(person.name) : person.name == null;
        }

        @Override
        public int hashCode() {
            return name != null ? name.hashCode() : 0;
        }
    }

    public static class Location {
        private final String name;

        public Location(final String name) {
            this.name = name;
        }

        @SuppressWarnings("checkstyle:DesignForExtension")
        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return "Location{name='" + name + '\'' + '}';
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            Location location = (Location) o;

            return name != null ? name.equals(location.name) : location.name == null;
        }

        @Override
        public int hashCode() {
            return name != null ? name.hashCode() : 0;
        }
    }

    private static List<Inst> createEvents(final String text) {
        return Arrays.stream(text.split("\n")).map(Instance::parse)
                .sorted(Comparator.comparing(instance -> instance.getPerson().getName()))
                .collect(toList());
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    private static List<Instance> createEvents(final int limit, final int persons) {
        return new Random().ints(1, persons).limit(limit).boxed().flatMap(count -> {
                    final DateRange range = randomDateRange();
                    final Type type = randomType();
                    final Location location = randomLocation();
                    return new Random().ints(1, 7).boxed().limit(count)
                            .distinct().map(i -> new Instance(type, range, person(i), location));
                })
                .sorted(Comparator.comparing(instance -> instance.getPerson().getName()))
                .collect(toList());
    }

    private static DateRange randomDateRange() {
        final int start = new Random().nextInt(10) * 30;
        final int finish = start + 30 + new Random().nextInt(5) * 30;
        return new DateRange(
                OffsetDateTime.now().plus(start, ChronoUnit.MINUTES).truncatedTo(ChronoUnit.MINUTES),
                OffsetDateTime.now().plus(finish, ChronoUnit.MINUTES).truncatedTo(ChronoUnit.MINUTES)
        );
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    private static Type randomType() {
        return Type.values()[new Random().nextInt(2)];
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    private static Location randomLocation() {
        final List<String> names =
                List.of("Drama Hall", "Sports Hall", "Office", "Room A", "Room B", "Room C", "Chemistry 1");
        return new Location(names.get(new Random().nextInt(6)));
    }

    private static Person person(final int i) {
        final List<String> names = List.of("Hans", "Richard", "Mike", "Flora", "Cathy", "Marco", "Harsha");
        return new Person(names.get(i));
    }
}
