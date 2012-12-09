package com.sanoma.rssmergett;

import java.util.*;
import java.io.*;
import java.net.*;
import org.apache.log4j.*;

public class RssFetchAndCombine 
{
    /**
     * Class for fetching and combining serveral RSS feeds into one.
     * 
     * @author Tuomas Tynjälä
     */

    /* Logger for the class */
    protected static final Logger logger = Logger.getLogger(RssFetchAndCombine.class);

    /** Time (in milliseconds) when cached combined RSS result was last updated */
    private static long gLastFetchTime = 0L;

    /** Time interval (in milliseconds) after which we regenerate the combined RSS result */
    private static long gFetchIntervalInMillis = 60000L;

    /** Cached combined RSS result, as UTF-8 encoded XML */
    private static volatile byte[] gLastFetchedData = null;

    /**
     * Method for loading, caching and combining several RSS feeds.
     * @param title Title text for the resulting combined RSS feed.
     * @param imageURL Image url for the resulting combined RSS feed.
     * @param imageTitle Imagetitle for the resulting combined RSS feed.
     * @param imageLink Image link for the resulting combined RSS feed.
     * @param description Description for the resulting combined RSS feed.
     * @param link Link for the resulting combined RSS feed.
     * @param language Language code for the resulting combined RSS feed. See
     *        <a href="http://www.rssboard.org/rss-language-codes">http://www.rssboard.org/rss-language-codes</a>
     *        for supported codes.
     * @return The combined RSS XML content as UTF-8 encoded binary.
     */
    public static byte[] fetchAndCombineRSS(String title,
                                            String imageURL,
                                            String imageTitle,
                                            String imageLink,
                                            String description,
                                            String link,
                                            String language) throws Exception
    {

        long currentTime = System.currentTimeMillis();
        synchronized (RssFetchAndCombine.class) {
            /* Check if we can use the casched combined result */
            if ((currentTime - gLastFetchTime) > gFetchIntervalInMillis) {
                /** Cashed copy is old or does not exists, need to (re)generate */
                gLastFetchTime = currentTime;

                /* List of source RSS feeds that we'll combine */
                String[] sourceRssURLStrings = new String[] {
                    "http://rss.kauppalehti.fi/rss/yritysuutiset.jsp",
                    "http://rss.kauppalehti.fi/rss/omaraha.jsp",
                    "http://rss.kauppalehti.fi/rss/etusivun_mobiili.jsp",
                    "http://rss.kauppalehti.fi/rss/auto.jsp"
                };
                int nrFeeds = sourceRssURLStrings.length;
                
                logger.info("Start fetching streams..");
                
                URL[] sourceRssURLs = new URL[nrFeeds];
                InputStream[] sourceRssInputs = new InputStream[nrFeeds];
                String xml = null;
                try { 
                    /* Open source RSS streams for reading */
                    for (int i=0; i<nrFeeds; i++) {
                        sourceRssURLs[i] = new URL(sourceRssURLStrings[i]);
                        sourceRssInputs[i] = sourceRssURLs[i].openStream();
                    }

                    /* Use helper for loading and combining the RSS feeds */
                    RssXmlMerger merger = new RssXmlMerger();
                    xml = new String(merger.merge(sourceRssInputs, 
                                                  title,
                                                  imageURL,
                                                  imageTitle,
                                                  imageLink,
                                                  description,
                                                  link,
                                                  language),
                                     "UTF-8");
                } finally {
                    /* Close the source RSS feed streams */
                    if (sourceRssInputs != null) {
                        for (InputStream is : sourceRssInputs) {
                            if (is != null) {
                                try { is.close(); } catch (Throwable tt) { /* ignore */ }
                            }
                        }
                    }
                }
                
                /* Get the generated combined RSS XML as UTF-8 bytes */
                byte[] bytes = null;
                try { 
                    bytes = xml.getBytes("UTF-8"); 
                } catch (Throwable tt) { 
                    tt.printStackTrace(); // utf-8 is always supported so this should not happen 
                }
                logger.info("Fetching streams done");
                gLastFetchedData = bytes;
            }
        }
        return gLastFetchedData;
    }

}
