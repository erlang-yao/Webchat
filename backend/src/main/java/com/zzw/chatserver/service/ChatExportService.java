package com.zzw.chatserver.service;

import com.zzw.chatserver.pojo.vo.ExportRequestVo;
import com.zzw.chatserver.utils.FastDFSUtil;
import com.zzw.chatserver.utils.LocalFileUtil;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 聊天记录导出服务
 * 从 MongoDB 获取消息，下载媒体文件，生成 ZIP 包
 */
@Service
public class ChatExportService {
    private static final Logger logger = LoggerFactory.getLogger(ChatExportService.class);
    /** 最大导出消息数量 */
    private static final int MAX_MESSAGES = 5000;
    /** 单个媒体文件最大体积 50MB */
    private static final long MAX_MEDIA_SIZE = 50 * 1024 * 1024;

    @Resource
    private MongoTemplate mongoTemplate;

    @Value("${file.base-url:http://localhost:5555/chat}")
    private String fileBaseUrl;

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    // ────────────────────────── 消息内部表示 ──────────────────────────

    private static class MsgInfo {
        String senderNickname;
        Date time;
        String message;       // 文本内容或媒体 URL/fileId
        String messageType;   // text/img/file/voice/video/audio/sys/emoji
        String fileRawName;   // 媒体文件原始名称
        String inlineDataUri; // 导出 HTML 内联数据，优先用于图片/语音

        boolean isMediaType() {
            return "img".equals(messageType) || "file".equals(messageType)
                    || "voice".equals(messageType) || "video".equals(messageType);
        }

        String typeFolder() {
            switch (messageType) {
                case "img":   return "images";
                case "voice": return "voices";
                case "video": return "videos";
                case "file":  return "files";
                default:      return null;
            }
        }
    }

    // ────────────────────────── 主入口 ──────────────────────────

    /**
     * 导出聊天记录为 ZIP 字节数组
     */
    public byte[] exportChatHistory(ExportRequestVo request) {
        // 1. 从 MongoDB 拉取消息
        List<MsgInfo> allMessages = fetchMessages(request);

        if (allMessages.isEmpty()) {
            logger.warn("未查询到任何消息，roomId={}", request.getRoomId());
            return createEmptyZip(request.getConversationName());
        }

        // 2. 按发送者分组
        Map<String, List<MsgInfo>> bySender = new LinkedHashMap<>();
        for (MsgInfo msg : allMessages) {
            String sender = msg.senderNickname != null && !msg.senderNickname.isEmpty()
                    ? msg.senderNickname
                    : "未知用户";
            bySender.computeIfAbsent(sender, k -> new ArrayList<>()).add(msg);
        }

        // 3. 收集错误备注
        List<String> downloadErrors = new ArrayList<>();

        // 4. 生成 ZIP
        String rootFolder = sanitizeName(request.getConversationName()) + "_聊天记录";
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            // 4a. 文本消息 + 媒体文件
            for (Map.Entry<String, List<MsgInfo>> entry : bySender.entrySet()) {
                String senderName = entry.getKey();
                List<MsgInfo> senderMsgs = entry.getValue();
                String senderFolder = rootFolder + "/" + sanitizeName(senderName);

                // 收集该发送者的文本消息
                List<MsgInfo> textMsgs = new ArrayList<>();
                for (MsgInfo msg : senderMsgs) {
                    if (msg.isMediaType()) {
                        downloadAndAddMedia(zos, senderFolder, msg, downloadErrors);
                    } else {
                        textMsgs.add(msg);
                    }
                }

                // 写出文本文件
                if (!textMsgs.isEmpty()) {
                    String textContent = buildTextFile(textMsgs);
                    addZipEntry(zos, senderFolder + "/文本消息.txt", textContent.getBytes("UTF-8"));
                }
            }

            // 4b. index.html
            String html = buildIndexHtml(allMessages, request.getConversationName(), downloadErrors);
            addZipEntry(zos, rootFolder + "/index.html", html.getBytes("UTF-8"));

            zos.finish();
        } catch (IOException e) {
            logger.error("生成 ZIP 文件失败", e);
            throw new RuntimeException("生成 ZIP 文件失败: " + e.getMessage(), e);
        }

