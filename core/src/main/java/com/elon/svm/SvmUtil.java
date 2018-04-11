package com.elon.svm;


import com.elon.algroi.ProjectionSplit;
import com.elon.entity.Contants;
import com.elon.entity.ImageType;
import com.elon.util.ImageCommUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SvmUtil {

    /**
     * 生成model文件
     *
     * @param imageDir
     * @param modelFile 待保存的文件名,不带后缀名
     * @throws Exception
     */
    public static void generateModel(String imageDir, String modelFile, int featureValue) throws Exception {
        getSvmFeaturesdata(imageDir, modelFile, featureValue);
        svm_train train = new svm_train();
        train.run(new String[]{modelFile});
    }

    /**
     * 生成文字图片的标签和特征文
     *
     * @param imageDir  文字图片文件夹
     * @param modelFile 特征文件,
     * @throws Exception
     */
    public static void getSvmFeaturesdata(String imageDir, String modelFile, int featureValue) throws Exception {
        FileOutputStream fs = new FileOutputStream(new File(modelFile));
        File[] files = new File(imageDir).listFiles();
        for (File file : files) {
            if (!file.getName().endsWith(".bmp")) {
                continue;
            }
            BufferedImage imgdest = ImageIO.read(file);
            //文件名+空格
            String label = (file.getName().split("\\.")[0].split("_")[0] + " ");
            fs.write(label.getBytes());
            int index = 1;
            for (int x = 0; x < imgdest.getWidth(); ++x) {
                for (int y = 0; y < imgdest.getHeight(); ++y) {
                    int feature = ImageCommUtil.isBlack(imgdest.getRGB(x, y), featureValue) ? 1 : 0;
                    fs.write((index++ + ":" + feature + " ").getBytes());
                }
            }
            fs.write("\r\n".getBytes());
        }
        fs.close();
    }

    /**
     * 识别小图片的对应的index(也就是对应的图片名字编号)
     *
     * @param smallImage 图片
     * @param modelFile  模板数据路径
     * @throws Exception
     */
    public static StringBuffer recognizeSmallImageIndex(BufferedImage smallImage, int featureValue, String modelFile) {
        // 将样本转化为svm数据
        StringBuffer data = getSvmdata(smallImage, featureValue);
        StringBuffer value = SvmPredict.main(data, modelFile);
        return value;
    }

    /**
     * 识别大图片的信息(不换行)
     *
     * @param bigImage       等待识别的大图片
     * @param modelFile      model特征文件,带后缀
     * @param featureValue   特征阀值  大于这个特征是一个值,小于这个特征是另一个值
     * @param fitValue       归一化文字图片到统一的大小(单位:像素)
     * @param validLineIndex 有效的行图片编号(从0开始的)
     * @return
     */
    public static String recognizeBigImageMsg_noByLine(BufferedImage bigImage, int featureValue, int fitValue, int[] validLineIndex, String[] msgs, String modelFile) throws IOException {
        List<BufferedImage> smallImages = ProjectionSplit.cutImage_noByLine(bigImage, featureValue, fitValue, validLineIndex);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < smallImages.size(); i++) {
            BufferedImage smallImage = smallImages.get(i);
            StringBuffer stringBuffer = SvmUtil.recognizeSmallImageIndex(smallImage, featureValue, modelFile);
            if (stringBuffer != null) {
                int index = Integer.parseInt(stringBuffer.toString().replaceAll("\\s", ""));
                try {
                    String msg = msgs[index];
                    System.out.println("图片识别成功,图片标签:" + index + "对应文字:" + msg);
                    sb.append(msg);
                } catch (Exception e) {
                    System.out.println("图片识别失败,图片标签:" + index + " 对应的文字不存在,请在数组中添加对应的文字");
                }
            } else {
                System.out.println("识别失败...");
            }

        }
        return sb.toString();
    }

    /**
     * 识别大图片的信息(按照图片的每行信息返回)
     *
     * @param bigImage       等待识别的大图片
     * @param modelFile      model特征文件,带后缀
     * @param featureValue   特征阀值  大于这个特征是一个值,小于这个特征是另一个值
     * @param fitValue       归一化文字图片到统一的大小(单位:像素)
     * @param validLineIndex 有效的行图片编号(从0开始的)
     * @return
     */
    public static List<String> recognizeBigImageMsg_byLine(BufferedImage bigImage, int featureValue, int fitValue, int[] validLineIndex, String[] msgs, String modelFile) throws IOException {
        List<List<BufferedImage>> smallImages = ProjectionSplit.cutImage_byLine(bigImage, featureValue, fitValue, validLineIndex);
        List<String> bigImageMsg = new ArrayList<>();
        for (int i = 0; i < smallImages.size(); i++) {
            List<BufferedImage> everyLineSmallImages = smallImages.get(i);
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < everyLineSmallImages.size(); j++) {
                BufferedImage smallImage = everyLineSmallImages.get(j);
                StringBuffer stringBuffer = SvmUtil.recognizeSmallImageIndex(smallImage, featureValue, modelFile);
                int index = Integer.parseInt(stringBuffer.toString().replaceAll("\\s", ""));
                try {
                    ImageCommUtil.saveImage(smallImage, ImageType.BMP, "D:\\picture\\11_5\\src\\version2\\small_image", System.nanoTime() + "");
                    String msg = msgs[index];
                    System.out.println("图片识别成功,图片标签:" + index + "对应文字:" + msg);
                    sb.append(msg);
                } catch (Exception e) {
                    System.out.println("图片识别失败,图片标签:" + index + " 对应的文字不存在,请在数组中添加对应的文字");
                }
            }
            bigImageMsg.add(sb.toString());
        }
        return bigImageMsg;
    }

    /**
     * 识别大图片的有效区域信息(按照图片的每行信息返回)
     *
     * @param bigImage                  等待识别的大图片
     * @param modelFile                 model特征文件,带后缀
     * @param featureValue              特征阀值  大于这个特征是一个值,小于这个特征是另一个值
     * @param fitValue                  归一化文字图片到统一的大小(单位:像素)
     * @param validLineIndexOfValidArea 有效的行图片编号(从0开始的)
     * @return
     */
    public static List<String> recognizeValidAreaMsg_byLine(BufferedImage bigImage, int featureValue, int fitValue, int[] validLineIndexOfValidArea, int xPoint, int yPoint, int width, int height, String[] msgs, String modelFile) throws IOException {
        BufferedImage targetImage = ImageCommUtil.clipImage(bigImage, xPoint, yPoint, width, height);
        return recognizeBigImageMsg_byLine(targetImage, featureValue, fitValue, validLineIndexOfValidArea, msgs, modelFile);
    }

    /**
     * 得到文字图片的特征值
     * 注意此方法是向文件内追加内容，如果是测试文件请删除之前的文件
     *
     * @throws Exception
     */
    public static StringBuffer getSvmdata(BufferedImage imgdest, int featureValue) {
        StringBuffer data = new StringBuffer();
        data.append("0 ");
        int index = 1;
        for (int x = 0; x < imgdest.getWidth(); ++x) {
            for (int y = 0; y < imgdest.getHeight(); ++y) {
                int feature = ImageCommUtil.isBlack(imgdest.getRGB(x, y), featureValue) ? 1 : 0;
                data.append(index++ + ":" + feature + " ");
            }
        }
        data.append("\r\n");
        return data;
    }

    public static void main(String[] args) throws Exception {
        String imageDir = "D:/picture/saved_picture";
        String modelName = "D:/small_11_5";
        imageDir = "D:/picture/jc/small_image/uniono";
        modelName = "D:/picture/jc/jc_feature";
        imageDir = "D:/picture/11_5/src/version2/small_image/union";
        modelName = "D:/picture/11_5/src/version2/11_5_v2_feature";

        //获取model特征文件
        boolean isReturn = false;
        //generateModel(imageDir, modelName, 300);
        //isReturn = true;
        if (isReturn) {
            return;
        }
        String bigImage = "D:/picture/jc/501/20171101114844.BMP";
        bigImage = "D:\\picture\\11_5\\src\\version2\\small_image\\86374196169551.BMP";
        BufferedImage image = ImageIO.read(new File(bigImage));
        //8串1
        int[] validLineIndex = new int[]{1, 2, 4, 6, 7, 9, 10, 12, 13, 15, 16, 18, 19, 21, 22, 24, 25, 27, 39};
        //单场固定
        //validLineIndex = new int[]{1, 2, 4, 6, 7};
        //String result = recognizeBigImageMsg(image,500,46,validLineIndex, Contants.chars, "D:/picture/model/small_11_5.model");
        //识别图片
        //List<String> result_ = recognizeBigImageMsg_byLine(image, 500, 46, validLineIndex, Contants.JC_Chars, "D:/picture/jc/jc_feature.model");
        List<String> result_ = recognizeBigImageMsg_byLine(image, 300, 46, null, Contants.GP_Charss, "D:\\picture\\11_5\\src\\version2\\model\\11_5_v2_feature.model");
        for (String everyLine : result_) {
            System.out.println(everyLine);
        }
        //bigImage = "D:\\picture\\11_5\\src\\version2\\small_image\\93201563873340.BMP";
        //ProjectionSplit.cutImage_vertical(image,500,46, ImageType.BMP,"D:\\picture\\11_5\\src\\version2\\small_image");
        //image = ImageIO.read(new File(bigImage));
        //String index = recognizeSmallImageIndex(image, 300, "D:\\picture\\11_5\\src\\version2\\model\\11_5_v2_feature.model").toString().replaceAll("\\s", "");
        //System.out.println(index + "  " + Contants.GP_Charss[Integer.parseInt(index)]);
        //System.out.println(Contants.GP_Charss[Integer.parseInt(index)]);

    }
}
