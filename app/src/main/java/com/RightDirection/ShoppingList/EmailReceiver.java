package com.RightDirection.ShoppingList;

import android.content.Context;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;

import javax.activation.CommandMap;
import javax.activation.MailcapCommandMap;
import javax.mail.Address;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.search.FlagTerm;

/**
 * Получение электронных писем
 *
 * @author www.codejava.net
 *
 */
public class EmailReceiver{

    Properties mProperties;
    private String mLogin;
    private String mPassword;
    private String mProtocol;
    private Context mContext;
    private Store mStore = null;
    private Folder mFolderInbox = null;

    public EmailReceiver(Context context){
        mContext = context;

        fixJavaMailBugs();
    }

    /** Обход ошибки "Can not cast IMAPInputStream to Multipart"
     * Источник http://stackoverflow.com/questions/10302564/can-not-cast-imapinputstream-to-multipart
     */
    private void fixJavaMailBugs(){
        MailcapCommandMap mc = (MailcapCommandMap) CommandMap.getDefaultCommandMap();
        mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html");
        mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml");
        mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain");
        mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed");
        mc.addMailcap("message/rfc822;; x-java-content-handler=com.sun.mail.handlers.message_rfc822");
        CommandMap.setDefaultCommandMap(mc);
    }

    /**
     * Возрващает объект Properties для POP3/IMAP почтового сервера.
     *
     * @param host
     * @param port
     */
    public void setServerProperties(String host, String port) throws WrongEmailProtocolException {
        // Пока поддерживается только протокол imap. Pop3 требует хранения id всех писем на
        // клиенте для получения новых писем, поэтому пока не используется.
        mProtocol = "imap";

        mProperties = new Properties();

        boolean isImap = host.contains(mProtocol);
        if (!isImap) throw new WrongEmailProtocolException();

        // server setting
        mProperties.put(String.format("mail.%s.host", mProtocol), host);
        mProperties.put(String.format("mail.%s.port", mProtocol), port);

        // SSL setting
        mProperties.setProperty(
                String.format("mail.%s.socketFactory.class", mProtocol),
                "javax.net.ssl.SSLSocketFactory");
        mProperties.setProperty(
                String.format("mail.%s.socketFactory.fallback", mProtocol),
                "false");
        mProperties.setProperty(
                String.format("mail.%s.socketFactory.port", mProtocol),
                String.valueOf(port));
    }


    public ArrayList<String> getShoppingListsJSONFilesFromUnreadEmails() throws IOException {

        ArrayList<String> fileNames = new ArrayList<>();

        Message[] messages = downloadMessages(new FlagTerm(new Flags(Flags.Flag.SEEN), false));
        for (int i = 0; i < messages.length; i++) {
            Message msg = messages[i];
            try {

                String contentType = getEmailContentType(msg);
                if (contentType.contains("multipart")) {
                    // Это сообщение может содеражать вложения
                    Multipart multiPart = (Multipart) getEmailContent(msg);
                    for (int j = 0; j < multiPart.getCount(); j++) {
                        MimeBodyPart part = (MimeBodyPart) multiPart.getBodyPart(j);
                        // Проверим - вложение ли это
                        if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
                            String attachmentFileName = part.getFileName();
                            // Сначала проверим, стоит ли обрабатывать файл вложения
                            if (!(attachmentFileName.contains(mContext.getString(R.string.json_file_identifier))
                                    && attachmentFileName.contains("json"))){
                                continue;
                            }
                            String fileName = mContext.getCacheDir() + File.separator + attachmentFileName;
                            // Сохраним вложение в файл
                            part.saveFile(fileName);
                            // Добавим имя файла в массив для его (файла) дальнейшей обработки
                            fileNames.add(fileName);
                        }
                    }
                }

            } catch (MessagingException e) {
                e.printStackTrace();
            }
        }

