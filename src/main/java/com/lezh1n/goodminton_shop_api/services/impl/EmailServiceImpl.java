package com.lezh1n.goodminton_shop_api.services.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.lezh1n.goodminton_shop_api.exceptions.AppException;
import com.lezh1n.goodminton_shop_api.exceptions.ErrorCode;
import com.lezh1n.goodminton_shop_api.services.EmailService;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {
    private final JavaMailSender emailSender;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Value("${spring.mail.username}")
    private String sourceEmail;

    @Override
    public void sendPasswordResetEmail(String recieveEmail, String token) {
        try {
            MimeMessage mimeMessage = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(sourceEmail);
            helper.setTo(recieveEmail);
            helper.setSubject("Reset your password");

            String resetUrl = frontendUrl + "/reset-password?token=" + token;

            String htmlContent = "<!DOCTYPE html>\n" +
                    "<html lang=\"en\">\n" +
                    "<head>\n" +
                    "    <meta charset=\"UTF-8\">\n" +
                    "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                    "    <title>Reset your password</title>\n" +
                    "    <style>\n" +
                    "        body {\n" +
                    "            font-family: Arial, Helvetica, sans-serif;\n" +
                    "            line-height: 1.6;\n" +
                    "            color: #333333;\n" +
                    "            max-width: 600px;\n" +
                    "            margin: 0 auto;\n" +
                    "            padding: 20px;\n" +
                    "        }\n" +
                    "        .header {\n" +
                    "            background-color: #4285f4;\n" +
                    "            padding: 20px;\n" +
                    "            text-align: center;\n" +
                    "            border-radius: 5px 5px 0 0;\n" +
                    "        }\n" +
                    "        .header h1 {\n" +
                    "            color: white;\n" +
                    "            margin: 0;\n" +
                    "            font-size: 24px;\n" +
                    "        }\n" +
                    "        .content {\n" +
                    "            background-color: #f9f9f9;\n" +
                    "            padding: 20px;\n" +
                    "            border: 1px solid #dddddd;\n" +
                    "            border-top: none;\n" +
                    "            border-radius: 0 0 5px 5px;\n" +
                    "        }\n" +
                    "        .button {\n" +
                    "            display: inline-block;\n" +
                    "            background-color: #4285f4;\n" +
                    "            color: white !important;\n" +
                    "            text-decoration: none;\n" +
                    "            padding: 12px 24px;\n" +
                    "            margin: 20px 0;\n" +
                    "            border-radius: 4px;\n" +
                    "            font-weight: bold;\n" +
                    "        }\n" +
                    "        .footer {\n" +
                    "            margin-top: 20px;\n" +
                    "            text-align: center;\n" +
                    "            font-size: 12px;\n" +
                    "            color: #777777;\n" +
                    "        }\n" +
                    "        .info {\n" +
                    "            background-color: #fff8e1;\n" +
                    "            border-left: 4px solid #ffca28;\n" +
                    "            padding: 12px;\n" +
                    "            margin: 15px 0;\n" +
                    "            font-size: 14px;\n" +
                    "        }\n" +
                    "    </style>\n" +
                    "</head>\n" +
                    "<body>\n" +
                    "    <div class=\"header\">\n" +
                    "        <h1>Reset Your Password</h1>\n" +
                    "    </div>\n" +
                    "    <div class=\"content\">\n" +
                    "        <p>Hi, we received a request to reset the password for your Goodminton Shop account.</p>\n" +
                    "        <p>Click the button below to reset your password:</p>\n" +
                    "        \n" +
                    "        <div style=\"text-align: center;\">\n" +
                    "            <a href=\"" + resetUrl + "\" class=\"button\">Reset Password</a>\n" +
                    "        </div>\n" +
                    "        \n" +
                    "        <div class=\"info\">\n" +
                    "            <p><strong>Important:</strong> This link expires in 15 minutes.</p>\n" +
                    "        </div>\n" +
                    "        \n" +
                    "        <p>If the button does not work, copy and paste the URL below into your browser:</p>\n" +
                    "        <p style=\"word-break: break-all;\">" + resetUrl + "</p>\n" +
                    "        \n" +
                    "        <p>If you did not request a password reset, please ignore this email or contact the system administrator.</p>\n" +
                    "    </div>\n" +
                    "    <div class=\"footer\">\n" +
                    "        <p>This email was sent automatically, please do not reply.</p>\n" +
                    "        <p>&copy; 2026 Goodminton Shop. All rights reserved.</p>\n" +
                    "    </div>\n" +
                    "</body>\n" +
                    "</html>";

            helper.setText(htmlContent, true);

            emailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new AppException(ErrorCode.ACCOUNT_EMAIL_RESET_SEND_FAILED);
        }
    }

}
