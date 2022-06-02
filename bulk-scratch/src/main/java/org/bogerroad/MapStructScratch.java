package org.bogerroad;


//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
import org.bogerroad.MapStructScratch.Input.IdWrapper;
import org.bogerroad.MapStructScratch.Input.Source;
import org.bogerroad.MapStructScratch.Input.Id;
import org.bogerroad.MapStructScratch.Input.Complex;
import org.bogerroad.MapStructScratch.Input.Things;
import org.bogerroad.MapStructScratch.Input.Thing;
import org.bogerroad.MapStructScratch.Output.Od;
import org.bogerroad.MapStructScratch.Output.OdWrapper;
import org.bogerroad.MapStructScratch.Output.Target;
import org.bogerroad.MapStructScratch.Output.Widget;
import org.mapstruct.AfterMapping;
import org.mapstruct.BeforeMapping;
import org.mapstruct.Context;
import org.mapstruct.DecoratedWith;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.Qualifier;
import org.mapstruct.TargetType;
import org.mapstruct.factory.Mappers;

import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.bogerroad.MapStructScratch.Input.Thing.thing;
import static org.bogerroad.MapStructScratch.Input.Things.things;

@SuppressWarnings("checkstyle:HideUtilityClassConstructor")
public class MapStructScratch {

    public static void main(final String[] args) {
        final BasicContext context = new BasicContext();
        System.out.println(Mappers.getMapper(BasicMapper.class).map(
                new Source(new Id(UUID.randomUUID(), UUID.randomUUID()),
                        things(thing("A", 1), thing("B", 2)),
                        "A", "B", "C"),
                context
        ));
        System.out.println(Mappers.getMapper(BasicMapper.class).map(
                new Target(UUID.randomUUID().toString(),
                        List.of(new Widget("A-1"), new Widget("B-2"), new Widget("C-3")),
                        "A-B-C")));
        System.out.println(context);

        final BasicContext ctx = new BasicContext();
        System.out.println(Mappers.getMapper(WrapperMapper.class).map(
                new IdWrapper(new Id(UUID.randomUUID(), UUID.randomUUID())), ctx));
        System.out.println(Mappers.getMapper(WrapperMapper.class).map(
                new OdWrapper(new Od(UUID.randomUUID().toString(), UUID.randomUUID().toString())), ctx));
        System.out.println(ctx);

    }

    public static class Input {

        public static class Id {
            private final UUID id1;
            private final UUID id2;

            public Id(final UUID id1, final UUID id2) {
                this.id1 = id1;
                this.id2 = id2;
            }

            @SuppressWarnings("checkstyle:DesignForExtension")
            public UUID getId1() {
                return id1;
            }

            @SuppressWarnings("checkstyle:DesignForExtension")
            public UUID getId2() {
                return id2;
            }

            @Override
            public String toString() {
                return "Id{id1=" + id1 + ", id2=" + id2 + '}';
            }
        }

        public static class Complex {
            private final String a;
            private final String b;
            private final String c;

            public Complex(final String a, final String b, final String c) {
                this.a = a;
                this.b = b;
                this.c = c;
            }

            @SuppressWarnings("checkstyle:DesignForExtension")
            public String getA() {
                return a;
            }

            @SuppressWarnings("checkstyle:DesignForExtension")
            public String getB() {
                return b;
            }

            @SuppressWarnings("checkstyle:DesignForExtension")
            public String getC() {
                return c;
            }

            @Override
            public String toString() {
                return "Complex{a='" + a + '\'' + ", b='" + b + '\'' + ", c='" + c + '\'' + '}';
            }
        }

        public static class Thing {
            private final String a;
            private final Integer b;

            public Thing(final String a, final Integer b) {
                this.a = a;
                this.b = b;
            }

            public static Thing thing(final String a, final Integer b) {
                return new Thing(a, b);
            }

            @SuppressWarnings("checkstyle:DesignForExtension")
            public String getA() {
                return a;
            }

            @SuppressWarnings("checkstyle:DesignForExtension")
            public Integer getB() {
                return b;
            }

