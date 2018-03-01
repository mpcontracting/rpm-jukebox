package uk.co.mpcontracting.rpmdata.ng.support;

import java.security.MessageDigest;

import org.apache.commons.codec.binary.Hex;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class HashGenerator {
    private HashGenerator() {}
    
    public static final String generateHash(Object... objects) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("Generating hash for - " + objects);
        }
        
        if (objects == null || objects.length == 0) {
            throw new IllegalArgumentException("Objects for hash generation must have at least one value");
        }
        
        try {
            StringBuilder builder = new StringBuilder();
            
            for (Object object : objects) {
                if (object == null) {
                    continue;
                }
                
                builder.append(object.toString());
            }
            
            if (builder.length() < 1) {
                throw new Exception("Hash generation string must have a length > 1");
            }
            
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.reset();
            digest.update(builder.toString().getBytes("UTF-8"));
            
            return Hex.encodeHexString(digest.digest());
        } catch (Exception e) {
            throw new Exception("Error generating hash for - " + objects, e);
        }
    }
}

