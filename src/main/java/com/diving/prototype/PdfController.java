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

    @PostMapping("/form")
    public String showSignatureForm(@ModelAttribute SignatureData data, Model model) {
        data.setInstructorName("김재호");
        data.setPdfFile("/pdf/" + data.getPdfFile() + "1.jpg");
        model.addAttribute("signatureData", data);
        return "signature-form";
    }

    @PostMapping("/generate")
    @ResponseBody
    public ResponseEntity<byte[]> generateAndSavePdf(@RequestBody String imageBase64) {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            // 폰트 로드
            PDType0Font font = PDType0Font.load(document, PdfController.class.getResourceAsStream("/fonts/NotoSansKR-Medium.ttf"));

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                // 서명 이미지를 Base64 디코딩하여 추가
                if (imageBase64 != null && !imageBase64.isEmpty()) {
                    byte[] decodedImage = java.util.Base64.getDecoder().decode(imageBase64);

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

    @PostMapping("/sign-pdf-and-send-email")
    @ResponseBody
    public ResponseEntity<byte[]> generatePdfAndSendEmail(@RequestBody FormData formData) {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            PDType0Font font = PDType0Font.load(document, PdfController.class.getResourceAsStream("/fonts/NotoSansKR-Medium.ttf"));

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                // 면책동의서 제목 추가
                contentStream.beginText();
                contentStream.setFont(font, 18);
                contentStream.newLineAtOffset(50, 750);
                contentStream.showText("면책동의서");
                contentStream.endText();

                // 면책동의서 내용 추가
                contentStream.beginText();
                contentStream.setFont(font, 12);
                contentStream.newLineAtOffset(50, 730);
                contentStream.showText("1. 본인은 다이빙 등(프리다이빙, 스쿠버다이빙, 스노클링 등 기타 물 또는 물 밖에서 하는 모든 활동)의 특성상 항상 잠재적인 위험성이 있음을 인지하고 있으며, 안전에 대한 주의 의무는 오로지 본인에게 있음을 확인한다.");
                contentStream.newLine();
                contentStream.showText("2. 본인은 다이빙 등을 함에 있어서 오직 본인의 책임 하에 항상 본인의 능력과 컨디션에 맞추어 활동해야 함을 확인한다.");
                contentStream.newLine();
                contentStream.showText("3. 강사 또는 타 주최자와 동반하여 투어(해양, 풀장 및 그 외 모든 모임 포함) 및 트레이닝(이하 트레이닝 등)을 함에 있어서 트레이닝 등은 오직 본인의 자발적인 의사에 의하여 참여하는 것임을 확인한다.");
                contentStream.newLine();
                contentStream.showText("4. 강사 또는 타 주최자가 트레이닝 등을 공지하는 경우에도 이는 어디까지나 본인의 이익을 위해서(예: 숙식 및 뱃삯 등 비용 절감, 버디 매칭 편의) 본인이 함께 참석하는 것 일 뿐, 강사 또는 주최자는 사고 발생과(트레이닝 등 및 이동 과정, 기타 숙식관련 포함) 관련하여 어떠한 책임도지지 않는다.");
                contentStream.newLine();
                contentStream.showText("5. 본인은 트레이닝 등의 과정에서 발생한 사고(파손 및 분실 포함)와 관련하여 강사 또는 주최자, 참여자에게 민•형사상 어떠한 책임도 묻지 않을 것임을 확인한다. 다만 타인(강사, 주최자, 참여자 등)의 직접적인 실수로 인한 장비 파손 및 분실의 경우 해당인은 책임을 질 수 있다.");
                contentStream.newLine();
                contentStream.showText("6. 본인은 트레이닝 등을 함에 있어서 인증, 후기 작성, 동호회 알림 등의 용도로 참여자에 의하여 사진 및 동영상이 촬영 및 게시가 될 수 있음을 사전 인지하고 있으며, 이에 동의한다.");
                contentStream.newLine();
                contentStream.showText("7. 본 면책동의서는 향후 본 카페의 회원이 주최하는 트레이닝 등에 참가하는 경우 효력을 갖습니다.");
                contentStream.endText();

                // 사용자 입력 데이터 및 서명 정보 추가
                contentStream.beginText();
                contentStream.setFont(font, 12);
                contentStream.newLineAtOffset(50, 630); // 조정된 시작 좌표
                contentStream.showText("이름: " + formData.getName());
                contentStream.newLine();
                contentStream.showText("생년월일: " + formData.getBirthdate());
                contentStream.newLine();
                contentStream.showText("성별: " + formData.getGender());
                contentStream.newLine();
                contentStream.showText("라이선스 레벨: " + formData.getLicenseLevel());
                contentStream.newLine();
                contentStream.showText("휴대폰 번호: " + formData.getPhone());
                contentStream.newLine();
                contentStream.showText("이메일: " + formData.getEmail());
                contentStream.endText();

                // 서명 이미지 추가
                byte[] decodedImage = java.util.Base64.getDecoder().decode(formData.getSignatureImageBase64());
                PDImageXObject pdImage = PDImageXObject.createFromByteArray(document, decodedImage, "signature");
                contentStream.drawImage(pdImage, 100, 400, pdImage.getWidth() / 2, pdImage.getHeight() / 2); // 이미지 좌표 수정
            }

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            document.save(byteArrayOutputStream);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("filename", "signed-document.pdf");

            return new ResponseEntity<>(byteArrayOutputStream.toByteArray(), headers, HttpStatus.OK);
        } catch (IOException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}