            @Override
            public String toString() {
                return "Thing{a='" + a + '\'' + ", b='" + b + '\'' + '}';
            }
        }

        public static class Things {
            private final List<Thing> things;

            public Things(final List<Thing> things) {
                this.things = things;
            }

            public static Things things(final Thing... things) {
                return new Things(List.of(things));
            }

            @SuppressWarnings("checkstyle:DesignForExtension")
            public List<Thing> getThings() {
                return things;
            }

            @Override
            public String toString() {
                return "Things{things=" + things + '}';
            }
        }

        public static class Source {
            private final Id id;
            private final Things things;
            private final String a;
            private final String b;
            private final String c;

            public Source(final Id id, final Things things, final String a, final String b, final String c) {
                this.id = id;
                this.things = things;
                this.a = a;
                this.b = b;
                this.c = c;
            }

            @SuppressWarnings("checkstyle:DesignForExtension")
            public Id getId() {
                return id;
            }

            @SuppressWarnings("checkstyle:DesignForExtension")
            public Things getThings() {
                return things;
            }

            @SuppressWarnings("checkstyle:DesignForExtension")
            public String getA() {
                return a;
            }

            @SuppressWarnings("checkstyle:DesignForExtension")
            public String getB() {
                return b;
            }

            @SuppressWarnings("checkstyle:DesignForExtension")
            public String getC() {
                return c;
            }

            @Override
            public String toString() {
                return "Source{id=" + id + ", things='" + things + '\'' + ", a='" + a + '\'' + ", b='" + b + '\'' + ", c='" + c + '\'' + '}';
            }
        }

        public static class IdWrapper {
            private final Id id;

            public IdWrapper(final Id id) {
                this.id = id;
            }

            @SuppressWarnings("checkstyle:DesignForExtension")
            public Id getId() {
                return id;
            }

            @Override
            public String toString() {
                return "IdWrapper{id=" + id + '}';
            }
        }

    }

    public static class Output {

        public static class Od {
            private final String id1;
            private final String id2;

            public Od(final String id1, final String id2) {
                this.id1 = id1;
                this.id2 = id2;
            }

            @SuppressWarnings("checkstyle:DesignForExtension")
            public String getId1() {
                return id1;
            }

            @SuppressWarnings("checkstyle:DesignForExtension")
            public String getId2() {
                return id2;
            }

            @Override
            public String toString() {
                return "Od{id1=" + id1 + ", id2=" + id2 + '}';
            }
        }

        public static final class Widget {
            private final String text;

            public Widget(final String text) {
                this.text = text;
            }

            public String getText() {
                return text;
            }

            @Override
            public String toString() {
                return "Widget{text='" + text + '\'' + '}';
            }
        }

        public static class Target {
            private final String id;
            private final String complex;
            private final List<Widget> widgets;

            public Target(final String id, final List<Widget> widgets, final String complex) {
                this.id = id;
                this.widgets = widgets;
                this.complex = complex;
            }

            @SuppressWarnings("checkstyle:DesignForExtension")
            public String getId() {
                return id;
            }

            @SuppressWarnings("checkstyle:DesignForExtension")
            public String getComplex() {
                return complex;
            }

            @SuppressWarnings("checkstyle:DesignForExtension")
            public List<Widget> getWidgets() {
                return widgets;
            }

            @Override
            public String toString() {
                return "Target{id='" + id + '\'' + ", complex='" + complex + '\'' + ", widgets=" + widgets + '}';
            }
        }

        public static class OdWrapper {
            private final Od od;

            public OdWrapper(final Od od) {
                this.od = od;
            }

            @SuppressWarnings("checkstyle:DesignForExtension")
            public Od getOd() {
                return od;
            }

            @Override
            public String toString() {
                return "OdWrapper{od=" + od + '}';
            }
        }
    }

    @Mapper
    public static class UUIDMapper {
        @SuppressWarnings("checkstyle:DesignForExtension")
        public String asString(final UUID source) {
            return source.toString();
        }

        @SuppressWarnings("checkstyle:DesignForExtension")
        public UUID asUUID(final String source) {
            return UUID.fromString(source);
        }
    }

