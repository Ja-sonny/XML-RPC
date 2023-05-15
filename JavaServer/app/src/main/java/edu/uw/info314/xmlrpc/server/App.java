package edu.uw.info314.xmlrpc.server;

import java.util.*;
import java.util.logging.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import java.io.StringReader;


import static spark.Spark.*;
import java.lang.StringBuilder;

class Call {
    public String name;
    public List<Object> args = new ArrayList<Object>();
}

public class App {
    public static final Logger LOG = Logger.getLogger(App.class.getCanonicalName());
    static int serverPort = 8080;

    public static void main(String[] args) {
        LOG.info("Starting up on port " + serverPort);

        port(serverPort);
        // This is the mapping for POST requests to "/RPC";
        // this is where you will want to handle incoming XML-RPC requests
        post("/RPC", (request, response) -> {
            response.type("text/xml");
            String body = request.body();
            Call calldata = extractXMLRPCCall(body);
            int res = calc(calldata);
            if(res > 0) {
                response.status(200);
            }
            return xmlbuilder(res);
        });

        before("/RPC/*", (request, response) -> {
            if (!request.requestMethod().equalsIgnoreCase("POST")) {
                response.header("Allow", "POST");
                halt(405, "Only POST is Allowed");
            }
        });

        // Using Route
        notFound((request, response) -> {
            response.status(404);
            return "Not Found";
        });
    }

    public static Call extractXMLRPCCall(String body){
        Call call = new Call();
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            Document document = builder.parse(new InputSource(new StringReader(body)));
            Element root = document.getDocumentElement();
            String methodName = root.getElementsByTagName("methodName").item(0).getTextContent();
            NodeList params = root.getElementsByTagName("i4");
            List<Object> plist = new ArrayList<Object>();

            if (params.getLength() >= 1) {
                for (int i = 0; i < params.getLength(); i++) {
                    Node pNode = params.item(i);
                    String val = pNode.getTextContent();
                    if(!isValid(val)) {
                        call.name = "error";
                        return call;
                    }
                    plist.add(Integer.parseInt(val));
                }
            }
            call.name = methodName;
            call.args = plist;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return call;
    }

    public static int calc(Call call) {
        if (call.name.equals("error")){
            return -3;
        }
        Calc calc = new Calc();
        String methodName = call.name;
        int res = 0;
        int s = call.args.size();
        switch (methodName) {
            case "add":
                int[] ilist = new int[s];
                for (int i = 0; i < s; i++){
                    ilist[i] = (int) call.args.get(i);
                }
                res += calc.add(ilist);
                return res;
            case "subtract":
                int lhs = (int) call.args.get(0);
                int rhs = (int) call.args.get(1);
                res += calc.subtract(lhs, rhs);
                return res;
            case "multiply":
                int[] idlist = new int[s];
                for (int i = 0; i < s; i++){
                    idlist[i] = (int) call.args.get(i);
                }
                res += calc.multiply(idlist);
                return res;
            case "divide":
                int lhsd = (int) call.args.get(0);
                int rhsd= (int) call.args.get(1);
                if (rhsd == 0){
                    return -1;
                }
                res += calc.divide(lhsd, rhsd);
                return res;
            case "modulo":
                int lhsm = (int) call.args.get(0);
                int rhsm = (int) call.args.get(1);
                res += calc.modulo(lhsm, rhsm);
                return res;
            default:
                return 404;
        }
    }

    public static String xmlbuilder (int result) {
        if (result < 0){
            String msg = "";
            if (result == -1) {
                msg += "divide by zero.";
            } else {
                msg += "illegal argument type.";
            }
            return createFaultCode(result, msg);
        }

        StringBuilder xres = new StringBuilder();
        xres.append("?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xres.append("<methodResponse>\n");
        xres.append("  <params>\n");
        xres.append("    <param>\n");
        xres.append("      <value><i4>").append(result).append("</i4></value>\n");
        xres.append("    </param>\n");
        xres.append("  </params>\n");
        xres.append("</methodResponse>\n");
        return xres.toString();
    }

    public static String createFaultCode(int faultNo, String msg) {
        StringBuilder xres = new StringBuilder();
        xres.append("?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xres.append("<methodResponse>\n");
        xres.append("  <fault>\n");
        xres.append("    <value>\n");
        xres.append("      <struct>\n");
        xres.append("        <member\n>");
        xres.append("          <name>faultCode</name>\n");
        xres.append("          <value><i4>").append(faultNo).append("</i4></value>\n");
        xres.append("          </member>\n");
        xres.append("        <member>\n");
        xres.append("          <name>faultString</name>\n");
        xres.append("          <value><string>").append(msg).append("</string></value>\n");
        xres.append("          </member>\n");
        xres.append("      </struct>\n");
        xres.append("    </value>\n");
        xres.append("  </fault>\n");
        xres.append("</methodResponse>\n");
        return xres.toString();
    }
    public static boolean isValid (String check){
        try {
            Integer.parseInt(check);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
