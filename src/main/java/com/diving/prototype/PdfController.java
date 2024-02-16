package com.diving.prototype;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.Base64;

import java.io.IOException;

@Controller
@RequestMapping("/sign-pdf")
public class PdfController {

    @GetMapping("/form")
    public String showSignatureForm(@RequestParam(name = "code", required = false) String code, Model model) {
        // 코드가 없거나 유효하지 않은 경우 홈 페이지로 리다이렉트
        if (code == null || !isValidCode(code)) {
            return "redirect:/";
        }

        if(code.equals("1234")){
            model.addAttribute("signerName", "김재호");
        }
        return "signature-form";
    }

    @PostMapping("/generate")
    @ResponseBody
    public ResponseEntity<byte[]> generateAndSavePdf(@RequestBody SignatureData signatureData) {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            // 폰트 로드
            PDType0Font font = PDType0Font.load(document, PdfController.class.getResourceAsStream("/fonts/NotoSansKR-Medium.ttf"));


            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                // PDF에 서명 정보 추가하는 로직
                contentStream.beginText();
                contentStream.setFont(font, 12); // 유니코드 폰트 설정
                contentStream.newLineAtOffset(50, 700);
                contentStream.showText(signatureData.getSignerName()); // 한글 텍스트 표시
                contentStream.endText();

                // 서명 이미지를 Base64 디코딩하여 추가
                if (signatureData.getSignatureImageBase64() != null && !signatureData.getSignatureImageBase64().isEmpty()) {
                    byte[] decodedImage = java.util.Base64.getDecoder().decode(signatureData.getSignatureImageBase64());

                    // PDF에 이미지 추가
                    PDImageXObject pdImage = PDImageXObject.createFromByteArray(document, decodedImage, "signature");
                    contentStream.drawImage(pdImage, 100, 500, pdImage.getWidth(), pdImage.getHeight());
                }
            }

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            document.save(byteArrayOutputStream);

            // PDF 문서를 클라이언트에게 반환하기 전에 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            // 파일명을 지정하는 경우, 사용자가 파일을 다운로드할 때 해당 이름을 제안받게 됩니다.
            headers.setContentDispositionFormData("filename", "signed-document.pdf");

            // ResponseEntity를 사용하여 바이트 배열과 헤더, 상태 코드를 함께 반환
            return new ResponseEntity<>(byteArrayOutputStream.toByteArray(), headers, HttpStatus.OK);
        } catch (IOException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private boolean isValidCode(String code) {
        // 여기에 코드의 유효성 검증 로직을 추가 (예제로는 간단하게 숫자 여부만 확인)
        try {
            Integer.parseInt(code);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}

