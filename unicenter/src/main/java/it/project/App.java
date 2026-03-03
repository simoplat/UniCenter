package it.project;

public class App {
    public static void main(String[] args) {
        System.out.println("BENVENUTO IN UNICENTER");
        Unicenter unicenter = Unicenter.getInstance();
        unicenter.start();

    }
}