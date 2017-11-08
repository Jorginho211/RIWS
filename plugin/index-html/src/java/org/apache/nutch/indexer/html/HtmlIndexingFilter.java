/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.nutch.indexer.html;

import java.util.Scanner;
import java.nio.ByteBuffer;
import java.io.ByteArrayInputStream;

import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.avro.util.Utf8;
import org.apache.commons.lang.StringUtils;
import org.apache.nutch.util.StringUtil;

import org.apache.commons.lang.time.DateUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.nutch.indexer.IndexingException;
import org.apache.nutch.indexer.IndexingFilter;
import org.apache.nutch.indexer.NutchDocument;
import org.apache.nutch.metadata.HttpHeaders;
import org.apache.nutch.net.protocols.HttpDateFormat;
import org.apache.nutch.storage.WebPage;
import org.apache.nutch.storage.WebPage.Field;
import org.apache.nutch.util.MimeUtil;
import org.apache.nutch.util.TableUtil;
import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.MatchResult;
import org.apache.oro.text.regex.PatternMatcher;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;
import org.apache.oro.text.regex.Perl5Pattern;
import org.apache.solr.common.util.DateUtil;
import org.mortbay.log.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Add HTML of page the document element so it can be indexed in scheme.xml
 *
 * @author Mohamed Meabed <mo.meabed@gmail.com>
 */

public class HtmlIndexingFilter implements IndexingFilter {
    public static final Logger LOG = LoggerFactory.getLogger(HtmlIndexingFilter.class);
    private Configuration conf;

    /**
     * Get the MimeTypes resolver instance.
     */
    private MimeUtil MIME;

    private static Collection<WebPage.Field> FIELDS = new HashSet<WebPage.Field>();

    static {
        FIELDS.add(WebPage.Field.CONTENT);
    }

    @Override
    public NutchDocument filter(NutchDocument doc, String url, WebPage page) throws IndexingException {
    	if(url.contains("/feria/")){
    		LOG.info("\n\nUrl: " + url + "\n\n");
        	String html = convertByteBufferToString(page.getContent());
    		filterTitle(doc, html);
    		filterFecha(doc, html);
    		filterHora(doc, html);
    		filterLocalizacion(doc, html);
    		filterLugar(doc,html);
    		filterOrganizacion(doc, html);
    		filterDescripcion(doc, html);
            return doc;
    	}
    	return null;
    }
    
    private void filterTitle(NutchDocument doc, String html){
    	Pattern pattern = Pattern.compile("<h2 class=\"node-title\">[^<]*</h2>");
    	Matcher matcher = pattern.matcher(html);
    	
    	String title = "";
    	if(matcher.find()){
    		title = matcher.group(0);
    		title = title.replace("<h2 class=\"node-title\">", "");
    		title = title.replace("</h2>", "");
    		
    		LOG.info("\n\nTitle: " + title + "\n\n");
    	}
    	doc.removeField("title");
    	doc.add("title", title);
    }
    
    private void filterFecha(NutchDocument doc, String html){
    	
    }
    
    private void filterHora(NutchDocument doc, String html){
    	
    }
    
    private void filterLugar(NutchDocument doc, String html){
    	Pattern pattern = Pattern.compile("Lugar:&nbsp;</div>(<div|<a|</a|[^<])*</div>");
    	Matcher matcher = pattern.matcher(html);
    	
    	String lugar = "";
    	if(matcher.find()){
    		lugar = matcher.group(0);
    		lugar = lugar.replace("Lugar:&nbsp;</div>", "");
    		lugar = lugar.replaceAll("<[^>]*>", "");
    		lugar = lugar.trim();
    		
    		LOG.info("\n\nLugar: " + lugar + "\n\n");
    	}
    	doc.add("lugar", lugar);
    }
    
    private void filterLocalizacion(NutchDocument doc, String html){
    	
    }
    
    private void filterOrganizacion(NutchDocument doc, String html){
    	
    }
    
    private void filterDescripcion(NutchDocument doc, String html){
    	
    }
    
    private String convertByteBufferToString(ByteBuffer contentPage){
    	String data = "";
    	if (contentPage != null) {
            ByteArrayInputStream arrayInputStream = new ByteArrayInputStream(contentPage.array(), contentPage.arrayOffset() + contentPage.position(), contentPage.remaining());
            Scanner scanner = new Scanner(arrayInputStream);
            scanner.useDelimiter("\\Z");//To read all scanner content in one String
            
            if (scanner.hasNext()) {
                data = scanner.next();
            }
            data = StringUtil.cleanField(data);
        }
    	
    	data = data.replaceAll("\\s{2,}", "");
    	data = data.replaceAll("\\n+", "");
    	return data;
    }

    public void addIndexBackendOptions(Configuration conf) {
    }

    public void setConf(Configuration conf) {
        this.conf = conf;
        MIME = new MimeUtil(conf);
    }

    public Configuration getConf() {
        return this.conf;
    }

    @Override
    public Collection<Field> getFields() {
        return FIELDS;
    }

}
