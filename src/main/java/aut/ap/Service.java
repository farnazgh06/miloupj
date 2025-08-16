package aut.ap;

import aut.ap.essentials.Email;
import aut.ap.essentials.User;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import java.time.LocalDateTime;
import java.util.*;

public class Service {
    private final SessionFactory sessionFactory;

    public Service() {
        this.sessionFactory = new Configuration().configure().buildSessionFactory();
    }

    public void closeSessionFactory() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }

    private String checkEmail(String email) {
        if (email.contains("@milou.com")) {
            return email;
        }
        return email + "@milou.com";
    }

    private String generateCode() {
        String chars = "abcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        String code = "";
        for (int i = 0; i < 6; i++) {
            code += chars.charAt(random.nextInt(chars.length()));
        }
        return code;
    }

    public User signUp(String name, String email, String password) {
        return sessionFactory.fromTransaction(session -> {
            List<User> existingUsers = session.createNativeQuery(
                            "SELECT * FROM app_user WHERE email = :email", User.class)
                    .setParameter("email", checkEmail(email))
                    .getResultList();

            if (!existingUsers.isEmpty()) {
                System.out.println("Email account already exists.");
                return null;
            }
            if (password.length() < 8) {
                System.out.println("Password must be at least 8 characters.");
                return null;
            }

            User newUser = new User(name, checkEmail(email), password);
            session.persist(newUser);
            return newUser;
        });
    }

    public User login(String email, String password) {
        return sessionFactory.fromTransaction(session -> {
            List<User> users = session.createNativeQuery(
                            "SELECT * FROM app_user WHERE email = :email AND password = :password", User.class)
                    .setParameter("email", checkEmail(email))
                    .setParameter("password", password)
                    .getResultList();

            if (users.size() == 1) {
                return users.get(0);
            }
            return null;
        });
    }

    public Email sendEmail(String senderEmail, String recipients, String subject, String body) {
        return sessionFactory.fromTransaction(session -> {
            if (body == null || body.trim().isEmpty()) {
                System.out.println("The email body cannot be empty.");
                return null;
            }

            String[] recipientAccs = recipients.split(",\\s*");
            for (String recipientAcc : recipientAccs) {
                List<User> existingUser = session.createNativeQuery(
                                "SELECT * FROM app_user WHERE email = :email", User.class)
                        .setParameter("email", checkEmail(recipientAcc))
                        .getResultList();

                if (existingUser.isEmpty()) {
                    System.out.println("you cant milou " + recipientAcc + "." + "\n this account doent exist.");
                    return null;
                }
            }

            Email newEmail = new Email(generateCode(), senderEmail, recipients, subject, body, LocalDateTime.now());
            session.persist(newEmail);
            return newEmail;
        });
    }

    public List<Email> getAllEmails(String userEmail) {
        return sessionFactory.fromTransaction(session ->
                session.createNativeQuery(
                                "SELECT * FROM emails WHERE sender_email = :email OR recipients LIKE :recipient_pattern ORDER BY dateEmailSend DESC", Email.class)
                        .setParameter("email", userEmail)
                        .setParameter("recipient_pattern", "%" + userEmail + "%")
                        .getResultList()
        );
    }

    public List<Email> getUnreadEmails(String userEmail) {
        return sessionFactory.fromTransaction(session ->
                session.createNativeQuery(
                                "SELECT * FROM emails WHERE recipients LIKE :recipient_pattern AND is_read = false ORDER BY dateEmailSend DESC", Email.class)
                        .setParameter("recipient_pattern", "%" + userEmail + "%")
                        .getResultList()
        );
    }

    public List<Email> getSentEmails(String userEmail) {
        return sessionFactory.fromTransaction(session ->
                session.createNativeQuery(
                                "SELECT * FROM emails WHERE sender_email = :email ORDER BY dateEmailSend DESC", Email.class)
                        .setParameter("email", userEmail)
                        .getResultList()
        );
    }

    public Email replyToEmail(String code, String replyBody, String senderEmail) {
        return sessionFactory.fromTransaction(session -> {
            List<Email> originalEmails = session.createNativeQuery(
                            "SELECT * FROM emails WHERE code = :code", Email.class)
                    .setParameter("code", code)
                    .getResultList();

            if (originalEmails.isEmpty()) {
                System.out.println("email not found.");
                return null;
            }
            Email originalEmail = originalEmails.get(0);

            String allRecipients = originalEmail.getRecipients() + "," + originalEmail.getSenderEmail();
            String[] recipientsArray = allRecipients.split(",\\s*");

            List<String> uniqueRecipients = new ArrayList<>();
            for (String email : recipientsArray) {
                if (!email.equals(senderEmail) && !uniqueRecipients.contains(email)) {
                    uniqueRecipients.add(email);
                }
            }

            String newRecipients = String.join(", ", uniqueRecipients);
            String newSubject = "[Re] " + originalEmail.getSubject();
            return sendEmail(senderEmail, newRecipients, newSubject, replyBody);
        });
    }

    public Email readEmailByCode(String code, String currentUserEmail) {
        return sessionFactory.fromTransaction(session -> {
            List<Email> emails = session.createNativeQuery(
                            "SELECT * FROM emails WHERE code = :code", Email.class)
                    .setParameter("code", code)
                    .getResultList();

            if (emails.size() == 1) {
                Email email = emails.get(0);
                String[] recipients = email.getRecipients().split(",\\s*");

                boolean isSender = email.getSenderEmail().equals(currentUserEmail);
                boolean isRecipient = Arrays.asList(recipients).contains(currentUserEmail);

                if (isSender || isRecipient) {
                    if (isRecipient && !email.isRead()) {
                        email.setRead(true);
                        session.merge(email);
                    }
                    return email;
                } else {
                    System.out.println("You cant read this milou");
                    return null;
                }
            }
            return null;
        });
    }



    public Email forwardEmail(String Code, String newRecipients, String senderEmail) {
        return sessionFactory.fromTransaction(session -> {
            List<Email> originalEmails = session.createNativeQuery(
                            "SELECT * FROM emails WHERE code = :code", Email.class)
                    .setParameter("code", Code)
                    .getResultList();

            if (originalEmails.isEmpty()) {
                System.out.println("email not found.");
                return null;
            }
            String newSubject = "[Fw] " + originalEmails.get(0).getSubject();
            String newBody = originalEmails.get(0).getBody();

            return sendEmail(senderEmail, newRecipients, newSubject, newBody);
        });
    }
}

