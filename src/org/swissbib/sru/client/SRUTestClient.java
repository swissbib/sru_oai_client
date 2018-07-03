package org.swissbib.sru.client;

import com.sun.jndi.toolkit.url.Uri;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by Project SwissBib, www.swissbib.org.
 * Author: Günter Hipler
 * Date: 28.01.2010
 * Time: 09:16:11
 */
public class SRUTestClient {

    private static DocumentBuilder documentBuilder;
    private static DocumentBuilderFactory factory;

    private static HashMap<String, String> arguments;


    public static void main(String[] args) {



        //String requestTemplate = "http://www.swissbib.ch/SRW/search/?query=%s=\"%s\"&version=1.1&operation=searchRetrieve&recordSchema=info:srw/schema/1/marcxml-v1.1&maximumRecords=%s&startRecord=%s&resultSetTTL=300&recordPacking=xml&recordXPath=&sortKeys=";
        //String requestTemplate = "http://localhost:18080/SRW/search/?query=%s=\"%s\"&version=1.1&operation=searchRetrieve&recordSchema=info:srw/schema/1/marcxml-v1.1&maximumRecords=%s&startRecord=%s&resultSetTTL=300&recordPacking=xml&recordXPath=&sortKeys=";
        //mit holdings
        //String requestTemplate = "http://localhost:18080/SRW/search/?query=%s=\"%s\"&version=1.1&operation=searchRetrieve&recordSchema=info:srw/schema/1/marcxml-v1.1&maximumRecords=%s&startRecord=%s&resultSetTTL=300&recordPacking=xml&recordXPath=&sortKeys=&x-info-10-get-holdings=true";
        //String requestTemplate = "http://www.swissbib.ch/SRW/search/?query=%s=\"%s\"&version=1.1&operation=searchRetrieve&recordSchema=info:srw/schema/1/marcxml-v1.1&maximumRecords=%s&startRecord=%s&resultSetTTL=300&recordPacking=xml&recordXPath=&sortKeys=&x-info-10-get-holdings=true";

        //String requestTemplate = "http://sru.swissbib.ch/sru/search/defaultdb?query=%s=\"%s\"&version=1.1&operation=searchRetrieve&recordSchema=info:srw/schema/1/marcxml-v1.1&maximumRecords=%s&startRecord=%s&resultSetTTL=300&recordPacking=xml&recordXPath=&sortKeys=";


        //String requestTemplateWithoutQuery = "http://sru.swissbib.ch/sru/search/E74?query=%s=%s&version=1.1&operation=searchRetrieve&recordSchema=info:srw/schema/1/marcxml-v1.1&maximumRecords=%s&startRecord=%s&resultSetTTL=300&recordPacking=xml&recordXPath=&sortKeys=";


        //http://sru.swissbib.ch/sru/search/E74?query=&operation=searchRetrieve&recordSchema=info%3Asrw%2Fschema%2F1%2Fmarcxml-v1.1-light&maximumRecords=10&startRecord=0&recordPacking=XML&availableDBs=E74



        //String requestTemplate = "http://srutest.swissbib.ch/search?query=%s=\"%s\"&version=1.1&operation=searchRetrieve&recordSchema=info:srw/schema/1/marcxml-v1.1&maximumRecords=%s&startRecord=%s&resultSetTTL=300&recordPacking=xml&recordXPath=&sortKeys=";
        //String requestTemplate = "http://localhost:8111/search?query=%s=\"%s\"&version=1.1&operation=searchRetrieve&recordSchema=info:srw/schema/1/marcxml-v1.1&maximumRecords=%s&startRecord=%s&resultSetTTL=300&recordPacking=xml&recordXPath=&sortKeys=";



        //String requestTemplate = "http://www.swissbib.ch/SRW/search/?query=dc.anywhere+%3D+%22xml%22&version=1.1&operation=searchRetrieve&recordSchema=info%3Asrw%2Fschema%2F1%2Fmarcxml-v1.1&maximumRecords=100&startRecord=%s&resultSetTTL=300&recordPacking=xml&recordXPath=&sortKeys=";
        //String request = String.format(requestTemplate,operator,"",maxRecords,1);

        initializeHashmapWithArguments(args);
        String request = formatQuery();
        System.out.println("used request: " + request);

        try {

            Document aDocument = SRUTestClient.getRecordsFromSwissBib(request);
            NodeList aNodeList;
            long numberOfRecords = 0;
            long nextRecordPosition = 0;
            if (aDocument != null) {

                numberOfRecords = Long.valueOf(aDocument.getElementsByTagName("numberOfRecords").item(0).getTextContent());

                if (numberOfRecords != 0 ){

                    nextRecordPosition = Long.valueOf(aDocument.getElementsByTagName("nextRecordPosition").item(0).getTextContent());

                    System.out.print("number of records: ");
                    System.out.println(numberOfRecords);                    
                    while (nextRecordPosition < numberOfRecords){

                        request = formatQuery(Long.toString(nextRecordPosition));
                        aDocument = SRUTestClient.getRecordsFromSwissBib(request);
                        if (null != aDocument) {
                            aNodeList = aDocument.getElementsByTagName("nextRecordPosition");


                            nextRecordPosition = numberOfRecords;
                            for (int i = 0; i < aNodeList.getLength(); i++) {
                                nextRecordPosition = Long.valueOf(aDocument.getElementsByTagName("nextRecordPosition").item(i).getTextContent());

                            }
                            System.out.print("next record: ");
                            System.out.println(nextRecordPosition);
                        }
                    }

                } else {
                    System.out.println("sorry no records were fetched...");
                }
            } else {
                System.out.println("sorry document-model wasn't initialized...");

            }




        }catch (Throwable th){
            System.out.println("general Exception");
            th.printStackTrace();
        }


    }

