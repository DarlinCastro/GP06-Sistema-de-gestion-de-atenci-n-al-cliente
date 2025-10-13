-- IDs USUARIO: 1 al 24
-- 10 Clientes (IDUSUARIO 1-10, IDPASWORD 1-10, IDTIPOUSUARIO 2)
INSERT INTO USUARIO (IDPASWORD, IDTIPOUSUARIO, NOMBRES, APELLIDOS, CORREOELECTRONICO) VALUES (1, 2, 'Ana', 'Martínez', 'ana.martinez@empresa.com ');
INSERT INTO USUARIO (IDPASWORD, IDTIPOUSUARIO, NOMBRES, APELLIDOS, CORREOELECTRONICO) VALUES (2, 2, 'Bernardo', 'Nuñez', 'ber.nunez@corp.com       ');
INSERT INTO USUARIO (IDPASWORD, IDTIPOUSUARIO, NOMBRES, APELLIDOS, CORREOELECTRONICO) VALUES (3, 2, 'Carla', 'Ochoa', 'carla.ochoa@mail.com     ');
INSERT INTO USUARIO (IDPASWORD, IDTIPOUSUARIO, NOMBRES, APELLIDOS, CORREOELECTRONICO) VALUES (4, 2, 'David', 'Pérez', 'david.perez@comp.com     ');
INSERT INTO USUARIO (IDPASWORD, IDTIPOUSUARIO, NOMBRES, APELLIDOS, CORREOELECTRONICO) VALUES (5, 2, 'Elena', 'Quinteros', 'elena.q@data.com         ');
INSERT INTO USUARIO (IDPASWORD, IDTIPOUSUARIO, NOMBRES, APELLIDOS, CORREOELECTRONICO) VALUES (6, 2, 'Felipe', 'Ríos', 'felipe.rios@solu.com     ');
INSERT INTO USUARIO (IDPASWORD, IDTIPOUSUARIO, NOMBRES, APELLIDOS, CORREOELECTRONICO) VALUES (7, 2, 'Gloria', 'Sánchez', 'gloria.s@soft.com        ');
INSERT INTO USUARIO (IDPASWORD, IDTIPOUSUARIO, NOMBRES, APELLIDOS, CORREOELECTRONICO) VALUES (8, 2, 'Hugo', 'Torres', 'hugo.torres@tech.com     ');
INSERT INTO USUARIO (IDPASWORD, IDTIPOUSUARIO, NOMBRES, APELLIDOS, CORREOELECTRONICO) VALUES (9, 2, 'Irene', 'Uribe', 'irene.u@global.com       ');
INSERT INTO USUARIO (IDPASWORD, IDTIPOUSUARIO, NOMBRES, APELLIDOS, CORREOELECTRONICO) VALUES (10, 2, 'Javier', 'Vargas', 'javier.v@group.com       ');
-- 3 Administradores (IDUSUARIO 11-13, IDPASWORD 11-13, IDTIPOUSUARIO 4)
INSERT INTO USUARIO (IDPASWORD, IDTIPOUSUARIO, NOMBRES, APELLIDOS, CORREOELECTRONICO) VALUES (11, 4, 'Kurt', 'Walker', 'kurt.w@admin.com         ');
INSERT INTO USUARIO (IDPASWORD, IDTIPOUSUARIO, NOMBRES, APELLIDOS, CORREOELECTRONICO) VALUES (12, 4, 'Laura', 'Ximena', 'laura.x@manag.com        ');
INSERT INTO USUARIO (IDPASWORD, IDTIPOUSUARIO, NOMBRES, APELLIDOS, CORREOELECTRONICO) VALUES (13, 4, 'Mario', 'Yañez', 'mario.y@chief.com        ');
-- 5 Técnicos (IDUSUARIO 14-18, IDPASWORD 14-18, IDTIPOUSUARIO 3)
INSERT INTO USUARIO (IDPASWORD, IDTIPOUSUARIO, NOMBRES, APELLIDOS, CORREOELECTRONICO) VALUES (14, 3, 'Nadia', 'Zepeda', 'nadia.z@support.com      ');
INSERT INTO USUARIO (IDPASWORD, IDTIPOUSUARIO, NOMBRES, APELLIDOS, CORREOELECTRONICO) VALUES (15, 3, 'Oscar', 'Alfaro', 'oscar.a@field.com        ');
INSERT INTO USUARIO (IDPASWORD, IDTIPOUSUARIO, NOMBRES, APELLIDOS, CORREOELECTRONICO) VALUES (16, 3, 'Paola', 'Brito', 'paola.b@hware.com        ');
INSERT INTO USUARIO (IDPASWORD, IDTIPOUSUARIO, NOMBRES, APELLIDOS, CORREOELECTRONICO) VALUES (17, 3, 'Raúl', 'Castro', 'raul.c@techsup.com       ');
INSERT INTO USUARIO (IDPASWORD, IDTIPOUSUARIO, NOMBRES, APELLIDOS, CORREOELECTRONICO) VALUES (18, 3, 'Silvia', 'Díaz', 'silvia.d@master.com      ');
-- 6 Programadores (IDUSUARIO 19-24, IDPASWORD 19-24, IDTIPOUSUARIO 1)
INSERT INTO USUARIO (IDPASWORD, IDTIPOUSUARIO, NOMBRES, APELLIDOS, CORREOELECTRONICO) VALUES (19, 1, 'Tomás', 'Esparza', 'tomas.e@dev.com          ');
INSERT INTO USUARIO (IDPASWORD, IDTIPOUSUARIO, NOMBRES, APELLIDOS, CORREOELECTRONICO) VALUES (20, 1, 'Úrsula', 'Fuentes', 'ursula.f@progdb.com      ');
INSERT INTO USUARIO (IDPASWORD, IDTIPOUSUARIO, NOMBRES, APELLIDOS, CORREOELECTRONICO) VALUES (21, 1, 'Víctor', 'Guzmán', 'victor.g@fullst.com      ');
INSERT INTO USUARIO (IDPASWORD, IDTIPOUSUARIO, NOMBRES, APELLIDOS, CORREOELECTRONICO) VALUES (22, 1, 'Wendy', 'Hernández', 'wendy.h@coder.com        ');
INSERT INTO USUARIO (IDPASWORD, IDTIPOUSUARIO, NOMBRES, APELLIDOS, CORREOELECTRONICO) VALUES (23, 1, 'Xavier', 'Ibarra', 'xavier.i@backend.com     ');
INSERT INTO USUARIO (IDPASWORD, IDTIPOUSUARIO, NOMBRES, APELLIDOS, CORREOELECTRONICO) VALUES (24, 1, 'Yadira', 'Jiménez', 'yadira.j@gitcom.com      ');


