package org.swissbib.sru.client;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

public class SRUTestClientRestlet {

    private static DocumentBuilder documentBuilder;
    private static DocumentBuilderFactory factory;


    public static void main(String[] args) {


        String baseURL = "http://sb-vf7.swissbib.unibas.ch/sru/search?";

        File f = new File("/home/swissbib/temp/srurquests/touse");
       DocumentBuilder documentBuilder;
        DocumentBuilderFactory factory;


        for (File singleFile:  f.listFiles()) {

            if (singleFile != null) {

                try {

                    BufferedReader br = new BufferedReader(new FileReader(singleFile));
                    String line;
                    while ((line = br.readLine()) != null) {

                        String request = baseURL + line;
                        System.out.println(request);
                        URL url = new URL( request );
                        InputStream  is = url.openStream();
                        factory = DocumentBuilderFactory.newInstance();
                        factory.setNamespaceAware(true);
                        factory.setValidating(false);
                        factory.setExpandEntityReferences(false);
                        documentBuilder =  factory.newDocumentBuilder();
                        Document d = documentBuilder.parse(is);

                        System.out.println(d.getElementsByTagName("query").item(0).getTextContent());
                        System.out.println(d.getElementsByTagName("numberOfRecords").item(0).getTextContent());




                        // process the line.
                    }
                    br.close();




                } catch (FileNotFoundException nfE) {

                    nfE.printStackTrace();

                } catch (IOException ioE) {

                    ioE.printStackTrace();
                } catch (ParserConfigurationException pcE) {
                    pcE.printStackTrace();
                } catch (SAXException sE) {

                    sE.printStackTrace();
                }


            }
        }


        if (args.length != 3) {
            System.out.println("use java .... [operator e.g. dc.anywhere] [value] [maximum records]");
            System.exit(0);
        }

        String operator = args[0];
        String maxRecords = args[2];
        String termvalue =  args[1];

        //String requestTemplate = "http://www.swissbib.ch/SRW/search/?query=%s=\"%s\"&version=1.1&operation=searchRetrieve&recordSchema=info:srw/schema/1/marcxml-v1.1&maximumRecords=%s&startRecord=%s&resultSetTTL=300&recordPacking=xml&recordXPath=&sortKeys=";
        //String requestTemplate = "http://localhost:18080/SRW/search/?query=%s=\"%s\"&version=1.1&operation=searchRetrieve&recordSchema=info:srw/schema/1/marcxml-v1.1&maximumRecords=%s&startRecord=%s&resultSetTTL=300&recordPacking=xml&recordXPath=&sortKeys=";
        //mit holdings
        //String requestTemplate = "http://localhost:18080/SRW/search/?query=%s=\"%s\"&version=1.1&operation=searchRetrieve&recordSchema=info:srw/schema/1/marcxml-v1.1&maximumRecords=%s&startRecord=%s&resultSetTTL=300&recordPacking=xml&recordXPath=&sortKeys=&x-info-10-get-holdings=true";
        //String requestTemplate = "http://www.swissbib.ch/SRW/search/?query=%s=\"%s\"&version=1.1&operation=searchRetrieve&recordSchema=info:srw/schema/1/marcxml-v1.1&maximumRecords=%s&startRecord=%s&resultSetTTL=300&recordPacking=xml&recordXPath=&sortKeys=&x-info-10-get-holdings=true";
        //String requestTemplate = "http://sru.swissbib.ch?query=%s=\"%s\"&version=1.1&operation=searchRetrieve&recordSchema=info:srw/schema/1/marcxml-v1.1&maximumRecords=%s&startRecord=%s&resultSetTTL=300&recordPacking=xml&recordXPath=&sortKeys=";
        //String requestTemplate = "http://srutest.swissbib.ch/search?query=%s=\"%s\"&version=1.1&operation=searchRetrieve&recordSchema=info:srw/schema/1/marcxml-v1.1&maximumRecords=%s&startRecord=%s&resultSetTTL=300&recordPacking=xml&recordXPath=&sortKeys=";
        String requestTemplate = "http://localhost:8111/search?query=%s=\"%s\"&version=1.1&operation=searchRetrieve&recordSchema=info:srw/schema/1/marcxml-v1.1&maximumRecords=%s&startRecord=%s&resultSetTTL=300&recordPacking=xml&recordXPath=&sortKeys=";



        //String requestTemplate = "http://www.swissbib.ch/SRW/search/?query=dc.anywhere+%3D+%22xml%22&version=1.1&operation=searchRetrieve&recordSchema=info%3Asrw%2Fschema%2F1%2Fmarcxml-v1.1&maximumRecords=100&startRecord=%s&resultSetTTL=300&recordPacking=xml&recordXPath=&sortKeys=";
        String request = String.format(requestTemplate,operator,termvalue,maxRecords,1);

        System.out.println("used request: " + request);

        try {

            Document aDocument = SRUTestClientRestlet.getRecordsFromSwissBib(request);
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

                        request = String.format(requestTemplate,operator,termvalue, maxRecords,nextRecordPosition);
                        aDocument = SRUTestClientRestlet.getRecordsFromSwissBib(request);
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



            if (SRUTestClientRestlet.documentBuilder == null){
                SRUTestClientRestlet.factory = DocumentBuilderFactory.newInstance();
                factory.setNamespaceAware(true);
                factory.setValidating(false);
                factory.setExpandEntityReferences(false);
                SRUTestClientRestlet.documentBuilder = factory.newDocumentBuilder();

            }
            //response = new Scanner( is ).useDelimiter( "\\Z" ).next();
            //System.out.println("Response:");
            //System.out.println(response);
            aDocument = SRUTestClientRestlet.documentBuilder.parse(is);
            //System.out.println(aDocument.getDocumentElement().toString());

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

    private void DumpResponse(InputStream is) {
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


}
