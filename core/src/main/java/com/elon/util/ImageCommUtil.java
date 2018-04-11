package com.elon.util;

import com.elon.entity.ImageType;
import org.apache.commons.lang3.StringUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;

/**
 * 图片公共类
 */
public class ImageCommUtil {


    /**
     * 保存图片
     *
     * @param srcImage
     * @param format
     * @param rootPath
     * @param fileName 不包含文件后缀
     * @throws IOException
     */
    public static void saveImage(BufferedImage srcImage, ImageType format, String rootPath, String fileName) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ImageIO.write(srcImage, format.getType(), byteArrayOutputStream);
        File rootFile = new File(rootPath);
        if (!rootFile.exists()) {
            rootFile.mkdirs();
        }
        File file = new File(rootFile, fileName + "." + format.getType());
        if (!file.exists()) {
            file.createNewFile();
        }
        BufferedOutputStream b0f = new BufferedOutputStream(new FileOutputStream(file));
        byte[] bytes = byteArrayOutputStream.toByteArray();
        b0f.write(bytes);
        b0f.close();
    }

    /**
     * 缩放图片,图片归一化
     *
     * @param image
     * @param fitValue 归一化到统一的尺寸
     * @return
     */
    public static BufferedImage reSquare(BufferedImage image, int fitValue) {
        if (image == null) {
            return null;
        }
        int w = image.getWidth();
        int h = image.getHeight();
        int max = Math.max(w, h);
        if (fitValue < max) {
            throw new RuntimeException("归一化像素值太小:" + fitValue + " 原图片宽高:" + w + " " + h);
        }
        BufferedImage dsc = new BufferedImage(fitValue, fitValue, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = (Graphics2D) dsc.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, fitValue, fitValue);// 填充整个屏幕
        g.dispose();

        int w1 = (fitValue - w) / 2;
        int h1 = (fitValue - h) / 2;
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                dsc.setRGB(x + w1, y + h1, image.getRGB(x, y));
            }
        }
        return dsc;
    }

    /**
     * 测试rgb值是否是黑色或者深色<br/>
     * 区分像素的特征
     *
     * @param featureValue 特征阀值  大于这个特征是一个值,小于这个特征是另一个值
     * @return
     */
    public static boolean isBlack(int rgb, int featureValue) {
        boolean isBlack = true;
        Color color = new Color(rgb);
        if (color.getRed() + color.getGreen() + color.getBlue() > featureValue) {//是白色
            isBlack = false;
        }
        return isBlack;
    }

    /**
     * 重命名文件名为 System.nanoTime().后缀
     *
     * @param fileDir
     */
    public static void rename_randm(String fileDir) {
        File rootFile = new File(fileDir);
        if (!rootFile.isDirectory()) {
            throw new RuntimeException("路径: " + fileDir + " 不是目录");
        }
        File[] childrenFiles = rootFile.listFiles();
        if (childrenFiles != null && childrenFiles.length > 0) {
            for (int i = 0; i < childrenFiles.length; i++) {
                File file = childrenFiles[i];
                if (!file.isDirectory()) {
                    String fileName = file.getName();
                    int mark = fileName.lastIndexOf(".");
                    if (mark != -1) {
                        String suffix = fileName.substring(mark + 1);
                        if (StringUtils.isNotEmpty(suffix)) {
                            String suffix_ = suffix.toLowerCase();
                            if (ImageType.JPG.getType().equals(suffix_) || ImageType.PNG.getType().equals(suffix_) || ImageType.GIF.getType().equals(suffix_) || ImageType.BMP.getType().equals(suffix_)) {
                                boolean isSuccess = file.renameTo(new File(rootFile, System.nanoTime() + "." + suffix));
                                if (!isSuccess) {
                                    throw new RuntimeException("文件：" + fileName + " 重命名失败");
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 重命名文件夹下的文件(按照数字命名)<br/>
     * 只支持 jpg png gif bmp的重命名
     *
     * @param beginIndex
     * @param fileDir
     */
    public static void renameByIndex(int beginIndex, String fileDir) {
        File rootFile = new File(fileDir);
        if (!rootFile.isDirectory()) {
            throw new RuntimeException("路径: " + fileDir + " 不是目录");
        }
        File[] childrenFiles = rootFile.listFiles();
        int index = beginIndex;
        if (childrenFiles != null && childrenFiles.length > 0) {
            for (int i = 0; i < childrenFiles.length; i++) {
                File file = childrenFiles[i];
                if (!file.isDirectory()) {
                    String fileName = file.getName();
                    int mark = fileName.lastIndexOf(".");
                    if (mark != -1) {
                        String suffix = fileName.substring(mark + 1);
                        if (StringUtils.isNotEmpty(suffix)) {
                            String suffix_ = suffix.toLowerCase();
                            if (ImageType.JPG.getType().equals(suffix_) || ImageType.PNG.getType().equals(suffix_) || ImageType.GIF.getType().equals(suffix_) || ImageType.BMP.getType().equals(suffix_)) {
                                boolean isSuccess = file.renameTo(new File(rootFile, index + "." + suffix));
                                if (isSuccess) {
                                    index = index + 1;
                                } else {
                                    throw new RuntimeException("文件：" + fileName + " 重命名失败");
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 二值化图片<br/>
     * 处理对象是:已经被灰度化了的图片<br/>
     * 功能是:把灰度图像变成只有黑白两色的图片
     *
     * @param srcImage
     * @return
     * @throws IOException
     */
    public static BufferedImage binaryImage(BufferedImage srcImage) throws IOException {
        int width = srcImage.getWidth();
        int height = srcImage.getHeight();
        BufferedImage binaryImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY);//重点，技巧在这个参数BufferedImage.TYPE_BYTE_BINARY
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int rgb = srcImage.getRGB(i, j);
                binaryImage.setRGB(i, j, rgb);
            }
        }
        return binaryImage;
    }

    /**
     * 每个像素是由 亮度信息和彩色信息组成<br/>
     * 灰度化图片:删除彩色信息,只保留亮度信息
     *
     * @param srcImage
     * @return
     * @throws IOException
     */
    public static BufferedImage grayImage(BufferedImage srcImage) throws IOException {
        int width = srcImage.getWidth();
        int height = srcImage.getHeight();
        BufferedImage grayImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);//重点，技巧在这个参数BufferedImage.TYPE_BYTE_GRAY
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int rgb = srcImage.getRGB(i, j);
                grayImage.setRGB(i, j, rgb);
            }
        }
        return grayImage;
    }

    /**
     * 剪裁图片
     *
     * @param srcImage
     * @param xPoint
     * @param yPoint
     * @param width
     * @param height
     * @return
     */
    public static BufferedImage clipImage(BufferedImage srcImage, int xPoint, int yPoint, int width, int height) {
        return srcImage.getSubimage(xPoint, yPoint, width, height);
    }

    /**
     * 上下合并多个图片
     *
     * @param srcImages
     * @param spacePx   合并的图片上下之间的间隙
     * @return
     */
    public static BufferedImage mergeImages(BufferedImage[] srcImages, int spacePx) {
        if (srcImages == null || srcImages.length == 0) {
            throw new RuntimeException("等待合并的图片数组不能为空");
        }
        BufferedImage mergedImage = srcImages[0];
        for (int i = 1; i < srcImages.length; i++) {
            BufferedImage temImage = srcImages[i];
            //正式合并图片
            mergedImage = mergeImage(mergedImage, temImage, spacePx);
        }
        return mergedImage;
    }

    /**
     * 上下合并两个图片
     *
     * @param first
     * @param second
     * @param spacePx 合并的图片上下之间的间隙
     * @return
     */
    public static BufferedImage mergeImage(BufferedImage first, BufferedImage second, int spacePx) {
        int first_width = first.getWidth();
        int first_height = first.getHeight();
        int second_width = second.getWidth();
        int second_height = second.getHeight();
        int all_height = first_height + second_height + spacePx;
        //取最大宽度
        int max_width = Math.max(first_width, second_width);
        BufferedImage mergedImage = new BufferedImage(max_width, all_height, BufferedImage.TYPE_INT_RGB);
        Graphics graphics = mergedImage.getGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, max_width, all_height);// 白色填充整个屏幕
        graphics.dispose();
        //正式合并两个图片
        for (int i = 0; i < first_width; i++) {
            for (int j = 0; j < first_height; j++) {
                int rgb = first.getRGB(i, j);
                mergedImage.setRGB(i, j, rgb);
            }
        }
        for (int i = 0; i < second_width; i++) {
            for (int j = 0; j < second_height; j++) {
                int rgb = second.getRGB(i, j);
                mergedImage.setRGB(i, j + first_height + spacePx, rgb);
            }
        }
        return mergedImage;
    }

    /**
     * 把图片转化为 imageType格式,然后输出到字节数组
     *
     * @param srcImage
     * @param imageType
     * @return
     * @throws IOException
     */
    public static byte[] image2Bytes(BufferedImage srcImage, ImageType imageType) throws IOException {
        byte[] result = null;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        if (ImageIO.write(srcImage, imageType.getType(), byteArrayOutputStream)) {
            result = byteArrayOutputStream.toByteArray();
        } else {
            throw new RuntimeException("格式" + imageType.getType() + "不支持,请换一种格式");
        }
        return result;
    }

    /**
     * 字节数组转为图片
     *
     * @param srcBytes
     * @return
     * @throws IOException
     */
    public static BufferedImage bytes2Image(byte[] srcBytes) throws IOException {
        ByteArrayInputStream byteArrayOutputStream = new ByteArrayInputStream(srcBytes);
        return ImageIO.read(byteArrayOutputStream);
    }

    public static void main(String[] args) throws IOException {
        rename_randm("D:\\picture\\11_5\\src\\version2\\small_image\\union");
        renameByIndex(0, "D:\\picture\\11_5\\src\\version2\\small_image\\union");
        /*String first_image = "D:/picture/saved_picture/91215893875450.bmp";
        String second_image = "D:/picture/saved_picture/91215986895220.bmp";
        BufferedImage mergedImage = mergeImage(ImageIO.read(new File(first_image)), ImageIO.read(new File(second_image)), 5);
        ImageCommUtil.saveImage(mergedImage, ImageType.BMP, "D:/picture/saved_picture", "merged.bmp");*/

        /*String gray = "D:/image/";
        //BufferedImage image = grayImage(ImageIO.read(new File(gray + "/grey.bmp")));
        BufferedImage image = binaryImage(ImageIO.read(new File(gray + "/grey_result.bmp")));
        saveImage(image, ImageType.BMP, gray, "binary_result_");*/
    }


}
