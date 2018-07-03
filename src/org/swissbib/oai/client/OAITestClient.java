package org.swissbib.oai.client;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

/**
 * Created by Project SwissBib, www.swissbib.org.
 * Author: Günter Hipler
 * Date: 02.02.2010
 * Time: 09:16:11
 */
public class OAITestClient {

    private static DocumentBuilder documentBuilder;
    private static DocumentBuilderFactory factory;

    private static int totalNumberOfFetchedRecords = 0;

    private static boolean debug = false;
    private static String url = null;
    private static String nextURL = null;



    public static void main(String[] args) {

        if (args.length < 1 ||  args.length > 3) {
            System.out.println("use java .... [url] [urlResumption] [debug]");
            System.exit(0);
        }

        if (args.length == 1) {
            url = args[0];
        }

        if (args.length == 2 || args.length == 3)  {
            url = args[0];
            nextURL = args[1];
        }

        if (args.length == 3) {

            if (args[2].equalsIgnoreCase("debug")) {
                debug = true;
            }
        }


        if (debug) {
            System.out.println("used request: " + url);
        }

        if (args.length == 2 || args.length == 3) {
            processListRecords();
        } else {
            processSingleRequest();
        }

    }

    private static Document getRecordsFromOAIRepository(String urlString){
        Document aDocument = null;
        InputStream is = null;


        try {
            URL url = new URL( urlString );
            Date date1 = new Date();

            is = url.openStream();

            Date date2 = new Date();

            long second2 = date2.getTime();
            long second1 = date1.getTime();
            if (debug) {
                System.out.println("fetched time: " + (second2 - second1) + " ms" );
            }



            if (OAITestClient.documentBuilder == null){
                OAITestClient.factory = DocumentBuilderFactory.newInstance();
                factory.setNamespaceAware(true);
                factory.setValidating(false);
                factory.setExpandEntityReferences(false);
                OAITestClient.documentBuilder = factory.newDocumentBuilder();

            }
            aDocument = OAITestClient.documentBuilder.parse(is);

        }
        catch(MalformedURLException mURLE){
            mURLE.printStackTrace();
        }  catch (IOException ioE){
            ioE.printStackTrace();
        }catch (SAXException sE) {
            sE.printStackTrace();
        }catch (Throwable th){
            System.out.println("general Exception");
            th.printStackTrace();
        }
        finally {
            if (is != null){
                try {
                    is.close();
                } catch (IOException ioE){
                    ioE.printStackTrace();
                }
            }
        }

        return aDocument;

    }

    private static void analyzeFetchedDocuments(Document aDocument) {

        /*

        XPath xpath = XPathFactory.newInstance().newXPath();

        try {
        XPathExpression contentExpression = xpath.compile("//ListRecords/record/header");

        NodeList nodelist = (NodeList) contentExpression.evaluate(aDocument,XPathConstants.NODESET);

        for (int z = 0; z< nodelist.getLength();z++){

            Node node = nodelist.item(z);
            System.out.println(node.getLocalName());

        }


        } catch (XPathExpressionException xPE) {
            xPE.printStackTrace();
        }

        */

        NodeList aNodeListRecords = aDocument.getElementsByTagName("header");

        if (debug) {
            System.out.println("number of fetched records: " + aNodeListRecords.getLength());
        }

        OAITestClient.totalNumberOfFetchedRecords += aNodeListRecords.getLength();

        //NodeList headerChilds = (NodeList) aNodeListRecords


        

        for (int m = 0; m < aNodeListRecords.getLength();m++) {

            String identifier = "";
            String datetime = "";

            NodeList headerList = (NodeList)aNodeListRecords.item(m).getChildNodes();
            for (int bb =0; bb < headerList.getLength();bb++) {

                //System.out.println(headerList.item(bb).getTextContent());
                //System.out.println(headerList.item(bb).getLocalName());

                if (headerList.item(bb).getLocalName() == null) {
                    //really strange: SNL has contains node values with local name = null!
                    continue;    
                }

                if ( headerList.item(bb).getLocalName().toLowerCase().equals("identifier")) {
                    identifier = headerList.item(bb).getTextContent();
                } else if (headerList.item(bb).getLocalName().toLowerCase().equals("datestamp")) {
                    datetime = headerList.item(bb).getTextContent();

                }

            }
            if (debug) {
                System.out.println("fetched item " + identifier + ": datestamp " + datetime);
            }
        }
        /*
        for (int i = 0; i < aNodeListRecords.getLength(); i++) {

            Node recordNode =  aNodeListRecords.item(i);

            NodeList recordChildList = recordNode.getChildNodes();
            for (int j = 0; j < recordChildList.getLength();j++) {



                Node recordCilde = recordChildList.item(j);
                long laenge = recordCilde.getChildNodes().getLength();
                Node header = recordCilde.getFirstChild();
                System.out.println(header.getFirstChild().getNodeValue());
                System.out.println(header.getLastChild().getNodeValue());


                if (recordCilde.getChildNodes().getLength() == 2) {

                    String value1 = recordCilde.getFirstChild().getNodeValue();
                    String value2 = recordCilde.getLastChild().getNodeValue();
                    System.out.println();


                }
                else {
                    continue;
                }
                */
                //String headerName = header.getLocalName();

                //if (header.getLocalName().toLowerCase().equals("header")) {
                    /*
                    String identifier = "";
                    String datestanp = "";

                    NodeList headerChilds = header.getChildNodes();
                    for (int k = 0; k < headerChilds.getLength(); k++) {
                        if (headerChilds.item(k).getLocalName().toLowerCase().equals("identifier")) {
                            identifier = headerChilds.item(k).getTextContent();
                        } else if (headerChilds.item(k).getLocalName().toLowerCase().equals("datestamp")) {
                            datestanp = headerChilds.item(k).getTextContent();

                        }
                    }
                    System.out.println("fetched item " + identifier + ": datestamp " + datestanp);
                    */
                //}
    }


