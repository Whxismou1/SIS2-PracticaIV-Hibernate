#  SIS 2: Práctica 1 Hibernate + Java

## Descripción

Esta es la primera práctica de la asignatura Sistemas de la Información 2 de tercer curso de Ingeniería Informática de la Universidad de León. En esta práctica, se utiliza Java junto con Hibernate para crear un programa que:

1. Obtiene, a partir de un DNI o NIF, la información de un contribuyente.
2. Imprime la información del contribuyente.
3. Actualiza el importe total de los recibos del contribuyente.
4. Elimina recibos con base imponible menor que la media.

## Requisitos

- Java 8 o superior
- Hibernate 5.x
- Base de datos compatible con Hibernate (por ejemplo, MySQL)

## Instalación

1. Clona este repositorio:
   ```sh
   git clone https://github.com/Whxismou1/sis2-Practica1Hibernate
   ```
2. Importa el proyecto en tu IDE de preferencia (por ejemplo, IntelliJ IDEA o Eclipse).
3. Configura la conexión a la base de datos en el archivo hibernate.cfg.xml.

## Uso
Para ejecutar el programa, sigue estos pasos:

1. Abre el menú principal.
2. Ingresa el DNI o NIF del contribuyente que deseas consultar.
3. El programa realizará las siguientes acciones:
    * Verificará si el contribuyente existe en la base de datos.
    * Si el contribuyente existe, imprimirá su información.
    * Actualizará el importe total de los recibos a un valor fijo (ejemplo: 250.0).
    * Eliminará los recibos cuya base imponible sea menor que la media de todas las bases imponibles.
