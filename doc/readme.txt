
RSSMERGETT

1. Overview

  This is a simple web problem that combines RSS 2.0 feeds into
  one RSS 2.0 feed. The tool implements two rendering formats,
  the actual RSS 2.0 feed and a simple HTML rendering of the same.
  For performance reasons, the code caches the combined RSS
  output in the server side.

  The development was done in Fedora 17.

  The code is built around Spring MVC framework, using the
  spring framework sample 'mvc-basic' at 
  https://src.springframework.org/svn/spring-samples/
  as a starting point.

  In this example, the feeds being merged are:
  - http://rss.kauppalehti.fi/rss/yritysuutiset.jsp
  - http://rss.kauppalehti.fi/rss/omaraha.jsp
  - http://rss.kauppalehti.fi/rss/etusivun_mobiili.jsp
  - http://rss.kauppalehti.fi/rss/auto.jsp

2. Compiling and Running

  To try the code you need:
  - jdk 7 (java 6 might work but hasn't been tested)
  - maven (version 3.0.4 was used)
  - maven jetty plugin
  - jetty (version 8.1.2 was used)

  To compile the code, cd to the directory where pom.xml resides and say

    mvn jetty:run 

  Then point the browser to url

    http://localhost:8080/

  If everything went fine, you should see two links: One for the 
  RSS version of the feed and another for HTML rendering of the same.

3. Issues

  As always, there is room for improvement:
  - The caching is very simple. Also caching the source feeds would
    allow making the system more robust for the failure of the source
    RSS feeds. Now, if even one of the source feeds fail the combined
    feed will also fail. 
  - The feeds to be combined are hardcoded. This could be made configurable.
  - The XSLT used for generating is in the java code. It could be externalised
    as a resource.
  - Localization currently supports only one language. While the basic
    localizatoin mechanism is in place and is used, the controller code
    does not currently look for the end users locale. Instead it always
    uses the default locale.
  - The generated HTML rendering is very austere.

4. Copyright

This code is public domain. You are free to use as you like.

