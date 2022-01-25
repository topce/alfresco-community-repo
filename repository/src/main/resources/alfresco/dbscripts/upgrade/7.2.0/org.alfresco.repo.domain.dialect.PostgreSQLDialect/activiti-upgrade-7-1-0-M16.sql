insert into ACT_GE_PROPERTY
values ('schema.version', '7.1.0-M16', 1);

insert into ACT_GE_PROPERTY
values ('schema.history', 'create(7.1.0-M16)', 1);

alter table ACT_RE_DEPLOYMENT add KEY_ varchar(255);
alter table ACT_RE_DEPLOYMENT add ENGINE_VERSION_ varchar(255);
alter table ACT_RE_DEPLOYMENT add VERSION_ integer default 1;
alter table ACT_RE_DEPLOYMENT add PROJECT_RELEASE_VERSION_ varchar(255);


alter table ACT_RU_EXECUTION add ROOT_PROC_INST_ID_ varchar(64);
alter table ACT_RU_EXECUTION add IS_MI_ROOT_ smallint check(IS_MI_ROOT_ in (1,0));
alter table ACT_RU_EXECUTION add START_TIME_ timestamp;
alter table ACT_RU_EXECUTION add START_USER_ID_ varchar(255);
alter table ACT_RU_EXECUTION add IS_COUNT_ENABLED_ smallint check(IS_COUNT_ENABLED_ in (1,0));
alter table ACT_RU_EXECUTION add EVT_SUBSCR_COUNT_ integer;
alter table ACT_RU_EXECUTION add TASK_COUNT_ integer;
alter table ACT_RU_EXECUTION add JOB_COUNT_ integer;
alter table ACT_RU_EXECUTION add TIMER_JOB_COUNT_ integer;
alter table ACT_RU_EXECUTION add SUSP_JOB_COUNT_ integer;
alter table ACT_RU_EXECUTION add DEADLETTER_JOB_COUNT_ integer;
alter table ACT_RU_EXECUTION add VAR_COUNT_ integer;
alter table ACT_RU_EXECUTION add ID_LINK_COUNT_ integer;
alter table ACT_RU_EXECUTION add APP_VERSION_ integer;

create table ACT_RU_TIMER_JOB (
    ID_ varchar(64) NOT NULL,
    REV_ integer,
    TYPE_ varchar(255) NOT NULL,
    LOCK_EXP_TIME_ timestamp,
    LOCK_OWNER_ varchar(255),
    EXCLUSIVE_ boolean,
    EXECUTION_ID_ varchar(64),
    PROCESS_INSTANCE_ID_ varchar(64),
    PROC_DEF_ID_ varchar(64),
    RETRIES_ integer,
    EXCEPTION_STACK_ID_ varchar(64),
    EXCEPTION_MSG_ varchar(4000),
    DUEDATE_ timestamp,
    REPEAT_ varchar(255),
    HANDLER_TYPE_ varchar(255),
    HANDLER_CFG_ varchar(4000),
    TENANT_ID_ varchar(255) default '',
    primary key (ID_)
);

create table ACT_RU_SUSPENDED_JOB (
    ID_ varchar(64) NOT NULL,
    REV_ integer,
    TYPE_ varchar(255) NOT NULL,
    EXCLUSIVE_ boolean,
    EXECUTION_ID_ varchar(64),
    PROCESS_INSTANCE_ID_ varchar(64),
    PROC_DEF_ID_ varchar(64),
    RETRIES_ integer,
    EXCEPTION_STACK_ID_ varchar(64),
    EXCEPTION_MSG_ varchar(4000),
    DUEDATE_ timestamp,
    REPEAT_ varchar(255),
    HANDLER_TYPE_ varchar(255),
    HANDLER_CFG_ varchar(4000),
    TENANT_ID_ varchar(255) default '',
    primary key (ID_)
);

