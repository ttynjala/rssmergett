package com.sanoma.rssmergett;

import java.util.*;
import java.io.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;

import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.Document;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.w3c.dom.ls.LSOutput;

import org.apache.log4j.*;

public class RssXmlMerger
{
    /**
     * Class for combining several RSS XML streams into one.
     * @author Tuomas Tynjälä
     */

    /* Logger for the class */
    protected final Logger logger = Logger.getLogger(RssXmlMerger.class);
    
    public RssXmlMerger() 
    {
    }

    /**
     * Get and catenate all text node contents directly under node.
     * @param node Node DOM tree node the texts under which we get
     * @return Contained text as a string
     */
    private String getElementTexts(Node node)
    {
        /* Catenating all text element contents */
        String value = "";
        NodeList nodeChildren = node.getChildNodes();
        for(int k=0; k<nodeChildren.getLength(); k++) {
            Node nodeChild = nodeChildren.item(k);
            if (nodeChild.getNodeType() == Node.TEXT_NODE) {
                value += nodeChild.getNodeValue();
            }
        }
        return value;
    }

    /** 
     * Get and catenate all text node contents under child 
     * tag of given node. 
     * @param node Node DOM tree node the texts under which we look for the child.
     * @param childTag Tag name of the child under which we take the text nodes.
     * @return Contained text as a string
     */
    private String getChildTexts(Node node, String childTag)
    {
        if (!(node instanceof Element)) return null;
        String value = "";
        NodeList nodeChildren = ((Element)node).getElementsByTagName(childTag);
        for(int k=0; k<nodeChildren.getLength(); k++) {
            Node nodeChild = nodeChildren.item(k);
            value += getElementTexts(nodeChild);
        }
        return value;
    }

    /**
     * Render XML DOM document as binary to a stream.
     * @param doc XML DOM document to render
     * @param outputStream OutputStream to render into.
     */
    private void serializeDOMTree(Document doc, OutputStream outputStream) 
        throws java.lang.ClassNotFoundException, java.lang.InstantiationException, java.lang.IllegalAccessException
    {
        DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
        DOMImplementationLS impl = (DOMImplementationLS)registry.getDOMImplementation("LS");
        LSSerializer writer = impl.createLSSerializer();
        LSOutput output = impl.createLSOutput();
        output.setByteStream(outputStream);
        writer.write(doc, output);
    }

    /**
     * Create a new element with given text in given place in XML DOM document.
     * @param doc XML DOM document to generate te node into.
     * @param parent The node under which the node is generated (as the last child node)
     * @param tag Tag name for the element to be created
     * @parma text Text contents to put under the created element
     */
    private void createTextChild(Document doc, Node parent, String tag, String text)
    {
        if (text == null) return; 
        Element newElement =  doc.createElement(tag);
        parent.appendChild(newElement);
        Text newElementText = doc.createTextNode(text);
        newElement.appendChild(newElementText);
    }

    /**
     * The actual worker method for comining RSS feeds into one.
     * @param inputs Array of input streams providing the RSS feeds to be combined
     * @param combinedTitle Title text for the combined RSS feed
     * @param combinedImageURL Image URl for the combined RSS feed
     * @param combinedImageTitle Image title for the combined RSS feed
     * @param combinedImageLink Image link for the combined RSS feed
     * @param combinedDescription Description for the combined RSS feed
     * @param combinedLink Link for the combined RSS feed
     * @param combinedLanguage Language code for the combined RSS feed. See
     *        <a href="http://www.rssboard.org/rss-language-codes">http://www.rssboard.org/rss-language-codes</a>
     *        for supported codes.
     * @return The combined RSS XML content as UTF-8 encoded binary.
     * @throws org.xml.sax.SAXException If one of the incoming RSS feeds is invalid XML.
     */
    public byte[] merge(InputStream[] inputs, 
                        String combinedTitle,
                        String combinedImageURL,
                        String combinedImageTitle,
                        String combinedImageLink,
                        String combinedDescription,
                        String combinedLink,
                        String combinedLanguage)
        throws IOException, javax.xml.parsers.ParserConfigurationException, org.xml.sax.SAXException,
               java.lang.ClassNotFoundException, java.lang.InstantiationException, java.lang.IllegalAccessException
    {
        // Iterate all source rss xml streams, parse them and create sorted list of item nodes
        // according to the dc:date value inside the item node.

        HashMap<String, LinkedList<Node>> dateItemsH = new HashMap<String, LinkedList<Node>>(); // group feed items according to their date
        ArrayList<String> dateList = new ArrayList<String>(); // list of unique datetimes in the feeds
        Document[] doms = new Document[inputs.length];
        for (int i=0; i<inputs.length; i++) {
            // Parse xml as dom tree
            doms[i] = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputs[i]);
            // Find all dc:date entries in the DOM tree and group items according to their dc:date
            NodeList dcDates = doms[i].getElementsByTagName("dc:date");
            for(int j=0; j<dcDates.getLength(); j++) {
                Node dcDateNode=dcDates.item(j);
                Node itemNode = dcDateNode.getParentNode();
                String itemDate = getElementTexts(dcDateNode);
                LinkedList<Node> dateNodes = dateItemsH.get(itemDate);
                if (dateNodes == null) {
                    dateNodes = new LinkedList<Node>();
                    dateItemsH.put(itemDate, dateNodes);
                    dateList.add(itemDate);
                }
                dateNodes.add(itemNode);
            }
        }
        
