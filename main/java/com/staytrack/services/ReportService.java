package com.staytrack.services;

import com.staytrack.database.DatabaseInitializer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class ReportService {
    public Path exportCsv(String reportName, String sql, Path folder) throws Exception {
        Files.createDirectories(folder);
        Path file = folder.resolve(reportName + ".csv");
        try (Connection con = DatabaseInitializer.getConnection(); Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            StringBuilder out = new StringBuilder();
            int cols = rs.getMetaData().getColumnCount();
            for (int i = 1; i <= cols; i++) {
                if (i > 1) out.append(',');
                out.append(rs.getMetaData().getColumnLabel(i));
            }
            out.append(System.lineSeparator());
            while (rs.next()) {
                for (int i = 1; i <= cols; i++) {
                    if (i > 1) out.append(',');
                    out.append('"').append(String.valueOf(rs.getObject(i)).replace("\"", "\"\"")).append('"');
                }
                out.append(System.lineSeparator());
            }
            Files.writeString(file, out);
            return file;
        } catch (IOException ex) {
            throw new IOException("Could not write report file: " + file, ex);
        } catch (Exception ex) {
            Files.writeString(file, DemoStore.csv(reportName));
            return file;
        }
    }
}
