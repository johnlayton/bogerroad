package org.bogerroad;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.webmvc.spi.BackendIdConverter;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import java.lang.reflect.Field;

import javax.persistence.*;
import javax.transaction.Transactional;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.util.*;

import static java.nio.charset.StandardCharsets.UTF_8;
import static javax.persistence.CascadeType.ALL;
import static javax.persistence.CascadeType.REFRESH;

@SpringBootApplication(scanBasePackages = {
        "org.bogerroad"
})
@EnableJpaRepositories(considerNestedRepositories = true)
public class BulkTraitApplication {
    private static final Logger LOGGER = LoggerFactory.getLogger(BulkTraitApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(BulkTraitApplication.class, args);
    }

    @Component
    public static class TimeFactory {
        public String getTime() {
            return Clock.systemUTC().instant().toString();
        }
    }

    @Component
    public static class BulkIdGenerator implements IdentifierGenerator {

        private final TimeFactory timeFactory;

        public BulkIdGenerator(TimeFactory timeFactory) {
            this.timeFactory = timeFactory;
        }

        @Override
        public void configure(Type type, Properties params, ServiceRegistry serviceRegistry) throws MappingException {
        }

        @Override
        public Serializable generate(SharedSessionContractImplementor session, Object object) throws HibernateException {

//            session.getFactory().

//            session.getTenantIdentifier()
//            UUIDGenerator
            LOGGER.warn("Create Id .... " + timeFactory.getTime());
            return new BulkId(UUID.randomUUID(), UUID.randomUUID());
//            String prefix = "STUDENT";
//            String suffix = "";
//            try {
//                Connection connection = session.connection();
//                Statement statement = connection.createStatement();
//                ResultSet resultSet = statement.executeQuery("select count(id) from student");
//                if(resultSet.next()) {
//                    Integer id = resultSet.getInt(1) + 1;
//                    suffix = id.toString();
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            return prefix + suffix;
        }
    }



    @Embeddable
    public static class BulkId implements Serializable {
//        @GenericGenerator(name = "uuid2", strategy = "uuid2")
//        @GeneratedValue
        private UUID id1;

//        @GenericGenerator(name = "uuid2", strategy = "uuid2")
//        @GeneratedValue
        private UUID id2;

        public BulkId() {
        }

        public BulkId(UUID id1, UUID id2) {
            this.id1 = id1;
            this.id2 = id2;
        }

        public void setId1(UUID id1) {
            this.id1 = id1;
        }

        public void setId2(UUID id2) {
            this.id2 = id2;
        }

        public UUID getId1() {
            return id1;
        }

        public UUID getId2() {
            return id2;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            BulkId bulkId = (BulkId) o;

            if (!Objects.equals(id1, bulkId.id1)) return false;
            return Objects.equals(id2, bulkId.id2);
        }

        @Override
        public int hashCode() {
            int result = id1 != null ? id1.hashCode() : 0;
            result = 31 * result + (id2 != null ? id2.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "BulkId{id1=" + id1 + ", id2=" + id2 + '}';
        }
    }

    @Entity(name = "Teacher")
    public static class Teacher {

//        @Id
//        @GenericGenerator(name = "uuid2", strategy = "uuid2")
//        @GeneratedValue
        @EmbeddedId
        @GenericGenerator(name = "BulkId", strategy = "org.bogerroad.BulkTraitApplication$BulkIdGenerator")
        @GeneratedValue(generator = "BulkId")
//        @GeneratedValue
        private BulkId id;

        private String code;

//        @OneToMany ///(cascade = CascadeType.ALL)
//        @OneToMany(mappedBy = "teacher", fetch = FetchType.LAZY, cascade = {REFRESH})
//        @OneToMany //(mappedBy = "teacher")
//        @OneToMany(mappedBy = "teacher", cascade = {ALL})
        @OneToMany(mappedBy = "teacher", fetch = FetchType.LAZY, cascade = {REFRESH})
        private Set<Trait> traits;

//        @Cascade(org.hibernate.annotations.CascadeType.ALL)
        @OneToOne(mappedBy = "teacher", fetch = FetchType.LAZY, cascade = {ALL})
        private StudyActivityTrait studyTrait;

        public Teacher() {
        }

        public Teacher(final String code,
                       final Set<Trait> traits) {
            this.code = code;
            this.traits = traits;
        }

        public String getCode() {
            return code;
        }

        public Set<Trait> getTraits() {
            return traits;
        }

        public StudyActivityTrait getStudyTrait() {
            return studyTrait;
        }

        public void setStudyTrait(final StudyActivityTrait studyTrait) {
            studyTrait.setTeacher(this);
            this.studyTrait = studyTrait;
        }
    }

    public interface Visitor<T> {
        T visit(T1 t1);
        T visit(T2 t1);
    }

    public interface Visitable {
        public <T> T accept(Visitor<T> visitor);
    }

    public class T1 implements Visitable {
        @Override
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visit(this);
        }
    }

