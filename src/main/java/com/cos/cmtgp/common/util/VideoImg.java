package com.cos.cmtgp.common.util;




/**
 * @author pengxiangZhao
 * @ClassName: VideoImg
 * @Date 2020/5/22 0022
 */
public class VideoImg {


    /**
     * 截取视频封面
     * @param imgUrl
     * @param videoUrl
     */
    public static void getTempPath(String imgUrl,String videoUrl) {
//        String tempPath=imgUrl;//保存的目标路径
//        File targetFile = new File(tempPath);
//        if (!targetFile.getParentFile().exists()) {
//            targetFile.getParentFile().mkdirs();
//        }
//        try{
//            File file2 = new File(videoUrl);
//            if (file2.exists()) {
//
//                FFmpegFrameGrabber ff = new FFmpegFrameGrabber(file2);
//                ff.start();
//                int ftp = ff.getLengthInFrames();
//                int flag=0;
//                Frame frame = null;
//                while (flag <= ftp) {
//                    //获取帧
//                    frame = ff.grabImage();
//                    //过滤前3帧，避免出现全黑图片
//                    if ((flag>3)&&(frame != null)) {
//                        break;
//                    }
//                    flag++;
//                }
//                ImageIO.write(FrameToBufferedImage(frame), "jpg", targetFile);
//                ff.close();
//                ff.stop();
//            }
//        }catch(Exception e){
//            e.printStackTrace();
//        }
    }
//    private static RenderedImage FrameToBufferedImage(Frame frame) {
//        //创建BufferedImage对象
//        Java2DFrameConverter converter = new Java2DFrameConverter();
//        BufferedImage bufferedImage = converter.getBufferedImage(frame);
//        return bufferedImage;
//    }
}
