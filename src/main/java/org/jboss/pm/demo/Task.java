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

    private long startTime;

    public void execute(TaskContext ctx) throws Exception {
        preExecute(ctx);
        doExecute(ctx);
        postExecute(ctx);
    }

    protected void preExecute(TaskContext ctx) throws Exception {
        System.out.print("Executing " + getClass().getSimpleName() + "...");
        startTime = System.currentTimeMillis();
    }

    protected abstract void doExecute(TaskContext ctx) throws Exception;

    protected void postExecute(TaskContext ctx) throws Exception {
        final long time = System.currentTimeMillis() - startTime;
        final long seconds = time / 1000;
        System.out.println(" done in " + seconds + '.' + (time - seconds*1000) + " sec");
    }
}
