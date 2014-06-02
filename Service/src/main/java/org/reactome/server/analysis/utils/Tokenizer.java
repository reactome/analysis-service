package org.reactome.server.analysis.utils;

import org.apache.log4j.Logger;
import org.apache.xml.security.exceptions.Base64DecodingException;
import org.apache.xml.security.utils.Base64;
import org.reactome.server.analysis.exception.ResourceNotFoundException;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
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

    public static String getOrCreateToken(String md5, boolean toHuman){
        String token = md5ToToken.get(md5 + toHuman);
        if(token==null){
            token = getToken();
            md5ToToken.put(md5 + toHuman, token);
        }
        return token;
    }

    public static boolean hasToken(String md5, boolean toHuman){
        return md5ToToken.containsKey(md5 + toHuman);
    }

    public static String getName(String token){
        //noinspection TryWithIdenticalCatches
        try {
            token = URLDecoder.decode(token, "UTF-8");
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

    private static synchronized String getToken(){
        SimpleDateFormat sdf = new SimpleDateFormat("MMddHHmmss");
        String current = sdf.format(System.currentTimeMillis());
        String token = current + "_" + lastToken++;
        token = Base64.encode(token.getBytes());
        try {
            return URLEncoder.encode(token, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }
}