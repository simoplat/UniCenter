package it.project;

public class Main {

    public static void main(String[] args) {

        Unicenter unicenter = Unicenter.getInstance();

        ConsoleUI ui = ConsoleUI.getInstance();
   
        MenuController menuController = MenuController.getInstance();

        menuController.avvia();

    
    }


}