-- IDs TICKET: 1 al 10
-- Asignado a Técnicos (14-18) y Programadores (19-24)
INSERT INTO TICKET (IDESTADOTICKET, IDUSUARIO, FECHAASIGNACION, NUMEROTICKET) VALUES (3, 15, '2025-08-01', 'T0001'); -- Alta, Oscar (Técnico)
INSERT INTO TICKET (IDESTADOTICKET, IDUSUARIO, FECHAASIGNACION, NUMEROTICKET) VALUES (2, 20, '2025-08-05', 'T0002'); -- Media, Úrsula (Programador)
INSERT INTO TICKET (IDESTADOTICKET, IDUSUARIO, FECHAASIGNACION, NUMEROTICKET) VALUES (4, 14, '2025-08-10', 'T0003'); -- Urgente, Nadia (Técnico)
INSERT INTO TICKET (IDESTADOTICKET, IDUSUARIO, FECHAASIGNACION, NUMEROTICKET) VALUES (1, 19, '2025-08-12', 'T0004'); -- Baja, Tomás (Programador)
INSERT INTO TICKET (IDESTADOTICKET, IDUSUARIO, FECHAASIGNACION, NUMEROTICKET) VALUES (3, 16, '2025-08-15', 'T0005'); -- Alta, Paola (Técnico)
INSERT INTO TICKET (IDESTADOTICKET, IDUSUARIO, FECHAASIGNACION, NUMEROTICKET) VALUES (2, 21, '2025-08-18', 'T0006'); -- Media, Víctor (Programador)
INSERT INTO TICKET (IDESTADOTICKET, IDUSUARIO, FECHAASIGNACION, NUMEROTICKET) VALUES (4, 17, '2025-08-20', 'T0007'); -- Urgente, Raúl (Técnico)
INSERT INTO TICKET (IDESTADOTICKET, IDUSUARIO, FECHAASIGNACION, NUMEROTICKET) VALUES (1, 22, '2025-08-22', 'T0008'); -- Baja, Wendy (Programador)
INSERT INTO TICKET (IDESTADOTICKET, IDUSUARIO, FECHAASIGNACION, NUMEROTICKET) VALUES (3, 18, '2025-08-25', 'T0009'); -- Alta, Silvia (Técnico)
INSERT INTO TICKET (IDESTADOTICKET, IDUSUARIO, FECHAASIGNACION, NUMEROTICKET) VALUES (2, 23, '2025-08-28', 'T0010'); -- Media, Xavier (Programador)


