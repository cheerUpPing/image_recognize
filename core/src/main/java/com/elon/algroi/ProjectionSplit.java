package com.elon.algroi;

import com.elon.entity.ImageType;
import com.elon.util.ImageCommUtil;
import org.apache.commons.lang3.StringUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 投影分割算法(适合源图片文字之间有空白间隙)
 * <p>
 * 适用图片在横向或者竖向可以分割
 */
public class ProjectionSplit {

    /**
     * 水平/垂直统计像素点 的特征
     *
     * @param srcImage       等待统计的图片
     * @param isOnHorizontal true水平统计  false垂直统计
     * @param featureValue   特征值,大于这个值是一个特征，小于这个值是另一个特征
     * @return
     */
    public static List<Integer> calculateBlackPx(BufferedImage srcImage, int featureValue, boolean isOnHorizontal) {
        int width = srcImage.getWidth();
        int height = srcImage.getHeight();
        List<Integer> backCountList = new ArrayList<Integer>();
        for (int i = 0; i < (isOnHorizontal ? height : width); i++) {
            int everyLine = 0;
            for (int j = 0; j < (!isOnHorizontal ? height : width); j++) {
                int x_point = isOnHorizontal ? j : i;
                int y_point = !isOnHorizontal ? j : i;
                int rgb = srcImage.getRGB(x_point, y_point);
                if (ImageCommUtil.isBlack(rgb, featureValue)) {
                    everyLine = everyLine + 1;
                }
            }
            backCountList.add(everyLine);
        }
        System.out.println("图片宽高：" + width + " " + height + (isOnHorizontal ? ",水平" : ",垂直") + "统计,具体特征数值：" + backCountList);
        return backCountList;
    }

    /**
     * 水平方向切割图片并保存
     * 水平方向切割，开始x坐标都是0
     *
     * @param srcImage
     * @param horizontalBackCountList
     * @param validLineIndex          有效的行图片编号(从0开始的)
     * @return
     */
    public static List<BufferedImage> cutImage_horizontal(BufferedImage srcImage, List<Integer> horizontalBackCountList, int[] validLineIndex, ImageType imageType, String rootPath) throws IOException {
        int width = srcImage.getWidth();
        int height = srcImage.getHeight();
        //保存水平分割的图片
        List<BufferedImage> horizontalImages = new ArrayList<BufferedImage>();
        //保存用于分割的y坐标
        List<Integer> yPointList_top = new ArrayList<Integer>();
        List<Integer> yPointList_boom = new ArrayList<Integer>();
        //确定图片上边缘坐标y
        for (int i = 0; i < height; i++) {
            int everyLineBackCount = horizontalBackCountList.get(i);
            if (everyLineBackCount == 0) {
                if (i == height - 1) {
                    yPointList_top.add(i);
                } else {
                    int nextLineBackCount = horizontalBackCountList.get(i + 1);
                    if (nextLineBackCount != 0) {
                        yPointList_top.add(i);
                    }
                }
            }
        }
        //确定图片下边缘坐标y
        for (int i = 0; i < height; i++) {
            int everyLineBackCount = horizontalBackCountList.get(i);
            if (everyLineBackCount != 0) {
                if (i == height - 1) {
                    int backLineBackCount = horizontalBackCountList.get(i - 1);
                    if (backLineBackCount != 0) {
                        yPointList_boom.add(i);
                    }
                } else {
                    int nextLineBackCount = horizontalBackCountList.get(i + 1);
                    if (nextLineBackCount == 0) {
                        yPointList_boom.add(i + 1);
                    }
                }
            }
        }
        //正式分割图片
        for (int i = 0; i < yPointList_boom.size(); i++) {
            int yPoint_top = yPointList_top.get(i);
            int yPoint_boom = yPointList_boom.get(i);
            BufferedImage cutImage = srcImage.getSubimage(0, yPoint_top, width, yPoint_boom - yPoint_top);
            horizontalImages.add(cutImage);
            if ((validLineIndex == null || validLineIndex.length == 0) && imageType != null && StringUtils.isNotEmpty(rootPath)) {
                ImageCommUtil.saveImage(cutImage, imageType, rootPath, System.nanoTime() + "");
            }
        }
        //保存有效的行图片
        if (validLineIndex != null && validLineIndex.length > 0) {
            List<BufferedImage> validLineImages = new ArrayList<>(validLineIndex.length);
            for (int lineIndex : validLineIndex) {
                BufferedImage validLineImage = horizontalImages.get(lineIndex);
                //存在这行对应的图片
                if (validLineImage != null) {
                    validLineImages.add(validLineImage);
                    if (imageType != null && StringUtils.isNotEmpty(rootPath)) {
                        ImageCommUtil.saveImage(validLineImage, imageType, rootPath, System.nanoTime() + "");
                    }
                }
            }
            horizontalImages = validLineImages;
        }
        return horizontalImages;
    }

