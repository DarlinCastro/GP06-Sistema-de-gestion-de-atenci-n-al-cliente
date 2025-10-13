/*==============================================================*/
/* DBMS name:      PostgreSQL 9.x                               */
/* Created on:     12/10/2025 01:35:41 (Consolidado)            */
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
/* Table: TICKET (MODIFICADA para incluir IDUSUARIO)            */
/*==============================================================*/
create table TICKET (
    IDTICKET               SERIAL                not null,
    IDESTADOTICKET         INT4                  not null,
    IDUSUARIO              INT4                  not null, -- Agregado de crebas.sql
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
/* Index: DA_SEGUIMIENTO_FK (Agregado de crebas.sql)            */
/*==============================================================*/
create  index DA_SEGUIMIENTO_FK on TICKET (
IDUSUARIO
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
      
alter table TICKET
   add constraint FK_TICKET_DA_SEGUIM_USUARIO foreign key (IDUSUARIO) -- Agregado de crebas.sql
      references USUARIO (IDUSUARIO)
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

-- Tabla PASSWORD
-- 10 Clientes (C-) 
INSERT INTO PASWORD (CLAVEACCESO, IDENTIFICADOR) VALUES ('securecliw', 'C-MA00014'); 
INSERT INTO PASWORD (CLAVEACCESO, IDENTIFICADOR) VALUES ('passclient', 'C-NB00015'); 
INSERT INTO PASWORD (CLAVEACCESO, IDENTIFICADOR) VALUES ('miclave123', 'C-OC00016'); 
INSERT INTO PASWORD (CLAVEACCESO, IDENTIFICADOR) VALUES ('empresa#04', 'C-PD00017'); 
INSERT INTO PASWORD (CLAVEACCESO, IDENTIFICADOR) VALUES ('datos2025 ', 'C-QE00018'); 
INSERT INTO PASWORD (CLAVEACCESO, IDENTIFICADOR) VALUES ('cli_pass06', 'C-RF00019'); 
INSERT INTO PASWORD (CLAVEACCESO, IDENTIFICADOR) VALUES ('usuario789', 'C-SG00020'); 
INSERT INTO PASWORD (CLAVEACCESO, IDENTIFICADOR) VALUES ('claveunica', 'C-TH00021'); 
INSERT INTO PASWORD (CLAVEACCESO, IDENTIFICADOR) VALUES ('client_09v', 'C-UI00022'); 
INSERT INTO PASWORD (CLAVEACCESO, IDENTIFICADOR) VALUES ('client_10_', 'C-VJ00023'); 

-- 3 Administradores (A-) 
INSERT INTO PASWORD (CLAVEACCESO, IDENTIFICADOR) VALUES ('superadmin', 'A-WK00002'); 
INSERT INTO PASWORD (CLAVEACCESO, IDENTIFICADOR) VALUES ('manager#2 ', 'A-XL00003');  
INSERT INTO PASWORD (CLAVEACCESO, IDENTIFICADOR) VALUES ('chief_it3 ', 'A-YM00004');  

-- 5 Técnicos (T-) 
INSERT INTO PASWORD (CLAVEACCESO, IDENTIFICADOR) VALUES ('tecsupport', 'T-ZN00020'); 
INSERT INTO PASWORD (CLAVEACCESO, IDENTIFICADOR) VALUES ('fieldtech5', 'T-AO00021'); 
INSERT INTO PASWORD (CLAVEACCESO, IDENTIFICADOR) VALUES ('hardwarefi', 'T-BP00022'); 
INSERT INTO PASWORD (CLAVEACCESO, IDENTIFICADOR) VALUES ('support#08', 'T-CQ00023'); 
INSERT INTO PASWORD (CLAVEACCESO, IDENTIFICADOR) VALUES ('tec_master', 'T-DR00024'); 

-- 6 Programadores (P-) 
INSERT INTO PASWORD (CLAVEACCESO, IDENTIFICADOR) VALUES ('dev_code9', 'P-ES00003'); 
INSERT INTO PASWORD (CLAVEACCESO, IDENTIFICADOR) VALUES ('prog_db_y', 'P-FT00004'); 
INSERT INTO PASWORD (CLAVEACCESO, IDENTIFICADOR) VALUES ('fullstack', 'P-GU00005'); 
INSERT INTO PASWORD (CLAVEACCESO, IDENTIFICADOR) VALUES ('coderpass', 'P-HV00006'); 
INSERT INTO PASWORD (CLAVEACCESO, IDENTIFICADOR) VALUES ('backenddX', 'P-IW00007'); 
INSERT INTO PASWORD (CLAVEACCESO, IDENTIFICADOR) VALUES ('git_commi', 'P-JX00008'); 