    public class T2 implements Visitable {
        @Override
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visit(this);
        }
    }

    @Entity
    @Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
    public abstract static class Trait {
//        @Id
//        @GenericGenerator(name = "uuid2", strategy = "uuid2")
//        @GeneratedValue
//        private UUID id;
        @EmbeddedId
        @GenericGenerator(name = "BulkId", strategy = "org.bogerroad.BulkTraitApplication$BulkIdGenerator")
        @GeneratedValue(generator = "BulkId")
        private BulkId id;

//        @JoinColumn(name = "teacher_id")
        @ManyToOne
        private Teacher teacher;

    }

    @Entity(name = "SupportTrait")
    public static class SupportTrait extends Trait {
    }

    @Entity(name = "TeachingTrait")
    public static class TeachingTrait extends Trait {
    }

    @Entity(name = "YardDutyTrait")
    public static class YardDutyTrait extends Trait {
    }

    @Entity(name = "StudyActivityTrait")
    public static class StudyActivityTrait { //extends Trait {

//        @GenericGenerator(name = "BulkId", strategy = "org.bogerroad.BulkTraitApplication$BulkIdGenerator")
//        @GeneratedValue(generator = "BulkId")
        @EmbeddedId
        private BulkId id;

//        @Id
//        @GenericGenerator(name = "uuid2", strategy = "uuid2")
//        @GeneratedValue
//        @MapsId("")
        @MapsId
        @OneToOne
        @JoinColumn(name = "id1", referencedColumnName = "id1")
        @JoinColumn(name = "id2", referencedColumnName = "id2")
        private Teacher teacher;

        public void setTeacher(Teacher teacher) {
            this.teacher = teacher;
        }

//        @OneToOne(fetch = FetchType.LAZY, cascade = {ALL})
//        @MapsId
//        @OneToOne
//        @OneToOne(fetch = FetchType.LAZY, cascade = {ALL})
//        @JoinColumn(name = "teacherId")
//        @Cascade(org.hibernate.annotations.CascadeType.SAVE_UPDATE)
//        private Teacher teacher;

    }

    //    @Repository
    @RepositoryRestResource(collectionResourceRel = "teachers", path = "teachers")
    public interface TeacherRepository extends JpaRepository<Teacher, BulkId> {
    }

    @Service
    public static class TeachingService {

        private final TeacherRepository repository;

        public TeachingService(TeacherRepository repository) {
            this.repository = repository;
        }

        @Transactional
        public void createTeacher(final Teacher teacher) {
            repository.save(teacher);
        }

        public void deleteAll() {
            repository.deleteAll();
        }
    }

    @Component
    public static class DataLoader {

        private final TeachingService service;

        public DataLoader(TeachingService service) {
            this.service = service;
        }

        @EventListener
        public void onApplicationReadyEvent(final ApplicationReadyEvent event) {
            service.deleteAll();
//            service.createTeacher(new Teacher("A", Set.of(new TeachingTrait())));
//            service.createTeacher(new Teacher("B", Set.of(new TeachingTrait())));
//            service.createTeacher(new Teacher("C", Set.of(new TeachingTrait())));
            service.createTeacher(new Teacher("D", Set.of(new TeachingTrait())));
            final Teacher teacher = new Teacher("E", Set.of(new TeachingTrait()));
//            service.createTeacher(teacher);
            teacher.setStudyTrait(new StudyActivityTrait());
            service.createTeacher(teacher);
        }
    }


    @Component
    public static class EmbeddedBackendIdConverter implements BackendIdConverter {
        private final ObjectMapper objectMapper;

        public EmbeddedBackendIdConverter(final ObjectMapper objectMapper) {
            this.objectMapper = objectMapper;
        }

        @Override
        public Serializable fromRequestId(String id, Class<?> entityType) {
            return getFieldWithEmbeddedAnnotation(entityType)
                    .map(Field::getType)
                    .map(ret -> {
                        try {
                            String decodedId = new String(Base64.getUrlDecoder().decode(id));
                            return (Serializable) objectMapper.readValue(decodedId, ret);
                        } catch (final JsonProcessingException ignored) {
                            return null;
                        }
                    })
                    .orElse(id);
        }

        @Override
        public String toRequestId(Serializable id, Class<?> entityType) {
            try {
                String json = objectMapper.writeValueAsString(id);
                return Base64.getUrlEncoder().encodeToString(json.getBytes(UTF_8));
            } catch (final JsonProcessingException ignored) {
                return id.toString();
            }
        }

        @Override
        public boolean supports(Class<?> entity) {
            return isEmbeddedIdAnnotationPresent(entity);
        }

        private boolean isEmbeddedIdAnnotationPresent(Class<?> entity) {
            return getFieldWithEmbeddedAnnotation(entity)
                    .isPresent();
        }

        private static Optional<Field> getFieldWithEmbeddedAnnotation(Class<?> entity) {
            return Arrays.stream(entity.getDeclaredFields())
                    .filter(method -> method.isAnnotationPresent(EmbeddedId.class))
                    .findFirst();
        }
    }

}
