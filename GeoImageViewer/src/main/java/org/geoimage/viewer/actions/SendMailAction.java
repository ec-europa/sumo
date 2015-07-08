/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.geoimage.viewer.actions;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
//import org.geoimage.utils.IProgress;
import org.geoimage.viewer.core.api.Argument;
import org.geoimage.viewer.core.api.iactions.AbstractAction;
import org.slf4j.LoggerFactory;

/**
 *
 * @author leforth
 */
public class SendMailAction extends AbstractAction  {
	private static org.slf4j.Logger logger=LoggerFactory.getLogger(SendMailAction.class);

    boolean done = false;
    private final String message = "Sending email. Please wait...";
    private static Properties fMailServerConfig = new Properties();
    
    /**
    * Send a single email.
    */
    public void sendEmail(String username, String password, String aFromEmailAddr, String aToEmailAddr, String aSubject, String aBody, String attachmentfilename) {
        //Here, no Authenticator argument is used (it is null).
        //Authenticators are used to prompt the user for user
        //name and password.
        Session session = Session.getDefaultInstance( fMailServerConfig, null );
        MimeMessage message = new MimeMessage( session );
        try {
            //the "from" address may be set in code, or set in the
            //config file under "mail.from" ; here, the latter style is used
            //message.setFrom( new InternetAddress(aFromEmailAddr) );
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(aToEmailAddr));
            message.setSubject( aSubject );
            try {
                if (attachmentfilename != null) {
                    // Attach the specified file.
                    // We need a multipart message to hold the attachment.
                    final MimeBodyPart mbp1 = new MimeBodyPart();
                    mbp1.setText(aBody);
                    MimeBodyPart mbp2 = new MimeBodyPart();
                    mbp2.attachFile(attachmentfilename);
                    final MimeMultipart mp = new MimeMultipart();
                    mp.addBodyPart(mbp1);
                    mp.addBodyPart(mbp2);
                    message.setContent(mp);
                }
            } catch (IOException ex) {
                logger.error(ex.getMessage(),ex);
            }
            String protocol = "smtp";
            Transport t = session.getTransport(protocol);
            t.connect(username, password);
            t.sendMessage(message, message.getAllRecipients());
            t.close();
        }
        catch (MessagingException ex){
            System.err.println("Cannot send email. " + ex);
        }
    }

    public String getName() {
        return "sendmail";
    }

    public String getDescription() {
        return " Send an email with an attachment option\n" +
                " Use sendmail emailaccount emailaddress subject emailtext attachmentfilename";
    }

    public String getPath() {
        return "Tools/SendMail";
    }

    public boolean execute(String[] args) {
        if (args.length == 0) {
            return true;
        }
        done = false;
        try {
            fMailServerConfig.setProperty("mail.host", args[0]);
            fMailServerConfig.setProperty("mail.from", "sumoemail@jrc.it");
            // check if multiple destinary for email
            String[] emailaddresses = args[3].split(";");
            for(String emailaddress : emailaddresses)
            {
                if(args.length == 6)
                    sendEmail(args[1], args[2], "sumoemail@jrc.it", emailaddress, "SUMO email - " + args[4], args[5], null);
                if(args.length == 7)
                    sendEmail(args[1], args[2], "sumoemail@jrc.it", emailaddress, "SUMO email - " + args[4], args[5], args[6]);
            }
        } catch (Exception e) {
            errorWindow("Problem opening file");
        }
        done = true;
        return true;
    }

    public List<Argument> getArgumentTypes() {

        Vector<Argument> out = new Vector<Argument>();
        
        Argument a1 = new Argument("email account", Argument.STRING, false, "ipsc-mail.jrc.it");
        out.add(a1);

        Argument a2 = new Argument("email account user name", Argument.STRING, false, "leforth");
        out.add(a2);

        Argument a3 = new Argument("email account user password", Argument.STRING, false, "");
        out.add(a3);
        
        Argument a4 = new Argument("destinary email account", Argument.STRING, false, "");
        out.add(a4);

        Argument a5 = new Argument("subject", Argument.STRING, false, "");
        out.add(a5);
        
        Argument a6 = new Argument("email text", Argument.STRING, false, "");
        out.add(a6);
        
        Argument a7 = new Argument("attachment file", Argument.FILE, false, "");
        out.add(a7);
        
        return out;
    }

    public boolean isIndeterminate() {
        return true;
    }

    public boolean isDone() {
        return done;
    }

    public int getMaximum() {
        return 1;
    }

    public int getCurrent() {
        return 1;
    }

    public String getMessage() {
        return this.message;
    }

    public void setCurrent(int i) {
    }

    public void setMaximum(int size) {
    }

    public void setMessage(String string) {
    }

    public void setIndeterminate(boolean value) {
    }

    public void setDone(boolean value) {
        done = value;
    }

}
