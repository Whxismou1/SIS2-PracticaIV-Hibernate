/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mainpkg;

import java.util.Scanner;

/**
 *
 * @author moasin
 */
public class MainMenu {

    private Scanner sc;

    private String nif;

    public void printMenu() {
        System.out.println("Welcome");
    }

    public void getNIFInputUser() {
        sc = new Scanner(System.in);
        System.out.print("Introduce el dni: ");
        nif = sc.nextLine();
    }

    public void closeScanner() {
        if (sc != null) {
            sc.close();
        }
    }

    public String getDNI() {
        return this.nif;
    }
}
