package com.example.save_template_backend;

import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class PdfService {

    /**
     * Generate PDF bytes from HTML (with placeholders).
     * This function:
     *  - replaces placeholders {{key}} with provided data
     *  - converts simple display:flex ... space-between blocks into table markup (iText-friendly)
     *  - injects CSS to map Quill alignment classes (.ql-align-center, etc.) to text-align rules
     */
    public byte[] generatePdf(String html, Map<String, Object> data) throws IOException {
        // 1) Replace placeholders
        if (data != null) {
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue() == null ? "" : entry.getValue().toString();
                html = html.replace("{{" + key + "}}", escapeHtmlForPdf(value));
            }
        }

        // 2) Convert simple flex containers (two child divs) into a table so alignment is preserved in PDF.
        //    This handles patterns like:
        //    <div style="display:flex; justify-content:space-between; ...">
        //       <div>Left content</div>
        //       <div>Right content</div>
        //    </div>
        html = convertFlexSpaceBetweenToTable(html);

        // 3) Wrap and inject global CSS that maps Quill classes and provides stable PDF-friendly styles
        String styledHtml = buildStyledHtml(html);

        // 4) Convert to PDF using iText html2pdf
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ConverterProperties props = new ConverterProperties();
        props.setCharset(StandardCharsets.UTF_8.name());
        // If you need to resolve resources (images, fonts) from a base path, set props.setBaseUri(...)
        HtmlConverter.convertToPdf(styledHtml, out, props);

        return out.toByteArray();
    }

    // Simple HTML escaping for inserted values (keeps <, >, & safe)
    private String escapeHtmlForPdf(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    // Converts flex+space-between patterns to table markup.
    // Uses a regex that looks for a div with display:*flex* containing two child divs.
    private String convertFlexSpaceBetweenToTable(String html) {
        // Pattern explained:
        // (?is) -> case-insensitive and dot matches newline
        // <div[^>]*display\s*:\s*[^;>]*flex[^;>]*[^>]*>  -> div opening that has 'display: ... flex' in style attr
        // \s*<div[^>]*>(.*?)</div>\s*<div[^>]*>(.*?)</div>\s*</div>
        String patternStr = "(?is)<div[^>]*style\\s*=\\s*\"[^\"]*display\\s*:\\s*[^;\\\"]*flex[^\"]*\"[^>]*>\\s*<div[^>]*>(.*?)</div>\\s*<div[^>]*>(.*?)</div>\\s*</div>";
        Pattern p = Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher m = p.matcher(html);
        StringBuffer sb = new StringBuffer();

        while (m.find()) {
            String left = m.group(1) == null ? "" : m.group(1).trim();
            String right = m.group(2) == null ? "" : m.group(2).trim();

            // Build table replacement - right cell aligned right
            String replacement = "<table style=\"width:100%;\"><tr>"
                    + "<td style=\"text-align:left; vertical-align:top;\">" + left + "</td>"
                    + "<td style=\"text-align:right; vertical-align:top;\">" + right + "</td>"
                    + "</tr></table>";

            // Escape $ signs in replacement (since appendReplacement treats $ specially)
            replacement = Matcher.quoteReplacement(replacement);
            m.appendReplacement(sb, replacement);
        }
        m.appendTail(sb);
        return sb.toString();
    }

    // Build the HTML wrapper with CSS that maps Quill classes to real text-align rules
    private String buildStyledHtml(String bodyHtml) {
        String css = """
            /* Basic document formatting */
            body {
              font-family: Arial, Helvetica, sans-serif;
              font-size: 12pt;
              line-height: 1.4;
              color: #000;
            }
            p { margin: 6px 0; }
            h1,h2,h3,h4,h5 { margin: 8px 0; }
            
            /* Tables */
            table { width: 100%; border-collapse: collapse; }
            td { padding: 2px 4px; vertical-align: top; }
            
            /* Quill editor classes — map alignments */
            .ql-editor { /* when preview content came from Quill, ensure editor styles apply */ 
              white-space: normal;
              word-wrap: break-word;
            }
            .ql-align-center { text-align: center !important; }
            .ql-align-right { text-align: right !important; }
            .ql-align-justify { text-align: justify !important; }
            .ql-align-left { text-align: left !important; }
            
            /* Support text-indent which Quill may use */
            p[style*=\"text-indent\"], div[style*=\"text-indent\"] {
              /* leave as-is, iText should pick text-indent if specified inline */
            }
            
            /* If any inline styles used for bold/weight, keep them visible */
            strong { font-weight: 700; }
            b { font-weight: 700; }
            
            /* Additional safety: force block-level centering when style attr exists */
            div[style*=\"text-align:center\"] { text-align: center; }
            div[style*=\"text-align:right\"] { text-align: right; }
            div[style*=\"text-align:justify\"] { text-align: justify; }
            """;

        String html = "<!doctype html>\n"
                + "<html>\n<head>\n<meta charset='utf-8'/>\n"
                + "<style>" + css + "</style>\n"
                + "</head>\n<body>\n"
                + bodyHtml
                + "\n</body>\n</html>";
        return html;
    }
}