        // Sort nodes according to their date/time
        String[] uniqueDates = dateList.toArray(new String[0]);
        Arrays.sort(uniqueDates); // ISO representation of dates sort nicely simply using alphabetic sort

        // Merge dom trees into a new DOM tree

        // Create empty RSS DOM tree
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = builderFactory.newDocumentBuilder();
        Document resultDoc = builder.newDocument();
        Element resultRoot = resultDoc.createElement("rss");
        resultDoc.appendChild(resultRoot);
        resultRoot.setAttribute("xmlns:rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        resultRoot.setAttribute("xmlns:dc", "http://purl.org/dc/elements/1.1/");
        resultRoot.setAttribute("version", "2.0");
        Element channelElement = resultDoc.createElement("channel");
        resultRoot.appendChild(channelElement);
        createTextChild(resultDoc, channelElement, "title", combinedTitle);
        Element imageElement = resultDoc.createElement("image");
        channelElement.appendChild(imageElement);
        createTextChild(resultDoc, imageElement, "url", combinedImageURL);
        createTextChild(resultDoc, imageElement, "title", combinedImageTitle);
        createTextChild(resultDoc, imageElement, "link", combinedImageLink);
        createTextChild(resultDoc, channelElement, "description", combinedDescription);
        createTextChild(resultDoc, channelElement, "link", combinedLink);
        createTextChild(resultDoc, channelElement, "language", combinedLanguage);
    
        // Iterate dates in inverse order because sorting organized them so that oldest is first
        // Insert items in new DOM tree in correct order
        for (int dateInd = uniqueDates.length-1; dateInd >= 0; dateInd--) {
            String dateString = uniqueDates[dateInd];
            for (Node item : dateItemsH.get(dateString)) {
                Element newItemElement = resultDoc.createElement("item");
                channelElement.appendChild(newItemElement);
                createTextChild(resultDoc, newItemElement, "title", getChildTexts(item, "title"));
                createTextChild(resultDoc, newItemElement, "link", getChildTexts(item, "link"));
                createTextChild(resultDoc, newItemElement, "guid", getChildTexts(item, "guid"));
                createTextChild(resultDoc, newItemElement, "description", getChildTexts(item, "description"));
                createTextChild(resultDoc, newItemElement, "pubDate", getChildTexts(item, "pubDate"));
                createTextChild(resultDoc, newItemElement, "dc:date", dateString);
            }
        }
        // Extract resulting XML as UTF-8 encoded binary 
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        serializeDOMTree(resultDoc, bos);
        bos.close();
        byte[] result = bos.toByteArray();
        return result;
    }

    /** Test driver */
    public static void main(String[] arg) 
    {
        if (arg.length == 0) {
            System.err.println("usage: java com.sanoma.rssmergett.RssXmlMerger file1.xml file2.xml..\n");
            System.exit(-10);
        }
        FileInputStream[] inputs = new FileInputStream[arg.length];
        try {
            for (int i=0; i<arg.length; i++) {
                inputs[i] = new FileInputStream(arg[i]);
            }
            
            RssXmlMerger merger = new RssXmlMerger();
            String result = new String(merger.merge(inputs, 
                                                    "combinedTitle",
                                                    "combinedImageURL",
                                                    "combinedImageTitle",
                                                    "combinedImageLink",
                                                    "combinedDescription",
                                                    "combinedLink",
                                                    "combinedLanguage"),
                                       "UTF-8");
            System.out.println(result);

        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            for (FileInputStream fis : inputs) {
                if (fis != null) { try { fis.close(); } catch (Throwable tt) { /*ignore */ } }
            }
        }
    }

}