    /**
     * 水平方向切割图片不保存
     *
     * @param srcImage
     * @param horizontalBackCountList
     * @param validLineIndex          有效的行图片编号(从0开始的)
     * @return
     * @throws IOException
     */
    public static List<BufferedImage> cutImage_horizontal(BufferedImage srcImage, List<Integer> horizontalBackCountList, int[] validLineIndex) throws IOException {
        return cutImage_horizontal(srcImage, horizontalBackCountList, validLineIndex, null, null);
    }

    /**
     * 竖向切割图片并保存
     *
     * @param srcImage     待切割的图片
     * @param fitValue     归一化文字图片到统一的大小(单位:像素)
     * @param featureValue 特征阀值  大于这个特征是一个值,小于这个特征是另一个值
     * @param imageType
     * @param rootPath
     * @return
     * @throws IOException
     */
    public static List<BufferedImage> cutImage_vertical(BufferedImage srcImage, int featureValue, int fitValue, ImageType imageType, String rootPath) throws IOException {
        int width = srcImage.getWidth();
        int height = srcImage.getHeight();
        List<BufferedImage> verticalImages = new ArrayList<BufferedImage>();
        List<Integer> verticalBackCountList = calculateBlackPx(srcImage, featureValue, false);
        //切割照片左边的x坐标
        List<Integer> xPointList_left = new ArrayList<Integer>();
        List<Integer> xPointList_right = new ArrayList<Integer>();
        for (int i = 0; i < width; i++) {
            //每一竖向黑色或深色的数量
            int everyLineBackCount = verticalBackCountList.get(i);
            if (everyLineBackCount == 0) {
                if (i == width - 1) {
                    xPointList_left.add(i);
                } else {
                    int nextLineBackcount = verticalBackCountList.get(i + 1);
                    if (nextLineBackcount != 0) {
                        xPointList_left.add(i);
                    }
                }
            }
        }
        for (int i = 0; i < width; i++) {
            int everyLineBackCount = verticalBackCountList.get(i);
            if (everyLineBackCount != 0) {
                if (i == width - 1) {
                    int backLineBackCount = verticalBackCountList.get(i - 1);
                    if (backLineBackCount != 0) {
                        xPointList_right.add(i);
                    }
                } else {
                    int nextLineBackCount = verticalBackCountList.get(i + 1);
                    if (nextLineBackCount == 0) {
                        xPointList_right.add(i + 1);
                    }
                }
            }
        }

        //正式分割图片
        for (int i = 0; i < xPointList_right.size(); i++) {
            int xPoint_right = xPointList_right.get(i);
            int xPoint_left = xPointList_left.get(i);
            BufferedImage cutImage = srcImage.getSubimage(xPoint_left, 0, xPoint_right - xPoint_left, height);
            //缩放图片
            System.out.println("小图片宽高:" + cutImage.getWidth() + " " + cutImage.getHeight() + ",准备缩放为:" + fitValue);
            cutImage = ImageCommUtil.reSquare(cutImage, fitValue);
            verticalImages.add(cutImage);
            if (imageType != null && StringUtils.isNotEmpty(rootPath)) {
                ImageCommUtil.saveImage(cutImage, imageType, rootPath, System.nanoTime() + "");
            }
        }
        return verticalImages;
    }

    /**
     * 竖向切割图片不保存
     *
     * @param srcImage
     * @param featureValue 特征阀值  大于这个特征是一个值,小于这个特征是另一个值
     * @param fitValue     归一化文字图片到统一的大小(单位:像素)
     * @return
     * @throws IOException
     */
    public static List<BufferedImage> cutImage_vertical(BufferedImage srcImage, int featureValue, int fitValue) throws IOException {
        return cutImage_vertical(srcImage, featureValue, fitValue, null, null);
    }


    /**
     * 大图片切割成文字图片并保存
     * 不按行返回文字图片
     *
     * @param srcImage       大图片
     * @param imageType      为空的话就不保存文字图片
     * @param featureValue   特征阀值  大于这个特征是一个值,小于这个特征是另一个值
     * @param fitValue       归一化文字图片到统一的大小(单位:像素)
     * @param validLineIndex 有效的行图片编号(从0开始的)
     * @param rootPath
     * @return
     * @throws IOException
     */
    public static List<BufferedImage> cutImage_noByLine(BufferedImage srcImage, int featureValue, int fitValue, int[] validLineIndex, ImageType imageType, String rootPath) throws IOException {
        List<BufferedImage> smallImages = new ArrayList<>();
        List<Integer> horizontalBackCountList = calculateBlackPx(srcImage, featureValue, true);
        List<BufferedImage> horizontalImages = cutImage_horizontal(srcImage, horizontalBackCountList, validLineIndex);
        for (int i = 0; i < horizontalImages.size(); i++) {
            BufferedImage everyImage_horizontal = horizontalImages.get(i);
            List<BufferedImage> verticalImages = cutImage_vertical(everyImage_horizontal, featureValue, fitValue, imageType, rootPath);
            smallImages.addAll(verticalImages);
        }
        return smallImages;
    }

