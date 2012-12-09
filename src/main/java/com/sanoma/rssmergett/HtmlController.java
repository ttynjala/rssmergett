package com.sanoma.rssmergett;

import java.io.*;
import java.util.*;
import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.apache.log4j.*;

@Controller
@RequestMapping(value="/html")
public class HtmlController implements MessageSourceAware {

    /**
     * Spring Controller class  for serving requests for HTML version of the combined
     * RSS feed. This class queries the combined RSS data from RssXmlMerger and
     * uses XSLT for converting it into HTML source. 
     *
     * @author Tuomas Tynjälä
     */

    /* Logger for the class */
    protected static final Logger logger = Logger.getLogger(HtmlController.class);

    /** MessageSource for accessing localized strings */
    private MessageSource messageSource;

    /** Setter for injecting the MessageSource bean.
     * @param bundle Injected MessageSource bean.
     */
    public void setMessageSource(MessageSource bundle) {
        messageSource = bundle;
    }

    /** Helper class for getting simple localized string from properties.
     * However, currently this does NOT support several locales as the default locale
     * is used.
     * @param key Text resource key in the bundle
     * @return Localized text
     */
    private String getText(String key) 
    {
        return messageSource.getMessage(key, new Object[0], Locale.getDefault());
    }

    /**
     * Action handler for generating the combined RSS feed request in HTML format.
     * This method loads the source RSS feeds, combines them and renders the result
     * as HTML.
     * @param model Data model for the rendering jsp
     * @return Spring view for the JSP rendering of result
     */
    @RequestMapping(method=RequestMethod.GET)
    public String htmlAction(Model model) throws Exception {
        
        logger.info("Incoming HTML query");
        String html = "";
        try {

            /** Get the combined RSS feed as XML */
            RssXmlMerger rssMerget = new RssXmlMerger();
            byte[] xmlInUTF8 = 
                RssFetchAndCombine.fetchAndCombineRSS(getText("feed.title"),
                                                      getText("feed.imageURL"),
                                                      getText("feed.imageTitle"),
                                                      getText("feed.imageLink"),
                                                      getText("feed.description"),
                                                      getText("feed.link"),
                                                      getText("feed.language"));

            /* XSLT template to use in converting XML to HTML */
            String xsltCode = 
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"+
                "<xsl:stylesheet xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" version=\"1.0\">\n" +
                "<xsl:output method=\"html\" encoding=\"UTF-8\"/>\n" + 
                "<xsl:template match=\"/\">\n" + 
                "<html>\n" + 
                "  <head>\n" + 
                "     <title>\n"+
                "      <xsl:value-of select=\"/rss/channel/title\"/>\n"+
                "     </title>\n" + 
                "  </head>\n" + 
                "  <body>\n" + 
                "    <xsl:for-each select=\"/rss/channel/item\">\n" +
                "      <hr/>\n" +
                "      <a>\n"+
                "        <xsl:attribute name=\"href\">\n"+
                "          <xsl:value-of select=\"link\"/>\n"+
                "        </xsl:attribute>\n"+
                "        <xsl:value-of select=\"title\"/>\n"+
                "      </a>\n"+
                "      <br/>\n" +
                "      <xsl:value-of select=\"pubDate\"/>\n"+
                "      <br/>\n" +
                "      <xsl:value-of select=\"description\"/>\n"+
                "    </xsl:for-each>\n" +
                "  </body>\n" + 
                "</html>\n" + 
                "</xsl:template>\n"+
                "</xsl:stylesheet>\n";
            
            /** Convert XML to HTML using XSLT*/
            TransformerFactory factory = TransformerFactory.newInstance();
            Source xslt = new StreamSource(new ByteArrayInputStream(xsltCode.getBytes("UTF-8")));
            Transformer transformer = factory.newTransformer(xslt);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            Source text = new StreamSource(new ByteArrayInputStream(xmlInUTF8));
            transformer.transform(text, new StreamResult(bos));
            html = new String(bos.toByteArray(), "UTF-8");


        } catch (Throwable t) {
            logger.error("Incoming HTML query failed with error", t);
            throw new Exception("INTERNAL ERROR");
        }
        
        /* Pass generated HTML to JSP for rendering */
        model.addAttribute("html", html);
        logger.info("Incoming HTML query done");
        return "rssmergett/html";
    }
}
