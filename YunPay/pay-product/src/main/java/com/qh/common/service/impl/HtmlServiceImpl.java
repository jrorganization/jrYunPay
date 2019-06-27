package com.qh.common.service.impl;

import com.qh.common.service.HtmlService;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

@Service
public class HtmlServiceImpl implements HtmlService {

    @Override
    public String makeHtml(String orderNo,String content) {

        String path= "/www/wwwroot/acc01/"+orderNo+".html";
        try{
            File directory = new File("");// 参数为空
            String courseFile = directory.getCanonicalPath();
//            FileWriter writer=new FileWriter();

            System.out.println(courseFile);
            //1.创建配置类
            Configuration configuration=new Configuration(Configuration.getVersion());
            //2.设置模板所在的目录  D:\idea\Project\yunpay2.0\YunPay\pay-product\src\main\resources\ftl\testhtml.ftl
            configuration.setDirectoryForTemplateLoading(new File("/classes/ftl/"));
            //3.设置字符集
            configuration.setDefaultEncoding("utf-8");
            //4.加载模板
            Template template = configuration.getTemplate("testhtml.ftl");
            //5.创建数据模型
            Map map=new HashMap();
            map.put("content", content);
            //6.创建Writer对象
            Writer out =new FileWriter(new File(path));
            //7.输出
            template.process(map, out);
            //8.关闭Writer对象
            out.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        return path;
    }

    @Override
    public String make(String orderNo,String content) {
        String path1="/www/wwwroot/acc01/"+orderNo+".html";
//        String path1="D:\\"+orderNo+".html";
        try {
            File file = new File(path1);
            // 创建文件
            file.createNewFile();
            // creates a FileWriter Object
            FileWriter writer = new FileWriter(file);
            // 向文件写入内容
            writer.write(content);
            writer.flush();
            writer.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        return "http://123.1.170.6/"+orderNo+".html";
    }
}