        return baos.toByteArray();
    }

    // ────────────────────────── MongoDB 查询 ──────────────────────────

    private List<MsgInfo> fetchMessages(ExportRequestVo request) {
        String collection = "GROUP".equalsIgnoreCase(request.getConversationType())
                ? "groupmessages"
                : "singlemessages";

        Criteria criteria = Criteria.where("roomId").is(request.getRoomId());

        // 日期范围过滤
        if (request.getStartDate() != null || request.getEndDate() != null) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                if (request.getStartDate() != null) {
                    Date start = sdf.parse(request.getStartDate());
                    criteria.and("time").gte(start);
                }
                if (request.getEndDate() != null) {
                    Date end = sdf.parse(request.getEndDate());
                    // 结束日期扩展到当天 23:59:59
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(end);
                    cal.add(Calendar.DATE, 1);
                    criteria.and("time").lt(cal.getTime());
                }
            } catch (Exception e) {
                logger.warn("日期解析失败，忽略日期过滤: {}", e.getMessage());
            }
        }

        // 消息类型过滤
        if (request.getTypes() != null && !request.getTypes().isEmpty()) {
            criteria.and("messageType").in(request.getTypes());
        }

        Query query = new Query(criteria);
        query.with(Sort.by(Sort.Direction.ASC, "_id"));
        query.limit(MAX_MESSAGES);

        List<Document> docs = mongoTemplate.find(query, Document.class, collection);
        List<MsgInfo> result = new ArrayList<>(docs.size());

        for (Document doc : docs) {
            MsgInfo info = new MsgInfo();
            info.senderNickname = doc.getString("senderNickname");
            info.message = doc.getString("message");
            info.messageType = doc.getString("messageType");
            info.fileRawName = doc.getString("fileRawName");
            info.time = doc.getDate("time");
            result.add(info);
        }

        logger.info("从集合 {} 中查询到 {} 条消息，roomId={}", collection, result.size(), request.getRoomId());
        return result;
    }

    // ────────────────────────── 媒体下载 ──────────────────────────

    /**
     * 下载媒体文件并添加到 ZIP
     */
    private void downloadAndAddMedia(ZipOutputStream zos, String senderFolder,
                                     MsgInfo msg, List<String> errors) {
        if (msg.message == null || msg.message.isEmpty()) return;

        byte[] fileBytes = null;
        try {
            fileBytes = downloadMedia(msg.message);
        } catch (Exception e) {
            String note = "下载失败 [" + msg.fileRawName + "]: " + e.getMessage();
            logger.warn(note);
            errors.add(note);
            return;
        }

        if (fileBytes == null || fileBytes.length == 0) return;
        if (fileBytes.length > MAX_MEDIA_SIZE) {
            errors.add("文件过大已跳过 [" + msg.fileRawName + "]: " + (fileBytes.length / 1024 / 1024) + "MB");
            return;
        }

        // 图片/语音优先内联到 HTML，避免导出包外部路径失效
        msg.inlineDataUri = buildInlineDataUri(msg, fileBytes);

        // 生成媒体文件名
        String ext = extractExt(msg);
        String ts = formatTimestamp(msg.time);
        String safeName = sanitizeName(msg.fileRawName != null ? msg.fileRawName : "file");
        // 如果 safeName 没有扩展名则补上
        if (!safeName.contains(".") && ext != null) {
            safeName = safeName + "." + ext;
        }
        String entryName = senderFolder + "/" + msg.typeFolder() + "/" + ts + "_" + safeName;

        try {
            addZipEntry(zos, entryName, fileBytes);
        } catch (IOException e) {
            errors.add("写入ZIP失败 [" + msg.fileRawName + "]: " + e.getMessage());
        }
    }

    /**
     * 下载媒体文件字节
     */
    private byte[] downloadMedia(String urlOrFileId) throws Exception {
        if (urlOrFileId == null || urlOrFileId.trim().isEmpty()) {
            return null;
        }

        String normalized = urlOrFileId.trim();
        if (isDirectLocalPath(normalized)) {
            return downloadFromLocalPath(normalized);
        }

        if (normalized.startsWith("http://") || normalized.startsWith("https://")) {
            // 本地文件 URL
            if (normalized.contains("/uploads/")) {
                return downloadFromLocal(normalized);
            }
            // FastDFS 完整 URL（nginxHost + fileId）
            String fileId = extractFastDfsFileId(normalized);
            if (fileId != null) {
                try {
                    return FastDFSUtil.downloadFile(fileId);
                } catch (Exception e) {
                    // FastDFS 不可用时尝试 HTTP 下载
                    return downloadViaHttp(normalized);
                }
            }
            // 纯 HTTP 下载
            return downloadViaHttp(normalized);
        }
        // 纯 FastDFS fileId
        return FastDFSUtil.downloadFile(normalized);
    }

    /** 从本地磁盘读取文件 */
    private byte[] downloadFromLocal(String url) throws IOException {
        // URL 格式: https://webchat.beer/chat/uploads/image/uuid.jpg
        // 去掉 baseUrl 前缀得到相对路径
        String pathPart = url;
        // 尝试去掉 baseUrl
        String normalizedBase = fileBaseUrl.endsWith("/") ? fileBaseUrl.substring(0, fileBaseUrl.length() - 1) : fileBaseUrl;
        if (pathPart.startsWith(normalizedBase)) {
            pathPart = pathPart.substring(normalizedBase.length());
        } else {
            // 回退：提取 /uploads/ 之后的部分
            int idx = pathPart.indexOf("/uploads/");
            if (idx >= 0) {
                pathPart = pathPart.substring(idx);
            }
        }
        // 去掉开头的 /
        if (pathPart.startsWith("/")) pathPart = pathPart.substring(1);

        // 构建绝对路径：优先按配置的上传目录解析
        Path uploadRoot = Paths.get(LocalFileUtil.resolveUploadDir(uploadDir)).toAbsolutePath().normalize();
        String relativePath = pathPart;
        if (relativePath.startsWith("uploads/")) {
            relativePath = relativePath.substring("uploads/".length());
        }
        Path localFile = uploadRoot.resolve(relativePath).normalize();
        if (!localFile.startsWith(uploadRoot)) {
            throw new IOException("Invalid local file path");
        }
        return Files.readAllBytes(localFile);
    }

    /** 直接本地绝对路径/相对 uploads 路径 */
    private byte[] downloadFromLocalPath(String pathStr) throws IOException {
        Path uploadRoot = Paths.get(LocalFileUtil.resolveUploadDir(uploadDir)).toAbsolutePath().normalize();
        Path candidate = resolveLocalCandidate(pathStr, uploadRoot);
        if (candidate != null && Files.exists(candidate)) {
            return Files.readAllBytes(candidate);
        }

        throw new IOException("Local file not found: " + pathStr);
    }

    private boolean isDirectLocalPath(String value) {
        return value.startsWith("/")
                || value.matches("^[A-Za-z]:[\\\\/].*")
                || value.startsWith("uploads/")
                || value.startsWith("uploads\\");
    }

    private Path resolveLocalCandidate(String pathStr, Path uploadRoot) {
        String normalized = pathStr.replace('\\', '/');
        if (normalized.startsWith("/www/wwwroot/uploads/")) {
            String relative = normalized.substring("/www/wwwroot/uploads/".length());
            Path fallback = uploadRoot.resolve(relative).normalize();
            if (fallback.startsWith(uploadRoot)) {
                return fallback;
            }
        }

        int uploadsIndex = normalized.indexOf("/uploads/");
        if (uploadsIndex >= 0) {
            String relative = normalized.substring(uploadsIndex + "/uploads/".length());
            Path fallback = uploadRoot.resolve(relative).normalize();
            if (fallback.startsWith(uploadRoot)) {
                return fallback;
            }
        }

        Path candidate = Paths.get(pathStr);
        if (!candidate.isAbsolute()) {
            return uploadRoot.resolve(pathStr).normalize();
        }

        candidate = candidate.toAbsolutePath().normalize();
        if (Files.exists(candidate)) {
            return candidate;
        }

        return candidate;
    }

    /** 通过 HTTP 下载文件 */
    private byte[] downloadViaHttp(String urlStr) throws IOException {
        URL url = new URL(urlStr);
        URLConnection conn = url.openConnection();
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(30000);
        try (InputStream is = conn.getInputStream();
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            byte[] buf = new byte[8192];
            int len;
            while ((len = is.read(buf)) != -1) {
                bos.write(buf, 0, len);
            }
            return bos.toByteArray();
        }
    }

    /** 从 FastDFS 完整 URL 中提取 fileId */
    private String extractFastDfsFileId(String url) {
        try {
            // URL 格式: https://xxxxx/group1/M00/00/00/xxx.jpg
            String path = new URL(url).getPath(); // /group1/M00/00/00/xxx.jpg
            if (path != null && path.startsWith("/")) {
                path = path.substring(1); // group1/M00/00/00/xxx.jpg
            }
            if (path != null && path.contains("group")) {
                return path;
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    /** 从 message URL 或 fileRawName 中提取扩展名 */
    private String extractExt(MsgInfo msg) {
        // 优先从 fileRawName 取
        if (msg.fileRawName != null && msg.fileRawName.contains(".")) {
            return msg.fileRawName.substring(msg.fileRawName.lastIndexOf(".") + 1).toLowerCase();
        }
        // 从 message URL 取
        if (msg.message != null && msg.message.contains(".")) {
            String lower = msg.message.toLowerCase();
            // 取最后一个 . 之后、? 之前的部分
            int dotIdx = lower.lastIndexOf(".");
            int qIdx = lower.indexOf("?", dotIdx);
            if (qIdx > 0) {
                return lower.substring(dotIdx + 1, qIdx);
            }
            return lower.substring(dotIdx + 1);
        }
        // 默认扩展名
        switch (msg.messageType) {
            case "img":   return "jpg";
            case "voice": return "webm";
            case "video": return "mp4";
            default:      return "bin";
        }
    }

    // ────────────────────────── 文本文件生成 ──────────────────────────

    private String buildTextFile(List<MsgInfo> messages) {
        StringBuilder sb = new StringBuilder();
        sb.append("========================================\n");
        sb.append("         聊天消息记录\n");
        sb.append("========================================\n\n");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (MsgInfo msg : messages) {
            sb.append("[").append(sdf.format(msg.time)).append("]\n");
            sb.append(msg.message != null ? msg.message : "").append("\n");
            sb.append("----------------------------------------\n");
        }
        return sb.toString();
    }

    // ────────────────────────── index.html 生成 ──────────────────────────

    private String buildIndexHtml(List<MsgInfo> allMessages, String conversationName,
                                  List<String> errors) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        StringBuilder sb = new StringBuilder();

        sb.append("<!DOCTYPE html>\n<html lang=\"zh-CN\">\n<head>\n");
        sb.append("<meta charset=\"UTF-8\">\n");
        sb.append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
        sb.append("<title>").append(escapeHtml(conversationName)).append(" - 聊天记录</title>\n");
        sb.append("<style>\n");
        sb.append("*{margin:0;padding:0;box-sizing:border-box;}\n");
        sb.append("body{font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',sans-serif;background:#f5f6fa;color:#2c3e50;padding:20px;}\n");
        sb.append(".container{max-width:900px;margin:0 auto;background:#fff;border-radius:12px;box-shadow:0 2px 12px rgba(0,0,0,.08);overflow:hidden;}\n");
        sb.append(".header{background:linear-gradient(135deg,#667eea,#764ba2);color:#fff;padding:30px;text-align:center;}\n");
        sb.append(".header h1{font-size:24px;margin-bottom:8px;}\n");
        sb.append(".header p{opacity:.85;font-size:14px;}\n");
        sb.append(".summary{padding:20px 30px;background:#fafbfc;border-bottom:1px solid #eee;display:flex;gap:24px;flex-wrap:wrap;}\n");
        sb.append(".stat{text-align:center;}\n");
        sb.append(".stat .num{font-size:28px;font-weight:700;color:#667eea;}\n");
        sb.append(".stat .label{font-size:12px;color:#888;margin-top:4px;}\n");
        sb.append(".msg-list{padding:0;}\n");
        sb.append(".msg-item{display:flex;padding:16px 30px;border-bottom:1px solid #f0f0f0;gap:14px;transition:background .15s;}\n");
        sb.append(".msg-item:hover{background:#fafbfc;}\n");
        sb.append(".avatar{width:40px;height:40px;border-radius:50%;background:#e0e3eb;display:flex;align-items:center;justify-content:center;font-size:16px;color:#667eea;flex-shrink:0;font-weight:700;}\n");
        sb.append(".msg-body{flex:1;min-width:0;}\n");
        sb.append(".msg-meta{display:flex;justify-content:space-between;margin-bottom:6px;}\n");
        sb.append(".msg-sender{font-weight:600;color:#333;}\n");
        sb.append(".msg-time{font-size:12px;color:#aaa;}\n");
        sb.append(".msg-content{line-height:1.7;word-break:break-word;}\n");
        sb.append(".msg-content img{max-width:300px;max-height:300px;border-radius:8px;cursor:pointer;border:1px solid #eee;}\n");
        sb.append(".msg-content a{color:#667eea;text-decoration:none;}\n");
        sb.append(".msg-content a:hover{text-decoration:underline;}\n");
        sb.append(".msg-content audio{margin:4px 0;}\n");
        sb.append(".msg-content video{max-width:400px;max-height:300px;border-radius:8px;}\n");
        sb.append(".type-badge{display:inline-block;padding:1px 8px;border-radius:10px;font-size:11px;margin-left:6px;}\n");
        sb.append(".badge-img{background:#e3f2fd;color:#1976d2;}\n");
        sb.append(".badge-voice{background:#fce4ec;color:#c2185b;}\n");
        sb.append(".badge-video{background:#fff3e0;color:#e65100;}\n");
        sb.append(".badge-file{background:#e8f5e9;color:#2e7d32;}\n");
        sb.append(".errors{padding:20px 30px;background:#fff3e0;border-top:2px solid #ff9800;}\n");
        sb.append(".errors h3{color:#e65100;margin-bottom:8px;font-size:14px;}\n");
        sb.append(".errors li{font-size:12px;color:#bf360c;margin-left:20px;margin-bottom:2px;}\n");
        sb.append("@media(max-width:640px){.msg-item{padding:12px 16px;} .header{padding:20px;} .summary{padding:16px 20px;}}\n");
        sb.append("</style>\n</head>\n<body>\n<div class=\"container\">\n");

        // 头部
        sb.append("<div class=\"header\">\n");
        sb.append("<h1>").append(escapeHtml(conversationName)).append(" - 聊天记录</h1>\n");
        sb.append("<p>导出时间: ").append(sdf.format(new Date())).append("</p>\n");
        sb.append("</div>\n");

        // 统计
        long textCount = allMessages.stream().filter(m -> !m.isMediaType()).count();
        long imgCount  = allMessages.stream().filter(m -> "img".equals(m.messageType)).count();
        long fileCount = allMessages.stream().filter(m -> "file".equals(m.messageType)).count();
        long voiceCount= allMessages.stream().filter(m -> "voice".equals(m.messageType)).count();
        Set<String> senders = new HashSet<>();
        allMessages.forEach(m -> senders.add(m.senderNickname));
        sb.append("<div class=\"summary\">\n");
        sb.append("<div class=\"stat\"><div class=\"num\">").append(allMessages.size()).append("</div><div class=\"label\">消息总数</div></div>\n");
        sb.append("<div class=\"stat\"><div class=\"num\">").append(senders.size()).append("</div><div class=\"label\">参与人数</div></div>\n");
        sb.append("<div class=\"stat\"><div class=\"num\">").append(textCount).append("</div><div class=\"label\">文本消息</div></div>\n");
        sb.append("<div class=\"stat\"><div class=\"num\">").append(imgCount).append("</div><div class=\"label\">图片</div></div>\n");
        sb.append("<div class=\"stat\"><div class=\"num\">").append(fileCount).append("</div><div class=\"label\">文件</div></div>\n");
        sb.append("<div class=\"stat\"><div class=\"num\">").append(voiceCount).append("</div><div class=\"label\">语音</div></div>\n");
        sb.append("</div>\n");

        // 消息列表
        sb.append("<div class=\"msg-list\">\n");
        for (MsgInfo msg : allMessages) {
            String initial = (msg.senderNickname != null && !msg.senderNickname.isEmpty())
                    ? msg.senderNickname.substring(0, 1) : "?";

            sb.append("<div class=\"msg-item\">\n");
            sb.append("<div class=\"avatar\">").append(escapeHtml(initial)).append("</div>\n");
            sb.append("<div class=\"msg-body\">\n");
            sb.append("<div class=\"msg-meta\">");
            sb.append("<span class=\"msg-sender\">").append(escapeHtml(msg.senderNickname)).append("</span>");
            sb.append("<span class=\"msg-time\">").append(sdf.format(msg.time)).append("</span>");
            sb.append("</div>\n");
            sb.append("<div class=\"msg-content\">");

            if (msg.isMediaType()) {
                String badgeClass = "";
                String badgeLabel = "";
                switch (msg.messageType) {
                    case "img":   badgeClass = "badge-img";   badgeLabel = "图片"; break;
                    case "voice": badgeClass = "badge-voice"; badgeLabel = "语音"; break;
                    case "video": badgeClass = "badge-video"; badgeLabel = "视频"; break;
                    case "file":  badgeClass = "badge-file";  badgeLabel = "文件"; break;
                }
                String senderFolder = sanitizeName(msg.senderNickname != null ? msg.senderNickname : "未知用户");
                String ts = formatTimestamp(msg.time);
                String safeName = sanitizeName(msg.fileRawName != null ? msg.fileRawName : "file");
                if (!safeName.contains(".")) {
                    safeName = safeName + "." + extractExt(msg);
                }
                String relPath = senderFolder + "/" + msg.typeFolder() + "/" + ts + "_" + safeName;

                sb.append("<span class=\"type-badge ").append(badgeClass).append("\">").append(badgeLabel).append("</span> ");

                switch (msg.messageType) {
                    case "img":
                        sb.append("<br><img src=\"")
                                .append(escapeHtml(msg.inlineDataUri != null ? msg.inlineDataUri : relPath))
                                .append("\" alt=\"图片\" loading=\"lazy\">");
                        break;
                    case "voice":
                        sb.append("<audio src=\"")
                                .append(escapeHtml(msg.inlineDataUri != null ? msg.inlineDataUri : relPath))
                                .append("\" controls preload=\"none\"></audio>");
                        break;
                    case "video":
                        sb.append("<video src=\"").append(relPath).append("\" controls preload=\"none\"></video>");
                        break;
                    case "file":
                        sb.append("<a href=\"").append(relPath).append("\" download>")
                                .append(escapeHtml(msg.fileRawName != null ? msg.fileRawName : "文件")).append("</a>");
                        break;
                }
            } else {
                // 文本消息
                sb.append(escapeHtml(msg.message != null ? msg.message : ""));
                if ("sys".equals(msg.messageType)) {
                    sb.append(" <span style=\"color:#999;font-size:11px;\">[系统消息]</span>");
                }
            }

            sb.append("</div>\n</div>\n</div>\n");
        }
        sb.append("</div>\n");

        // 错误信息
        if (!errors.isEmpty()) {
            sb.append("<div class=\"errors\">\n");
            sb.append("<h3>⚠ 以下文件下载失败（已跳过）</h3>\n<ul>\n");
            for (String err : errors) {
                sb.append("<li>").append(escapeHtml(err)).append("</li>\n");
            }
            sb.append("</ul>\n</div>\n");
        }

        sb.append("</div>\n</body>\n</html>");
        return sb.toString();
    }

    // ────────────────────────── 工具方法 ──────────────────────────

    /** 向 ZIP 添加一个条目 */
    private void addZipEntry(ZipOutputStream zos, String entryPath, byte[] content) throws IOException {
        ZipEntry entry = new ZipEntry(entryPath);
        entry.setSize(content.length);
        zos.putNextEntry(entry);
        zos.write(content);
        zos.closeEntry();
    }

    /** 生成时间戳前缀，用于媒体文件名 */
    private String formatTimestamp(Date date) {
        if (date == null) return "unknown";
        return new SimpleDateFormat("yyyy-MM-dd_HHmmss").format(date);
    }

    /** 文件名安全化，去除非法字符 */
    private String sanitizeName(String name) {
        if (name == null || name.isEmpty()) return "unknown";
        // 去除或替换 Windows/Linux 文件名中的非法字符
        return name.replaceAll("[\\\\/:*?\"<>|]", "_").trim();
    }

    /** HTML 转义 */
    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;");
    }

    /** 将图片/语音转成 data URI，便于导出 HTML 直接打开 */
    private String buildInlineDataUri(MsgInfo msg, byte[] content) {
        if (msg == null || content == null || content.length == 0) {
            return null;
        }
        if (!"img".equals(msg.messageType) && !"voice".equals(msg.messageType)) {
            return null;
        }

        String mimeType;
        String ext = extractExt(msg);
        if ("img".equals(msg.messageType)) {
            mimeType = guessImageMimeType(ext);
        } else {
            mimeType = guessAudioMimeType(ext);
        }
        return "data:" + mimeType + ";base64," + Base64.getEncoder().encodeToString(content);
    }

    private String guessImageMimeType(String ext) {
        if (ext == null) {
            return "image/jpeg";
        }
        switch (ext.toLowerCase()) {
            case "png":
                return "image/png";
            case "gif":
                return "image/gif";
            case "webp":
                return "image/webp";
            case "bmp":
                return "image/bmp";
            case "svg":
            case "svgz":
                return "image/svg+xml";
            default:
                return "image/jpeg";
        }
    }

    private String guessAudioMimeType(String ext) {
        if (ext == null) {
            return "audio/webm";
        }
        switch (ext.toLowerCase()) {
            case "mp3":
                return "audio/mpeg";
            case "ogg":
                return "audio/ogg";
            case "wav":
                return "audio/wav";
            case "m4a":
            case "aac":
                return "audio/aac";
            default:
                return "audio/webm";
        }
    }

    /** 创建一个 ZIP 字节数组（仅含空提示） */
    private byte[] createEmptyZip(String conversationName) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            String folder = sanitizeName(conversationName) + "_聊天记录";
            String html = "<!DOCTYPE html><html lang=\"zh-CN\"><head><meta charset=\"UTF-8\">" +
                    "<title>聊天记录</title></head><body><h2>聊天记录为空</h2>" +
                    "<p>该会话暂无消息记录。</p></body></html>";
            addZipEntry(zos, folder + "/index.html", html.getBytes("UTF-8"));
            zos.finish();
        } catch (IOException ignored) {
        }
        return baos.toByteArray();
    }
}
