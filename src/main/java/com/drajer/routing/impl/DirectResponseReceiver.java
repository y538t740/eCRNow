package com.drajer.routing.impl;

import com.drajer.eca.model.PatientExecutionState;
import com.drajer.ecrapp.util.ApplicationUtils;
import com.drajer.routing.RRReceiver;
import com.drajer.sof.model.LaunchDetails;
import java.io.File;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Header;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.InternetAddress;
import javax.mail.search.FlagTerm;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.entity.mime.content.FileBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DirectResponseReceiver extends RRReceiver {

  private final Logger logger = LoggerFactory.getLogger(DirectResponseReceiver.class);

  @Override
  public Object receiveRespone(Object context) {

    logger.info(" **** START Executing Direct Receive **** ");

    if (context instanceof LaunchDetails) {

      logger.info(" Obtained Launch Details ");
      LaunchDetails details = (LaunchDetails) context;
      PatientExecutionState state = null;

      state = ApplicationUtils.getDetailStatus(details);

      readMail(details);
    }

    return null;
  }

  public void readMail(LaunchDetails details) {

    try {

      String mId = null;

      /*Properties prop = new Properties();
      String path = "./application.properties";
      FileInputStream file = new FileInputStream(path);
      prop.load(file);
      file.close();*/

      // Properties for Javamail
      Properties props = new Properties();
      Session session = Session.getInstance(props, null);

      Store store = session.getStore("imap");
      int port = 143; // Integer.parseInt(prop.getProperty("port"));
      logger.info("Connecting to IMAP Inbox");
      store.connect(details.getDirectHost(), port, details.getDirectUser(), details.getDirectPwd());

      Folder inbox = store.getFolder("Inbox");
      inbox.open(Folder.READ_WRITE);

      Flags seen = new Flags(Flags.Flag.SEEN);
      FlagTerm unseenFlagTerm = new FlagTerm(seen, false);
      Message[] messages = inbox.search(unseenFlagTerm);

      for (Message message : messages) {

        logger.info("Found unread emails");
        Enumeration headers = message.getAllHeaders();

        while (headers.hasMoreElements()) {
          Header h = (Header) headers.nextElement();
          if (h.getName().contains("Message-ID")) {
            mId = h.getValue();
          }
        }

        Address[] froms = message.getFrom();
        String senderAddress = froms == null ? null : ((InternetAddress) froms[0]).getAddress();

        if (message.getContent() instanceof Multipart) {
          Multipart multipart = (Multipart) message.getContent();
          for (int i = 0; i < multipart.getCount(); i++) {
            BodyPart bodyPart = multipart.getBodyPart(i);

            if (bodyPart.getFileName() != null) {
              String filename = bodyPart.getFileName();
              if ((filename.contains(".xml") || filename.contains(".XML"))) {

                logger.info("Found XML Attachment");

                try (InputStream stream = bodyPart.getInputStream()) {
                  byte[] targetArray = IOUtils.toByteArray(stream);
                  FileUtils.writeByteArrayToFile(new File(filename), targetArray);
                }
                File file1 = new File(filename);
                FileBody fileBody = new FileBody(file1);

                logger.info(
                    " Need to determine what to do with the response received from :  {}",
                    senderAddress);
              }
            }
          }
        }
      }

      deleteMail(details.getDirectHost(), details.getDirectUser(), details.getDirectPwd());

    } catch (Exception e) {

      logger.error("Error while reading mail", e);
    }
  }

  public void deleteMail(String host, String username, String password) throws Exception {

    Properties props = new Properties();
    Session session = Session.getInstance(props, null);

    Store store = session.getStore("imap");
    int port = 143; // Integer.parseInt(prop.getProperty("port"));
    store.connect(host, username, password);

    Folder inbox = store.getFolder("Inbox");
    inbox.open(Folder.READ_WRITE);
    Flags seen = new Flags(Flags.Flag.SEEN);

    FlagTerm seenFlagTerm = new FlagTerm(seen, true);
    Message[] messages = inbox.search(seenFlagTerm);

    for (Message message : messages) {

      message.setFlag(Flags.Flag.DELETED, true);
    }

    inbox.close(true);
  }
}
