/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.camel.kamelets.utils.format.converter.aws2.s3;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.camel.CamelExecutionException;
import org.apache.camel.Exchange;
import org.apache.camel.component.aws2.s3.AWS2S3Constants;
import org.apache.camel.kamelets.utils.format.spi.DataTypeConverter;
import org.apache.camel.kamelets.utils.format.spi.annotations.DataType;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.utils.IoUtils;

/**
 * Json output data type represents file name as key and file content as Json structure.
 * <p/>
 * Example Json structure: { "key": "myFile.txt", "content": "Hello", }
 */
@DataType(scheme = "aws2-s3", name = "json")
public class AWS2S3JsonOutputType implements DataTypeConverter {

    private static final String TEMPLATE = "{" +
            "\"key\": \"%s\", " +
            "\"content\": \"%s\"" +
            "}";

    @Override
    public void convert(Exchange exchange) {
        String key = exchange.getMessage().getHeader(AWS2S3Constants.KEY, String.class);

        ResponseInputStream<?> bodyInputStream = exchange.getMessage().getBody(ResponseInputStream.class);
        if (bodyInputStream != null) {
            try {
                exchange.getMessage().setBody(String.format(TEMPLATE, key, IoUtils.toUtf8String(bodyInputStream)));
                return;
            } catch (IOException e) {
                throw new CamelExecutionException("Failed to convert AWS S3 body to Json", exchange, e);
            }
        }

        byte[] bodyContent = exchange.getMessage().getBody(byte[].class);
        if (bodyContent != null) {
            exchange.getMessage().setBody(String.format(TEMPLATE, key, new String(bodyContent, StandardCharsets.UTF_8)));
        }
    }
}