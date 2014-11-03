//
// This file is part of the Fuel Java SDK.
//
// Copyright (C) 2013, 2014 ExactTarget, Inc.
// All rights reserved.
//
// Permission is hereby granted, free of charge, to any person
// obtaining a copy of this software and associated documentation
// files (the "Software"), to deal in the Software without restriction,
// including without limitation the rights to use, copy, modify,
// merge, publish, distribute, sublicense, and/or sell copies
// of the Software, and to permit persons to whom the Software
// is furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be
// included in all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY
// KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
// WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
// PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
// OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES
// OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT
// OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH
// THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
//

package com.exacttarget.fuelsdk.audiencebuilder;

import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import com.exacttarget.fuelsdk.ETFilter;
import com.exacttarget.fuelsdk.ETRestObject;
import com.exacttarget.fuelsdk.ETSdkException;
import com.exacttarget.fuelsdk.annotations.ExternalName;
import com.exacttarget.fuelsdk.annotations.RestObject;

@RestObject(path = "/internal/v1/AudienceBuilder/Dimension/{id}",
            primaryKey = "id",
            collection = "entities",
            totalCount = "totalCount")
public class ETDimension extends ETRestObject {
    @Expose @SerializedName("dimensionID")
    @ExternalName("id")
    String id = null;
    @Expose
    @ExternalName("name")
    private String name = null;
    @Expose
    @ExternalName("type")
    private Integer type = null;
    // XXX better name for customObjectField?
    @Expose @SerializedName("customObjectFieldID")
    @ExternalName("customObjectFieldId")
    private String customObjectFieldId = null;
    @Expose
    @ExternalName("customObjectFieldName")
    private String customObjectFieldName = null;
    @Expose @SerializedName("dataType")
    @ExternalName("customObjectFieldType")
    private Integer customObjectFieldType = null;
    @Expose @SerializedName("recordCount")
    @ExternalName("count")
    private Integer count = null;
    @Expose
    @ExternalName("values")
    private List<ETDimensionValue> values = null;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public void setDescription(String description) {
    }

    public Integer getType() {
        return type;
    }

    public String getCustomObjectFieldId() {
        return customObjectFieldId;
    }

    public String getCustomObjectFieldName() {
        return customObjectFieldName;
    }

    public Integer getCustomObjectFieldType() {
        return customObjectFieldType;
    }

    public Integer getCount() {
        return count;
    }

    public List<ETDimensionValue> getValues() {
        return values;
    }

    // XXX
    @Override
    protected String getFilterQueryParams(ETFilter filter)
        throws ETSdkException
    {
        StringBuilder stringBuilder = new StringBuilder();

        String internalProperty = getInternalProperty(ETDimension.class,
                                                      filter.getProperty());

        stringBuilder.append(internalProperty);

        stringBuilder.append("=");

        ETFilter.Operator operator = filter.getOperator();
        switch(operator) {
          case EQUALS:
            stringBuilder.append(filter.getValue());
            break;
          case NOT_EQUALS:
            stringBuilder.append("not(" + filter.getValue() + ")");
            break;
          case LESS_THAN:
            stringBuilder.append("lt(" + filter.getValue() + ")");
            break;
          case LESS_THAN_OR_EQUALS:
            stringBuilder.append("lte(" + filter.getValue() + ")");
            break;
          case GREATER_THAN:
            stringBuilder.append("gt(" + filter.getValue() + ")");
            break;
          case GREATER_THAN_OR_EQUALS:
            stringBuilder.append("gte(" + filter.getValue() + ")");
            break;
          case IN:
            stringBuilder.append("in(");
            boolean first = true;
            for (String value : filter.getValues()) {
                if (first) {
                    first = false;
                } else {
                    stringBuilder.append(",");
                }
                stringBuilder.append(value);
            }
            stringBuilder.append(")");
            break;
          default:
            // XXX throw exception unsupported
        }

        return stringBuilder.toString();
    }
}
