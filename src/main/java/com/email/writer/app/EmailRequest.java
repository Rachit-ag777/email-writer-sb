package com.email.writer.app;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

public class EmailRequest {

    @JsonProperty("emailContent")
    @JsonAlias({"emailcontent", "email_content"})
    private String emailContent;

    private String tone;

    public String getEmailContent() {
        return emailContent;
    }

    public void setEmailContent(String emailContent) {
        this.emailContent = emailContent;
    }

    public String getTone() {
        return tone;
    }

    public void setTone(String tone) {
        this.tone = tone;
    }
}