create table ACT_RU_DEADLETTER_JOB (
    ID_ varchar(64) NOT NULL,
    REV_ integer,
    TYPE_ varchar(255) NOT NULL,
    EXCLUSIVE_ boolean,
    EXECUTION_ID_ varchar(64),
    PROCESS_INSTANCE_ID_ varchar(64),
    PROC_DEF_ID_ varchar(64),
    EXCEPTION_STACK_ID_ varchar(64),
    EXCEPTION_MSG_ varchar(4000),
    DUEDATE_ timestamp,
    REPEAT_ varchar(255),
    HANDLER_TYPE_ varchar(255),
    HANDLER_CFG_ varchar(4000),
    TENANT_ID_ varchar(255) default '',
    primary key (ID_)
);

alter table ACT_RE_PROCDEF add ENGINE_VERSION_ varchar(255);
alter table ACT_RE_PROCDEF add APP_VERSION_ integer;

alter table ACT_RU_TASK add BUSINESS_KEY_ varchar(255);
alter table ACT_RU_TASK add CLAIM_TIME_ timestamp;
alter table ACT_RU_TASK add APP_VERSION_ integer;

create table ACT_RU_INTEGRATION (
    ID_ varchar(64) not null,
    EXECUTION_ID_ varchar(64),
    PROCESS_INSTANCE_ID_ varchar(64),
    PROC_DEF_ID_ varchar(64),
    FLOW_NODE_ID_ varchar(64),
    CREATED_DATE_ timestamp,
    primary key (ID_)
);

create index ACT_IDX_EXE_ROOT on ACT_RU_EXECUTION(ROOT_PROC_INST_ID_);

create index ACT_IDX_JOB_EXECUTION_ID on ACT_RU_JOB(EXECUTION_ID_);
alter table ACT_RU_JOB
    add constraint ACT_FK_JOB_EXECUTION
    foreign key (EXECUTION_ID_)
    references ACT_RU_EXECUTION (ID_);

create index ACT_IDX_JOB_PROCESS_INSTANCE_ID on ACT_RU_JOB(PROCESS_INSTANCE_ID_);
alter table ACT_RU_JOB
    add constraint ACT_FK_JOB_PROCESS_INSTANCE
    foreign key (PROCESS_INSTANCE_ID_)
    references ACT_RU_EXECUTION (ID_);

create index ACT_IDX_JOB_PROC_DEF_ID on ACT_RU_JOB(PROC_DEF_ID_);
alter table ACT_RU_JOB
    add constraint ACT_FK_JOB_PROC_DEF
    foreign key (PROC_DEF_ID_)
    references ACT_RE_PROCDEF (ID_);

create index ACT_IDX_TIMER_JOB_EXECUTION_ID on ACT_RU_TIMER_JOB(EXECUTION_ID_);
alter table ACT_RU_TIMER_JOB
    add constraint ACT_FK_TIMER_JOB_EXECUTION
    foreign key (EXECUTION_ID_)
    references ACT_RU_EXECUTION (ID_);

create index ACT_IDX_TIMER_JOB_PROCESS_INSTANCE_ID on ACT_RU_TIMER_JOB(PROCESS_INSTANCE_ID_);
alter table ACT_RU_TIMER_JOB
    add constraint ACT_FK_TIMER_JOB_PROCESS_INSTANCE
    foreign key (PROCESS_INSTANCE_ID_)
    references ACT_RU_EXECUTION (ID_);

create index ACT_IDX_TIMER_JOB_PROC_DEF_ID on ACT_RU_TIMER_JOB(PROC_DEF_ID_);
alter table ACT_RU_TIMER_JOB
    add constraint ACT_FK_TIMER_JOB_PROC_DEF
    foreign key (PROC_DEF_ID_)
    references ACT_RE_PROCDEF (ID_);

create index ACT_IDX_TIMER_JOB_EXCEPTION on ACT_RU_TIMER_JOB(EXCEPTION_STACK_ID_);
alter table ACT_RU_TIMER_JOB
    add constraint ACT_FK_TIMER_JOB_EXCEPTION
    foreign key (EXCEPTION_STACK_ID_)
    references ACT_GE_BYTEARRAY (ID_);

create index ACT_IDX_SUSPENDED_JOB_EXECUTION_ID on ACT_RU_SUSPENDED_JOB(EXECUTION_ID_);
alter table ACT_RU_SUSPENDED_JOB
    add constraint ACT_FK_SUSPENDED_JOB_EXECUTION
    foreign key (EXECUTION_ID_)
    references ACT_RU_EXECUTION (ID_);

