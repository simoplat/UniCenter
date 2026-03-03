package it.project;

public class Unicenter {
    
    private Unicenter() {
    }
    
    public static Unicenter getInstance() {
        return UnicenterHolder.INSTANCE;
    }
    
    private static class UnicenterHolder {

        private static final Unicenter INSTANCE = new Unicenter();
    }

    public void start(){
        
    }
}
