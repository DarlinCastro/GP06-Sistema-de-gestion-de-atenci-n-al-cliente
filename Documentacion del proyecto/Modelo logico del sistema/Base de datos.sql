/*==============================================================*/
/* DBMS name:      PostgreSQL 9.x                               */
/* Created on:     3/10/2025 20:47:46                           */
/*==============================================================*/

/*==============================================================*/
/* CREACION DE TABLAS (Solo con SERIAL como se solicitó)        */
/*==============================================================*/

/*==============================================================*/
/* Table: ESTADO_SOLICITUD                                      */
/*==============================================================*/
create table ESTADO_SOLICITUD (
    IDESTADOSOLICITUD      SERIAL                not null,
    ESTADOSOLICITUD        CHAR(10)              not null,
    constraint PK_ESTADO_SOLICITUD primary key (IDESTADOSOLICITUD)
);

/*==============================================================*/
/* Index: ESTADO_SOLICITUD_PK                                   */
/*==============================================================*/
create unique index ESTADO_SOLICITUD_PK on ESTADO_SOLICITUD (
IDESTADOSOLICITUD
);

/*==============================================================*/
/* Table: ESTADO_TICKET                                         */
/*==============================================================*/
create table ESTADO_TICKET (
    IDESTADOTICKET         SERIAL                not null,
    NIVELPRIORIDAD         CHAR(10)              not null,
    constraint PK_ESTADO_TICKET primary key (IDESTADOTICKET)
);

/*==============================================================*/
/* Index: ESTADO_TICKET_PK                                      */
/*==============================================================*/
create unique index ESTADO_TICKET_PK on ESTADO_TICKET (
IDESTADOTICKET
);

/*==============================================================*/
/* Table: PASWORD                                               */
/*==============================================================*/
create table PASWORD (
    IDPASWORD              SERIAL                not null,
    CLAVEACCESO            VARCHAR(10)           not null,
    IDENTIFICADOR          CHAR(9)               not null,
    constraint PK_PASWORD primary key (IDPASWORD)
);

/*==============================================================*/
/* Index: PASWORD_PK                                            */
/*==============================================================*/
create unique index PASWORD_PK on PASWORD (
IDPASWORD
);

/*==============================================================*/
/* Table: SOLICITUD                                             */
/*==============================================================*/
create table SOLICITUD (
    IDSOLICITUD            SERIAL                not null,
    IDUSUARIO              INT4                  not null,
    IDTIPOSERVICIO         INT4                  not null,
    IDESTADOSOLICITUD      INT4                  not null,
    IDTICKET               INT4                  not null,
    FECHACREACION          DATE                  not null,
    DESCRIPCION            CHAR(300)             not null,
    constraint PK_SOLICITUD primary key (IDSOLICITUD)
);

/*==============================================================*/
/* Index: SOLICITUD_PK                                          */
/*==============================================================*/
create unique index SOLICITUD_PK on SOLICITUD (
IDSOLICITUD
);

/*==============================================================*/
/* Index: GENERA_FK                                             */
/*==============================================================*/
create  index GENERA_FK on SOLICITUD (
IDUSUARIO
);

/*==============================================================*/
/* Index: REQUIERE_FK                                           */
/*==============================================================*/
create  index REQUIERE_FK on SOLICITUD (
IDTIPOSERVICIO
);

/*==============================================================*/
/* Index: ADOPTA_FK                                             */
/*==============================================================*/
create  index ADOPTA_FK on SOLICITUD (
IDESTADOSOLICITUD
);

/*==============================================================*/
/* Index: ORIGINA_FK                                            */
/*==============================================================*/
create  index ORIGINA_FK on SOLICITUD (
IDTICKET
);

/*==============================================================*/
/* Table: TICKET                                                */
/*==============================================================*/
create table TICKET (
    IDTICKET               SERIAL                not null,
    IDESTADOTICKET         INT4                  not null,
    FECHAASIGNACION        DATE                  not null,
    NUMEROTICKET           CHAR(5)               not null,
    constraint PK_TICKET primary key (IDTICKET)
);

