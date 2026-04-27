package hisab.config;

import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class GoogleDriveBackupService {

    private static final String PARVEZ_FILE_ID = "1mWFH-g1003gAwLc86m4JaWPck5nMDOIS"; // ANIKA FILE ID
    private static final String ANIKA_FILE_ID = "15U3WCbrXoo4Ki3qVakGvR5xqokestEVz";  // PARVEZ FILE ID
    private static final String PARVEZ_PDF_ID = "1WV3qpQOD3_uCXzd3tpFiwsAOCYvEBjp-"; // PARVEZ PDF FILE ID
    private static final String ANIKA_PDF_ID = "15U3WCbrXoo4Ki3qVakGvR5xqokestEVz";  // ANIKA PDF FILE ID

    @Value("${local.excel.file.path}")
    private String localExcelPath;

    @Autowired
    private Drive driveService;

    public void syncExcelToDrive(Long id) {
        try {
            java.io.File localFile = new java.io.File(localExcelPath);
            if (!localFile.exists()) {
                System.out.println("⚠️ Local file not found: " + localExcelPath);
                return;
            }
 FileContent mediaContent = new FileContent("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", localFile);
 String FILE_ID = id.equals(1L)?PARVEZ_FILE_ID:ANIKA_FILE_ID;
 File updatedFile = driveService.files()
                    .update(FILE_ID, null, mediaContent)
                    .setFields("id, webViewLink, modifiedTime")
                    .execute();

        } catch (Exception e) {
            System.err.println("❌ Backup failed: " + e.getMessage());
            e.printStackTrace();
        }
    }


        public void syncPdfToDrive(Long id , byte[] pdfBytes) {
            try {
       ByteArrayContent mediaContent = new ByteArrayContent("application/pdf", pdfBytes);
       String pdf_id=id.equals(1L)?PARVEZ_PDF_ID:ANIKA_PDF_ID;
        File updatedFile = driveService.files()
                        .update(pdf_id, null, mediaContent)
                        .setFields("id, name, webViewLink, modifiedTime, size")
                        .execute();

            } catch (Exception e) {
                System.err.println("❌ Failed to upload PDF to Google Drive");
                e.printStackTrace();
            }
        }

}