    @Mapper(uses = UUIDMapper.class)
    public abstract static class IdMapper {

        public abstract Od map(Id id, @Context BasicContext context);

        public abstract Id map(Od od, @Context BasicContext context);

        @SuppressWarnings("checkstyle:DesignForExtension")
        public String asString(final Id source) {
            return source.getId1().toString();
        }

        @SuppressWarnings("checkstyle:DesignForExtension")
        public Id asId(final String source) {
            return new Id(UUID.fromString(source), null);
        }
    }

//    @Mapper
//    public abstract static class ThingMapper {
//        @SuppressWarnings("checkstyle:DesignForExtension")
//        @Mapping(target = "text", ignore = true)
//        public Widget map(final Thing thing, @Context final BasicContext context) {
//            return new Widget(String.format("%s-%d", thing.getA(), thing.getB()));
////            return things.getThings().stream()
////                    .map(thing -> {
////                        return new Widget(String.format("%s-%d", thing.getA(), thing.getB()));
////                    })
////                    .collect(Collectors.toList());
//        }
//
//        @SuppressWarnings("checkstyle:DesignForExtension")
//        @InheritInverseConfiguration
//        @Mapping(target = "a", ignore = true)
//        @Mapping(target = "b", ignore = true)
//        public Thing map(final Widget widget, @Context final BasicContext context) {
//            final String[] strings = widget.getText().split("-");
//            return new Thing(strings[0], Integer.parseInt(strings[1]));
////            return new Things(widgets.stream()
////                    .map(widget -> {
////                        final String[] strings = widget.getText().split("-");
////                        return new Thing(strings[0], Integer.parseInt(strings[1]));
////                    })
////                    .collect(Collectors.toList()));
//        }
//    }

//    @Mapper
//    public interface ThingsMapper {
//        @IterableMapping(qualifiedByName = "ToThing")
//        List<Thing> mapToThings(List<Widget> source, @Context BasicContext context);
//        @IterableMapping(qualifiedByName = "ToWidget")
//        List<Widget> mapToWidgets(List<Thing> source, @Context BasicContext context);
//
//        @Named("ToThing")
//        default Thing toWidget(final Widget widget) {
//            final String[] strings = widget.getText().split("-");
//            return new Thing(strings[0], Integer.parseInt(strings[1]));
//        }
//
//        @Named("ToWidget")
//        default Widget toThing(final Thing thing) {
//            return new Widget(String.format("%s-%d", thing.getA(), thing.getB()));
//        }
//    }

    public static class ThingsMapper {
        @SuppressWarnings("checkstyle:DesignForExtension")
        public List<Widget> toWidgets(final Things things, final @Context BasicContext context) {
            return things.getThings().stream()
                    .map(thing -> {
                        return new Widget(String.format("%s-%d", thing.getA(), thing.getB()));
                    })
                    .collect(Collectors.toList());
        }

        @SuppressWarnings("checkstyle:DesignForExtension")
        public Things toThings(final List<Widget> widgets) {
            return new Things(widgets.stream()
                    .map(widget -> {
                        final String[] strings = widget.getText().split("-");
                        return new Thing(strings[0], Integer.parseInt(strings[1]));
                    })
                    .collect(Collectors.toList()));
        }
    }

    public abstract static class BasicDecorator implements BasicMapper {

        private final BasicMapper delegate;

        public BasicDecorator(final BasicMapper delegate) {
            this.delegate = delegate;
        }

        @Override
        public Target map(final Source source, @Context final BasicContext context) {
            return delegate.map(source, context);
        }
    }

    @Mapper(uses = IdMapper.class)
    public interface WrapperMapper {
        @Mapping(source = "id", target = "od")
        OdWrapper map(IdWrapper source, @Context BasicContext context);

        @InheritInverseConfiguration
        IdWrapper map(OdWrapper source, @Context BasicContext context);
    }

    @Mapper(uses = {
            UUIDMapper.class,
            IdMapper.class,
            ThingsMapper.class
    })
    @DecoratedWith(BasicDecorator.class)
    public interface BasicMapper {

//        BasicMapper INSTANCE = Mappers.getMapper(BasicMapper.class);