/*==============================================================*/
/* Index: TICKET_PK                                             */
/*==============================================================*/
create unique index TICKET_PK on TICKET (
IDTICKET
);

/*==============================================================*/
/* Index: CONTIENE_FK                                           */
/*==============================================================*/
create  index CONTIENE_FK on TICKET (
IDESTADOTICKET
);

/*==============================================================*/
/* Table: TIPO_SERVICIO                                         */
/*==============================================================*/
create table TIPO_SERVICIO (
    IDTIPOSERVICIO         SERIAL                not null,
    NOMBRESERVICIO         CHAR(80)              not null,
    constraint PK_TIPO_SERVICIO primary key (IDTIPOSERVICIO)
);

/*==============================================================*/
/* Index: TIPO_SERVICIO_PK                                      */
/*==============================================================*/
create unique index TIPO_SERVICIO_PK on TIPO_SERVICIO (
IDTIPOSERVICIO
);

/*==============================================================*/
/* Table: TIPO_USUARIO                                          */
/*==============================================================*/
create table TIPO_USUARIO (
    IDTIPOUSUARIO          SERIAL                not null,
    CARGO                  CHAR(30)              not null,
    constraint PK_TIPO_USUARIO primary key (IDTIPOUSUARIO)
);

/*==============================================================*/
/* Index: TIPO_USUARIO_PK                                       */
/*==============================================================*/
create unique index TIPO_USUARIO_PK on TIPO_USUARIO (
IDTIPOUSUARIO
);

/*==============================================================*/
/* Table: USUARIO                                               */
/*==============================================================*/
create table USUARIO (
    IDUSUARIO              SERIAL                not null,
    IDPASWORD              INT4                  not null,
    IDTIPOUSUARIO          INT4                  not null,
    NOMBRES                CHAR(30)              not null,
    APELLIDOS              CHAR(30)              not null,
    CORREOELECTRONICO      CHAR(25)              not null,
    constraint PK_USUARIO primary key (IDUSUARIO)
);

/*==============================================================*/
/* Index: USUARIO_PK                                            */
/*==============================================================*/
create unique index USUARIO_PK on USUARIO (
IDUSUARIO
);

/*==============================================================*/
/* Index: POSEE_FK                                              */
/*==============================================================*/
create  index POSEE_FK on USUARIO (
IDPASWORD
);

/*==============================================================*/
/* Index: PERTENECE_FK                                          */
/*==============================================================*/
create  index PERTENECE_FK on USUARIO (
IDTIPOUSUARIO
);

/*==============================================================*/
/* Definición de Claves Foráneas                                */
/*==============================================================*/

alter table SOLICITUD
   add constraint FK_SOLICITU_ADOPTA_ESTADO_S foreign key (IDESTADOSOLICITUD)
      references ESTADO_SOLICITUD (IDESTADOSOLICITUD)
      on delete restrict on update restrict;

alter table SOLICITUD
   add constraint FK_SOLICITU_GENERA_USUARIO foreign key (IDUSUARIO)
      references USUARIO (IDUSUARIO)
      on delete restrict on update restrict;

alter table SOLICITUD
   add constraint FK_SOLICITU_ORIGINA_TICKET foreign key (IDTICKET)
      references TICKET (IDTICKET)
      on delete restrict on update restrict;

alter table SOLICITUD
   add constraint FK_SOLICITU_REQUIERE_TIPO_SER foreign key (IDTIPOSERVICIO)
      references TIPO_SERVICIO (IDTIPOSERVICIO)
      on delete restrict on update restrict;

alter table TICKET
   add constraint FK_TICKET_CONTIENE_ESTADO_T foreign key (IDESTADOTICKET)
      references ESTADO_TICKET (IDESTADOTICKET)
      on delete restrict on update restrict;

