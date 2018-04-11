package com.elon.service;

import com.elon.entity.TicketInfo;
import com.elon.util.DBUtil;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TicketInfoService {

    public static List<TicketInfo> getTicketInfo(long tempId) throws SQLException, ClassNotFoundException {
        String sql = "SELECT ti.TEMPID,cp.GAMECODE,ti.BYTE_IMAGE,ti.TICKET_OCR  FROM SZHZ_TICKETINFO ti LEFT JOIN SZHZ_T_CHIPIN_TEMP cp ON cp.TEMPID = ti.TEMPID WHERE ti.TEMPID =" + tempId;
        Connection connection = DBUtil.getConnection(DBUtil.db_url, DBUtil.username, DBUtil.userpass, DBUtil.drive_name);
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        ResultSet resultSet = preparedStatement.executeQuery();
        List<TicketInfo> list = new ArrayList<>();
        while (resultSet.next()) {
            long tempId_ = resultSet.getLong("TEMPID");
            int gameCode = resultSet.getInt("GAMECODE");
            Blob imageIps = (Blob) resultSet.getObject("BYTE_IMAGE");
            Blob orcIps = (Blob) resultSet.getObject("TICKET_OCR");
            TicketInfo ticketInfo = new TicketInfo(tempId_, gameCode, imageIps.getBinaryStream(), orcIps.getBinaryStream());
            list.add(ticketInfo);
        }
        return list;
    }

    public static void main(String[] args) throws SQLException, ClassNotFoundException, IOException {
        /*List<TicketInfo> ticketInfoList = getTicketInfo(1000000162622509L);
        for (TicketInfo ticketInfo : ticketInfoList) {
            byte[] imageBytes = IOUtils.toByteArray(ticketInfo.getImageIps());
            ImageCommUtil.saveImage(ImageCommUtil.bytes2Image(imageBytes), ImageType.BMP, "D:\\picture\\11_5", System.nanoTime() + "");
            String orc_str = IOUtils.toString(ticketInfo.getOrcIps(),"utf-8");
            System.out.println(orc_str);
        }*/
    }
}
