package it.project;

import java.util.Scanner;

public class App {
    public static void main(String[] args) {
        System.out.println("Avvio in corso...");
        System.out.println("BENVENUTO IN UNICENTER");
        Scanner sc = new Scanner(System.in);

        //Test manuale
        Professore prof = new Professore();
        prof.menuPersonale(sc);
    }
}