alter table USUARIO
   add constraint FK_USUARIO_PERTENECE_TIPO_USU foreign key (IDTIPOUSUARIO)
      references TIPO_USUARIO (IDTIPOUSUARIO)
      on delete restrict on update restrict;

alter table USUARIO
   add constraint FK_USUARIO_POSEE_PASWORD foreign key (IDPASWORD)
      references PASWORD (IDPASWORD)
      on delete restrict on update restrict;


/*==============================================================*/
/* INSERCIÓN DE DATOS (DML)                                     */
/*==============================================================*/

-- Tabla TIPO_USUARIO 
INSERT INTO TIPO_USUARIO (CARGO) VALUES ('Programador ');
INSERT INTO TIPO_USUARIO (CARGO) VALUES ('Cliente     ');
INSERT INTO TIPO_USUARIO (CARGO) VALUES ('Técnico     ');
INSERT INTO TIPO_USUARIO (CARGO) VALUES ('Admin       '); 

-- Tabla ESTADO_SOLICITUD
INSERT INTO ESTADO_SOLICITUD (ESTADOSOLICITUD) VALUES ('En Proceso');
INSERT INTO ESTADO_SOLICITUD (ESTADOSOLICITUD) VALUES ('Pendiente ');
INSERT INTO ESTADO_SOLICITUD (ESTADOSOLICITUD) VALUES ('Cancelado ');
INSERT INTO ESTADO_SOLICITUD (ESTADOSOLICITUD) VALUES ('Finalizado');

-- Tabla ESTADO_TICKET
INSERT INTO ESTADO_TICKET (NIVELPRIORIDAD) VALUES ('Baja      ');
INSERT INTO ESTADO_TICKET (NIVELPRIORIDAD) VALUES ('Media     ');
INSERT INTO ESTADO_TICKET (NIVELPRIORIDAD) VALUES ('Alta      ');
INSERT INTO ESTADO_TICKET (NIVELPRIORIDAD) VALUES ('Urgente   ');

-- Tabla TIPO_SERVICIO
INSERT INTO TIPO_SERVICIO (NOMBRESERVICIO) VALUES ('Instalación y configuración de equipos informáticos                               ');
INSERT INTO TIPO_SERVICIO (NOMBRESERVICIO) VALUES ('Mantenimiento preventivo y correctivo de hardware                                 ');
INSERT INTO TIPO_SERVICIO (NOMBRESERVICIO) VALUES ('Diagnóstico y reparación de fallos en equipos                                     ');
INSERT INTO TIPO_SERVICIO (NOMBRESERVICIO) VALUES ('Soporte de conectividad                                                           ');
INSERT INTO TIPO_SERVICIO (NOMBRESERVICIO) VALUES ('Instalación, configuración y actualización de software interno                      ');
INSERT INTO TIPO_SERVICIO (NOMBRESERVICIO) VALUES ('Recuperación de acceso                                                            ');

-- Tabla PASWORD
-- P = Programador, C = Cliente, T = Técnico, A = Admin
INSERT INTO PASWORD (CLAVEACCESO, IDENTIFICADOR) VALUES ('progpass', 'P-AB00001');
INSERT INTO PASWORD (CLAVEACCESO, IDENTIFICADOR) VALUES ('clipass1', 'C-AC00001');
INSERT INTO PASWORD (CLAVEACCESO, IDENTIFICADOR) VALUES ('clipass2', 'C-AC00002');
INSERT INTO PASWORD (CLAVEACCESO, IDENTIFICADOR) VALUES ('tecpass1', 'T-FD00001');
INSERT INTO PASWORD (CLAVEACCESO, IDENTIFICADOR) VALUES ('tecpass2', 'T-FD00002');
INSERT INTO PASWORD (CLAVEACCESO, IDENTIFICADOR) VALUES ('admin123', 'A-JF00001');