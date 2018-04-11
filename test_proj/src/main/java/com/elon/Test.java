package com.elon;

import com.elon.util.DBUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

public class Test {

    public static void main(String[] args) throws SQLException, ClassNotFoundException, IOException {
        String db_url = DBUtil.properties.getProperty("db_url");
        String username = DBUtil.properties.getProperty("username");
        String userpass = DBUtil.properties.getProperty("userpass");
        String drive_name = DBUtil.properties.getProperty("drive_name");
        System.out.println(db_url + " " + username + " " + userpass + " " + drive_name);
        DBUtil.getConnection(db_url, username, userpass, drive_name);

        String bigImage = "D:\\picture\\11_5\\src\\version2\\14.BMP";
        bigImage = "D:\\picture\\11_5\\src\\version2\\small_image\\86374196169551.BMP";
        BufferedImage image = ImageIO.read(new File(bigImage));
        //8串1
        int[] validLineIndex = new int[]{1, 2, 4, 6, 7, 9, 10, 12, 13, 15, 16, 18, 19, 21, 22, 24, 25, 27, 39};
        //单场固定
        //validLineIndex = new int[]{1, 2, 4, 6, 7};
        //String result = recognizeBigImageMsg(image,500,46,validLineIndex, Contants.chars, "D:/picture/model/small_11_5.model");
        //识别图片
        //List<String> result_ = recognizeBigImageMsg_byLine(image, 500, 46, validLineIndex, Contants.JC_Chars, "D:/picture/jc/jc_feature.model");
        //List<String> result_ = SvmUtil.recognizeBigImageMsg_byLine(image, 500, 46, null, GameContants.GP_Chars, "D:\\picture\\11_5\\src\\version2\\model\\11_5_v2_feature.model");
        validLineIndex = new int[]{1};
        //List<String> result_ = SvmUtil.recognizeValidAreaMsg_byLine(image, 500, 46, validLineIndex, 0, 0, image.getWidth(), 300, GameContants.GP_Chars, "11_5_v2_feature.model");
        /*for (String everyLine : result_) {
            System.out.println(everyLine);
        }*/
        //List<Integer> blackCount_horizotal = ProjectionSplit.calculateBlackPx(image,500,true);
        //ProjectionSplit.cutImage_horizontal(image,blackCount_horizotal,validLineIndex, ImageType.BMP,"D:\\picture\\11_5\\src\\version2\\small_image");
        //image = ImageCommUtil.clipImage(image,0,0,495,image.getHeight());
        //ImageCommUtil.saveImage(image,ImageType.BMP,"D:\\picture\\11_5\\src\\version2\\small_image\\",System.nanoTime()+"");
        /*List<String> result_ = SvmUtil.recognizeValidAreaMsg_byLine(image, 500, 46, null, 0, 0, image.getWidth(), image.getHeight(), GameContants.GP_Chars, "11_5_v2_feature.model");
        for (String everyLine : result_) {
            System.out.println(everyLine);
        }*/
        /*List<Integer> blackCount_vertical = ProjectionSplit.calculateBlackPx(image, 500, false);
        for (int i = 0; i < blackCount_vertical.size(); i++) {
            System.out.println(i + "--" + blackCount_vertical.get(i));
        }*/

    }
}
