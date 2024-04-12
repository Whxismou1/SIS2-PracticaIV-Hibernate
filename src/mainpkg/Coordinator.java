/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mainpkg;

import POJOS.Contribuyente;
import POJOS.Recibos;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

/**
 *
 * @author moasin
 */
public class Coordinator {

    public void init() {

        MainMenu mainMenu = new MainMenu();

        mainMenu.printMenu();
        mainMenu.getNIFInputUser();

        String userNIF = mainMenu.getDNI();

        if (!isNIFOnBBDD(userNIF)) {
            System.out.println("ERROR: El Trabajador no se encuentra en el sistema");
            mainMenu.closeScanner();
            return;
        } else {
            System.out.println("Si existe");

        }

        Contribuyente contribuyenteActual = getUserInfo(userNIF);

        printInfoContribuyente(contribuyenteActual);
        actualizarImporteTotalRecibos(contribuyenteActual);
        eliminarRecibosConBaseImponibleMenorQueMedia(contribuyenteActual);
        mainMenu.closeScanner();
    }

    private boolean isNIFOnBBDD(String nif) {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            String hql = "SELECT COUNT(*) FROM Contribuyente WHERE NIFNIE = :nif";
            Query<Long> query = session.createQuery(hql, Long.class);
            query.setParameter("nif", nif);
            Long count = query.uniqueResult();

            if (count < 1) {
                return false;
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (session != null) {
                session.close();
            }
        }

    }

    private Contribuyente getUserInfo(String nif) {
        Session session = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();

            String hql = "FROM Contribuyente WHERE NIFNIE = :nif";
            Query<Contribuyente> query = session.createQuery(hql, Contribuyente.class);
            query.setParameter("nif", nif);

            return query.uniqueResult();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (session != null) {
                session.close();
            }
        }

    }

    private void printInfoContribuyente(Contribuyente contribuyente) {
        System.out.println("Información del Contribuyente:");
        System.out.println("ID: " + contribuyente.getIdContribuyente());
        System.out.println("Nombre: " + contribuyente.getNombre());
        System.out.println("Apellidos: " + contribuyente.getApellido1() + " " + contribuyente.getApellido2());
        System.out.println("NIF/NIE: " + contribuyente.getNifnie());
        System.out.println("Dirección: " + contribuyente.getDireccion());
        System.out.println("Número: " + contribuyente.getNumero());
        System.out.println("País CCC: " + contribuyente.getPaisCcc());
        System.out.println("CCC: " + contribuyente.getCcc());
        System.out.println("IBAN: " + contribuyente.getIban());
        System.out.println("E-mail: " + contribuyente.getEemail());
        System.out.println("Exención: " + contribuyente.getExencion());
        System.out.println("Bonificación: " + contribuyente.getBonificacion());
        System.out.println("Fecha de Alta: " + contribuyente.getFechaAlta());
        System.out.println("Fecha de Baja: " + contribuyente.getFechaBaja());
    }

    private void actualizarImporteTotalRecibos(Contribuyente contribuyenteActual) {
        Session session = null;
        Transaction transact = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            transact = session.beginTransaction();

            String queryHQL = "UPDATE Recibos SET totalRecibo = :nuevoImporte WHERE idContribuyente = :idContribuyente";
            Query query = session.createQuery(queryHQL);
            query.setParameter("nuevoImporte", 250.0);
            query.setParameter("idContribuyente", contribuyenteActual.getIdContribuyente());

            int rowsChanged = query.executeUpdate();
            System.out.println("Se han actualizado " + rowsChanged + " filas");
            transact.commit();
        } catch (Exception e) {
            if (transact != null) {
                transact.rollback();
            }
            e.printStackTrace();
        } finally {
            if (session != null) {
                session.close();
            }
        }

    }

    private void eliminarRecibosConBaseImponibleMenorQueMedia(Contribuyente contribuyenteActual) {
        Session session = null;
        Transaction transac = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            transac = session.beginTransaction();

            // Consulta para obtener la lista de bases imponibles de los recibos del contribuyente
            String hql = "SELECT lr.recibos.numeroRecibo FROM Lineasrecibo lr WHERE lr.recibos.contribuyente.idContribuyente = :idContribuyente";
            Query<Integer> query = session.createQuery(hql, Integer.class);
            query.setParameter("idContribuyente", contribuyenteActual.getIdContribuyente());
            List<Integer> numerosRecibos = query.getResultList();

            if (!numerosRecibos.isEmpty()) {
                // Consulta para calcular la media de las bases imponibles de los recibos del contribuyente
                String hqlMedia = "SELECT AVG(lr.baseImponible) FROM Lineasrecibo lr WHERE lr.recibos.numeroRecibo IN (:numerosRecibos)";
                Query<Double> queryMedia = session.createQuery(hqlMedia, Double.class);
                queryMedia.setParameterList("numerosRecibos", numerosRecibos);
                Double mediaBaseImponible = queryMedia.uniqueResult();

                // Consulta para eliminar los recibos cuya base imponible sea menor que la media
                String hqlEliminarRecibos = "DELETE FROM Recibos r WHERE r.totalBaseImponible < :media";
                Query queryEliminarRecibos = session.createQuery(hqlEliminarRecibos);
                queryEliminarRecibos.setParameter("media", mediaBaseImponible);
                int filasEliminadasRecibos = queryEliminarRecibos.executeUpdate();

                transac.commit();
                System.out.println("Se han eliminado " + filasEliminadasRecibos + " recibos con base imponible menor que la media.");
            } else {
                System.out.println("No hay recibos para eliminar.");
            }
        } catch (Exception e) {
            if (transac != null) {
                transac.rollback();
            }
            e.printStackTrace();
        } finally {
            if (session != null) {
                session.close();
            }
        }

    }

    private double getMediaBases(Contribuyente contribuyente) {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();

            // Consulta para obtener la media de las bases imponibles de los recibos del contribuyente
            String hql = "SELECT AVG(r.totalBaseImponible) FROM Recibos r WHERE r.contribuyente.idContribuyente = :idContribuyente";
            Query<Double> query = session.createQuery(hql, Double.class);
            query.setParameter("idContribuyente", contribuyente.getIdContribuyente());
            return query.uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return 0.0; // En caso de error, devolver 0.0
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

}
