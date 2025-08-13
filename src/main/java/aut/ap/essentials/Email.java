package aut.ap.essentials;

import jakarta.persistence.*;
import java.time.LocalDateTime;


@Entity
@Table(name = "emails")
public class Email {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(unique = true)
    private String code;

    @Column(name = "sender_email")
    private String senderEmail;

    private String recipients;

    private String subject;

    @Column(columnDefinition = "TEXT")
    private String body;

    @Column(name = "dateEmailSend")
    private LocalDateTime dateEmailSend;

    @Column(name = "is_read")
    private boolean isRead;

    public Email() {
    }

    public Email(String code, String senderEmail, String recipients, String subject, String body, LocalDateTime dateEmailSend) {
        this.code = code;
        this.senderEmail = senderEmail;
        this.recipients = recipients;
        this.subject = subject;
        this.body = body;
        this.dateEmailSend = dateEmailSend;
        this.isRead = false;
    }


    public int getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getSenderEmail() {
        return senderEmail;
    }

    public void setSenderEmail(String senderEmail) {
        this.senderEmail = senderEmail;
    }

    public String getRecipients() {
        return recipients;
    }

    public void setRecipients(String recipients) {
        this.recipients = recipients;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public LocalDateTime getDateEmailSend() {
        return dateEmailSend;
    }
    public void setDateEmailSend(LocalDateTime dateEmailSend) {
        this.dateEmailSend = dateEmailSend;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }
}
