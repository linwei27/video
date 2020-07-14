package com.morninfo.wenfang.control;

import com.alibaba.fastjson.JSONObject;
import com.morninfo.wenfang.util.ImgBase64;
import okhttp3.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;


@RestController
@RequestMapping("/Videos")
public class Videos {

    //视频文件路径：
    public static String videoPath = "D:/test/video";

    //存放截取视频某一帧的图片
    public static String videoFramesPath = "D:/test/img/";

    /**
     * 将frame转换为bufferedImage对象
     *
     * @param frame
     * @return
     */
    public static BufferedImage FrameToBufferedImage(Frame frame) {
        //创建BufferedImage对象
        Java2DFrameConverter converter = new Java2DFrameConverter();
        BufferedImage bufferedImage = converter.getBufferedImage(frame);
        return bufferedImage;
    }


    /**
     * 获取视频时长和某一帧的截图
     *
     * @param path
     * @param frames
     * @return
     */
    @RequestMapping("/VideoProcessing")
    @ResponseBody
    public String VideoProcessing(
            @RequestParam(value = "path", defaultValue = "") String path,
            @RequestParam(value = "frames", defaultValue = "") String frames
    ) {

        JSONObject object = new JSONObject();

        //Frame对象
        Frame frame = null;
        //标识
        int flag = 0;
        try {
			 /*
            获取视频文件
            */
            FFmpegFrameGrabber fFmpegFrameGrabber = new FFmpegFrameGrabber(path);
            fFmpegFrameGrabber.start();

            //获取视频总帧数
            int ftp = fFmpegFrameGrabber.getLengthInFrames();
            //获取时长
//            String duration = ftp / fFmpegFrameGrabber.getFrameRate() / 60 + "";
            String duration = ftp / fFmpegFrameGrabber.getFrameRate() + "";

            System.out.println("时长 " + duration);

            //放入json对象
            object.put("duration", duration);

            while (flag <= ftp) {
                frame = fFmpegFrameGrabber.grabImage();
				/*
				对视频的第framgs帧进行处理
				 */
                if (frame != null && flag == Integer.parseInt(frames)) {

                    //获取项目路径
                    String userdir = System.getProperty("user.dir");

                    //获取年月日
                    String currentDate = new SimpleDateFormat("yyyyMMdd").format(new Date());

                    //创建文件夹
                    File file = new File(userdir + "/" + currentDate);

                    if (!file.exists() && !file.isDirectory()) {
                        file.mkdir();
                    }

                    //文件绝对路径+名字
                    String fileName = userdir + "/" + currentDate + "/" + UUID.randomUUID().toString() + "_" + flag + ".jpg";

                    //文件储存对象
                    File outPut = new File(fileName);
                    //写入文件
                    ImageIO.write(FrameToBufferedImage(frame), "jpg", outPut);
                    //文件转base64
                    String base64 = ImgBase64.getImgStr(fileName);

                    //放入json对象
                    object.put("base64", base64);

//                    System.out.println(base64);
                    //视频第五帧图的路径
//                    String savedUrl = PropertyPlaceholder.getProperty("img_path") + outPut.getName();
                    //String savedUrl = "D:/tmp"  + "/" + outPut.getName();
                    //videPicture = savedUrl;

                    break;
                }
                flag++;
            }
            fFmpegFrameGrabber.stop();
            fFmpegFrameGrabber.close();
        } catch (Exception E) {
            E.printStackTrace();
        }

        return object.toJSONString();

    }

    /**
     * 文芳发送短信接口
     *
     * @param phone
     * @param msg
     */
    @RequestMapping("/smsSend")
    @ResponseBody
    public String smsSend(
            @RequestParam(value = "phone", defaultValue = "") String phone,
            @RequestParam(value = "msg", defaultValue = "") String msg
    ) {

        //请求内容
        String content = "------WebKitFormBoundary7MA4YWxkTrZu0gW\r\nContent-Disposition: form-data; name=\"conditon\"\r\n\r\n{\"PHONE\":" + "\"" + phone + "\"" + ",\"MSG\":" + "\"" + msg + "\"" + ",\"QX_DM\":\"500108000000\",\"USER_NAME\":\"jb_qb\",\"USER_PASSWORD\":\"jbqb110!@#\"}\r\n------WebKitFormBoundary7MA4YWxkTrZu0gW--";

        System.out.println(content);

        OkHttpClient client = new OkHttpClient();

        MediaType mediaType = MediaType.parse("multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW");
        RequestBody body = RequestBody.create(mediaType, "------WebKitFormBoundary7MA4YWxkTrZu0gW\r\nContent-Disposition: form-data; name=\"conditon\"\r\n\r\n{\"PHONE\":" + "\"" + phone + "\"" + ",\"MSG\":" + "\"" + msg + "\"" + ",\"QX_DM\":\"500108000000\",\"USER_NAME\":\"jb_qb\",\"USER_PASSWORD\":\"jbqb110!@#\"}\r\n------WebKitFormBoundary7MA4YWxkTrZu0gW--");
        Request request = new Request.Builder()
                .url("http://10.154.3.134/ldfwsjjk/xtjk/msg/accept.do")
                .post(body)
                .addHeader("content-type", "multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW")
                .addHeader("cache-control", "no-cache")
                .addHeader("postman-token", "ab130bad-9eb6-f812-e1c6-e91c29f89e7e")
                .build();

        try {
            Response response = client.newCall(request).execute();
            System.out.println(JSONObject.toJSONString(response));
            return JSONObject.toJSONString(response);
        } catch (IOException e) {
            e.printStackTrace();
            return JSONObject.toJSONString(e.getMessage());
        }
    }


}
