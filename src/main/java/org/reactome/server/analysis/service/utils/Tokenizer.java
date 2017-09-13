package org.reactome.server.analysis.service.utils;

import org.apache.log4j.Logger;
import org.apache.xml.security.exceptions.Base64DecodingException;
import org.apache.xml.security.utils.Base64;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.reactome.server.analysis.service.exception.ResourceNotFoundException;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
public abstract class Tokenizer {

    private static Logger logger = Logger.getLogger(Tokenizer.class.getName());

    private static Map<String, String> md5ToToken = new HashMap<String, String>();

    private static Long lastToken = 1L;

    private static final int DAYS_TO_LIVE = 7;

    private static final String DATE_PATTERN = "yyyyMMddHHmmss";

    public static String getOrCreateToken(String md5, boolean toHuman, boolean includeInteractors){
        String token = md5ToToken.get(md5 + toHuman + includeInteractors);
        if(token==null){
            token = getToken();
            md5ToToken.put(md5 + toHuman + includeInteractors, token);
        }
        return token;
    }

    public static boolean hasToken(String md5, boolean toHuman, boolean includeInteractors){
        return md5ToToken.containsKey(md5 + toHuman + includeInteractors);
    }

    public static String getName(String token){
        //noinspection TryWithIdenticalCatches
        try {
            String aux = URLDecoder.decode(token, "UTF-8");
            while(!aux.equals(token)) {
                token = aux;
                aux = URLDecoder.decode(token, "UTF-8");
            }
            return new String(Base64.decode(token));
        } catch (Base64DecodingException e) {
            //Nothing here
        } catch (UnsupportedEncodingException e) {
            //Nothing here
        } catch (NoClassDefFoundError e){
            //Nothing here
        }
        throw new ResourceNotFoundException();
    }

    public static boolean removeAssociatedToken(String fileName){
        Pattern pattern = Pattern.compile("res_(.*?).bin");
        Matcher matcher = pattern.matcher(fileName);
        String token = null;
        if (matcher.find()){
            token = matcher.group(1);
            logger.trace(String.format("'%s' candidate to be deleted from the map (if exists)", token));
        }

        if(token!=null){
            for (String md5 : md5ToToken.keySet()) {
                if(md5ToToken.get(md5).equals(token)){
                    md5ToToken.remove(md5);
                    logger.info(String.format("'%s' has been deleted from the map", token));
                    return true;
                }
            }
            logger.trace(String.format("'%s' not found in the map... (it could be because the server was restarted)", token));
        }else{
            logger.warn("%s has never been a result file. Check the configuration.");
        }
        return false;
    }

    public static boolean shouldBeAlive(String token){
        boolean rtn = false;
        DateTime now = new DateTime(System.currentTimeMillis());
        DateTime tokenDate = getTokenDate(token);
        if(tokenDate!=null){
            int days = Days.daysBetween(tokenDate, now).getDays();
            rtn = ( days <= DAYS_TO_LIVE );
        }
        return rtn;
    }

    private static synchronized String getToken(){
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);
        String current = sdf.format(System.currentTimeMillis());
        String token = current + "_" + lastToken++;
        token = Base64.encode(token.getBytes());
        try {
            return URLEncoder.encode(token, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    private static DateTime getTokenDate(String token){
        DateTime date = null;
        String name = Tokenizer.getName(token);
        if(name.contains("_")){
            String d =  name.split("_")[0];
            DateFormat df = new SimpleDateFormat(DATE_PATTERN);
            try {
                date = new DateTime(df.parse(d));
            } catch (ParseException e) {
                //Nothing here (date is null already)
            }
        }
        return date;
    }
}