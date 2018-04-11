package com.elon.predo;

import com.elon.algroi.ProjectionSplit;
import com.elon.entity.ImageType;
import com.elon.entity.Version;
import com.elon.util.ImageCommUtil;
import org.apache.commons.lang3.StringUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * 11选5图片预处理类
 */
public class PreDo_11_5 {


    /**
     * 预处理11选5图片
     * 保存合并好的图片
     *
     * @param srcImage
     * @param featureValue
     * @param version
     * @return
     * @throws IOException
     */
    public static BufferedImage clipValidImage(BufferedImage srcImage, int featureValue, Version version, ImageType imageType, String rootPath) throws IOException {
        BufferedImage resultImage = null;
        if (version.equals(Version.V2)) {
            int validHeight = 310;
            BufferedImage validImage = ImageCommUtil.clipImage(srcImage, 0, 0, srcImage.getWidth(), validHeight);
            List<Integer> horizontalBackCountList = ProjectionSplit.calculateBlackPx(validImage, featureValue, true);
            List<BufferedImage> images_horizontal = ProjectionSplit.cutImage_horizontal(validImage, horizontalBackCountList, null);
            BufferedImage image_pass = images_horizontal.get(1);
            BufferedImage image_pass_of_delete_eng = ImageCommUtil.clipImage(image_pass, 0, 0, 495, image_pass.getHeight());
            images_horizontal.remove(1);
            images_horizontal.add(1, image_pass_of_delete_eng);
            BufferedImage[] images = new BufferedImage[images_horizontal.size()];
            images = images_horizontal.toArray(images);
            resultImage = ImageCommUtil.mergeImages(images, 2);
            if (imageType != null && StringUtils.isNotEmpty(rootPath)) {
                ImageCommUtil.saveImage(resultImage, imageType, rootPath, System.nanoTime() + "");

            }
        } else if (version.equals(Version.V3)) {

        }
        return resultImage;
    }

    /**
     * 预处理11选5图片
     * 不保存合并好的图片
     *
     * @param srcImage
     * @param featureValue
     * @param version
     * @return
     * @throws IOException
     */
    public static BufferedImage clipValidImage(BufferedImage srcImage, int featureValue, Version version) throws IOException {
        return clipValidImage(srcImage, featureValue, version, null, null);
    }

    public static void main(String[] args) throws IOException {
        String imagePath = "d:/picture/11_5/src/version2/7.BMP";
        BufferedImage srcImage = ImageIO.read(new File(imagePath));
        BufferedImage mergedImage = clipValidImage(srcImage, 300, Version.V2);
    }

}
