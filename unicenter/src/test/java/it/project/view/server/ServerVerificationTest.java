package it.project.view.server;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import it.project.Unicenter;

public class ServerVerificationTest {
    public static void main(String[] args) throws Exception {
        Unicenter unicenter = Unicenter.getInstance();
        unicenter.popolaDataBase();

        UniCenterWebServer server = new UniCenterWebServer(unicenter);
        boolean started = server.start(8085);
        if (!started) {
            System.err.println("FAILED to start server");
            System.exit(1);
        }

        String baseUrl = server.getBaseUrl();
        System.out.println("Testing server on " + baseUrl);
        HttpClient client = HttpClient.newHttpClient();

        // 1. Test Static Index
        HttpRequest reqIndex = HttpRequest.newBuilder(URI.create(baseUrl + "/")).build();
        HttpResponse<String> respIndex = client.send(reqIndex, HttpResponse.BodyHandlers.ofString());
        assert respIndex.statusCode() == 200 : "Index status code not 200: " + respIndex.statusCode();
        assert respIndex.body().contains("UniCenter") : "Index content missing UniCenter";
        System.out.println("✓ Static index.html served correctly (status 200)");

        // 2. Test Static CSS
        HttpRequest reqCss = HttpRequest.newBuilder(URI.create(baseUrl + "/css/style.css")).build();
        HttpResponse<String> respCss = client.send(reqCss, HttpResponse.BodyHandlers.ofString());
        assert respCss.statusCode() == 200 : "CSS status code not 200: " + respCss.statusCode();
        System.out.println("✓ Static style.css served correctly (status 200)");

        // 3. Test Static JS
        HttpRequest reqJs = HttpRequest.newBuilder(URI.create(baseUrl + "/js/app.js")).build();
        HttpResponse<String> respJs = client.send(reqJs, HttpResponse.BodyHandlers.ofString());
        assert respJs.statusCode() == 200 : "JS status code not 200: " + respJs.statusCode();
        System.out.println("✓ Static app.js served correctly (status 200)");

        // 4. Test API Demo Users
        HttpRequest reqDemo = HttpRequest.newBuilder(URI.create(baseUrl + "/api/auth/demo-users")).build();
        HttpResponse<String> respDemo = client.send(reqDemo, HttpResponse.BodyHandlers.ofString());
        assert respDemo.statusCode() == 200 : "Demo users status not 200: " + respDemo.statusCode();
        System.out.println("✓ API /api/auth/demo-users: " + respDemo.body());

        // 5. Test Student Login API
        String loginJson = "{\"email\":\"mario.rossi@studenti.it\",\"password\":\"pass123\"}";
        HttpRequest reqLogin = HttpRequest.newBuilder(URI.create(baseUrl + "/api/auth/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(loginJson))
                .build();
        HttpResponse<String> respLogin = client.send(reqLogin, HttpResponse.BodyHandlers.ofString());
        assert respLogin.statusCode() == 200 : "Login status not 200: " + respLogin.statusCode();
        System.out.println("✓ API /api/auth/login (Student): " + respLogin.body());

        // 6. Test Student Dashboard API
        HttpRequest reqDash = HttpRequest.newBuilder(URI.create(baseUrl + "/api/student/dashboard")).build();
        HttpResponse<String> respDash = client.send(reqDash, HttpResponse.BodyHandlers.ofString());
        assert respDash.statusCode() == 200 : "Student dashboard status not 200: " + respDash.statusCode();
        System.out.println("✓ API /api/student/dashboard: " + respDash.body());

        // 7. Test Student Appelli Disponibili
        HttpRequest reqAppelli = HttpRequest.newBuilder(URI.create(baseUrl + "/api/student/appelli-disponibili")).build();
        HttpResponse<String> respAppelli = client.send(reqAppelli, HttpResponse.BodyHandlers.ofString());
        assert respAppelli.statusCode() == 200 : "Appelli status not 200: " + respAppelli.statusCode();
        System.out.println("✓ API /api/student/appelli-disponibili: " + respAppelli.body());

        // 7b. Test Student Libretto
        HttpRequest reqLib = HttpRequest.newBuilder(URI.create(baseUrl + "/api/student/libretto")).build();
        HttpResponse<String> respLib = client.send(reqLib, HttpResponse.BodyHandlers.ofString());
        assert respLib.statusCode() == 200 : "Libretto status not 200: " + respLib.statusCode();
        System.out.println("✓ API /api/student/libretto: " + respLib.body());

        // 8. Test Professor Login API
        String profLoginJson = "{\"email\":\"mario.rossi@unicenter.it\",\"password\":\"pass123\"}";
        HttpRequest reqProfLogin = HttpRequest.newBuilder(URI.create(baseUrl + "/api/auth/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(profLoginJson))
                .build();
        HttpResponse<String> respProfLogin = client.send(reqProfLogin, HttpResponse.BodyHandlers.ofString());
        assert respProfLogin.statusCode() == 200 : "Prof login status not 200: " + respProfLogin.statusCode();
        System.out.println("✓ API /api/auth/login (Professor): " + respProfLogin.body());

        // 9. Test Professor Materie API
        HttpRequest reqProfMat = HttpRequest.newBuilder(URI.create(baseUrl + "/api/professor/materie")).build();
        HttpResponse<String> respProfMat = client.send(reqProfMat, HttpResponse.BodyHandlers.ofString());
        assert respProfMat.statusCode() == 200 : "Prof materie status not 200: " + respProfMat.statusCode();
        System.out.println("✓ API /api/professor/materie: " + respProfMat.body());

        // 9b. Test Professor Notifiche API
        HttpRequest reqProfNotif = HttpRequest.newBuilder(URI.create(baseUrl + "/api/professor/notifiche")).build();
        HttpResponse<String> respProfNotif = client.send(reqProfNotif, HttpResponse.BodyHandlers.ofString());
        assert respProfNotif.statusCode() == 200 : "Prof notifiche status not 200: " + respProfNotif.statusCode();
        System.out.println("✓ API /api/professor/notifiche: " + respProfNotif.body());

        // 10. Test Admin Login API
        String adminLoginJson = "{\"email\":\"admin@unicenter.it\",\"password\":\"admin123\"}";
        HttpRequest reqAdminLogin = HttpRequest.newBuilder(URI.create(baseUrl + "/api/auth/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(adminLoginJson))
                .build();
        HttpResponse<String> respAdminLogin = client.send(reqAdminLogin, HttpResponse.BodyHandlers.ofString());
        assert respAdminLogin.statusCode() == 200 : "Admin login status not 200: " + respAdminLogin.statusCode();
        System.out.println("✓ API /api/auth/login (Admin): " + respAdminLogin.body());

        // 11. Test Admin Stats API
        HttpRequest reqAdminStats = HttpRequest.newBuilder(URI.create(baseUrl + "/api/admin/stats")).build();
        HttpResponse<String> respAdminStats = client.send(reqAdminStats, HttpResponse.BodyHandlers.ofString());
        assert respAdminStats.statusCode() == 200 : "Admin stats status not 200: " + respAdminStats.statusCode();
        System.out.println("✓ API /api/admin/stats: " + respAdminStats.body());

        server.stop();
        System.out.println("\nALL 11 INTEGRATION VERIFICATIONS PASSED SUCCESSFULLY!");
    }
}
