create table teacher
(
    id1  uuid not null,
    id2  uuid not null,
    code varchar(255),
    primary key (id1, id2)
);
create table study_activity_trait
(
    id1 uuid not null,
    id2 uuid not null,
    primary key (id1, id2)
);
create table support_trait
(
    id1         uuid not null,
    id2         uuid not null,
    teacher_id1 uuid,
    teacher_id2 uuid,
    primary key (id1, id2)
);
create table teaching_trait
(
    id1         uuid not null,
    id2         uuid not null,
    teacher_id1 uuid,
    teacher_id2 uuid,
    primary key (id1, id2)
);
create table yard_duty_trait
(
    id1         uuid not null,
    id2         uuid not null,
    teacher_id1 uuid,
    teacher_id2 uuid,
    primary key (id1, id2)
);
alter table if exists study_activity_trait
    add constraint FKmgnmvafac46m42w3ff8kqpnda foreign key (id1, id2) references teacher deferrable initially deferred;
alter table if exists support_trait
    add constraint FK_9wx40g84dx74bnjx5el6lk8pl foreign key (teacher_id1, teacher_id2) references teacher deferrable initially deferred;
alter table if exists teaching_trait
    add constraint FK_2omlqob05l9ix8piemxgnr8mk foreign key (teacher_id1, teacher_id2) references teacher deferrable initially deferred;
alter table if exists yard_duty_trait
    add constraint FK_3bkw77eu2ilc8mac8au3r111i foreign key (teacher_id1, teacher_id2) references teacher deferrable initially deferred;
