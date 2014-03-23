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

package com.exacttarget.fuelsdk.soap;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;

import org.apache.log4j.Logger;

import com.exacttarget.fuelsdk.ETClient;
import com.exacttarget.fuelsdk.ETGetService;
import com.exacttarget.fuelsdk.ETResponse;
import com.exacttarget.fuelsdk.ETSdkException;
import com.exacttarget.fuelsdk.ETSoapObject;
import com.exacttarget.fuelsdk.annotations.InternalSoapField;
import com.exacttarget.fuelsdk.annotations.InternalSoapType;
import com.exacttarget.fuelsdk.filter.ETComplexFilter;
import com.exacttarget.fuelsdk.filter.ETFilter;
import com.exacttarget.fuelsdk.filter.ETSimpleFilter;
import com.exacttarget.fuelsdk.internal.APIObject;
import com.exacttarget.fuelsdk.internal.ComplexFilterPart;
import com.exacttarget.fuelsdk.internal.FilterPart;
import com.exacttarget.fuelsdk.internal.LogicalOperators;
import com.exacttarget.fuelsdk.internal.RetrieveRequest;
import com.exacttarget.fuelsdk.internal.RetrieveRequestMsg;
import com.exacttarget.fuelsdk.internal.RetrieveResponseMsg;
import com.exacttarget.fuelsdk.internal.SimpleFilterPart;
import com.exacttarget.fuelsdk.internal.SimpleOperators;
import com.exacttarget.fuelsdk.internal.Soap;

