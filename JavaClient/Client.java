import java.io.*;
import java.net.*;
import java.net.http.*;
import javax.xml.parsers.*;
import javax.xml.xpath.*;
import org.w3c.dom.*;

import java.io.StringReader;


import static spark.Spark.*;
import java.lang.StringBuilder;

import org.w3c.dom.Node;

/**
 * This approach uses the java.net.http.HttpClient classes, which
 * were introduced in Java11.
 */
public class Client {
    private static DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    public static String uri = "";

    public static void main(String... args) throws Exception {

        String host = args[0];
        int port = Integer.parseInt(args[1]);
        uri = "http://" + host + ":" + port + "/RPC";

        System.out.println(add() == 0);
        System.out.println(add(1, 2, 3, 4, 5) == 15);
        System.out.println(add(2, 4) == 6);
        System.out.println(subtract(12, 6) == 6);
        System.out.println(multiply(3, 4) == 12);
        System.out.println(multiply(1, 2, 3, 4, 5) == 120);
        System.out.println(divide(10, 5) == 2);
        System.out.println(modulo(10, 5) == 0);
    }
    public static int add(int lhs, int rhs) throws Exception {
        return helper("add", lhs, rhs);
    }
    public static int add(Integer... params) throws Exception {
        return helper("add", (int[])args);
    }
    public static int subtract(int lhs, int rhs) throws Exception {
        return helper("subtract", lhs, rhs);
    }
    public static int multiply(int lhs, int rhs) throws Exception {
        return helper("multiply", lhs, rhs);
    }
    public static int multiply(Integer... params) throws Exception {
        return helper("multiply", (int[])args);
    }
    public static int divide(int lhs, int rhs) throws Exception {
        return helper("divide", lhs, rhs);
    }
    public static int modulo(int lhs, int rhs) throws Exception {
        return helper("modulo", lhs, rhs);
    }

    public static int helper (String methodName, int... args) {
        StringBuilder xreq = new StringBuilder();
        xres.append("?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xres.append("<methodCall>\n");
        xres.append("<methodName>").append(methodName).append("</methodName>\n");
        for (int arg:args) {
            xres.append("<params><param><value><i4>").append(arg).append("</i4></value></param></params>\n");
        }
        xres.append("</methodCall>");
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest req = HttpRequest.newBuilder()
            .uri(URI.create(uri))
            .header("Content-Type", "text/xml")
            .POST(HttpRequest.BodyPublishers.ofString(xreq))
            .build();
        HttpResponse<String> response = client.send(req, HttpResponse.BodyHandlers.ofString());
        DocumentBuilder builder = dbf.newDocumentBuilder();

        Document document = builder.parse(new InputSource(new StringReader(body)));
        Element root = document.getDocumentElement();
        String ans = root.getElementsByTagName("i4").item(0).getTextContent();
        return (int) ans;
    }
}
