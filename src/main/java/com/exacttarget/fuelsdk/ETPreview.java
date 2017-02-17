package com.exacttarget.fuelsdk;

public class ETPreview
{
    private String htmlContent;
    private String absolutePath;

    public ETPreview(final String htmlContent, final String absolutePath)
    {
        this.htmlContent = htmlContent;
        this.absolutePath = absolutePath;
    }

    public String getHtmlContent()
    {
        return htmlContent;
    }

    public void setHtmlContent(final String htmlContent)
    {
        this.htmlContent = htmlContent;
    }

    public String getAbsolutePath()
    {
        return absolutePath;
    }

    public void setAbsolutePath(final String absolutePath)
    {
        this.absolutePath = absolutePath;
    }
}
