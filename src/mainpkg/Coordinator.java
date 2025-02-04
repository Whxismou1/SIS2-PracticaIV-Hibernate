/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mainpkg;

import POJOS.Contribuyente;
import POJOS.Lineasrecibo;
import POJOS.Recibos;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
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
        System.exit(0);
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
        System.out.println("-----------------------------------------------------");
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
        System.out.println("-----------------------------------------------------");
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

            String hqlMedia = "SELECT AVG(r.totalBaseImponible) FROM Recibos r";
            Query<Double> queryMedia = session.createQuery(hqlMedia, Double.class);
            Double mediaBaseImponible = queryMedia.uniqueResult();
            System.out.println("Media base imponible: " + mediaBaseImponible);

            String hqlEliminarRecibos = "FROM Recibos r WHERE r.totalBaseImponible < :media";
            Query<Recibos> eliminarRecibos = session.createQuery(hqlEliminarRecibos, Recibos.class);
            eliminarRecibos.setParameter("media", mediaBaseImponible);
            List<Recibos> recibosAEliminar = eliminarRecibos.getResultList();
            int count = recibosAEliminar.size();
            for (Recibos recibo : recibosAEliminar) {

                Set<Lineasrecibo> lineasrecibos = recibo.getLineasrecibos();

                // Eliminar cada línea de recibo
                for (Lineasrecibo linea : lineasrecibos) {
                    session.delete(linea);
                }

                session.delete(recibo);
            }

            session.getTransaction().commit();
            System.out.println("Se han eliminado " + count + " registros ");
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

}
