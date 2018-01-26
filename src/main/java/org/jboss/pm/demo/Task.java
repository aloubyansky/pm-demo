/*
 * Copyright 2016-2018 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.pm.demo;

/**
 *
 * @author Alexey Loubyansky
 */
public abstract class Task {

    private static final int EXECUTING_MSG_LENGTH = 90;

    private long startTime;
    private String executingMsg;

    protected String logAs() {
        return getClass().getSimpleName();
    }

    public void execute(TaskContext ctx) throws Exception {
        preExecute(ctx);
        doExecute(ctx);
        postExecute(ctx);
    }

    protected void preExecute(TaskContext ctx) throws Exception {
        executingMsg = logAs();
        System.out.print(executingMsg);
        startTime = System.currentTimeMillis();
    }

    protected abstract void doExecute(TaskContext ctx) throws Exception;

    protected void postExecute(TaskContext ctx) throws Exception {

        final StringBuilder buf = new StringBuilder();
        final int offset = EXECUTING_MSG_LENGTH - executingMsg.length();
        if(offset > 0) {
            for(int i = 0; i < offset; ++i) {
                buf.append(' ');
            }
        }
        final long time = System.currentTimeMillis() - startTime;
        final long seconds = time / 1000;
        buf.append(" done in ").append(seconds).append('.').append(time - seconds*1000).append(" sec");
        System.out.println(buf.toString());
    }
}