        //        @Mapping(source = "id.id1", target = "id")
        @Mapping(source = ".", target = "complex", qualifiedByName = "MapComplex")
        @Mapping(source = "things", target = "widgets")
        Target map(Source source, @Context BasicContext context);

        @InheritInverseConfiguration
/*
        @Mapping(source = "complex", target = "a", qualifiedByName = "ExtractA")
        @Mapping(source = "complex", target = "b", qualifiedByName = "ExtractB")
        @Mapping(source = "complex", target = "c", qualifiedByName = "ExtractC")
*/
        @Mapping(source = "complex", target = "a", qualifiedBy = ExtractA.class)
        @Mapping(source = "complex", target = "b", qualifiedBy = ExtractB.class)
        @Mapping(source = "complex", target = "c", qualifiedBy = ExtractC.class)
        @Mapping(source = "widgets", target = "things")
        Source map(Target source);

/*
        default String map(UUID id) {
            return id.toString();
        }

        default UUID map(String id) {
            return UUID.fromString(id);
        }
*/

        /*
                default List<Widget> toWidgets(final Things things, @Context BasicContext context) {
                    return things.getThings().stream()
                            .map(thing -> {
                                return new Widget(String.format("%s-%d", thing.getA(), thing.getB()));
                            })
                            .collect(Collectors.toList());
                }

                default Things toThings(final List<Widget> widgets) {
                    return new Things(widgets.stream()
                            .map(widget -> {
                                final String[] strings = widget.getText().split("-");
                                return new Thing(strings[0], Integer.parseInt(strings[1]));
                            })
                            .collect(Collectors.toList()));
                }
        */
        @Named("ExtractA")
        @ExtractA
        default String extractA(String source) {
            return source.split("-")[0];
        }

        @Named("ExtractB")
        @ExtractB
        default String extractB(String source) {
            return source.split("-")[1];
        }

        @Named("ExtractC")
        @ExtractC
        default String extractC(String source) {
            return source.split("-")[2];
        }

        @Named("MapComplex")
        default String mapComplex(Source source) {
            return String.format("%s-%s-%s", source.getA(), source.getB(), source.getC());
        }

        @AfterMapping
        default void doAfter(Source source, @MappingTarget Target target) {
/*
            System.out.println(String.format("Source %s -> Target %s", source, target));
*/
        }

        @AfterMapping
        default void doAfter(Target source, @MappingTarget Source target) {
/*
            System.out.println(String.format("Source %s -> Target %s", source, target));
*/
        }
    }

    public static class BasicContext {
        private final Map<Object, Object> knownInstances = new IdentityHashMap<>();

        @BeforeMapping
        @SuppressWarnings("checkstyle:DesignForExtension")
        public <T> T getMappedInstance(final Object source, @TargetType final Class<T> targetType) {
            System.out.printf("Looking for %s%n", source);
            return targetType.cast(knownInstances.get(source));
        }

        @BeforeMapping
        @SuppressWarnings("checkstyle:DesignForExtension")
        public void storeMappedInstance(final Object source, @MappingTarget final Object target) {
            knownInstances.put(source, target);
        }

        @Override
        public String toString() {
            return knownInstances.entrySet().stream()
                    .map(entry -> String.format("%s -> %s", entry.getKey(), entry.getValue()))
                    .collect(Collectors.joining("\n"));
        }
    }


    @Qualifier
    @java.lang.annotation.Target(ElementType.METHOD)
    @java.lang.annotation.Retention(RetentionPolicy.CLASS)
    @interface ExtractA {
    }

    @Qualifier
    @java.lang.annotation.Target(ElementType.METHOD)
    @java.lang.annotation.Retention(RetentionPolicy.CLASS)
    @interface ExtractB {
    }

    @Qualifier
    @java.lang.annotation.Target(ElementType.METHOD)
    @java.lang.annotation.Retention(RetentionPolicy.CLASS)
    @interface ExtractC {
    }
}
