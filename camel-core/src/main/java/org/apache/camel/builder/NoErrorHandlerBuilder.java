/**
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
package org.apache.camel.builder;

import org.apache.camel.AsyncCallback;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.processor.DelegateAsyncProcessor;
import org.apache.camel.spi.RouteContext;

/**
 * A builder to disable the use of an error handler so that any exceptions are thrown.
 * This not recommended in general, the
 * <a href="http://camel.apache.org/dead-letter-channel.html">Dead Letter Channel</a> should be used
 * if you are unsure; however it can be useful sometimes to disable an error handler inside a complex route
 * so that exceptions bubble up to the parent {@link Processor}
 */
public class NoErrorHandlerBuilder extends ErrorHandlerBuilderSupport {

    public Processor createErrorHandler(RouteContext routeContext, Processor processor) {
        return new DelegateAsyncProcessor(processor) {
            @Override
            public boolean process(final Exchange exchange, final AsyncCallback callback) {
                return super.process(exchange, new AsyncCallback() {
                    @Override
                    public void done(boolean doneSync) {
                        exchange.removeProperty(Exchange.REDELIVERY_EXHAUSTED);
                        callback.done(doneSync);
                    }
                });
            }

            @Override
            public String toString() {
                if (processor == null) {
                    // if no output then dont do any description
                    return "";
                }
                return "NoErrorHandler[" + processor + "]";
            }
        };
    }

    public boolean supportTransacted() {
        return false;
    }

    @Override
    public ErrorHandlerBuilder cloneBuilder() {
        NoErrorHandlerBuilder answer = new NoErrorHandlerBuilder();
        cloneBuilder(answer);
        return answer;
    }
}
