package com.zzw.chatserver.controller;

import com.zzw.chatserver.common.R;
import com.zzw.chatserver.filter.SensitiveFilter;
import com.zzw.chatserver.pojo.FeedBack;
import com.zzw.chatserver.pojo.SensitiveMessage;
import com.zzw.chatserver.pojo.User;
import com.zzw.chatserver.pojo.vo.FeedBackResultVo;
import com.zzw.chatserver.pojo.vo.SensitiveMessageResultVo;
import com.zzw.chatserver.pojo.vo.SystemUserResponseVo;
import com.zzw.chatserver.service.OnlineUserService;
import com.zzw.chatserver.service.SysService;
import com.zzw.chatserver.service.UserService;
import com.zzw.chatserver.utils.FastDFSUtil;
import com.zzw.chatserver.utils.LocalFileUtil;
import com.zzw.chatserver.utils.SystemUtil;
import org.apache.commons.io.IOUtils;
import org.csource.common.MyException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/sys")
public class SysController {

    private static final int FACE_IMAGE_COUNT = 6;

    @Resource
    private SysService sysService;

    @Value("${fastdfs.nginx.host}")
    private String nginxHost;

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Value("${file.base-url:http://localhost:5555/chat}")
    private String fileBaseUrl;

    @Resource
    private SensitiveFilter sensitiveFilter;

    @Resource
    private UserService userService;

    @Resource
    private OnlineUserService onlineUserService;


    /**
     * 获取注册时的头像列表
     */
    @GetMapping("/getFaceImages")
    @ResponseBody
    public R getFaceImages() {
        //String path = ClassUtils.getDefaultClassLoader().getResource("").getPath() + "static/face";
        //System.out.println(path);
        ArrayList<String> files = new ArrayList<>();
        //File file = new File(path);
        /*for (File item : Objects.requireNonNull(file.listFiles())) {
            files.add(item.getName());
        }*/
        for (int i = 1; i <= FACE_IMAGE_COUNT; i++) {
            files.add("face" + i + ".jpg");
        }
        return R.ok().data("files", files);
    }

    /**
     * 获取系统用户
     */
    @GetMapping("/getSysUsers")
    @ResponseBody
    public R getSysUsers() {
        List<SystemUserResponseVo> sysUsers = sysService.getSysUsers();
        // System.out.println("系统用户有：" + sysUsers);
        return R.ok().data("sysUsers", sysUsers);
    }

    /**
     * 上传文件
     */
    @PostMapping("/uploadFile")
    @ResponseBody
    public R uploadFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return R.error().message("文件不能为空");
        }
        try {
            // 优先走 FastDFS；本地开发未配置 FastDFS 时自动回退到本地磁盘
            String filePartName = FastDFSUtil.uploadFile(file);
            String filePath = nginxHost + filePartName;
            return R.ok().data("filePath", filePath);
        } catch (Exception fastDfsError) {
            try {
                String absoluteUploadDir = LocalFileUtil.resolveUploadDir(uploadDir);
                String filePath = LocalFileUtil.upload(file, absoluteUploadDir, fileBaseUrl);
                return R.ok().data("filePath", filePath);
            } catch (IOException localError) {
                localError.printStackTrace();
                return R.error().message("文件上传失败：" + localError.getMessage());
            }
        }
    }

    //获取文件的真实地址，主要用于防盗链
    /*@GetMapping("/getRealFilePath")
    public R getRealFilePath(String fileId) throws UnsupportedEncodingException, NoSuchAlgorithmException, MyException {
        String fileUrl = FastDFSUtil.getToken(fileId);
        System.out.println("返回的真实路径为：" + fileUrl);
        return R.ok().data("realFilePath", fileUrl);
    }*/

    /**
     * 历史记录中的文件下载接口
     * 前端从消息中的 FastDFS 路径截取 fileId，下载时使用 fileRawName 作为保存文件名
     */
    @GetMapping("/downloadFile")
    public void downloadFile(@RequestParam("fileId") String fileId,
                             @RequestParam("fileName") String fileName,
                             HttpServletResponse resp) {
        try {
            // 从 FastDFS 读取文件二进制内容
            byte[] bytes = FastDFSUtil.downloadFile(fileId);
            resp.setCharacterEncoding("UTF-8");
            // attachment 触发浏览器下载，而非直接在页面打开
            resp.setHeader("Content-disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8"));
            ServletOutputStream outputStream = resp.getOutputStream();
            IOUtils.write(bytes, outputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 搜索好友或加过的群聊列表
     */
    /*@GetMapping("/topSearch")
    @ResponseBody
    public R topSearch(String keyword) {

        return R.ok();
    }*/

    /**
     * 用户反馈
     */
    @PostMapping("/addFeedBack")
    @ResponseBody
    public R addFeedBack(@RequestBody FeedBack feedBack) {
        // System.out.println("反馈请求参数为：" + feedBack);
        sysService.addFeedBack(feedBack);
        return R.ok().message("感谢您的反馈！");
    }

    /**
     * 过滤发送的消息
     */
    @PostMapping("/filterMessage")
    @ResponseBody
    public R filterMessage(@RequestBody SensitiveMessage sensitiveMessage) {
        String[] res = sensitiveFilter.filter(sensitiveMessage.getMessage());
        String filterContent = "";
        if (res != null) {
            filterContent = res[0];
            if (res[1].equals("1")) {
                //判断出敏感词，插入到敏感词表中
                sysService.addSensitiveMessage(sensitiveMessage);
            }
        }
        return R.ok().data("message", filterContent);
    }

    /**
     * 获取系统cpu、内存使用率
     */
    @GetMapping("/sysSituation")
    @ResponseBody
    public R getSysInfo() {
        double cpuUsage = SystemUtil.getSystemCpuLoad();
        double memUsage = SystemUtil.getSystemMemLoad();
        return R.ok().data("cpuUsage", cpuUsage).data("memUsage", memUsage);
    }

    /**
     * 获取所有用户信息
     */
    @GetMapping("/getAllUser")
    @ResponseBody
    public R getAllUser() {
        List<User> userList = userService.getUserList();
        return R.ok().data("userList", userList);
    }

    /**
     * 根据注册时间获取用户
     */
    @GetMapping("/getUsersBySignUpTime")
    @ResponseBody
    public R getUsersBySignUpTime(String lt, String rt) {
        List<User> userList = userService.getUsersBySignUpTime(lt, rt);
        return R.ok().data("userList", userList);
    }

    /**
     * 获取在线用户个数
     */
    @GetMapping("/countOnlineUser")
    @ResponseBody
    public R getOnlineUserNums() {
        int onlineUserCount = onlineUserService.countOnlineUser();
        return R.ok().data("onlineUserCount", onlineUserCount);
    }

    /**
     * 更改用户状态
     */
    @GetMapping("/changeUserStatus")
    @ResponseBody
    public R changeUserStatus(String uid, Integer status) {
        userService.changeUserStatus(uid, status);
        return R.ok();
    }

    /**
     * 获取所有敏感消息列表
     */
    @GetMapping("/getSensitiveMessageList")
    @ResponseBody
    public R getSensitiveMessageList() {
        List<SensitiveMessageResultVo> sensitiveMessageList = sysService.getSensitiveMessageList();
        return R.ok().data("sensitiveMessageList", sensitiveMessageList);
    }

    /**
     * 获取所有反馈记录列表
     */
    @GetMapping("/getFeedbackList")
    @ResponseBody
    public R getFeedbackList() {
        List<FeedBackResultVo> feedbackList = sysService.getFeedbackList();
        return R.ok().data("feedbackList", feedbackList);
    }
}
