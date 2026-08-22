package it.project.view.server;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import it.project.Unicenter;

public class MaterialeServerIntegrationTest {
    public static void main(String[] args) throws Exception {
        System.out.println("==================================================");
        System.out.println("   INTEGRATION TEST: API REST MATERIALE DIDATTICO ");
        System.out.println("==================================================");

        Unicenter unicenter = Unicenter.getInstance();
        unicenter.popolaDataBase();

        UniCenterWebServer server = new UniCenterWebServer(unicenter);
        boolean started = server.start(8089);
        if (!started) {
            System.err.println("FAILED to start server on 8089");
            System.exit(1);
        }

        String baseUrl = server.getBaseUrl();
        HttpClient client = HttpClient.newHttpClient();

        try {
            // 1. Login Professore Mario Rossi
            String profLogin = "{\"email\":\"mario.rossi@unicenter.it\",\"password\":\"pass123\"}";
            HttpRequest reqProfLogin = HttpRequest.newBuilder(URI.create(baseUrl + "/api/auth/login"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(profLogin))
                    .build();
            HttpResponse<String> respProfLogin = client.send(reqProfLogin, HttpResponse.BodyHandlers.ofString());
            assert respProfLogin.statusCode() == 200 : "Prof login failed: " + respProfLogin.body();
            System.out.println("✓ 1. Login Docente riuscito");

            // 2. Fetch Materie per il docente
            HttpRequest reqMat = HttpRequest.newBuilder(URI.create(baseUrl + "/api/materiale/materie")).build();
            HttpResponse<String> respMat = client.send(reqMat, HttpResponse.BodyHandlers.ofString());
            assert respMat.statusCode() == 200 : "Fetch materie failed: " + respMat.body();
            assert respMat.body().contains("IS01") : "IS01 not found in teacher subjects";
            System.out.println("✓ 2. Materie docente recuperate con successo: " + respMat.body());

            // 3. Fetch Albero Composite IS01
            HttpRequest reqTree = HttpRequest.newBuilder(URI.create(baseUrl + "/api/materiale/albero?codiceMateria=IS01")).build();
            HttpResponse<String> respTree = client.send(reqTree, HttpResponse.BodyHandlers.ofString());
            assert respTree.statusCode() == 200 : "Fetch tree failed: " + respTree.body();
            assert respTree.body().contains("Slide") : "Slide folder missing from tree";
            System.out.println("✓ 3. Albero Composite IS01 recuperato con successo");

            // 4. Creazione nuova cartella via API (Professore UC6)
            String nuovaCartellaJson = "{\"codiceMateria\":\"IS01\",\"nome\":\"Test_Integration_Folder\",\"descrizione\":\"Cartella creata da test\"}";
            HttpRequest reqNewFolder = HttpRequest.newBuilder(URI.create(baseUrl + "/api/materiale/cartella"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(nuovaCartellaJson))
                    .build();
            HttpResponse<String> respNewFolder = client.send(reqNewFolder, HttpResponse.BodyHandlers.ofString());
            assert respNewFolder.statusCode() == 200 : "Create folder failed: " + respNewFolder.body();
            System.out.println("✓ 4. Creazione nuova cartella via API riuscita");

            // 5. Upload materiale via API (Professore UC6)
            String uploadJson = "{\"codiceMateria\":\"IS01\",\"nome\":\"test_api_doc.txt\",\"descrizione\":\"Test Upload API\",\"tipo\":\"TESTO\",\"contenutoTesto\":\"Contenuto di verifica REST API\"}";
            HttpRequest reqUpload = HttpRequest.newBuilder(URI.create(baseUrl + "/api/materiale/upload"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(uploadJson))
                    .build();
            HttpResponse<String> respUpload = client.send(reqUpload, HttpResponse.BodyHandlers.ofString());
            assert respUpload.statusCode() == 200 : "Upload failed: " + respUpload.body();
            System.out.println("✓ 5. Upload file via API riuscito: " + respUpload.body());

            // 6. Login Studente Mario Rossi
            String stLogin = "{\"email\":\"mario.rossi@studenti.it\",\"password\":\"pass123\"}";
            HttpRequest reqStLogin = HttpRequest.newBuilder(URI.create(baseUrl + "/api/auth/login"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(stLogin))
                    .build();
            HttpResponse<String> respStLogin = client.send(reqStLogin, HttpResponse.BodyHandlers.ofString());
            assert respStLogin.statusCode() == 200 : "Student login failed: " + respStLogin.body();
            System.out.println("✓ 6. Login Studente riuscito");

            // 7. Fetch Preferiti Studente (UC10)
            HttpRequest reqPrefs = HttpRequest.newBuilder(URI.create(baseUrl + "/api/materiale/preferiti")).build();
            HttpResponse<String> respPrefs = client.send(reqPrefs, HttpResponse.BodyHandlers.ofString());
            assert respPrefs.statusCode() == 200 : "Fetch preferiti failed: " + respPrefs.body();
            System.out.println("✓ 7. Preferiti studente recuperati: " + respPrefs.body());

            // 8. Download file binario (UC10)
            // Troviamo un elemento dall'albero
            var rootCartella = unicenter.getAlberoMaterialeMateria("IS01");
            var primoElem = rootCartella.elenca().stream()
                    .flatMap(c -> c.elenca().stream())
                    .filter(e -> !e.isCartella())
                    .findFirst()
                    .orElse(null);

            assert primoElem != null : "Nessun file trovato per test download";
            String downloadUrl = baseUrl + "/api/materiale/download?id=" + primoElem.getId();
            HttpRequest reqDl = HttpRequest.newBuilder(URI.create(downloadUrl)).build();
            HttpResponse<byte[]> respDl = client.send(reqDl, HttpResponse.BodyHandlers.ofByteArray());
            assert respDl.statusCode() == 200 : "Download failed with status: " + respDl.statusCode();
            assert respDl.body().length > 0 : "Download returned empty body";
            System.out.println("✓ 8. Download binario risorsa (" + primoElem.getNome() + ", " + respDl.body().length + " bytes) riuscito");

            // 9. Anteprima Polimorfica (UC10)
            HttpRequest reqAnt = HttpRequest.newBuilder(URI.create(baseUrl + "/api/materiale/anteprima?id=" + primoElem.getId())).build();
            HttpResponse<String> respAnt = client.send(reqAnt, HttpResponse.BodyHandlers.ofString());
            assert respAnt.statusCode() == 200 : "Anteprima failed: " + respAnt.body();
            System.out.println("✓ 9. Anteprima polimorfica recuperata: " + respAnt.body());

            // 10. Toggle Preferito (UC10)
            String toggleJson = "{\"idElemento\":\"" + primoElem.getId() + "\"}";
            HttpRequest reqToggle = HttpRequest.newBuilder(URI.create(baseUrl + "/api/materiale/preferiti/toggle"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(toggleJson))
                    .build();
            HttpResponse<String> respToggle = client.send(reqToggle, HttpResponse.BodyHandlers.ofString());
            assert respToggle.statusCode() == 200 : "Toggle preferito failed: " + respToggle.body();
            System.out.println("✓ 10. Toggle preferito studente riuscito: " + respToggle.body());

            System.out.println("\n>>> TUTTI I 10 TEST DI INTEGRAZIONE REST API SONO STATI SUPERATI CON SUCCESSO! <<<");
        } finally {
            server.stop();
        }
    }
}
