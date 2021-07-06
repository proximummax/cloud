package com.serv;

import java.io.*;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import java.util.ArrayList;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.*;

@WebServlet("/Download")

public class Download extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final String url = "jdbc:postgresql://localhost/logs";
	private static final String password = "vnc9094842";
	private static final String username = "postgres";
	private ServletFileUpload uploader = null;

	public void init() throws ServletException { // метод для инициализации DiskFileItemFactory объекта нашего сервлета
		DiskFileItemFactory fileFactory = new DiskFileItemFactory();
		File fileDir = (File) getServletContext().getAttribute("FILES_DIR_FILE");
		fileFactory.setRepository(fileDir);
		this.uploader = new ServletFileUpload(fileFactory);
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String fileName = request.getParameter("fileName");
		if (fileName == null || fileName.equals("")) {
			throw new ServletException("File Name can't be null or empty"); // проверка на корректное имя файла
		}
		File file = new File(request.getServletContext().getAttribute("FILES_DIR") + File.separator + fileName);
		if (!file.exists()) {
			throw new ServletException("File doesn't exists on server."); // проверка на корректную загрузку
		}
		System.out.println("File location on server::" + file.getAbsolutePath());
		ServletContext ctx = getServletContext();
		InputStream fis = new FileInputStream(file);
		String mimeType = ctx.getMimeType(file.getAbsolutePath());
		response.setContentType(mimeType != null ? mimeType : "application/octet-stream");// используем т.к тип файла
																							// будет неизвестен
		response.setContentLength((int) file.length());
		response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");// заголовок

		downloadFile(file, response, fis);

	}

	public void downloadFile(File file, HttpServletResponse response, InputStream fis)
			throws ServletException, IOException { // метод загрузки файла

		try (ServletOutputStream os = response.getOutputStream()) { // процесс загрузки файла
			byte[] bufferData = new byte[1024];
			int read = 0;
			while ((read = fis.read(bufferData)) != -1) {
				os.write(bufferData, 0, read);
			}
			os.flush();
			os.close();
			fis.close();
			System.out.println("File downloaded at client successfully");

		}
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		if (!ServletFileUpload.isMultipartContent(request)) {
			throw new ServletException("Content type is not multipart/form-data");
		}
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		out.write("<html><head></head><body>");
		try {
			List<FileItem> fileItemsList = uploader.parseRequest(request);
			Iterator<FileItem> fileItemsIterator = fileItemsList.iterator();
			// CreateBdTable();
			while (fileItemsIterator.hasNext()) { // информация о действиях на сервере
				FileItem fileItem = fileItemsIterator.next();
				File file = new File(
						request.getServletContext().getAttribute("FILES_DIR") + File.separator + fileItem.getName());
				System.out.println("Absolute Path at server=" + file.getAbsolutePath());
				fileItem.write(file);
				printOut(out, fileItem);// общий список файлов
				InsertBd(fileItem); // выгрузка в бд
				out.write("File uploaded successfully.");
			}
		} catch (FileUploadException e) {
			out.write("<h2>" + "You have not selected a file to download" + "</h2" + "<br>");
		} catch (Exception e) {
			out.write("<h2>" + "You have not selected a file to download" + "</h2" + "<br>");
		}

		out.write("<form action =" + " http://localhost:8080/Cloud" + ">" + "<button style=" + "width:200px;height:50px"
				+ ">Back</button></form>");
	}

	private void printOut(PrintWriter out, FileItem fileItem) throws Exception {
		writer(fileItem.getName() + "\n");
	}

	public void writer(String inf) throws Exception {
		FileWriter files = new FileWriter("C:\\Users\\kazan\\eclipse-workspace\\files.txt", true);
		files.write(inf);
		files.close();
	}

	public void InsertBd(FileItem fileItem) {
		try {
			String name = fileItem.getName();
			String type = fileItem.getContentType();
			String size = Long.toString(fileItem.getSize());
			Class.forName("org.postgresql.Driver").getDeclaredConstructor().newInstance();
			String sql = "INSERT INTO log(Id,FileName,ContentType,SizeInBytes) VALUES (DEFAULT,(?),(?),(?))";

			try (Connection conn = DriverManager.getConnection(url, username, password)) {
				PreparedStatement statement = conn.prepareStatement(sql);
				statement.setString(1, name);
				statement.setString(2, type);
				statement.setString(3, size);
				statement.executeUpdate();

			}
		}

		catch (Exception ex) {
			System.out.println("Connection failed...");

			System.out.println(ex);
		}
	}

}
