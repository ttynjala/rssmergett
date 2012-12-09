package com.sanoma.rssmergett;

import java.util.*;
import java.io.*;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;

import org.apache.log4j.*;

@Controller
@RequestMapping(value="/rss")
public class RssController implements MessageSourceAware {

    /**
     * Spring Controller class for serving request for XML/RSS version of the
     * RSS feed. This class queries the combined RSS data from RssXmlMerger and
     * renders the result as it is (RSS/XML).
     *
     * @author Tuomas Tynjälä
     */

    /* Logger for the class */
    protected static final Logger logger = Logger.getLogger(RssController.class);

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
     * Action handler for generating the combined RSS feed request in RSS format.
     * This method loads the source RSS feeds and renders it as it is.
     * @param model Data model for the rendering jsp
     * @return Spring view for the JSP rendering of result
     */
    @RequestMapping(method=RequestMethod.GET)
    public String rssAction(Model model) throws Exception
    {      
        logger.info("Incoming RSS query");

        try {
            /** Get the combined RSS feed as XML */
            RssXmlMerger rssMerget = new RssXmlMerger();
            byte[] xmlBytes = 
                RssFetchAndCombine.fetchAndCombineRSS(getText("feed.title"),
                                                      getText("feed.imageURL"),
                                                      getText("feed.imageTitle"),
                                                      getText("feed.imageLink"),
                                                      getText("feed.description"),
                                                      getText("feed.link"),
                                                      getText("feed.language"));
            
            /* Pass generated XML to JSP for rendering */
            String xml = new String(xmlBytes, "UTF-8");
            model.addAttribute("xml", xml);
            
        } catch (Throwable t) {
            logger.error("Incoming XML query failed with error", t);
            throw new Exception("INTERNAL ERROR");
        }

        logger.info("Incoming RSS query done");
        return "rssmergett/xml";
    }

}
