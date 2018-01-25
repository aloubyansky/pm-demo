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

package org.jboss.pm.demo.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

/**
 *
 * @author Alexey Loubyansky
 */
@WebServlet(name="PmDemoServlet", urlPatterns= {"/hello"})
public class DemoServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        final PrintWriter writer = res.getWriter();

        writer.println("<html><body>");

        Context ctx = null;
        try {
            ctx = new InitialContext();
            final DataSource ds = (DataSource) ctx.lookup("java:jboss/datasources/MySqlDS");
            demoDb(ds, writer);
        } catch (Exception e) {
            throw new ServletException("Failed to demo the db stuff", e);
        } finally {
            if(ctx != null) {
                try {
                    ctx.close();
                } catch (NamingException e) {
                }
            }
            writer.println("</body></html>");
        }
    }

    private void demoDb(DataSource ds, PrintWriter writer) throws Exception {

        try(Connection con = ds.getConnection()) {
            writer.print("<h1>Current count is: ");
            final int count = readCount(con);
            writer.print(count);
            writer.println("</h1>");
            updateCount(con, count);
        }
    }

    private void updateCount(Connection con, int count) throws Exception {
        if(count == 0) {
            initCount(con);
            return;
        }
        try(PreparedStatement ps = con.prepareStatement("UPDATE demo SET cnt=? WHERE id=1 AND cnt=?")) {
            ps.setInt(1, count + 1);
            ps.setInt(2, count);
            final int result = ps.executeUpdate();
            if(result != 1) {
                throw new Exception("Expected one updated row but got " + result);
            }
        }
    }

    private void initCount(Connection con) throws Exception {
        try(PreparedStatement ps = con.prepareStatement("INSERT INTO demo (id, cnt) VALUES (?, ?)")) {
            ps.setInt(1, 1);
            ps.setInt(2, 1);
            final int result = ps.executeUpdate();
            if(result != 1) {
                throw new Exception("Expected one updated row but got " + result);
            }
        }
    }

    private int readCount(Connection con) throws Exception {
        try(PreparedStatement ps = con.prepareStatement("SELECT cnt FROM demo");
                ResultSet rs = ps.executeQuery()) {
            if(!rs.next()) {
                return 0;
            }
            return rs.getInt(1);
        }
    }
}
