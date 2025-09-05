package dao;

import db.MyConnection;
import Model.data;

import java.awt.*;
import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DataDAO {

    public static List<data> getALLFiles(String email) throws SQLException {
        Connection conn = MyConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement("SELECT * FROM data WHERE email=?");
        ps.setString(1, email);
        ResultSet rs = ps.executeQuery();
        List<data> files = new ArrayList<>();

        while (rs.next()) {
            int id = rs.getInt("id");
            String nameoffile = rs.getString("nameoffile");
            String path = rs.getString("path");
            files.add(new data(id, nameoffile, path));
        }

        rs.close();
        ps.close();
        conn.close();
        return files;
    }

    public static int hideFile(data file) throws SQLException, IOException {
        Connection conn = MyConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO data(nameoffile, path, email, bin_data) VALUES (?, ?, ?, ?)");

        ps.setString(1, file.getFileName());
        ps.setString(2, file.getPath());
        ps.setString(3, file.getEmail());

        File f = new File(file.getPath());
        FileInputStream fis = new FileInputStream(f);
        ps.setBinaryStream(4, fis, (int) f.length());

        int ans = ps.executeUpdate();
        fis.close();
        f.delete(); // delete original file after hiding

        ps.close();
        conn.close();
        return ans;
    }

    public static void unhide(int id) throws SQLException, IOException {
        Connection conn = MyConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement("SELECT path, bin_data FROM data WHERE id=?");
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            String path = rs.getString("path");
            Blob blob = rs.getBlob("bin_data");

            InputStream is = blob.getBinaryStream();
            FileOutputStream fos = new FileOutputStream(path);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }

            fos.close();
            is.close();

            // remove file record after unhide
            ps = conn.prepareStatement("DELETE FROM data WHERE id=?");
            ps.setInt(1, id);
            ps.executeUpdate();

            System.out.println("Successfully unhidden the file at: " + path);
        } else {
            System.out.println("No file found for ID " + id);
        }

        rs.close();
        ps.close();
        conn.close();
    }

    public static void openHiddenFile(int id) throws SQLException, IOException {
        Connection conn = MyConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement("SELECT nameoffile, bin_data FROM data WHERE id=?");
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            String fileName = rs.getString("nameoffile");
            Blob blob = rs.getBlob("bin_data");

            // Save to temp file
            File tempFile = File.createTempFile("hidden_", "_" + fileName);
            InputStream is = blob.getBinaryStream();
            FileOutputStream fos = new FileOutputStream(tempFile);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }

            fos.close();
            is.close();

            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(tempFile);
            } else {
                System.out.println("Desktop not supported. File saved at: " + tempFile.getAbsolutePath());
            }
        } else {
            System.out.println("File not found in DB with id: " + id);
        }

        rs.close();
        ps.close();
        conn.close();
    }
}
