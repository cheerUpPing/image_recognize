package com.elon.algroi;

import com.elon.entity.ImageType;
import com.elon.entity.Point;
import com.elon.util.ExtreNum;
import com.elon.util.ImageCommUtil;
import org.apache.commons.lang3.StringUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 水滴分割图片
 */
public class WaterDropSplit {


    /**
     * 滴水法入口
     *
     * @param sourceImage
     * @param featureValue     像素特征阀值
     * @param waterWidth       大水滴的宽度 2*B+1,取0或者1效果最好
     * @param minCharWidth     最小字符宽度
     * @param maxCharWidth     最大字符宽度
     * @param averageCharWidth 平均字符宽度
     * @return 切割完图片的数组
     */
    public static List<BufferedImage> waterDrop(BufferedImage sourceImage, int featureValue, int waterWidth, int minCharWidth, int maxCharWidth, int averageCharWidth, ImageType imageType, String rootPath) throws IOException {
        List<BufferedImage> cutImages = new ArrayList<>();
        int width = sourceImage.getWidth();
        int height = sourceImage.getHeight();
        //在x轴的投影
        int[] histData = new int[width];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (ImageCommUtil.isBlack(sourceImage.getRGB(x, y), featureValue)) {
                    histData[x]++;
                }
            }
        }
        List<Integer> extrems = ExtreNum.getMinExtrem(histData);
        Point[] startRoute = new Point[height];
        Point[] endRoute = null;
        for (int y = 0; y < height; y++) {
            startRoute[y] = new Point(0, y);
        }
        int num = (int) Math.round((double) width / averageCharWidth);//字符的个数
        int lastP = 0; //上一次分割的位置
        int curSplit = 1;//分割点的个数，小于等于 num - 1;
        for (int i = 0; i < extrems.size(); i++) {
            if (curSplit > num - 1) {
                break;
            }
            //判断两个分割点之间的距离是否合法
            int curP = extrems.get(i);
            int dBetween = curP - lastP + 1;
            if (dBetween < minCharWidth || dBetween > maxCharWidth) {
                continue;
            }
//			//判断当前分割点与末尾结束点的位置是否合法
//			int dAll = width - curP + 1;
//			if (dAll < minD*(num - curSplit) || dAll > maxD*(num - curSplit)) {
//				continue;
//			}
            endRoute = getRainRoute(sourceImage, featureValue, waterWidth, new Point(curP, 0), height, curSplit, averageCharWidth);
            BufferedImage cutImage = doSplit(sourceImage, featureValue, startRoute, endRoute);
            cutImages.add(cutImage);
            startRoute = endRoute;
            lastP = curP;
            curSplit++;
            //System.out.println(curP);
        }
        endRoute = new Point[height];
        for (int y = 0; y < height; y++) {
            endRoute[y] = new Point(width - 1, y);
        }
        BufferedImage cutImage = doSplit(sourceImage, featureValue, startRoute, endRoute);
        cutImages.add(cutImage);
        for (BufferedImage smallImage : cutImages) {
            System.out.println("图片宽高:" + smallImage.getWidth() + " " + smallImage.getHeight());
            if (imageType != null && StringUtils.isNotEmpty(rootPath)) {
                ImageCommUtil.saveImage(smallImage, imageType, rootPath, System.nanoTime() + "");
            }
        }
        return cutImages;
    }

    /**
     * 滴水法入口
     *
     * @param sourceImage
     * @param featureValue     像素特征阀值
     * @param waterWidth       大水滴的宽度 2*B+1,取0或者1效果最好
     * @param minCharWidth     最小字符宽度
     * @param maxCharWidth     最大字符宽度
     * @param averageCharWidth 平均字符宽度
     * @return 切割完图片的数组
     */
    public static List<BufferedImage> waterDrop(BufferedImage sourceImage, int featureValue, int waterWidth, int minCharWidth, int maxCharWidth, int averageCharWidth) throws IOException {
        return waterDrop(sourceImage, featureValue, waterWidth, minCharWidth, maxCharWidth, averageCharWidth, null, null);
    }

    /**
     * 获得滴水的路径
     *
     * @param startP
     * @param height
     * @return
     */
    private static Point[] getRainRoute(BufferedImage srcImage, int featureValue, int waterWidth, Point startP, int height, int curSplit, int averageCharWidth) {

        //获得分割的路径
        Point[] endRoute = new Point[height];
        Point curP = new Point(startP.x, startP.y);
        Point lastP = curP;
        endRoute[0] = curP;
        while (curP.y < height - 1) {
            int maxW = 0;
            int sum = 0;
            int nextX = curP.x;
            int nextY = curP.y;

            for (int j = 1; j <= 5; j++) {
                try {
                    int curW = getPixelValue(srcImage, featureValue, waterWidth, curP.x, curP.y, j) * (6 - j);
                    sum += curW;
                    if (curW > maxW) {
                        maxW = curW;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    System.exit(0);
                }
            }
            //如果全黑，需要看惯性
            if (sum == 0) {
                maxW = 4;
            }
            //如果周围全白，则默认垂直下落
            if (sum == 15) {
                maxW = 6;
            }
            switch (maxW) {
                case 1:
                    nextX = curP.x - 1;
                    nextY = curP.y;
                    break;
                case 2:
                    nextX = curP.x + 1;
                    nextY = curP.y;
                    break;
                case 3:
                    nextX = curP.x + 1;
                    nextY = curP.y + 1;
                    break;
                case 5:
                    nextX = curP.x - 1;
                    nextY = curP.y + 1;
                    break;
                case 6:
                    nextX = curP.x;
                    nextY = curP.y + 1;
                    break;
                case 4:
                    if (nextX > curP.x) {//具有向右的惯性
                        nextX = curP.x + 1;
                        nextY = curP.y + 1;
                    }

                    if (nextX < curP.x) {//向左的惯性或者sum = 0
                        nextX = curP.x;
                        nextY = curP.y + 1;
                    }

                    if (sum == 0) {
                        nextX = curP.x;
                        nextY = curP.y + 1;
                    }
                    break;

                default:

                    break;
            }
            //如果出现重复运动
            if (lastP.x == nextX && lastP.y == nextY) {
                if (nextX < curP.x) {//向左重复
                    maxW = 5;
                    nextX = curP.x + 1;
                    nextY = curP.y + 1;
                } else {//向右重复
                    maxW = 3;
                    nextX = curP.x - 1;
                    nextY = curP.y + 1;
                }
            }
            lastP = curP;
            int rightLimit = averageCharWidth * curSplit + 1;
            if (nextX > rightLimit) {
                nextX = rightLimit;
                nextY = curP.y + 1;
            }

            int leftLimit = averageCharWidth * (curSplit - 1) + averageCharWidth / 2;
            if (nextX < leftLimit) {
                nextX = leftLimit;
                nextY = curP.y + 1;
            }
            curP = new Point(nextX, nextY);

            endRoute[curP.y] = curP;
        }
        return endRoute;
    }

    /**
     * 具体实行切割
     *
     * @param srcImage 待分割的源图片
     * @param starts
     * @param ends
     */
    private static BufferedImage doSplit(BufferedImage srcImage, int featureValue, Point[] starts, Point[] ends) {
        int left = starts[0].x;
        int top = starts[0].y;
        int right = ends[0].x;
        int bottom = ends[0].y;

        for (int i = 0; i < starts.length; i++) {
            left = Math.min(starts[i].x, left);
            top = Math.min(starts[i].y, top);
            right = Math.max(ends[i].x, right);
            bottom = Math.max(ends[i].y, bottom);
        }

        int width = right - left + 1;
        int height = bottom - top + 1;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.setRGB(x, y, new Color(255, 255, 255).getRGB());
            }
        }
        for (int i = 0; i < ends.length; i++) {
            Point start = starts[i];
            Point end = ends[i];
            for (int x = start.x; x < end.x; x++) {
                if (ImageCommUtil.isBlack(srcImage.getRGB(x, i), featureValue)) {
//					System.out.println((x - left) + ", " + (start.y - top));
                    image.setRGB(x - left, start.y - top, new Color(0, 0, 0).getRGB());
                }
            }

        }
        return image;
    }

    /**
     * 获得大水滴中心点周围的像素值
     *
     * @param cx
     * @param cy
     * @param j  中心点周围的编号
     * @return
     */
    private static int getPixelValue(BufferedImage srcImage, int featureValue, int waterWidth, int cx, int cy, int j) {
        int rgb = 0;

        if (j == 4) {
            int right = cx + waterWidth + 1;
            right = right >= srcImage.getWidth() - 1 ? srcImage.getWidth() - 1 : right;
            rgb = srcImage.getRGB(right, cy);
            return ImageCommUtil.isBlack(rgb, featureValue) ? 0 : 1;
        }

        if (j == 5) {
            int left = cx - waterWidth - 1;
            left = left <= 0 ? 0 : left;
            rgb = srcImage.getRGB(left, cy);
            return ImageCommUtil.isBlack(rgb, featureValue) ? 0 : 1;
        }

        //如果 1<= j <= 3, 则判断下方的区域， 只要有一个黑点，则当做黑点，
        int start = cx - waterWidth + j - 2;
        int end = cx + waterWidth + j - 2;

        start = start <= 0 ? 0 : start;
        end = end >= srcImage.getWidth() - 1 ? srcImage.getWidth() - 1 : end;
        int blackNum = 0;
        int whiteNum = 0;
        for (int i = start; i <= end; i++) {
            rgb = srcImage.getRGB(i, cy + 1);
            if (ImageCommUtil.isBlack(rgb, featureValue)) {
                blackNum++;
            } else {
                whiteNum++;
            }
        }

        return (blackNum >= whiteNum) ? 0 : 1;
    }

    public static void main(String[] args) throws IOException {

        String image = "d:/image/92.jpg";
        String rootPath = "d:/image/small_image";
        BufferedImage src = ImageIO.read(new File(image));
        waterDrop(src,300,0,1,16,12,ImageType.BMP,rootPath);
    }
}
