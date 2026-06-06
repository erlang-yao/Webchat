package com.zzw.chatserver.common;

public interface ConstValueEnum {
    String FRIEND = "FRIEND";
    String GROUP = "GROUP";
    String VALIDATE = "VALIDATE";

    Integer USERTYPE = 1;
    Integer GROUPTYPE = 2;

    Integer ACCOUNT_USED = 1;
    Integer ACCOUNT_NOT_USED = 0;
    Integer ACCOUNT_NORMAL = 0;
    Integer ACCOUNT_FREEZED = 1;
    Integer ACCOUNT_CANCELED = 2;

    Long INITIAL_NUMBER = 10000000L;

    char[] cvCodeList = {
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
            'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
            '1', '2', '3', '4', '5', '6', '7', '8', '9', '0'
    };

    String[] nickNameList = {
            "\u661f\u6cb3\u65c5\u4eba",
            "\u665a\u98ce\u6765\u4fe1",
            "\u6708\u8272\u5fae\u51c9",
            "\u4e91\u8fb9\u5c0f\u7ad9",
            "\u6e05\u98ce\u5165\u6000",
            "\u5c71\u6d77\u8fc7\u5ba2",
            "\u661f\u5149\u62fe\u9057",
            "\u65e5\u843d\u6536\u85cf\u5bb6",
            "\u96e8\u540e\u9752\u82d4",
            "\u534a\u9897\u661f\u8fb0",
            "\u6a58\u5b50\u6c7d\u6c34",
            "\u6843\u5b50\u4e4c\u9f99",
            "\u8584\u8377\u5fae\u5149",
            "\u5976\u6cb9\u661f\u7403",
            "\u67e0\u6aac\u6d77\u76d0",
            "\u829d\u58eb\u5c0f\u961f\u957f",
            "\u7126\u7cd6\u5e03\u4e01",
            "\u8349\u8393\u4fe1\u7bb1",
            "\u82b1\u751f\u9171\u5148\u751f",
            "\u53ef\u53ef\u788e\u51b0",
            "\u6162\u901f\u661f\u7403",
            "\u6f2b\u6e38\u6307\u5357",
            "\u53d1\u5446\u51a0\u519b",
            "\u65e9\u7761\u9009\u624b",
            "\u6674\u5929\u6536\u96c6\u5458",
            "\u7075\u611f\u6355\u624b",
            "\u65e5\u5e38\u89c2\u5bdf\u5458",
            "\u5feb\u4e50\u5b58\u6863",
            "\u5468\u672b\u65c5\u884c\u5bb6",
            "\u4eca\u65e5\u4e0d\u52a0\u73ed",
            "\u5c0f\u5c0f\u68a6\u60f3\u5bb6",
            "\u95ea\u5149\u7ec3\u4e60\u751f",
            "\u5b87\u5b99\u901a\u8baf\u5458",
            "\u9ec4\u660f\u90ae\u9012\u5458",
            "\u68ee\u6797\u5bfc\u822a\u5458",
            "\u6d77\u5c9b\u503c\u73ed\u5458",
            "\u6781\u5149\u8bb0\u5f55\u8005",
            "\u98ce\u91cc\u6709\u6b4c",
            "\u4e00\u53e3\u597d\u5fc3\u60c5",
            "\u660e\u5929\u89c1"
    };
}
