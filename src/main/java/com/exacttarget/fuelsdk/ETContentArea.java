package com.exacttarget.fuelsdk;

import com.exacttarget.fuelsdk.annotations.ExternalName;
import com.exacttarget.fuelsdk.annotations.InternalName;
import com.exacttarget.fuelsdk.annotations.SoapObject;
import com.exacttarget.fuelsdk.internal.ContentArea;

@SoapObject (internalType = ContentArea.class)
public class ETContentArea extends ETSoapObject
{
    @ExternalName ("id")
    private String id = null;
    @ExternalName("key")
    @InternalName ("key")
    protected String key;
    @ExternalName("content")
    @InternalName ("content")
    protected String content;
    @ExternalName("isBlank")
    @InternalName ("isBlank")
    protected Boolean isBlank;

    @Override public String getId()
    {
        return id;
    }

    @Override public void setId(final String id)
    {
        this.id = id;
    }

    public String getKey()
    {
        return key;
    }

    public void setKey(final String key)
    {
        this.key = key;
    }

    public String getContent()
    {
        return content;
    }

    public void setContent(final String content)
    {
        this.content = content;
    }

    public Boolean getIsBlank()
    {
        return isBlank;
    }

    public void setIsBlank(final Boolean isBlank)
    {
        this.isBlank = isBlank;
    }
}