    /**
     * 大图片切割成文字图片并保存
     * 按行返回文字图片
     *
     * @param srcImage       大图片
     * @param imageType      为空的话就不保存文字图片
     * @param featureValue   特征阀值  大于这个特征是一个值,小于这个特征是另一个值
     * @param fitValue       归一化文字图片到统一的大小(单位:像素)
     * @param validLineIndex 有效的行图片编号(从0开始的)
     * @param rootPath
     * @return
     * @throws IOException
     */
    public static List<List<BufferedImage>> cutImage_byLine(BufferedImage srcImage, int featureValue, int fitValue, int[] validLineIndex, ImageType imageType, String rootPath) throws IOException {
        List<List<BufferedImage>> smallImages = new ArrayList<>();
        List<Integer> horizontalBackCountList = calculateBlackPx(srcImage, featureValue, true);
        List<BufferedImage> horizontalImages = cutImage_horizontal(srcImage, horizontalBackCountList, validLineIndex);
        for (int i = 0; i < horizontalImages.size(); i++) {
            BufferedImage everyImage_horizontal = horizontalImages.get(i);
            List<BufferedImage> verticalImages = cutImage_vertical(everyImage_horizontal, featureValue, fitValue, imageType, rootPath);
            smallImages.add(verticalImages);
        }
        return smallImages;
    }

    /**
     * 大图片切割成文字图片
     * 按行返回文字图片
     *
     * @param srcImage       大图片
     * @param featureValue   特征阀值  大于这个特征是一个值,小于这个特征是另一个值
     * @param fitValue       归一化文字图片到统一的大小(单位:像素)
     * @param validLineIndex 有效的行图片编号(从0开始的)
     * @return
     * @throws IOException
     */
    public static List<List<BufferedImage>> cutImage_byLine(BufferedImage srcImage, int featureValue, int fitValue, int[] validLineIndex) throws IOException {
        List<List<BufferedImage>> smallImages = new ArrayList<>();
        List<Integer> horizontalBackCountList = calculateBlackPx(srcImage, featureValue, true);
        List<BufferedImage> horizontalImages = cutImage_horizontal(srcImage, horizontalBackCountList, validLineIndex);
        for (int i = 0; i < horizontalImages.size(); i++) {
            BufferedImage everyImage_horizontal = horizontalImages.get(i);
            List<BufferedImage> verticalImages = cutImage_vertical(everyImage_horizontal, featureValue, fitValue, null, null);
            smallImages.add(verticalImages);
        }
        return smallImages;
    }

    /**
     * 大图片切割成文字图片
     * 不按行返回文字图片
     *
     * @param srcImage
     * @param featureValue   特征阀值  大于这个特征是一个值,小于这个特征是另一个值
     * @param fitValue       归一化文字图片到统一的大小(单位:像素)
     * @param validLineIndex 有效的行图片编号(从0开始的)
     * @return
     * @throws IOException
     */
    public static List<BufferedImage> cutImage_noByLine(BufferedImage srcImage, int featureValue, int fitValue, int[] validLineIndex) throws IOException {
        return cutImage_noByLine(srcImage, featureValue, fitValue, validLineIndex, null, null);
    }

    /**
     * 大图片切割为有效区域图片<br/>
     * 不按行返回文字图片
     *
     * @param srcImage                  大图片
     * @param featureValue              特征阀值  大于这个特征是一个值,小于这个特征是另一个值
     * @param fitValue                  归一化文字图片到统一的大小(单位:像素)
     * @param validLineIndexOfValidArea 有效区域图片中的有效行图片(从0开始的)
     * @param xPoint                    切割开始点x坐标
     * @param yPoint                    切割开始点y坐标
     * @param width                     有效图片宽度
     * @param height                    有效图片高度
     * @param imageType
     * @param rootPath
     * @return
     * @throws IOException
     */
    public static List<BufferedImage> cutImage_validArea_noByLine(BufferedImage srcImage, int featureValue, int fitValue, int[] validLineIndexOfValidArea, int xPoint, int yPoint, int width, int height, ImageType imageType, String rootPath) throws IOException {
        BufferedImage targetImage = ImageCommUtil.clipImage(srcImage, xPoint, yPoint, width, height);
        return cutImage_noByLine(targetImage, featureValue, fitValue, validLineIndexOfValidArea, imageType, rootPath);
    }

