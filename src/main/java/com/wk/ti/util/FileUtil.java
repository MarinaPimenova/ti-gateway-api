package com.wk.ti.util;

import com.wk.ti.exception.UploadException;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import static java.lang.String.format;

@Slf4j
public class FileUtil {
    private FileUtil() {
    }

    public static File store(MultipartFile file) {
        try {
            String uploadDir = System.getProperty("java.io.tmpdir");

            String sanitizedFilename = sanitizeFilename(file.getOriginalFilename());
            // Use sanitized filename as suffix, and "upload-" as prefix
            File tempFile = File.createTempFile("upload-", "-" + sanitizedFilename, new File(uploadDir));
            Files.copy(new ByteArrayInputStream(file.getBytes()), tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return tempFile;

        } catch (IOException e) {
            String error = format("Failed to upload file: %s. Caused: %s",
                    file.getOriginalFilename(), e.getMessage());
            log.error(error);
            throw new UploadException(error);
        }
    }

    /**
     * Creates a temporary file from a MultipartFile.
     * - Converts to UTF-8 if text file
     * - Copies as-is if binary
     */
    public static File toTempFileWithUtf8IfText(@NotNull MultipartFile multipartFile) throws IOException {

        File tempFile = store(multipartFile);

        String contentType = multipartFile.getContentType();
        boolean isText = isTextBased(contentType, multipartFile.getOriginalFilename());

        if (isText) {
            // Try to detect original charset (optional)
            Charset sourceCharset = detectCharset(multipartFile);
            try (
                    Reader reader = new InputStreamReader(multipartFile.getInputStream(), sourceCharset);
                    Writer writer = new OutputStreamWriter(new FileOutputStream(tempFile), StandardCharsets.UTF_8)
            ) {
                reader.transferTo(writer);
            }
        }

        return tempFile;
    }

    private static String sanitizeFilename(String filename) {
        if (!StringUtils.hasText(filename)) return "file";
        return filename.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private static boolean isTextBased(String contentType, String filename) {
        if (contentType == null) contentType = "";

        if (contentType.startsWith("text/")) return true;

        // Handle common structured text formats
        return contentType.contains("json")
                || contentType.contains("xml")
                || contentType.contains("csv")
                || filename.endsWith(".txt")
                || filename.endsWith(".csv")
                || filename.endsWith(".json")
                || filename.endsWith(".xml")
                || filename.endsWith(".yaml")
                || filename.endsWith(".yml");
    }

    /**
     * Detects the charset using Apache Tika.
     */
    public static Charset detectCharset(MultipartFile file) {
        try (InputStream is = file.getInputStream()) {
            Metadata metadata = new Metadata();
            metadata.set("resourceName", file.getOriginalFilename());

            BodyContentHandler handler = new BodyContentHandler(-1); // no write limit
            AutoDetectParser parser = new AutoDetectParser();
            ParseContext context = new ParseContext();

            parser.parse(is, handler, metadata, context);

            String encoding = metadata.get("Content-Encoding");
            if (encoding == null) {
                encoding = metadata.get("charset");
            }

            if (encoding != null) {
                return Charset.forName(encoding);
            }

        } catch (Exception e) {
            log.error("Failed detect Charset for file: {}. Caused: {}", file.getOriginalFilename(), e.getMessage());
        }

        // fallback
        return Charset.defaultCharset();
    }

}
