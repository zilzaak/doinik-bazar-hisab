package hisab.service;

import hisab.controller.UserExcelExplorer;
import hisab.entity.Market;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;
    DateTimeFormatter format = DateTimeFormatter.ofPattern("d MMMM yyyy");

    public void sendExcelReportEmail(List<String> recipients, String subject,
                                     String messageBody,
                                     List<Market> listNobis,
                                     Double total) throws MessagingException, IOException {

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(recipients.toArray(new String[0]));
        helper.setSubject(subject);
        helper.setText(messageBody);
        UserExcelExplorer excelExporter = new UserExcelExplorer(listNobis);
        byte[] excelBytes = excelExporter.exportToByteArray(total);
        String fileName="Shopping_Summary_from_"+listNobis.get(listNobis.size()-1).getDate().format(format)+"_To_"+listNobis.get(0).getDate().format(format)+".xlsx";
        helper.addAttachment(fileName, new org.springframework.core.io.ByteArrayResource(excelBytes));
        mailSender.send(message);
    }


    public void sendPdf(byte[] pdfData, List<String> recipients) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom("orchidpust@gmail.com");
            helper.setTo(recipients.toArray(new String[0]));
            helper.setSubject("Shopping Summary Report till "+LocalDate.now().format(format));
            helper.setText("Please find the attached PDF report.");

            helper.addAttachment("Shopping_items"+LocalDate.now().format(format)+".pdf",
                    new ByteArrayResource(pdfData)
            );

            mailSender.send(message);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}