    /**
     * 大图片切割为有效区域图片<br/>
     * 不按行返回文字图片
     *
     * @param srcImage                  大图片
     * @param featureValue              特征阀值  大于这个特征是一个值,小于这个特征是另一个值
     * @param fitValue                  归一化文字图片到统一的大小(单位:像素)
     * @param validLineIndexOfValidArea 有效区域图片中的有效行图片(从0开始的)
     * @param xPoint                    切割开始点x坐标
     * @param yPoint                    切割开始点y坐标
     * @param width                     有效图片宽度
     * @param height                    有效图片高度
     * @return
     * @throws IOException
     */
    public static List<BufferedImage> cutImage_validArea_noByLine(BufferedImage srcImage, int featureValue, int fitValue, int[] validLineIndexOfValidArea, int xPoint, int yPoint, int width, int height) throws IOException {
        return cutImage_validArea_noByLine(srcImage, featureValue, fitValue, validLineIndexOfValidArea, xPoint, yPoint, width, height, null, null);
    }

    /**
     * 大图片切割为有效区域图片<br/>
     * 不按行返回文字图片
     *
     * @param srcImage                  大图片
     * @param featureValue              特征阀值  大于这个特征是一个值,小于这个特征是另一个值
     * @param fitValue                  归一化文字图片到统一的大小(单位:像素)
     * @param validLineIndexOfValidArea 有效区域图片中的有效行图片(从0开始的)
     * @param xPoint                    切割开始点x坐标
     * @param yPoint                    切割开始点y坐标
     * @param width                     有效图片宽度
     * @param height                    有效图片高度
     * @param imageType
     * @param rootPath
     * @return
     * @throws IOException
     */
    public static List<List<BufferedImage>> cutImage_validArea_byLine(BufferedImage srcImage, int featureValue, int fitValue, int[] validLineIndexOfValidArea, int xPoint, int yPoint, int width, int height, ImageType imageType, String rootPath) throws IOException {
        BufferedImage targetImage = ImageCommUtil.clipImage(srcImage, xPoint, yPoint, width, height);
        return cutImage_byLine(targetImage, featureValue, fitValue, validLineIndexOfValidArea, imageType, rootPath);
    }

    /**
     * 大图片切割为有效区域图片<br/>
     * 不按行返回文字图片
     *
     * @param srcImage                  大图片
     * @param featureValue              特征阀值  大于这个特征是一个值,小于这个特征是另一个值
     * @param fitValue                  归一化文字图片到统一的大小(单位:像素)
     * @param validLineIndexOfValidArea 有效区域图片中的有效行图片(从0开始的)
     * @param xPoint                    切割开始点x坐标
     * @param yPoint                    切割开始点y坐标
     * @param width                     有效图片宽度
     * @param height                    有效图片高度
     * @return
     * @throws IOException
     */
    public static List<List<BufferedImage>> cutImage_validArea_byLine(BufferedImage srcImage, int featureValue, int fitValue, int[] validLineIndexOfValidArea, int xPoint, int yPoint, int width, int height) throws IOException {
        return cutImage_validArea_byLine(srcImage, featureValue, fitValue, validLineIndexOfValidArea, xPoint, yPoint, width, height, null, null);
    }

    public static void main(String[] args) throws IOException {
        String filePath = "d:/picture/11_5/src/version2/7.BMP";
        BufferedImage srcImage = ImageIO.read(new File(filePath));
        List<Integer> horizontalBackCountList = calculateBlackPx(srcImage, 300, true);
        for (int i = 0; i < horizontalBackCountList.size(); i++) {
            System.out.println(i + "---" + horizontalBackCountList.get(i));
        }
        //List<Integer> verticalBackCountList = calculateBlackPx(srcImage, false);
        //cutImage_horizontal(srcImage, horizontalBackCountList);
        //cutImage_horizontal(srcImage, horizontalBackCountList, ImageType.BMP, "d:/picture/saved_picture");
        //filePath = "D:/picture/20171101171217.bmp";
        //srcImage = ImageIO.read(new File(filePath));
        //cutImage_vertical(srcImage, ImageType.BMP, "d:/picture/saved_picture");
        int[] validLineIndex = {4};
        //cutImage_noByLine(srcImage, 500, 46, validLineIndex, ImageType.BMP, "d:/picture/jc/small_image");
        //cutImage_validArea_byLine(srcImage, 500, 46, validLineIndex, 0, 0, srcImage.getWidth(), 300, ImageType.BMP, "d:/picture/11_5/src/");
    }
}
