package util;

import java.util.Properties;
import javax.naming.Context;
import javax.naming.directory.InitialDirContext;

/**
 *
 * @author nick
 */
public class LDAP {

    private final static String IGM_LDAP_SERVER = "ldap://rbwdcmc001.mc.cumc.columbia.edu:389";

    /*
        check whether user has a valid CUMC MC account
    */
    public static boolean isMCAccountValid(String username, String password) {
        try {
            Properties props = new Properties();
            props.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
            props.put(Context.PROVIDER_URL, IGM_LDAP_SERVER);
            props.put(Context.SECURITY_PRINCIPAL, "MC\\" + username);
            props.put(Context.SECURITY_CREDENTIALS, password);

            InitialDirContext context = new InitialDirContext(props);

            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
