package aut.ap;

import aut.ap.essentials.Email;
import aut.ap.essentials.User;

import java.util.List;
import java.util.Scanner;


public class Main {
    private final Scanner scanner = new Scanner(System.in);
    private final Service service = new Service();
    private User currentUser = null;

    public static void main(String[] args) {
        Main app = new Main();
        app.start();
        app.close();
    }

    private void start() {
        System.out.println("Welcome to Milou Email Service!");
        while (currentUser == null) {
            firstMenu();
        }
        showMenu();
    }

    private void firstMenu() {
        System.out.println("[S]ign up | [L]ogin | [E]xit");
        String order = scanner.nextLine().toUpperCase();
        switch (order) {
            case "S":
                System.out.print("your name: ");
                String name = scanner.nextLine();
                System.out.print("your email: ");
                String email = scanner.nextLine();
                System.out.print("your password: ");
                String password = scanner.nextLine();
                User newUser = service.signUp(name, email, password);
                if (newUser != null) {
                    System.out.println("Your new account is created. \n" + "Go ahead and login!");
                }
                break;
            case "L":
                System.out.print("your email: ");
                String loginEmail = scanner.nextLine();
                System.out.print("your password: ");
                String loginPassword = scanner.nextLine();
                User loggedInUser = service.login(loginEmail, loginPassword);
                if (loggedInUser != null) {
                    currentUser = loggedInUser;
                    System.out.println("Welcome back " + currentUser.getName() + "!");
                } else {
                    System.out.println("MilouAccount or password is incorrect.");
                }
                break;
            case "E":
                System.exit(0);
        }
    }

    private void showMenu() {
        List<Email> unreadEmails = service.getUnreadEmails(currentUser.getEmail());
        if (!unreadEmails.isEmpty()) {
            System.out.println("\n* You have unread Milous!");
            displayEmails(unreadEmails, "unread");
        }

        while (currentUser != null) {
            System.out.println("\n* Menu ");
            System.out.println(" [S]end, [V]iew, [R]eply, [F]orward, [L]ogout");
            System.out.print("Enter your order: ");
            String order = scanner.nextLine().toUpperCase();

            switch (order) {
                case "S":
                    System.out.print("Recipient(s): ");
                    String recipients = scanner.nextLine();
                    System.out.print("Subject: ");
                    String subject = scanner.nextLine();
                    System.out.println("Body:");
                    String body = scanner.nextLine();
                    Email newEmail = service.sendEmail(currentUser.getEmail(), recipients, subject, body);
                    if (newEmail != null) {
                        System.out.println("Milou sent successfully!");
                        System.out.println("Code: " + newEmail.getCode());
                    } else {
                        System.out.println("Failed. try again!");
                    }
                    break;
                case "V":
                    ViewMenu();
                    break;
                case "R":
                    System.out.print("Code: ");
                    String replyCode = scanner.nextLine();
                    System.out.print("Body: ");
                    String replyBody = scanner.nextLine();
                    Email repliedEmail = service.replyToEmail(replyCode, replyBody, currentUser.getEmail());
                    if (repliedEmail != null) {
                        System.out.println("Successfully reply to Milou " + replyCode + ".");
                        System.out.println("Code: " + repliedEmail.getCode());
                    } else {
                        System.out.println("Failed to reply to Milou.");
                    }
                    break;
                case "F":
                    System.out.print("Code: ");
                    String forwardCode = scanner.nextLine();
                    System.out.print("Recipient(s): ");
                    String forwardRecipients = scanner.nextLine();
                    Email forwardedEmail = service.forwardEmail(forwardCode, forwardRecipients, currentUser.getEmail());
                    if (forwardedEmail != null) {
                        System.out.println("Successfully forwarded your email.");
                        System.out.println("Code: " + forwardedEmail.getCode());
                    } else {
                        System.out.println("Failed to forward email.");
                    }
                    break;
                case "L":
                    System.out.println("Logging out...");
                    currentUser = null;
                    break;
                default:
                    System.out.println("Please try again.");
                    break;
            }
        }
    }

    private void ViewMenu() {
        String submenuOrder = "";
        while (!submenuOrder.equals("B")) {
            System.out.println("\n* View Menu ");
            System.out.println("[A]ll Milous, [U]nread Milous, [S]ent Milous, Read by [C]ode, [B]ack");
            System.out.print("Enter your order: ");
            submenuOrder = scanner.nextLine().toUpperCase();

            switch (submenuOrder) {
                case "A":
                    List<Email> allEmails = service.getAllEmails(currentUser.getEmail());
                    System.out.println("* All Milous :");
                    displayEmails(allEmails, "all");
                    break;
                case "U":
                    List<Email> unreadEmailsView = service.getUnreadEmails(currentUser.getEmail());
                    System.out.println("* Unread Milous : ");
                    displayEmails(unreadEmailsView, "unread");
                    break;
                case "S":
                    List<Email> sentEmails = service.getSentEmails(currentUser.getEmail());
                    System.out.println("* Sent Milous :");
                    displayEmails(sentEmails, "sent");
                    break;
                case "C":
                    System.out.print("Enter Milou code: ");
                    String emailCode = scanner.nextLine();
                    Email emailByCode = service.readEmailByCode(emailCode, currentUser.getEmail());
                    if (emailByCode != null) {
                        displayFullEmail(emailByCode);
                    } else {
                        System.out.println("Milou doesnt exist.");
                    }
                    break;
                case "B":
                    System.out.println("Returning to menu..");
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
                    break;
            }
        }
    }

    private void displayEmails(List<Email> emails, String type) {
        if (emails.isEmpty()) {
            System.out.println("No milous found.");
        } else {
            for (Email email : emails) {
                if (type.equals("sent")) {
                    System.out.println("Recipient(s): " + email.getRecipients() + " - " + email.getSubject() + " (" + email.getCode() + ")");
                } else {
                    String status = email.isRead() ? "" : " (Unread)";
                    System.out.println("+ " + email.getSenderEmail() + " - " + email.getSubject() + " (" + email.getCode() + ")" + status);
                }
            }
        }
    }

    private void displayFullEmail(Email email) {
        System.out.println("Code: " + email.getCode());
        System.out.println("Sender: " + email.getSenderEmail());
        System.out.println("Recipient(s): " + email.getRecipients());
        System.out.println("Subject: " + email.getSubject());
        System.out.println("Date: " + email.getDateEmailSend());
        System.out.println("\n" + email.getBody() + "\n");
    }

    private void close() {
        System.out.println("closing milou...");
        scanner.close();
        service.closeSessionFactory();
    }
}