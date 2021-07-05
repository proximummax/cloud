package com.serv;

import java.io.File;
 
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
 
@WebListener
public class Context implements ServletContextListener {
 
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        String way = System.getProperty("catalina.home");	//папка, в которой установлен Apache Tomcat
        ServletContext context = servletContextEvent.getServletContext();//получение контекста сервера
        String param = context.getInitParameter("tempfile.dir");//передаем название параметра инициализации
        File file = new File(way + File.separator + param);
        if(!file.exists()) file.mkdirs();
        System.out.println("File Directory created to be used for storing files");	//информация о успешности создания директории для файлов 
        context.setAttribute("FILES_DIR_FILE", file);
        context.setAttribute("FILES_DIR", way + File.separator + param);
    }
 

}
