package it.project;

import java.util.Scanner;

public class ConsoleUI {
    private Scanner scanner;

    private ConsoleUI() {
        this.scanner = new Scanner(System.in);
    }

    private static class ConsoleUIHolder {
        private static final ConsoleUI INSTANCE = new ConsoleUI();
    }

    public static ConsoleUI getInstance() {
        return ConsoleUIHolder.INSTANCE;
    }

    public String leggiStringa(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }

    public int leggiIntero(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                String input = scanner.nextLine();
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Errore: Inserisci un numero intero valido.");
            }
        }
    }

    public void mostraMessaggio(String msg) {
        System.out.println(msg);
    }

    public void mostraErrore(String msg) {
        System.out.println("ERRORE: " + msg);
    }
}