public abstract class ETGetServiceImpl<T extends ETSoapObject>
    extends ETServiceImpl implements ETGetService
{
    private static Logger logger = Logger.getLogger(ETGetServiceImpl.class);

    public ETResponse<T> get(ETClient client, Class<T> type, String...properties)
        throws ETSdkException
    {
        return get(client, type, null, properties);
    }

    public ETResponse<T> get(ETClient client, Class<T> type, ETFilter filter, String... properties)
        throws ETSdkException
    {
        ETResponse<T> response = new ETResponseImpl<T>();

        Class<T> externalClass = type; // for code readability

        //
        // Use the @InternalSoapType annotation to determine internalType:
        //

        InternalSoapType internalClassAnnotation
            = externalClass.getAnnotation(InternalSoapType.class);
        assert internalClassAnnotation != null;
        Class<? extends APIObject> internalClass = internalClassAnnotation.type();
        assert internalClass != null;

        //
        // Convert external property names to internal property names:
        //

        List<String> internalProperties = null;

        // XXX it would be nice to not have to instantiate
        // an object just so we can call getProperties()..

        T o = null;
        try {
            o = type.newInstance();
        } catch (Exception ex) {
            throw new ETSdkException("could not instantiate "
                    + type.getName(), ex);
        }

        if (properties.length > 0) {
            //
            // Only request those properties specified:
            //

            internalProperties = new ArrayList<String>();

            String[] externalProperties = properties; // for code readability

            for (String externalProperty : externalProperties) {
                Field externalField = null;
                try {
                    externalField = o.getField(type, externalProperty);
                } catch (ETSdkException ex) {
                    throw new ETSdkException("invalid property: " + externalProperty, ex);
                }
                InternalSoapField internalFieldAnnotation
                    = externalField.getAnnotation(InternalSoapField.class);
                if (internalFieldAnnotation == null) {
                    throw new ETSdkException("invalid property: " + externalProperty);
                }

                // XXX duplicate code.. put into method

                String internalProperty = internalFieldAnnotation.serializedName();

                if (internalProperty.isEmpty()) {
                    //
                    // There is no property name specified
                    // in the annotation so look at the values of
                    // @XmlElement or @XmlElementRef on the corresponding
                    // internal field of the CXF generated class:
                    //

                    String internalFieldName = internalFieldAnnotation.name();

                    Field internalField = o.getField(internalClass,
                                                     internalFieldName);

                    XmlElement element =
                            internalField.getAnnotation(XmlElement.class);
                    if (element != null) {
                        internalProperty = element.name();
                    } else {
                        // optional dateTimes are annotated with XmlElementRef
                        XmlElementRef elementRef =
                                internalField.getAnnotation(XmlElementRef.class);
                        if (elementRef != null) {
                            internalProperty = elementRef.name();
                        }
                    }
                }

                assert internalProperty != null;

                internalProperties.add(internalProperty);
            }
        } else {
            //
            // No properties were explicitly requested:
            //

            internalProperties = o.getProperties();
        }

        //
        // Automatically refresh the token if necessary:
        //

        client.refreshToken();

        //
        // Perform the SOAP retrieve:
        //

        Soap soap = client.getSOAPConnection().getSoap();

        RetrieveRequest retrieveRequest = new RetrieveRequest();
        retrieveRequest.setObjectType(internalClass.getSimpleName());
        retrieveRequest.getProperties().addAll(internalProperties);
        if (filter != null) {
            retrieveRequest.setFilter(convertFilterPart(filter));
        }

        if (logger.isTraceEnabled()) {
            logger.trace("RetrieveRequest:");
            logger.trace("  objectType = " + retrieveRequest.getObjectType());
            String line = null;
            for (String property : retrieveRequest.getProperties()) {
                if (line == null) {
                    line = "  properties = { " + property;
                } else {
                    line += ", " + property;
                }
            }
            logger.trace(line + " }");
            // XXX print filter too
        }

        logger.trace("calling soap.retrieve...");

        RetrieveRequestMsg retrieveRequestMsg = new RetrieveRequestMsg();
        retrieveRequestMsg.setRetrieveRequest(retrieveRequest);

        RetrieveResponseMsg retrieveResponseMsg = soap.retrieve(retrieveRequestMsg);

        if (logger.isTraceEnabled()) {
            logger.trace("RetrieveResponseMsg:");
            logger.trace("  requestId = " + retrieveResponseMsg.getRequestID());
            logger.trace("  overallStatus = " + retrieveResponseMsg.getOverallStatus());
            logger.trace("  results = {");
            for (APIObject result : retrieveResponseMsg.getResults()) {
                logger.trace("    " + result);
            }
            logger.trace("  }");
        }

        response.setRequestId(retrieveResponseMsg.getRequestID());
        response.setStatus(retrieveResponseMsg.getOverallStatus().equals("OK"));
        response.setMessage(retrieveResponseMsg.getOverallStatus());
        for (APIObject internalObject : retrieveResponseMsg.getResults()) {
            T externalObject = null;
            try {
                externalObject = externalClass.newInstance();
            } catch (Exception ex) {
                throw new ETSdkException("could not instantiate "
                        + externalClass.getName(), ex);
            }
            response.getResults().add((T) externalObject.fromInternal(internalObject));
        }

        return response;
    }

    protected FilterPart convertFilterPart(ETFilter filter)
        throws ETSdkException
    {
        FilterPart filterPart = null;
        if (filter instanceof ETSimpleFilter) {
            filterPart = new SimpleFilterPart();
            ((SimpleFilterPart) filterPart)
                    .setProperty(((ETSimpleFilter) filter).getProperty());
            ((SimpleFilterPart) filterPart)
                    .setSimpleOperator(SimpleOperators
                            .valueOf(((ETSimpleFilter) filter).getOperator()
                                    .toString()));
            ((SimpleFilterPart) filterPart).getValue().addAll(
                    ((ETSimpleFilter) filter).getValues());
            if (null != ((ETSimpleFilter) filter).getDateValues()) {
                for (Date d : ((ETSimpleFilter) filter).getDateValues()) {
                    ((SimpleFilterPart) filterPart).getDateValue().add(d);
                }
            }
        } else if (filter instanceof ETComplexFilter) {
            filterPart = new ComplexFilterPart();
            ((ComplexFilterPart) filterPart)
                    .setLeftOperand(convertFilterPart(((ETComplexFilter) filter)
                            .getLeftOperand()));
            ((ComplexFilterPart) filterPart)
                    .setRightOperand(convertFilterPart(((ETComplexFilter) filter)
                            .getRightOperand()));
            if (null != ((ETComplexFilter) filter).getAdditionalOperands()) {
                for (ETFilter additional : ((ETComplexFilter) filter)
                        .getAdditionalOperands()) {
                    ((ComplexFilterPart) filterPart).getAdditionalOperands()
                            .getOperand().add(convertFilterPart(additional));
                }
            }
            ((ComplexFilterPart) filterPart)
                    .setLogicalOperator(LogicalOperators
                            .valueOf(((ETComplexFilter) filter).getOperator()
                                    .toString()));
        }
        return filterPart;
    }
}