    private static Document getRecordsFromSwissBib(String urlString){
        Document aDocument = null;
        InputStream is = null;

        try {

            System.out.println("********new request**************");
            System.out.println(urlString);

            URL url = new URL( urlString );
            Date date1 = new Date();

            is = url.openStream();

            Date date2 = new Date();

            long second2 = date2.getTime();
            long second1 = date1.getTime();
            System.out.println("fetched time: " + (second2 - second1) + " ms" );



            if (SRUTestClient.documentBuilder == null){
                SRUTestClient.factory = DocumentBuilderFactory.newInstance();
                factory.setNamespaceAware(true);
                factory.setValidating(false);
                factory.setExpandEntityReferences(false);
                SRUTestClient.documentBuilder = factory.newDocumentBuilder(); 

            }
            //response = new Scanner( is ).useDelimiter( "\\Z" ).next();
            //System.out.println("Response:");
            //System.out.println(response);
            //SRUTestClient.DumpResponse(is);
            aDocument = SRUTestClient.documentBuilder.parse(is);
            //SRUTestClient.DumpResponse(is);
            //System.out.println(aDocument.getDocumentElement().toString());

        }
        catch(MalformedURLException mURLE){
            mURLE.printStackTrace();
        }  catch (IOException ioE){
            ioE.printStackTrace();
        //}catch (SAXException sE) {

        //    sE.printStackTrace();
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

    private static void DumpResponse(InputStream is) {
        try {

            BufferedReader in = new BufferedReader(new InputStreamReader(is));
            String inputLine;
            while ((inputLine = in.readLine()) != null)
                System.out.println(inputLine);
            in.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }


    private static void garbage() {
/*
            //StringBuffer bufXServerResponse = new StringBuffer(xServerResponse);
            //ByteArrayInputStream bAxServerResponse = new ByteArrayInputStream(bufXServerResponse.toString().getBytes("UTF-8"));
            /*
            final XMLInputFactory inputFactory = XMLInputFactory.newInstance();


            XMLStreamReader p = inputFactory.createXMLStreamReader(is) ;

            while(p.hasNext() )
            {   try {
                    int type = p.next();

                    switch(type) {
                        case XMLStreamConstants.START_ELEMENT:
                            String name = p.getLocalName();
                            System.out.println(name);
                            if (name.equalsIgnoreCase("record")) {
                                String elementText = p.getElementText();
                                System.out.println(elementText);
                                //we got the correct rootElement within the XMLResponse
                            }
                    }


                } catch (Exception ex){
                    ex.printStackTrace();
                } catch (Throwable thEx) {
                    thEx.printStackTrace();
                }
            }

            */


    }

    private static void initializeHashmapWithArguments(String[] args) {

        SRUTestClient.arguments = new HashMap<>();
        if (args.length == 1) {
            SRUTestClient.arguments.put("operator", "");
            SRUTestClient.arguments.put("query", "");
            SRUTestClient.arguments.put("maxRecords", args[0]);

            //String requestTemplateWithoutQuery = "http://sru.swissbib.ch/sru/search/E74?query=%s=%s&version=1.1&operation=searchRetrieve&recordSchema=info:srw/schema/1/marcxml-v1.1&maximumRecords=%s&startRecord=%s&resultSetTTL=300&recordPacking=xml&recordXPath=&sortKeys=";
            //request =  String.format(requestTemplateWithoutQuery,args[0]);

        } else if (args.length == 3) {

            SRUTestClient.arguments.put("operator", args[0]);
            SRUTestClient.arguments.put("query", args[1]);
            SRUTestClient.arguments.put("maxRecords", args[2]);
            //String requestTemplateWithValues = "http://sru.swissbib.ch/sru/search/E74?query=%s=%s&version=1.1&operation=searchRetrieve&recordSchema=info:srw/schema/1/marcxml-v1.1&maximumRecords=%s&startRecord=%s&resultSetTTL=300&recordPacking=xml&recordXPath=&sortKeys=";
            //request =  String.format(requestTemplateWithValues,args[0], args[1], args[2]);
        } else {
            SRUTestClient.arguments.put("operator", "");
            SRUTestClient.arguments.put("query", "");
            SRUTestClient.arguments.put("maxRecords", "10");
        }

    }


    private static String formatQuery(String position) {

        //String requestTemplate = "http://sru.swissbib.ch/sru/search/E74?query=&operation=searchRetrieve&recordSchema=info:srw/schema/1/marcxml-v1.1-light&maximumRecords=%s&startRecord=%s&recordPacking=XML&availableDBs=E74";
        //String requestTemplate = "http://sru.swissbib.ch/sru/search/E74?query=&operation=searchRetrieve&recordSchema=info:srw/schema/1/marcxml-v1.1-light&maximumRecords=%s&x-info-10-get-holdings=true&startRecord=%s&recordPacking=XML&availableDBs=E74";
        String requestTemplate ="http://sru.swissbib.ch/sru/search/jusdb?query=&operation=searchRetrieve&recordSchema=info:srw/schema/1/marcxml-v1.1-light&maximumRecords=%s&x-info-10-get-holdings=true&startRecord=%s&recordPacking=XML&availableDBs=jusdb";


        return String.format(requestTemplate,  SRUTestClient.arguments.get("maxRecords"),position);

    }


    private static String formatQuery() {
        return formatQuery("0");

    }




}
