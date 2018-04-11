package com.elon.entity;

public class GameContants {


    public static String[] GP_Chars = {
            "体", "育", "1", "-", "三", "一", "二", "四", "胆", "拖", "五", "3", "六", "七", "丿", "乀", "前", "直", "组", "乐",
            "8", "2", "9", "6", "7", "4", "彩", "第", "期", "共", "/", "开", "奖", ":", "任", "选", "单", "票", "式", "票", "倍",
            "合", "计", "元", "①", "+", "②", ".", "<", "③", "④", "⑤", "5", "复", "0", "1", "2", "3", "4", "5", "6", "7", "8",
            "9", "*", "选", "<", "5", ">", "0"
    };

    public static void main(String[] args) {
        System.out.println(GP_Chars.length);
        System.out.println("===============================================");
        for (int i = 0; i < GP_Chars.length; i++) {
            System.out.println("标签:" + i + " 对应文字:" + GP_Chars[i]);
        }
    }
}
