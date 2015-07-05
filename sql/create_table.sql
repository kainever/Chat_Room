
create database chat_room;
use chat_room;

create table user (
	id int primary key auto_increment,
	name varchar(12) not null unique,
	password varchar(15) not null,
	ip varchar(14),
	port varchar(6),
	online boolean default true
);

insert into user value(null , 'hk' , 'hk' , null , null ,default);
insert into user value(null , 'b' , 'a' , null , null ,default);
insert into user value(null , 'c' , 'a' , null , null ,default);
insert into user value(null , 'd' , 'a' , null , null ,default);
insert into user value(null , 'e' , 'a' , null , null ,default);

create table friend
(
   user_Id              int not null,
   friend_id            int not null,
   primary key (user_Id, friend_id)
);

alter table friend add constraint FK_Relationship_6 foreign key (user_Id)
      references User (id) on delete restrict on update restrict;

alter table friend add constraint FK_Relationship_7 foreign key (friend_id)
      references User (id) on delete restrict on update restrict;
      
insert into friend values(1 , 2);
insert into friend values(2 , 1);
insert into friend values(1 , 3);
insert into friend values(3 , 1);
insert into friend values(1 , 4);
insert into friend values(4 , 1);
insert into friend values(1 , 5);
insert into friend values(5 , 1);