create index ACT_IDX_SUSPENDED_JOB_PROCESS_INSTANCE_ID on ACT_RU_SUSPENDED_JOB(PROCESS_INSTANCE_ID_);
alter table ACT_RU_SUSPENDED_JOB
    add constraint ACT_FK_SUSPENDED_JOB_PROCESS_INSTANCE
    foreign key (PROCESS_INSTANCE_ID_)
    references ACT_RU_EXECUTION (ID_);

create index ACT_IDX_SUSPENDED_JOB_PROC_DEF_ID on ACT_RU_SUSPENDED_JOB(PROC_DEF_ID_);
alter table ACT_RU_SUSPENDED_JOB
    add constraint ACT_FK_SUSPENDED_JOB_PROC_DEF
    foreign key (PROC_DEF_ID_)
    references ACT_RE_PROCDEF (ID_);

create index ACT_IDX_SUSPENDED_JOB_EXCEPTION on ACT_RU_SUSPENDED_JOB(EXCEPTION_STACK_ID_);
alter table ACT_RU_SUSPENDED_JOB
    add constraint ACT_FK_SUSPENDED_JOB_EXCEPTION
    foreign key (EXCEPTION_STACK_ID_)
    references ACT_GE_BYTEARRAY (ID_);

create index ACT_IDX_DEADLETTER_JOB_EXECUTION_ID on ACT_RU_DEADLETTER_JOB(EXECUTION_ID_);
alter table ACT_RU_DEADLETTER_JOB
    add constraint ACT_FK_DEADLETTER_JOB_EXECUTION
    foreign key (EXECUTION_ID_)
    references ACT_RU_EXECUTION (ID_);

create index ACT_IDX_DEADLETTER_JOB_PROCESS_INSTANCE_ID on ACT_RU_DEADLETTER_JOB(PROCESS_INSTANCE_ID_);
alter table ACT_RU_DEADLETTER_JOB
    add constraint ACT_FK_DEADLETTER_JOB_PROCESS_INSTANCE
    foreign key (PROCESS_INSTANCE_ID_)
    references ACT_RU_EXECUTION (ID_);

create index ACT_IDX_DEADLETTER_JOB_PROC_DEF_ID on ACT_RU_DEADLETTER_JOB(PROC_DEF_ID_);
alter table ACT_RU_DEADLETTER_JOB
    add constraint ACT_FK_DEADLETTER_JOB_PROC_DEF
    foreign key (PROC_DEF_ID_)
    references ACT_RE_PROCDEF (ID_);

create index ACT_IDX_DEADLETTER_JOB_EXCEPTION on ACT_RU_DEADLETTER_JOB(EXCEPTION_STACK_ID_);
alter table ACT_RU_DEADLETTER_JOB
    add constraint ACT_FK_DEADLETTER_JOB_EXCEPTION
    foreign key (EXCEPTION_STACK_ID_)
    references ACT_GE_BYTEARRAY (ID_);

alter table ACT_RU_INTEGRATION
    add constraint ACT_FK_INT_EXECUTION
    foreign key (EXECUTION_ID_)
    references ACT_RU_EXECUTION (ID_)
    on delete cascade;

alter table ACT_RU_INTEGRATION
    add constraint ACT_FK_INT_PROC_INST
    foreign key (PROCESS_INSTANCE_ID_)
    references ACT_RU_EXECUTION (ID_);

alter table ACT_RU_INTEGRATION
    add constraint ACT_FK_INT_PROC_DEF
    foreign key (PROC_DEF_ID_)
    references ACT_RE_PROCDEF (ID_);
--
-- Record script finish
--
DELETE FROM alf_applied_patch WHERE id = 'patch.db-V7.2-activiti-upgrade-7-1-0-M16';
INSERT INTO alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  VALUES
  (
    'patch.db-V7.2-activiti-upgrade-7-1-0-M16', 'Manually executed script upgrade patch.db-V7.2-activiti-upgrade-7-1-0-M16',
    0, 16000, -1, 16001, null, 'UNKNOWN', ${TRUE}, ${TRUE}, 'Script completed'
  );
