package com.example.save_template_backend.service;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;

@Service
public class TransferHmService {

    private static final String TEMPLATE_DIR = "src/main/resources/templates/";

    public void saveHtmlToFile(String html, String filename) {
        try {
            Path filePath = Paths.get(TEMPLATE_DIR + filename + ".html");
            Files.createDirectories(filePath.getParent());
            Files.writeString(filePath, html);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save HTML file: " + e.getMessage());
        }
    }

    public byte[] saveAndGeneratePdf(String html, String filename) {
        try {
            Path htmlPath = Paths.get(TEMPLATE_DIR + filename + ".html");
            Files.createDirectories(htmlPath.getParent());
            Files.writeString(htmlPath, html);
            return generatePdfFromHtml(htmlPath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save and generate PDF: " + e.getMessage());
        }
    }

    private byte[] generatePdfFromHtml(Path htmlFilePath) throws IOException {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            String html = Files.readString(htmlFilePath)
                    .replaceAll("<br>", "<br/>")
                    .replaceAll("<img([^>]+)(?<!/)>", "<img$1/>");

            // Extract the <div class="title-data"> ... </div>
            String titleData = "";
            java.util.regex.Matcher matcher = java.util.regex.Pattern
                    .compile("<div[^>]*class=['\"]title-data['\"][^>]*>(.*?)</div>", java.util.regex.Pattern.DOTALL)
                    .matcher(html);
            if (matcher.find()) {
                titleData = matcher.group(1).trim(); // extract inner content
                html = matcher.replaceFirst("");     // remove that div from main html
            }

            // Replace placeholders (optional)
            Map<String, String> data = new HashMap<>();
            data.put("district_name", "Guntur District");
            data.put("officer_name", "Sri Venkata Rao, DEO");
            data.put("rc_number", "RC/2025/0234");
            data.put("date", "03-Nov-2025");
            data.put("teacher_name", "Sri M. Srinivas");
            data.put("employee_id", "EMP56789");
            data.put("designation", "School Assistant (Maths)");
            data.put("subject", "Mathematics");
            data.put("working_school", "ZPHS, Tenali");
            data.put("mandal_name", "Tenali");
            data.put("transferred_to", "ZPHS, Ponnur");
            data.put("transfer_reason", "Administrative grounds");

            for (Map.Entry<String, String> entry : data.entrySet()) {
                html = html.replace("{{" + entry.getKey() + "}}", entry.getValue());
            }

            String baseUri = Paths.get("src/main/resources/static/").toUri().toString();

            String fullHtml = """
        <!DOCTYPE html>
        <html>
        <head>
          <meta charset='UTF-8'/>
          <style>
            body { 
              font-family: "Times New Roman", serif; 
              line-height: 1.6; 
              font-size: 14px; 
              margin: 40px; 
            }
            .title {
              text-align: center;
              font-size: 22px;
              font-weight: bold;
              margin-bottom: 15px;
              text-transform: uppercase;
            }
            table.header-table {
              width: 100%;
              border-collapse: collapse;
              margin-bottom: 20px;
            }
            table.header-table td {
              vertical-align: top;
            }
            .logo-cell {
              width: 120px;
            }
            .logo-cell img {
              width: 100px;
              height: 100px;
            }
            .header-text {
              font-size: 14px;
              line-height: 1.5;
              padding-left: 10px;
            }
            .ql-align-center { text-align: center; }
            .ql-align-right { text-align: right; }
            .ql-align-justify { text-align: justify; }
            ul { margin-left: 20px; }
          </style>
        </head>
        <body>
          <div class="title">TRANSFER ORDER</div>

          <table class="header-table">
            <tr>
              <td class="logo-cell">
                <img src="images/logo.png" alt="Logo"/>
              </td>
              <td class="header-text">
        """ + titleData + """
              </td>
            </tr>
          </table>

        """ + html + """
        </body>
        </html>
        """;



            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.withHtmlContent(fullHtml, baseUri);
            builder.toStream(os);
            builder.run();
            return os.toByteArray();
        } catch (Exception e) {
            throw new IOException("PDF generation failed: " + e.getMessage());
        }
    }



}

