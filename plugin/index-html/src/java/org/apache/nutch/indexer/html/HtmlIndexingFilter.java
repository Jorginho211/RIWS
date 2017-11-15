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

import com.sun.crypto.provider.DESCipher;


public class HtmlIndexingFilter implements IndexingFilter {
    public static final Logger LOG = LoggerFactory.getLogger(HtmlIndexingFilter.class);
    private Configuration conf;

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
		Pattern pattern = Pattern.compile("<span class=\"date-display-start\">[^<]*</span>");
    	Matcher matcher = pattern.matcher(html);
    	
    	String data = "";
    	if(matcher.find()){
    		data = matcher.group(0);
    		data = data.replace("<span class=\"date-display-start\">", "");
    		data = data.replace("</span>", "");
    		
    		LOG.info("\n\nData: " + data + "\n\n");
    	}
    	
    	pattern = Pattern.compile("<span class=\"date-display-end\">[^<]*</span>");
    	matcher = pattern.matcher(html);
    	
    	String data2 = "";
    	if(matcher.find()){
    		data2 = matcher.group(0);
    		data2 = data2.replace("<span class=\"date-display-end\">", "");
    		data2 = data2.replace("</span>", "");
    		
    		LOG.info("\n\nData2: " + data2 + "\n\n");
    	}
    	
    	doc.removeField("fecha");
    	doc.add("fecha", data + " - " + data2);
    	
    }
    
    private void filterHora(NutchDocument doc, String html){
    	Pattern pattern = Pattern.compile("Hora:&nbsp;</div>(<a|</a|[^<])*</div>");
    	Matcher matcher = pattern.matcher(html);
    	
    	String hora = "";
    	if(matcher.find()){
    		hora = matcher.group(0);
    		hora = hora.replace("Hora:&nbsp;", "");
    		hora = hora.replaceAll("<[^>]*>", "");
    		hora = hora.trim();
    		
    		LOG.info("\n\nHora: " + hora + "\n\n");
    	}
    	doc.add("hora", hora);
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
    	Pattern pattern = Pattern.compile("Localidade:&nbsp;</div>.*");
    	Matcher matcher = pattern.matcher(html);
    	
    	String loc = "";
    	if(matcher.find()){
    		loc = matcher.group(0);
    		loc = loc.replace("Localidade:&nbsp;</div>", "");
    		loc = loc.replaceAll("</ul>.*", "");
    		loc = loc.replaceAll("<[^>]*>", "");
    		loc = loc.replaceAll("›", " ");
    		
    		LOG.info("\n\nLocalizacion: " + loc + "\n\n");
    	}
    	doc.removeField("localizacion");
    	doc.add("localizacion", loc);
    }
    
    private void filterOrganizacion(NutchDocument doc, String html){
		
		Pattern pattern = Pattern.compile("Organiza:&nbsp;</div>.*");
    	Matcher matcher = pattern.matcher(html);
    	
    	String org = "";
    	if(matcher.find()){
    		org = matcher.group(0);
    		org = org.replace("Organiza:&nbsp;</div>", "");
    		org = org.replaceAll("</div.*", "");
    		org = org.replaceAll("<[^>]*>", "");
    		
    		LOG.info("\n\nOrganizacion: " + org + "\n\n");
    	}
    	doc.removeField("organizacion");
    	doc.add("organizacion", org);
           
    }
    
    private void filterDescripcion(NutchDocument doc, String html){
    	Pattern pattern = Pattern.compile("Descripcion</legend>.*</fieldset>");
    	Matcher matcher = pattern.matcher(html);
    	
    	String descripcion = "";
    	if(matcher.find()){
    		descripcion = matcher.group(0);
    		descripcion = descripcion.replaceAll("</fieldset>.*", "");
    		descripcion = descripcion.replace("Descripcion</legend>", "");
    		descripcion = descripcion.replaceAll("</p>", " ");
    		descripcion = descripcion.replaceAll("<[^>]*>", "");
    		descripcion = descripcion.trim();
    		
    		LOG.info("\n\nDescripcion: " + descripcion + "\n\n");
    	}
    	doc.add("descripcion", descripcion);
    }
    
    private String convertByteBufferToString(ByteBuffer contentPage){
    	String data = "";
    	if (contentPage != null) {
            ByteArrayInputStream arrayInputStream = new ByteArrayInputStream(contentPage.array(), contentPage.arrayOffset() + contentPage.position(), contentPage.remaining());
            Scanner scanner = new Scanner(arrayInputStream);
            scanner.useDelimiter("\\Z");
            
            if (scanner.hasNext()) {
                data = scanner.next();
            }
            data = StringUtil.cleanField(data);
        }
    	
    	data = data.replaceAll("\\s{2,}", " ");
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
