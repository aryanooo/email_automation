package org.example;
import javax.mail.*;
import javax.mail.internet.MimeMultipart;
import javax.mail.search.FlagTerm;
import java.util.Properties;
public class GmailReader {
    private final String userEmail;
    private final String appPassword;

    public GmailReader(String userEmail, String appPassword) {
        this.userEmail = userEmail;
        this.appPassword = appPassword;
    }

    // Add the ApprovalResponse class
    public static class ApprovalResponse {
        private final boolean received;
        private final boolean approved;
        private final String message;

        public ApprovalResponse(boolean received, boolean approved, String message) {
            this.received = received;
            this.approved = approved;
            this.message = message;
        }

        public boolean isReceived() { return received; }
        public boolean isApproved() { return approved; }
        public String getMessage() { return message; }
    }

    // Keep the existing method for backward compatibility
    public boolean checkForApprovalResponse(String recipientEmail) {
        ApprovalResponse response = checkForApprovalResponseDetailed(recipientEmail);
        return response.isReceived() && response.isApproved();
    }

    // Add the new detailed method
    public ApprovalResponse checkForApprovalResponseDetailed(String recipientEmail) {
        Properties props = new Properties();
        props.put("mail.store.protocol", "imaps");
        props.put("mail.imaps.host", "imap.gmail.com");
        props.put("mail.imaps.port", "993");
        props.put("mail.imaps.ssl.enable", "true");

        try {
            Session session = Session.getInstance(props);
            Store store = session.getStore("imaps");
            store.connect("imap.gmail.com", userEmail, appPassword);

            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_WRITE);

            Message[] messages = inbox.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));
            System.out.println("Found " + messages.length + " unread messages");

            for (int i = messages.length - 1; i >= 0; i--) {
                Message message = messages[i];
                if (message.getFrom()[0].toString().contains(recipientEmail)) {
                    String subject = message.getSubject();
                    if (subject != null && subject.contains("Response")) {
                        String content = getMessageContent(message);
                        message.setFlag(Flags.Flag.SEEN, true);

                        if (content.contains("Approved")) {
                            return new ApprovalResponse(true, true, "✅ Approved by " + recipientEmail);
                        } else if (content.contains("Rejected")) {
                            return new ApprovalResponse(true, false, "❌ Rejected by " + recipientEmail);
                        }
                    }
                }
            }

            inbox.close(false);
            store.close();

        } catch (Exception e) {
            System.err.println("Error checking mailbox: " + e.getMessage());
            e.printStackTrace();
            return new ApprovalResponse(false, false, "Error: " + e.getMessage());
        }

        return new ApprovalResponse(false, false, "No response yet");
    }

    private String getMessageContent(Message message) throws Exception {
        Object content = message.getContent();
        if (content instanceof String) {
            return (String) content;
        } else if (content instanceof MimeMultipart) {
            return getTextFromMimeMultipart((MimeMultipart) content);
        }
        return "";
    }

    private String getTextFromMimeMultipart(MimeMultipart mimeMultipart) throws Exception {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < mimeMultipart.getCount(); i++) {
            BodyPart bodyPart = mimeMultipart.getBodyPart(i);
            if (bodyPart.isMimeType("text/plain")) {
                result.append(bodyPart.getContent());
            }
        }
        return result.toString();
    }

}