-- IDs SOLICITUD: 1 al 10
-- Solicitante: Clientes (IDUSUARIO 1-10)
INSERT INTO SOLICITUD (IDUSUARIO, IDTIPOSERVICIO, IDESTADOSOLICITUD, IDTICKET, FECHACREACION, DESCRIPCION) VALUES (1, 1, 1, 1, '2025-07-30', 'Solicita instalacion de 5 nuevos equipos en el area de contabilidad                                                                                                                                                                                                                                                                                                        ');
INSERT INTO SOLICITUD (IDUSUARIO, IDTIPOSERVICIO, IDESTADOSOLICITUD, IDTICKET, FECHACREACION, DESCRIPCION) VALUES (2, 5, 1, 2, '2025-08-04', 'Error critico en la ultima actualizacion del software de inventario, se necesita reversion inmediata y diagnostico.                                                                                                                                                                                                                                                          ');
INSERT INTO SOLICITUD (IDUSUARIO, IDTIPOSERVICIO, IDESTADOSOLICITUD, IDTICKET, FECHACREACION, DESCRIPCION) VALUES (3, 6, 2, 3, '2025-08-09', 'El usuario Carla Ochoa ha olvidado su clave de acceso al portal de servicios internos, requiere reseteo de contraseña.                                                                                                                                                                                                                                                         ');
INSERT INTO SOLICITUD (IDUSUARIO, IDTIPOSERVICIO, IDESTADOSOLICITUD, IDTICKET, FECHACREACION, DESCRIPCION) VALUES (4, 3, 4, 4, '2025-08-11', 'Diagnostico finalizado de la falla en el servidor de impresión. El servidor fue reemplazado y configurado correctamente.                                                                                                                                                                                                                                                         ');
INSERT INTO SOLICITUD (IDUSUARIO, IDTIPOSERVICIO, IDESTADOSOLICITUD, IDTICKET, FECHACREACION, DESCRIPCION) VALUES (5, 2, 1, 5, '2025-08-14', 'Mantenimiento preventivo de los equipos de la sala de reuniones. Se requiere limpieza interna y verificacion de componentes.                                                                                                                                                                                                                                                 ');
INSERT INTO SOLICITUD (IDUSUARIO, IDTIPOSERVICIO, IDESTADOSOLICITUD, IDTICKET, FECHACREACION, DESCRIPCION) VALUES (6, 4, 2, 6, '2025-08-17', 'Problemas intermitentes con la conexion Wi-Fi en el tercer piso del edificio anexo. Se pierde la señal constantemente.                                                                                                                                                                                                                                                        ');
INSERT INTO SOLICITUD (IDUSUARIO, IDTIPOSERVICIO, IDESTADOSOLICITUD, IDTICKET, FECHACREACION, DESCRIPCION) VALUES (7, 5, 3, 7, '2025-08-19', 'Solicitud de instalacion de un software CAD. Se cancela porque la licencia no fue aprobada por el departamento de compras.                                                                                                                                                                                                                                                        ');
INSERT INTO SOLICITUD (IDUSUARIO, IDTIPOSERVICIO, IDESTADOSOLICITUD, IDTICKET, FECHACREACION, DESCRIPCION) VALUES (8, 1, 1, 8, '2025-08-21', 'Necesidad de configurar una nueva VPN para el acceso remoto del equipo de ventas.                                                                                                                                                                                                                                                                                               ');
INSERT INTO SOLICITUD (IDUSUARIO, IDTIPOSERVICIO, IDESTADOSOLICITUD, IDTICKET, FECHACREACION, DESCRIPCION) VALUES (9, 3, 4, 9, '2025-08-24', 'Fallo en la pantalla de un monitor. Se realizo el diagnostico y se confirmo que la pantalla estaba defectuosa. Se procedio al reemplazo.                                                                                                                                                                                                                                          ');
INSERT INTO SOLICITUD (IDUSUARIO, IDTIPOSERVICIO, IDESTADOSOLICITUD, IDTICKET, FECHACREACION, DESCRIPCION) VALUES (10, 6, 2, 10, '2025-08-27', 'El usuario Javier Vargas no puede iniciar sesion en su equipo de trabajo tras el cambio de contraseña forzado.                                                                                                                                                                                                                                                                   ');