        closeConnection();
        return fileNames;
    }

    private Message[] downloadMessages(FlagTerm flagTerm){
        Session session = Session.getDefaultInstance(mProperties);

        try {
            // Установка соед инения с хранилищем сообщений
            mStore = session.getStore(mProtocol);
            mStore.connect(mLogin, mPassword);

            // Открываем папку "Входящие"
            mFolderInbox = mStore.getFolder("INBOX");
            mFolderInbox.open(Folder.READ_ONLY);

            // Получим только непрочитанные сообщения. Для протокола POP3 не работает
            // (см. http://stackoverflow.com/questions/5925944/how-to-retrieve-gmail-sub-folders-labels-using-pop3)
            return mFolderInbox.search(flagTerm);
        } catch (NoSuchProviderException ex) {
            System.out.println("No provider for protocol: " + mProtocol);
            ex.printStackTrace();
        } catch (MessagingException ex) {
            System.out.println("Could not connect to the message store");
            ex.printStackTrace();
        }

        return new Message[]{};
    }

    private void closeConnection(){
        try {
            if (mFolderInbox != null && mFolderInbox.exists() && mFolderInbox.isOpen()){
                mFolderInbox.close(false);
            }
            if (mStore != null && mStore.isConnected()){
                mStore.close();
            }
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    /**
     * Скачивание новых сообщений и получение деталей для каждого из них.
     */
    public void downloadUnreadEmailsAndPrintDetails() throws IOException {

        Message[] messages = downloadMessages(new FlagTerm(new Flags(Flags.Flag.SEEN), false));
        for (int i = 0; i < messages.length; i++) {
            try {
                Message msg = messages[i];
                Address[] fromAddress = msg.getFrom();

                String from = "unknown";
                if (fromAddress.length > 0) from = fromAddress[0].toString();
                String subject = msg.getSubject();
                String toList = parseAddresses(msg
                        .getRecipients(RecipientType.TO));
                String ccList = parseAddresses(msg
                        .getRecipients(RecipientType.CC));
                String stringSentDate = "unknown";
                Date sentDate = msg.getSentDate();
                if (sentDate != null) {
                    stringSentDate = sentDate.toString();
                }

                String contentType = getEmailContentType(msg);
                String messageContent = "";

                if (contentType.contains("text/plain")
                        || contentType.contains("text/html")) {
                    try {
                        Object content = getEmailContent(msg);
                        if (content != null) {
                            messageContent = content.toString();
                        }
                    } catch (Exception ex) {
                        messageContent = "[Error downloading content]";
                        ex.printStackTrace();
                    }
                }

                // print out details of each message
                System.out.println("Message #" + (i + 1) + ":");
                System.out.println("\t From: " + from);
                System.out.println("\t To: " + toList);
                System.out.println("\t CC: " + ccList);
                System.out.println("\t Subject: " + subject);
                System.out.println("\t Sent Date: " + stringSentDate);
                System.out.println("\t Message: " + messageContent);
            } catch (MessagingException e) {
                e.printStackTrace();
            }
        }
    }

    /** Для обхода бага с ошибкой javax.mail.MessagingException: Unable to load BODYSTRUCTURE
     * при использовании протокола IMAP
     */
    private Object getEmailContent(Message email) throws IOException, MessagingException {
        Object content;
        try {
            content = email.getContent();
        } catch (MessagingException e) {
            // did this due to a bug
            // check: http://goo.gl/yTScnE and http://goo.gl/P4iPy7
            if (email instanceof MimeMessage && "Unable to load BODYSTRUCTURE".equalsIgnoreCase(e.getMessage())) {
                content = new MimeMessage((MimeMessage) email).getContent();
            } else {
                throw e;
            }
        }
        return content;
    }

    /** Для обхода бага с ошибкой javax.mail.MessagingException: Unable to load BODYSTRUCTURE
     * при использовании протокола IMAP
     */
    private String getEmailContentType(Message email) throws IOException, MessagingException {
        String content;
        try {
            content = email.getContentType();
        } catch (MessagingException e) {
            // did this due to a bug
            // check: http://goo.gl/yTScnE and http://goo.gl/P4iPy7
            if (email instanceof MimeMessage && "Unable to load BODYSTRUCTURE".equalsIgnoreCase(e.getMessage())) {
                content = new MimeMessage((MimeMessage) email).getContentType();
            } else {
                throw e;
            }
        }
        return content;
    }

    /**
     * Returns a list of addresses in String format separated by comma
     *
     * @param address an array of Address objects
     * @return a string represents a list of addresses
     */
    private String parseAddresses(Address[] address) {

        String listAddress = "";

        if (address != null) {
            for (int i = 0; i < address.length; i++) {
                listAddress += address[i].toString() + ", ";
            }
        }
        if (listAddress.length() > 1) {
            listAddress = listAddress.substring(0, listAddress.length() - 2);
        }

        return listAddress;
    }

    public void setLogin(String login) {
        mLogin = login;
    }

    public void setPassword(String password) {
        mPassword = password;
    }

}