package com.diving.prototype;

public class FormData {
    private String name;
    private String birthdate;
    private String gender;
    private String licenseLevel;
    private String phone;
    private String email;
    private String signatureImageBase64;

    // 생성자, getter 및 setter 생략

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(String birthdate) {
        this.birthdate = birthdate;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getLicenseLevel() {
        return licenseLevel;
    }

    public void setLicenseLevel(String licenseLevel) {
        this.licenseLevel = licenseLevel;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSignatureImageBase64() {
        return signatureImageBase64;
    }

    public void setSignatureImageBase64(String signatureImageBase64) {
        this.signatureImageBase64 = signatureImageBase64;
    }
}