    private static void printFetchedResult(Document aDocument) {

        if (!debug) {

            //wenn ich debug setze, möchte ich nur die Anzahl Sätze wissen

            try {

                TransformerFactory xf = TransformerFactory.newInstance();
                xf.setAttribute("indent-number", new Integer(2));

                Transformer xformer = xf.newTransformer();
                xformer.setOutputProperty(OutputKeys.METHOD, "xml");
                xformer.setOutputProperty(OutputKeys.INDENT, "yes");
                xformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

                //Result out = new StreamResult(new OutputStreamWriter(System.out,"UTF-8"));
                Result out = new StreamResult(new OutputStreamWriter(System.out));

                xformer.transform(new DOMSource(aDocument), out);

            } catch (TransformerConfigurationException tcEx) {
                tcEx.printStackTrace();
            } catch (TransformerException tex) {
                tex.printStackTrace();
            }
        }

        
    }

    private static void processListRecords() {
        try {

            Document aDocument = OAITestClient.getRecordsFromOAIRepository(url);

            printFetchedResult(aDocument);

            NodeList aNodeList;

            String resumptionToken = null;
            if (aDocument != null) {

                    OAITestClient.analyzeFetchedDocuments(aDocument);

                    aNodeList = aDocument.getElementsByTagName("resumptionToken");

                    for (int i = 0; i < aNodeList.getLength(); i++) {
                        resumptionToken = aDocument.getElementsByTagName("resumptionToken").item(0).getTextContent();

                    }
                    if (debug) {
                        System.out.println(resumptionToken);
                    }

                    int numberRequests = 0;

                    while (resumptionToken != null){

                        numberRequests++;

                        if (debug) {
                            System.out.println("number of Requests: " + numberRequests);
                            System.out.println("next resumption token: " + resumptionToken);
                        }

                        url = nextURL + "&resumptionToken=" + resumptionToken ;
                        aDocument = OAITestClient.getRecordsFromOAIRepository(url);
                        printFetchedResult(aDocument);

                        resumptionToken = null;

                        if (null != aDocument) {

                            OAITestClient.analyzeFetchedDocuments(aDocument);

                            aNodeList = aDocument.getElementsByTagName("resumptionToken");

                            for (int i = 0; i < aNodeList.getLength(); i++) {
                                resumptionToken = aDocument.getElementsByTagName("resumptionToken").item(0).getTextContent();

                            }
                        }
                        if (debug) {
                            System.out.println(resumptionToken);
                        }

                }

            } else {
                System.out.println("sorry document-model wasn't initialized...");

            }




        }catch (Throwable th){
            System.out.println("general Exception");
            th.printStackTrace();
        }
        if (debug) {
            System.out.println("total number of fetched records: " + OAITestClient.totalNumberOfFetchedRecords);
        }

    }


    private static void processSingleRequest(){
        Document aDocument = OAITestClient.getRecordsFromOAIRepository(url);
        printFetchedResult(aDocument);
    }



}
