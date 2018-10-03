-- apply changes
create table rc_economy_balance (
  id                            integer auto_increment not null,
  name                          varchar(255),
  balance                       double not null,
  exp                           integer not null,
  type                          varchar(6),
  constraint ck_rc_economy_balance_type check ( type in ('PLAYER','PLUGIN','CITY')),
  constraint pk_rc_economy_balance primary key (id)
);

create table rc_economy_bankchests (
  id                            integer auto_increment not null,
  player_id                     varchar(40),
  type                          varchar(255),
  last_emptying                 datetime(6),
  x                             integer not null,
  y                             integer not null,
  z                             integer not null,
  constraint pk_rc_economy_bankchests primary key (id)
);

create table rc_economy_bank_materials (
  id                            integer auto_increment not null,
  material                      varchar(255),
  price_buy                     double not null,
  price_sell                    double not null,
  buy                           tinyint(1) default 0 not null,
  sell                          tinyint(1) default 0 not null,
  constraint pk_rc_economy_bank_materials primary key (id)
);

create table rc_economy_flow (
  id                            integer auto_increment not null,
  amount                        double not null,
  source                        integer,
  detail                        varchar(255),
  date                          datetime(6),
  account_id                    integer,
  constraint ck_rc_economy_flow_source check ( source in (0,1,2,3,4,5,6,7,8,9,10,11,12,13)),
  constraint pk_rc_economy_flow primary key (id)
);

create index ix_rc_economy_flow_account_id on rc_economy_flow (account_id);
alter table rc_economy_flow add constraint fk_rc_economy_flow_account_id foreign key (account_id) references rc_economy_balance (id) on delete restrict on update restrict;

